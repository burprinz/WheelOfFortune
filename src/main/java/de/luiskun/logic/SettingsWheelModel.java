package de.luiskun.logic;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test-Rad für die Physik-Einstellungen (nutzt die Slider-Werte statt WheelConstants).
 */
public class SettingsWheelModel {

    private final List<String> names = List.of("Option 1", "Option 2", "Option 3", "Option 4");

    private double angle;
    private double speed;
    private double topSpeed;
    private boolean braking;
    private boolean spinning;

    private double currentAcceleration;
    private double currentFriction;
    private double currentStopSpeed;

    public List<String> getNames() {
        return names;
    }

    public double getAngle() {
        return angle;
    }

    public boolean isSpinning() {
        return spinning;
    }

    /**
     * Startet den Test-Spin mit den übergebenen Slider-Werten.
     */
    public boolean spin(double minTopSpeed, double maxTopSpeed, double acceleration, double friction, double stopSpeed) {
        if (spinning) {
            return false;
        }

        this.currentAcceleration = acceleration;
        this.currentFriction = friction;
        this.currentStopSpeed = stopSpeed;

        this.speed = 0;
        double minSpeed = Math.min(minTopSpeed, maxTopSpeed);
        double maxSpeed = Math.max(minTopSpeed, maxTopSpeed);
        this.topSpeed = minSpeed + ThreadLocalRandom.current().nextDouble() * Math.max(0.001, (maxSpeed - minSpeed));

        this.braking = false;
        this.spinning = true;
        return true;
    }

    /**
     * Simulationsschritt für das Test-Rad.
     */
    public boolean update() {
        if (!spinning) {
            return false;
        }

        angle = (angle + speed) % 360;

        if (!braking) {
            speed += currentAcceleration;
            if (speed >= topSpeed) {
                speed = topSpeed;
                braking = true;
            }
        } else {
            speed *= currentFriction;
            if (speed < currentStopSpeed) {
                speed = 0;
                spinning = false;
                return true;
            }
        }
        return false;
    }

    public void stop() {
        this.speed = 0;
        this.spinning = false;
        this.braking = false;
    }
}