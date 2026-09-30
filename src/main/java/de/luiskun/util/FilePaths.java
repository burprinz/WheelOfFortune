package de.luiskun.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public final class FilePaths {

    private static final String APP_NAME = "WheelOfFortune";
    private static final String LAST_WHEEL_FILE_NAME = "lastWheel.txt";
    private static final String RECENT_FILE_NAME = "recent.txt";

    private FilePaths() {
    }

    public static Path dataDir() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Paths.get(appData).resolve(APP_NAME);
        }

        Path home = Paths.get(System.getProperty("user.home"));
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac")) {
            return home.resolve("Library").resolve("Application Support").resolve(APP_NAME);
        }
        return home.resolve(".local").resolve("share").resolve(APP_NAME);
    }

    public static Path wheelsDir() throws IOException {
        Path dir = dataDir().resolve("wheels");
        Files.createDirectories(dir);
        return dir;
    }

    /**
     * Speicherort der Datei, die sich das zuletzt geöffnete Rad merkt.
     * Das Verzeichnis wird hier nicht angelegt.
     */
    public static Path lastWheelConfig() {
        return dataDir().resolve(LAST_WHEEL_FILE_NAME);
    }

    /**
     * Speicherort der Liste zuletzt verwendeter Räder (liegt jetzt ebenfalls im Datenverzeichnis).
     * Das Verzeichnis wird hier nicht angelegt.
     */
    public static Path recentFilesConfig() {
        return dataDir().resolve(RECENT_FILE_NAME);
    }
}
