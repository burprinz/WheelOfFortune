package de.luiskun.ui;

import de.luiskun.logic.Colors;
import de.luiskun.util.FileManager;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class MenuBar extends JMenuBar {

    private final MainFrame mainFrame;
    private final FileManager files;
    private final List<JMenuItem> fileItems = new ArrayList<>();

    public MenuBar(MainFrame main, FileManager files) {
        this.mainFrame = main;
        this.files = files;

        JMenu fileMenu = new JMenu("Wheels");
        fileMenu.add(item("Create",
                KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK),
                () -> {
                    if (Popups.unsavedMessageDialog(mainFrame, files)) {
                        files.newWheel();
                        mainFrame.unsavedChanges = false;
                    }
                }));
        fileMenu.add(item("Load",
                KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK),
                () -> {
                    if (Popups.unsavedMessageDialog(mainFrame, files)) {
                        files.load();
                        mainFrame.unsavedChanges = false;
                    }
                }));
        fileMenu.add(item("Save",
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
                () -> {
                    boolean saved = files.save();
                    if (saved) {
                        mainFrame.unsavedChanges = false;
                    }
                }));
        fileMenu.add(item("Save as",
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
                () -> {
                    boolean saved = files.saveAs();
                    if (saved) {
                        mainFrame.unsavedChanges = false;
                    }
                }));
        add(fileMenu);

        // Datei-Menüpunkte merken, damit sie während des Drehens gesperrt werden können
        for (Component c : fileMenu.getMenuComponents()) {
            if (c instanceof JMenuItem mi) {
                fileItems.add(mi);
            }
        }

        JMenu appearanceMenu = new JMenu("Appearance");
        appearanceMenu.add(item("Switch to Light-Mode", null, () -> {
            Colors.switchToLightMode();
            mainFrame.changeColorMode();
        }));
        appearanceMenu.add(item("Switch to Dark-Mode", null, () -> {
            Colors.switchToDarkMode();
            mainFrame.changeColorMode();
        }));
        add(appearanceMenu);

        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.add(item("Change wheel constants", null, () -> {
            mainFrame.switchMode(MainFrame.MODE_CONSTANT_SETTINGS);
        }));
        add(settingsMenu);

    }

    /**
     * Sperrt/entsperrt Create, Load, Save und Save as (inklusive Shortcuts).
     */
    public void setFileActionsEnabled(boolean enabled) {
        for (JMenuItem item : fileItems) {
            item.setEnabled(enabled);
        }
    }

    private static JMenuItem item(String text, KeyStroke key, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        if (key != null) {
            item.setAccelerator(key);
        }
        item.addActionListener(e -> action.run());
        return item;
    }

}