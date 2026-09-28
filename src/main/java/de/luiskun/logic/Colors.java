package de.luiskun.logic;

import java.awt.*;

public class Colors {

    private static final Color darkmodeBackground =  Color.decode("#121212");
    private static final Color darkmodeSelector = Color.WHITE;
    private static final Color darkmodeText = Color.WHITE;

    private static final Color lightmodeBackground =  Color.WHITE;
    private static final Color lightmodeSelector = Color.BLACK;
    private static final Color lightmodeText = Color.BLACK;

    public static Color backgroundColor = lightmodeBackground;
    public static Color textColor = lightmodeText;
    public static Color selectorColor = lightmodeSelector;


    public static void switchToDarkMode() {
        backgroundColor = darkmodeBackground;
        textColor = darkmodeText;
        selectorColor = darkmodeSelector;
    }

    public static void switchToLightMode() {
        backgroundColor = lightmodeBackground;
        textColor = lightmodeText;
        selectorColor = lightmodeSelector;
    }
}
