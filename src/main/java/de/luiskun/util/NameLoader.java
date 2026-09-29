package de.luiskun.util;

import java.util.List;

public final class NameLoader {

    private static final char BOM = '\uFEFF';

    private NameLoader() {
    }

    public static List<String> parse(String text) {
        if (!text.isEmpty() && text.charAt(0) == BOM) {
            text = text.substring(1);
        }
        return text.lines()
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}