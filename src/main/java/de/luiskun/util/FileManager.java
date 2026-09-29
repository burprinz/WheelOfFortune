package de.luiskun.util;

import de.luiskun.ui.SidePanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileManager {
    private static final String APP_TITLE = "WheelOfFortune";

    private static final List<String> DEFAULT_NAMES = List.of("Name1", "Name2", "Name3", "Name4");

    private final JFrame frame;
    private final SidePanel sidePanel;

    private Path currentFile;

    public FileManager(JFrame frame, SidePanel sidePanel) {
        this.frame = frame;
        this.sidePanel = sidePanel;
        updateTitle();
    }

    public List<String> loadLastOpenedWheel() {
        Path lastWheelConfig = FilePaths.lastWheelConfig();

        if (Files.exists(lastWheelConfig)) {
            try {
                String pathString = Files.readString(lastWheelConfig, StandardCharsets.UTF_8).strip();

                if (!pathString.isEmpty()) {
                    Path actualFilePath = Paths.get(pathString);

                    if (Files.exists(actualFilePath)) {
                        String content = Files.readString(actualFilePath, StandardCharsets.UTF_8);
                        List<String> loadedNames = NameLoader.parse(content);

                        if (!loadedNames.isEmpty()) {
                            currentFile = actualFilePath;
                            updateTitle();
                            return loadedNames;
                        }
                    }
                }
            } catch (IOException | InvalidPathException e) {
                e.printStackTrace();
            }
        }
        updateTitle();
        return DEFAULT_NAMES;
    }

    public void saveCurrentOpenedWheelAsDefault() {
        Path configPath = FilePaths.lastWheelConfig();

        if (currentFile != null) {
            try {
                Files.createDirectories(configPath.getParent());
                Files.writeString(configPath, currentFile.toAbsolutePath().toString(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.err.println("Fehler beim Speichern von lastWheel.txt: " + e.getMessage());
            }
        } else {
            try {
                Files.deleteIfExists(configPath);
            } catch (IOException e) {
                System.err.println("Fehler beim Löschen von lastWheel.txt: " + e.getMessage());
            }
        }
    }

    public void newWheel() {
        sidePanel.setNames(List.of());
        currentFile = null;
        updateTitle();
    }

    public void load() {
        JFileChooser chooser = createChooser();
        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path file = chooser.getSelectedFile().toPath();
        try {
            String text = Files.readString(file, StandardCharsets.UTF_8);
            sidePanel.setNames(NameLoader.parse(text));
            currentFile = file;
            updateTitle();
        } catch (IOException e) {
            showError("Could not read File:\n" + e.getMessage());
        }
    }

    public boolean save() {
        if (currentFile == null) {
            return saveAs();
        } else {
            return write(currentFile);
        }
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
        if (Files.exists(file) && !confirmOverwrite(file)) {
            return false;
        }
        return write(file);
    }

    private boolean write(Path file) {
        try {
            Files.writeString(file, String.join("\n", sidePanel.getNames()), StandardCharsets.UTF_8);
            currentFile = file;
            updateTitle();
            return true;
        } catch (IOException e) {
            showError("Could not save file:\n" + e.getMessage());
            return false;
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
        frame.setTitle(name + " – " + APP_TITLE);
    }


}