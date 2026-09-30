package de.luiskun.logic;

/**
 * Ein Eintrag des Rads: Name und Gewichtung.
 * <p>
 * Zeilenformat: {@code Name} oder {@code Name;Gewichtung}. Als Gewichtung zählt nur eine positive
 * ganze Zahl hinter dem letzten Semikolon der Zeile. Alles andere gehört zum Namen (Gewichtung 1).
 */
public record WheelEntry(String name, int weight) {

    private static final int MAX_WEIGHT = 1_000_000;

    public static WheelEntry parse(String line) {
        String text = line.strip();
        int semicolon = text.lastIndexOf(';');
        if (semicolon > 0) {
            String name = text.substring(0, semicolon).strip();
            String number = text.substring(semicolon + 1).strip();
            if (!name.isEmpty() && isWeight(number)) {
                return new WheelEntry(name, Integer.parseInt(number));
            }
        }
        return new WheelEntry(text, 1);
    }

    private static boolean isWeight(String s) {
        if (s.isEmpty() || s.length() > 7) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        int value = Integer.parseInt(s);
        return value >= 1 && value <= MAX_WEIGHT;
    }
}
