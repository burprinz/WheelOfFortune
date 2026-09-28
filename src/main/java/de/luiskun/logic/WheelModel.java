package de.luiskun.logic;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WheelModel {

    private static final double MIN_TOP_SPEED = 10;
    private static final double MAX_TOP_SPEED = 30;
    private static final double ACCELERATION = 0.5;
    private static final double FRICTION = 0.985;
    private static final double STOP_SPEED = 0.1;

    private List<String> names = List.of();

    private double angle;
    private double speed;
    private double topSpeed;
    private boolean braking;
    private boolean spinning;

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
     * Startet einen neuen Dreh.
     * @return false, wenn drehend oder kein Eintrag
     */
    public boolean spin() {
        if (spinning || names.isEmpty()) {
            return false;
        }
        speed = 0;
        topSpeed = MIN_TOP_SPEED + ThreadLocalRandom.current().nextDouble() * (MAX_TOP_SPEED - MIN_TOP_SPEED);
        braking = false;
        spinning = true;
        return true;
    }

    /**
     * Ein Simulationsschritt.
     *
     * @return true, wenn rad finished
     */
    public boolean update() {
        if (!spinning) {
            return false;
        }

        angle = (angle + speed) % 360;

        if (!braking) {
            speed += ACCELERATION;
            if (speed >= topSpeed) {
                speed = topSpeed;
                braking = true;
            }
        } else {
            speed *= FRICTION;
            if (speed < STOP_SPEED) {
                speed = 0;
                spinning = false;
                return true;
            }
        }
        return false;
    }


    public String getWinner() {
        if (names.isEmpty()) {
            return "";
        }
        int n = names.size();
        double sliceSize = 360.0 / n;
        double relativeAngle = ((90.0 - angle) % 360 + 360) % 360;
        return names.get((int) (relativeAngle / sliceSize) % n);
    }
}