package de.luiskun.logic;

public class WheelConstants {

    private static double DEFAULT_MIN_TOP_SPEED = 10;
    private static double DEFAULT_MAX_TOP_SPEED = 30;
    private static double DEFAULT_ACCELERATION = 0.5;
    private static double DEFAULT_FRICTION = 0.985;
    private static double DEFAULT_STOP_SPEED = 0.1;

    public static double MIN_TOP_SPEED = DEFAULT_MIN_TOP_SPEED;
    public static double MAX_TOP_SPEED = DEFAULT_MAX_TOP_SPEED;
    public static double ACCELERATION = DEFAULT_ACCELERATION;
    public static double FRICTION = DEFAULT_FRICTION;
    public static double STOP_SPEED = DEFAULT_STOP_SPEED;


    public static void restoreDefault() {
        MIN_TOP_SPEED = DEFAULT_MIN_TOP_SPEED;
        MAX_TOP_SPEED = DEFAULT_MAX_TOP_SPEED;
        ACCELERATION = DEFAULT_ACCELERATION;
        FRICTION = DEFAULT_FRICTION;
        STOP_SPEED = DEFAULT_STOP_SPEED;
    }

    public static void setNewValues(double min_top, double max_top, double acc, double fric, double stop) {
        MIN_TOP_SPEED = min_top;
        MAX_TOP_SPEED = max_top;
        ACCELERATION = acc;
        FRICTION = fric;
        STOP_SPEED = stop;
    }
}
