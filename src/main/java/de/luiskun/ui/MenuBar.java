package de.luiskun.ui;

import javax.swing.*;
import java.awt.*;

public class MenuBar extends JMenuBar {

    public MenuBar() {
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        JMenu fileMenu = new JMenu("Wheels");
        fileMenu.add(item("Create Wheel", null, () -> {

        }));
        fileMenu.add(item("Load Wheel", null, () -> {

        }));
        fileMenu.add(item("Save Wheel", null, () -> {

        }));
        add(fileMenu);

        JMenu appearanceMenu = new JMenu("Appearance");
        add(appearanceMenu);

        JMenu settingsMenu = new JMenu("Settings");
        add(settingsMenu);

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
