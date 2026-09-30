package de.luiskun.util;

import de.luiskun.ui.SidePanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final String APP_TITLE = "WheelOfFortune";
    private static final int MAX_RECENT = 8;

    private static final List<String> DEFAULT_NAMES = List.of("Name1", "Name2", "Name3", "Name4");

    private final JFrame frame;
    private final SidePanel sidePanel;

    private Path currentFile;
    private boolean unsaved;
    private Runnable wheelReplacedListener = () -> { };

    public FileManager(JFrame frame, SidePanel sidePanel) {
        this.frame = frame;
        this.sidePanel = sidePanel;
        updateTitle();
    }

    public boolean isUnsaved() {
        return unsaved;
    }

    /**
     * Markiert das aktuelle Rad als geändert (Titel bekommt einen Stern).
     */
    public void markUnsaved() {
        if (!unsaved) {
            unsaved = true;
            updateTitle();
        }
    }

    /**
     * Wird aufgerufen, wenn ein anderes Rad geladen oder ein neues erstellt wurde.
     */
    public void onWheelReplaced(Runnable listener) {
        this.wheelReplacedListener = listener;
    }

    public List<String> loadLastOpenedWheel() {
        try {
            Path config = FilePaths.lastWheelConfig();
            if (Files.exists(config)) {
                String pathString = Files.readString(config, StandardCharsets.UTF_8).strip();
                Path file = pathString.isEmpty() ? null : Paths.get(pathString);

                if (file != null && Files.isRegularFile(file)) {
                    List<String> names = NameLoader.parse(readText(file));
                    currentFile = file;
                    return names;
                }
            }
        } catch (IOException | InvalidPathException e) {
            System.err.println("Could not load last wheel: " + e.getMessage());
        } finally {
            unsaved = false;
            updateTitle();
        }
        return DEFAULT_NAMES;
    }

    public void saveCurrentOpenedWheelAsDefault() {
        Path configPath = FilePaths.lastWheelConfig();
        try {
            if (currentFile != null) {
                Files.createDirectories(configPath.getParent());
                Files.writeString(configPath, currentFile.toAbsolutePath().toString(), StandardCharsets.UTF_8);
            } else {
                Files.deleteIfExists(configPath);
            }
        } catch (IOException e) {
            System.err.println("Could not update " + configPath.getFileName() + ": " + e.getMessage());
        }
    }

    public void newWheel() {
        sidePanel.setNames(List.of());
        currentFile = null;
        unsaved = false;
        updateTitle();
        wheelReplacedListener.run();
    }

    public void load() {
        JFileChooser chooser = createChooser();
        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        load(chooser.getSelectedFile().toPath());
    }

    public void load(Path file) {
        try {
            sidePanel.setNames(NameLoader.parse(readText(file)));
            currentFile = file;
            remember(file);
            unsaved = false;
            updateTitle();
            wheelReplacedListener.run();
        } catch (IOException e) {
            showError("Could not read file:\n" + e.getMessage());
        }
    }

    public boolean save() {
        return currentFile == null ? saveAs() : write(currentFile);
    }

    public boolean saveAs() {
        JFileChooser chooser = createChooser();
        if (currentFile != null) {
            chooser.setSelectedFile(currentFile.toFile());
        }
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        Path file = withTxtExtension(chooser.getSelectedFile().toPath());
        // Die aktuell geöffnete Datei darf ohne Rückfrage überschrieben werden
        if (Files.exists(file) && !file.equals(currentFile) && !confirmOverwrite(file)) {
            return false;
        }
        return write(file);
    }

    private boolean write(Path file) {
        try {
            Files.writeString(file, String.join("\n", sidePanel.getNames()), StandardCharsets.UTF_8);
            currentFile = file;
            remember(file);
            unsaved = false;
            updateTitle();
            return true;
        } catch (IOException e) {
            showError("Could not save file:\n" + e.getMessage());
            return false;
        }
    }

    /**
     * Liest eine Textdatei als UTF-8. Ist sie kein gültiges UTF-8 (z. B. ältere Windows-Editoren),
     * wird sie als Windows-1252 gelesen statt mit einem Fehler abzubrechen.
     */
    private static String readText(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, Charset.forName("windows-1252"));
        }
    }

    private JFileChooser createChooser() {
        JFileChooser chooser = new JFileChooser();
        try {
            Path startDir = (currentFile != null && currentFile.getParent() != null)
                    ? currentFile.getParent()
                    : FilePaths.wheelsDir();
            chooser.setCurrentDirectory(startDir.toFile());
        } catch (IOException ignored) {
            // Dann eben das Standardverzeichnis des Choosers
        }
        chooser.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));
        return chooser;
    }

    private static Path withTxtExtension(Path file) {
        String name = file.getFileName().toString();
        return name.toLowerCase().endsWith(".txt") ? file : file.resolveSibling(name + ".txt");
    }

    private boolean confirmOverwrite(Path file) {
        int answer = JOptionPane.showConfirmDialog(frame,
                file.getFileName() + " already exists. Replace?",
                "Save as", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return answer == JOptionPane.YES_OPTION;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void updateTitle() {
        String name = currentFile == null ? "Untitled" : currentFile.getFileName().toString();
        frame.setTitle((unsaved ? "*" : "") + name + " – " + APP_TITLE);
    }

    /**
     * Zuletzt verwendete Räder (neueste zuerst, nur noch existierende Dateien).
     */
    public List<Path> getRecentFiles() {
        Path config = FilePaths.recentFilesConfig();
        if (!Files.exists(config)) {
            return List.of();
        }
        try {
            List<Path> result = new ArrayList<>();
            for (String line : Files.readAllLines(config, StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }
                try {
                    Path path = Paths.get(line.strip());
                    if (Files.isRegularFile(path)) {
                        result.add(path);
                    }
                } catch (InvalidPathException ignored) {
                    // Kaputte Zeile überspringen
                }
                if (result.size() >= MAX_RECENT) {
                    break;
                }
            }
            return result;
        } catch (IOException e) {
            return List.of();
        }
    }

    private void remember(Path file) {
        String newest = file.toAbsolutePath().toString();
        List<String> entries = new ArrayList<>();
        entries.add(newest);
        for (Path p : getRecentFiles()) {
            String s = p.toAbsolutePath().toString();
            if (!s.equals(newest) && entries.size() < MAX_RECENT) {
                entries.add(s);
            }
        }
        try {
            Path config = FilePaths.recentFilesConfig();
            Files.createDirectories(config.getParent());
            Files.write(config, entries, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // Die Recent-Liste ist nur ein Komfort und darf nie das Speichern/Laden stören
        }
    }
}
