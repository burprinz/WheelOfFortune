package de.luiskun.logic;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Physik und Zustand eines Rads. Wird für das Hauptrad (Werte aus {@link WheelConstants})
 * und für das Test-Rad der Physik-Einstellungen (Slider-Werte) verwendet.
 */
public class WheelModel {

    private List<String> names = List.of();

    private double angle;
    private double speed;
    private double topSpeed;
    private boolean braking;
    private boolean spinning;

    private double acceleration;
    private double friction;
    private double stopSpeed;

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = List.copyOf(names);
    }

    public double getAngle() {
        return angle;
    }

    public boolean isSpinning() {
        return spinning;
    }

    /**
     * Startet einen Dreh mit den aktuellen {@link WheelConstants}.
     *
     * @return false, wenn das Rad schon dreht oder keine Einträge hat
     */
    public boolean spin() {
        return spin(WheelConstants.MIN_TOP_SPEED, WheelConstants.MAX_TOP_SPEED,
                WheelConstants.ACCELERATION, WheelConstants.FRICTION, WheelConstants.STOP_SPEED);
    }

    /**
     * Startet einen Dreh mit den übergebenen Werten.
     *
     * @return false, wenn das Rad schon dreht oder keine Einträge hat
     */
    public boolean spin(double minTopSpeed, double maxTopSpeed, double acceleration, double friction, double stopSpeed) {
        if (spinning || names.isEmpty()) {
            return false;
        }
        this.acceleration = acceleration;
        this.friction = friction;
        this.stopSpeed = stopSpeed;

        double lo = Math.min(minTopSpeed, maxTopSpeed);
        double hi = Math.max(minTopSpeed, maxTopSpeed);
        this.topSpeed = lo + ThreadLocalRandom.current().nextDouble() * (hi - lo);

        this.speed = 0;
        this.braking = false;
        this.spinning = true;
        return true;
    }

    /**
     * Ein Simulationsschritt.
     *
     * @return true, wenn das Rad in diesem Schritt zum Stillstand gekommen ist
     */
    public boolean update() {
        if (!spinning) {
            return false;
        }

        angle = (angle + speed) % 360;

        if (!braking) {
            speed += acceleration;
            if (speed >= topSpeed) {
                speed = topSpeed;
                braking = true;
            }
        } else {
            speed *= friction;
            if (speed < stopSpeed) {
                stop();
                return true;
            }
        }
        return false;
    }

    /**
     * Index des Segments unter dem Zeiger (oben, bei 90°) oder -1 ohne Einträge.
     */
    public int getWinnerIndex() {
        if (names.isEmpty()) {
            return -1;
        }
        int n = names.size();
        double sliceSize = 360.0 / n;
        double relativeAngle = ((90.0 - angle) % 360 + 360) % 360;
        return (int) (relativeAngle / sliceSize) % n;
    }

    public void stop() {
        speed = 0;
        spinning = false;
        braking = false;
    }
}