package de.luiskun.ui;

import de.luiskun.util.FileManager;

import javax.swing.JOptionPane;
import java.awt.Component;

public final class Popups {

    private Popups() {
    }

    /**
     * Fragt bei ungespeicherten Änderungen nach.
     *
     * @return true, wenn die Aktion fortgesetzt werden darf (nichts offen, verworfen oder gespeichert)
     */
    public static boolean unsavedMessageDialog(Component parent, FileManager files) {
        if (!files.isUnsaved()) {
            return true;
        }

        Object[] options = {"Cancel", "Discard", "Save", "Save as"};
        int choice = JOptionPane.showOptionDialog(
                parent,
                "You have unsaved changes. Save?",
                "Unsaved changes",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]
        );

        return switch (choice) {
            case 1 -> true;
            case 2 -> files.save();
            case 3 -> files.saveAs();
            default -> false;
        };
    }

    /**
     * Zeigt den Gewinner.
     *
     * @return true, wenn der Eintrag entfernt werden soll ("Remove")
     */
    public static boolean showWinnerDialog(Component parent, String winner) {
        Object[] options = {"Keep", "Remove"};
        int choice = JOptionPane.showOptionDialog(
                parent,
                winner,
                "Winner",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );
        return choice == 1;
    }
}