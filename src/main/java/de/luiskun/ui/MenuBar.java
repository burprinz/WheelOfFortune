package de.luiskun.ui;

import de.luiskun.logic.Colors;
import de.luiskun.util.FileManager;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MenuBar extends JMenuBar {

    private static final int SHORTCUT_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

    private final List<JMenu> menus = new ArrayList<>();
    private final List<JMenuItem> items = new ArrayList<>();
    /** Einträge, die während des Drehens gesperrt sind (inklusive ihrer Shortcuts). */
    private final List<JMenuItem> spinLockedItems = new ArrayList<>();

    private JMenuItem undoItem;
    private JMenuItem redoItem;
    private boolean spinning;
    private boolean canUndo;
    private boolean canRedo;

    public MenuBar(MainFrame mainFrame, FileManager files) {
        JMenu wheels = menu("Wheels");
        spinLockedItems.add(item(wheels, "Create", shortcut(KeyEvent.VK_N, 0), () -> {
            if (Popups.unsavedMessageDialog(mainFrame, files)) {
                files.newWheel();
            }
        }));
        spinLockedItems.add(item(wheels, "Load", shortcut(KeyEvent.VK_L, 0), () -> {
            if (Popups.unsavedMessageDialog(mainFrame, files)) {
                files.load();
            }
        }));
        spinLockedItems.add(item(wheels, "Save", shortcut(KeyEvent.VK_S, 0), files::save));
        spinLockedItems.add(item(wheels, "Save as", shortcut(KeyEvent.VK_S, KeyEvent.SHIFT_DOWN_MASK), files::saveAs));

        JMenu recent = new JMenu("Recent");
        wheels.add(recent);
        menus.add(recent);
        spinLockedItems.add(recent);
        // Die Liste wird bei jedem Öffnen neu aufgebaut, sonst wäre sie nach dem ersten Start veraltet
        recent.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                rebuildRecent(recent, mainFrame, files);
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });

        JMenu appearance = menu("Appearance");
        JCheckBoxMenuItem darkMode = new JCheckBoxMenuItem("Dark mode", Colors.isDarkMode());
        darkMode.addActionListener(e -> {
            if (darkMode.isSelected()) {
                Colors.switchToDarkMode();
            } else {
                Colors.switchToLightMode();
            }
            mainFrame.changeColorMode();
        });
        appearance.add(darkMode);
        items.add(darkMode);

        JMenu settings = menu("Settings");
        spinLockedItems.add(item(settings, "Change wheel constants", null,
                () -> mainFrame.switchMode(MainFrame.MODE_CONSTANT_SETTINGS)));
        settings.addSeparator();
        undoItem = item(settings, "Undo removal", shortcut(KeyEvent.VK_Z, 0), mainFrame::undoRemoval);
        redoItem = item(settings, "Redo removal", shortcut(KeyEvent.VK_Z, KeyEvent.SHIFT_DOWN_MASK), mainFrame::redoRemoval);
        refreshHistoryItems();

        JMenu tools = menu("Tools");
        spinLockedItems.add(item(tools, "Shuffle", null, mainFrame::shuffleNames));
        spinLockedItems.add(item(tools, "Sort A-Z", null, mainFrame::sortNames));
        spinLockedItems.add(item(tools, "Remove duplicates", null, mainFrame::removeDuplicates));

        JMenu help = menu("Help");
        item(help, "How to use", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), mainFrame::showHelp);
    }

    private void rebuildRecent(JMenu recent, MainFrame mainFrame, FileManager files) {
        recent.removeAll();
        for (Path p : files.getRecentFiles()) {
            JMenuItem entry = new JMenuItem(p.getFileName().toString());
            entry.setToolTipText(p.toString());
            entry.addActionListener(e -> {
                if (Popups.unsavedMessageDialog(mainFrame, files)) {
                    files.load(p);
                }
            });
            styleItem(entry);
            recent.add(entry);
        }

        if (recent.getItemCount() == 0) {
            JMenuItem empty = new JMenuItem("Empty");
            empty.setEnabled(false);
            styleItem(empty);
            recent.add(empty);
        }
    }

    /**
     * Sperrt/entsperrt Create, Load, Save, Save as, Recent, die Physik-Einstellungen und die Namen-Werkzeuge,
     * solange das Rad dreht.
     */
    public void setSpinLock(boolean spinning) {
        this.spinning = spinning;
        for (JMenuItem item : spinLockedItems) {
            item.setEnabled(!spinning);
        }
        refreshHistoryItems();
    }

    /**
     * Aktiviert "Undo removal" / "Redo removal" nur, wenn es etwas rückgängig zu machen bzw. zu wiederholen gibt.
     */
    public void setHistoryState(boolean canUndo, boolean canRedo) {
        this.canUndo = canUndo;
        this.canRedo = canRedo;
        refreshHistoryItems();
    }

    private void refreshHistoryItems() {
        undoItem.setEnabled(!spinning && canUndo);
        redoItem.setEnabled(!spinning && canRedo);
    }

    public void changeColorMode() {
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.borderColor));
        for (JMenu menu : menus) {
            menu.setForeground(Colors.textColor);
            menu.getPopupMenu().setBackground(Colors.surfaceColor);
            menu.getPopupMenu().setBorder(new LineBorder(Colors.borderColor));
        }
        for (JMenuItem item : items) {
            styleItem(item);
        }
        repaint();
    }

    private static void styleItem(JMenuItem item) {
        item.setOpaque(true);
        item.setBackground(Colors.surfaceColor);
        item.setForeground(Colors.textColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Colors.surfaceColor);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private JMenu menu(String text) {
        JMenu menu = new JMenu(text);
        menus.add(menu);
        add(menu);
        return menu;
    }

    private JMenuItem item(JMenu menu, String text, KeyStroke key, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        if (key != null) {
            item.setAccelerator(key);
        }
        item.addActionListener(e -> action.run());
        menu.add(item);
        items.add(item);
        return item;
    }

    private static KeyStroke shortcut(int keyCode, int extraMask) {
        return KeyStroke.getKeyStroke(keyCode, SHORTCUT_MASK | extraMask);
    }
}
