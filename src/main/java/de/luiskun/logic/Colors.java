package de.luiskun.logic;

import java.awt.Color;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Zentrale Farbpalette für Light- und Dark-Mode. Der gewählte Modus wird gespeichert.
 */
public final class Colors {

    private static final Preferences PREFS = Preferences.userNodeForPackage(Colors.class);
    private static final String DARK_MODE_KEY = "darkMode";

    /** Textfarbe auf den bunten Radsegmenten (in beiden Modi dunkel, da die Segmente hell sind). */
    public static final Color SLICE_TEXT_COLOR = Color.decode("#1B1D26");

    public static Color backgroundColor;   // Fenster / Radfläche / Eingabefelder
    public static Color surfaceColor;      // Seitenleisten, Menü, Buttons
    public static Color textColor;
    public static Color mutedTextColor;
    public static Color borderColor;
    public static Color accentColor;       // Primär-Buttons, Textauswahl
    public static Color accentTextColor;   // Text auf accentColor
    public static Color selectorColor;     // Zeiger am Rad

    private static boolean darkMode;

    static {
        applyTheme(PREFS.getBoolean(DARK_MODE_KEY, false));
    }

    private Colors() {
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void switchToDarkMode() {
        setDarkMode(true);
    }

    public static void switchToLightMode() {
        setDarkMode(false);
    }

    private static void setDarkMode(boolean dark) {
        applyTheme(dark);
        PREFS.putBoolean(DARK_MODE_KEY, dark);
        try {
            PREFS.flush(); // sofort schreiben, nicht erst beim JVM-Ende
        } catch (BackingStoreException e) {
            System.err.println("Could not save dark mode setting: " + e.getMessage());
        }
    }

    public static Color colorFor(int index) {
        float hue = (0.58f + index * 0.618034f) % 1.0f;
        return darkMode
                ? Color.getHSBColor(hue, 0.55f, 0.85f)
                : Color.getHSBColor(hue, 0.45f, 0.98f);
    }

    private static void applyTheme(boolean dark) {
        darkMode = dark;
        if (dark) {
            backgroundColor = Color.decode("#14151A");
            surfaceColor = Color.decode("#1E2029");
            textColor = Color.decode("#E6E8EF");
            mutedTextColor = Color.decode("#8B91A3");
            borderColor = Color.decode("#343849");
            accentColor = Color.decode("#7C93FF");
            accentTextColor = Color.decode("#0F1020");
            selectorColor = Color.decode("#FF6B6B");
        } else {
            backgroundColor = Color.decode("#F3F4F8");
            surfaceColor = Color.decode("#FFFFFF");
            textColor = Color.decode("#1F2430");
            mutedTextColor = Color.decode("#7A8194");
            borderColor = Color.decode("#D9DCE6");
            accentColor = Color.decode("#4F6BED");
            accentTextColor = Color.WHITE;
            selectorColor = Color.decode("#E5484D");
        }
    }
}