package de.luiskun.util;

import de.luiskun.logic.WheelEntry;

import java.util.List;

public final class NameLoader {

    private static final char BOM = '\uFEFF';

    private NameLoader() {
    }

    /**
     * Zerlegt Text in Zeilen (getrimmt, ohne Leerzeilen). Gewichtungen bleiben Teil der Zeile.
     */
    public static List<String> parse(String text) {
        if (!text.isEmpty() && text.charAt(0) == BOM) {
            text = text.substring(1);
        }
        return text.lines()
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * Wie {@link #parse}, aber mit Namen und Gewichtung ("Name;3"). Die Reihenfolge entspricht
     * genau den Zeilen von {@link #parse}.
     */
    public static List<WheelEntry> parseEntries(String text) {
        return parse(text).stream().map(WheelEntry::parse).toList();
    }
}
