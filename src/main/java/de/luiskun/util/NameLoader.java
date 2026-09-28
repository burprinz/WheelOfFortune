package de.luiskun.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class NameLoader {


    private NameLoader() {
    }


    public static List<String> parse(String text) {
        return text.lines()
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}