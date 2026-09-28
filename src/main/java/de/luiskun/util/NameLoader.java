package de.luiskun.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class NameLoader {

    private static final String DEFAULT_RESOURCE = "/input.txt";

    private NameLoader() {
    }

    public static List<String> loadDefault() {
        try (InputStream in = NameLoader.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (in == null) {
                return List.of();
            }
            return parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            return List.of();
        }
    }

    public static List<String> parse(String text) {
        return text.lines()
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}