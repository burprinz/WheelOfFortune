package de.luiskun.logic;

public final class WheelConstants {

    public static final double DEFAULT_MIN_TOP_SPEED = 10;
    public static final double DEFAULT_MAX_TOP_SPEED = 30;
    public static final double DEFAULT_ACCELERATION = 0.5;
    public static final double DEFAULT_FRICTION = 0.985;
    public static final double DEFAULT_STOP_SPEED = 0.1;

    public static double MIN_TOP_SPEED = DEFAULT_MIN_TOP_SPEED;
    public static double MAX_TOP_SPEED = DEFAULT_MAX_TOP_SPEED;
    public static double ACCELERATION = DEFAULT_ACCELERATION;
    public static double FRICTION = DEFAULT_FRICTION;
    public static double STOP_SPEED = DEFAULT_STOP_SPEED;

    private WheelConstants() {
    }

    public static void setNewValues(double minTop, double maxTop, double acceleration, double friction, double stopSpeed) {
        MIN_TOP_SPEED = Math.min(minTop, maxTop);
        MAX_TOP_SPEED = Math.max(minTop, maxTop);
        ACCELERATION = acceleration;
        FRICTION = friction;
        STOP_SPEED = stopSpeed;
    }
}