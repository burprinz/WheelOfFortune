package de.luiskun.logic;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Physik-Werte des Rads. Die zuletzt mit "Apply Changes" gesetzten Werte werden gespeichert
 * und beim nächsten Start wieder geladen.
 */
public final class WheelConstants {

    public static final double DEFAULT_MIN_TOP_SPEED = 10;
    public static final double DEFAULT_MAX_TOP_SPEED = 30;
    public static final double DEFAULT_ACCELERATION = 0.5;
    public static final double DEFAULT_FRICTION = 0.985;
    public static final double DEFAULT_STOP_SPEED = 0.1;

    private static final Preferences PREFS = Preferences.userNodeForPackage(WheelConstants.class);
    private static final String KEY_MIN_TOP_SPEED = "minTopSpeed";
    private static final String KEY_MAX_TOP_SPEED = "maxTopSpeed";
    private static final String KEY_ACCELERATION = "acceleration";
    private static final String KEY_FRICTION = "friction";
    private static final String KEY_STOP_SPEED = "stopSpeed";

    public static double MIN_TOP_SPEED = DEFAULT_MIN_TOP_SPEED;
    public static double MAX_TOP_SPEED = DEFAULT_MAX_TOP_SPEED;
    public static double ACCELERATION = DEFAULT_ACCELERATION;
    public static double FRICTION = DEFAULT_FRICTION;
    public static double STOP_SPEED = DEFAULT_STOP_SPEED;

    static {
        load();
    }

    private WheelConstants() {
    }

    public static void setNewValues(double minTop, double maxTop, double acceleration, double friction, double stopSpeed) {
        MIN_TOP_SPEED = Math.min(minTop, maxTop);
        MAX_TOP_SPEED = Math.max(minTop, maxTop);
        ACCELERATION = acceleration;
        FRICTION = friction;
        STOP_SPEED = stopSpeed;
        save();
    }

    private static void load() {
        double min = PREFS.getDouble(KEY_MIN_TOP_SPEED, DEFAULT_MIN_TOP_SPEED);
        double max = PREFS.getDouble(KEY_MAX_TOP_SPEED, DEFAULT_MAX_TOP_SPEED);
        double acc = PREFS.getDouble(KEY_ACCELERATION, DEFAULT_ACCELERATION);
        double fric = PREFS.getDouble(KEY_FRICTION, DEFAULT_FRICTION);
        double stop = PREFS.getDouble(KEY_STOP_SPEED, DEFAULT_STOP_SPEED);

        // Kaputte Werte (z. B. Friction >= 1 würde das Rad nie stoppen) werden ignoriert
        if (isValid(min, max, acc, fric, stop)) {
            MIN_TOP_SPEED = Math.min(min, max);
            MAX_TOP_SPEED = Math.max(min, max);
            ACCELERATION = acc;
            FRICTION = fric;
            STOP_SPEED = stop;
        }
    }

    private static boolean isValid(double min, double max, double acc, double fric, double stop) {
        return Double.isFinite(min) && Double.isFinite(max) && Double.isFinite(acc)
                && Double.isFinite(fric) && Double.isFinite(stop)
                && min > 0 && max > 0 && acc > 0 && stop > 0
                && fric > 0 && fric < 1;
    }

    private static void save() {
        PREFS.putDouble(KEY_MIN_TOP_SPEED, MIN_TOP_SPEED);
        PREFS.putDouble(KEY_MAX_TOP_SPEED, MAX_TOP_SPEED);
        PREFS.putDouble(KEY_ACCELERATION, ACCELERATION);
        PREFS.putDouble(KEY_FRICTION, FRICTION);
        PREFS.putDouble(KEY_STOP_SPEED, STOP_SPEED);
        try {
            PREFS.flush();
        } catch (BackingStoreException e) {
            System.err.println("Could not save wheel constants: " + e.getMessage());
        }
    }
}