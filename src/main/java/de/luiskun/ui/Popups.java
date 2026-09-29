package de.luiskun.ui;

import de.luiskun.util.FileManager;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

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
     * Zeigt den Gewinner. Bei "Remove" wird genau der Eintrag mit dem Index entfernt
     * (bei doppelten Namen würde ein Entfernen per Name den falschen treffen).
     */
    public static void showWinnerDialog(Component parent, SidePanel sidePanel, FileManager files, int winnerIndex) {
        List<String> names = new ArrayList<>(sidePanel.getNames());
        if (winnerIndex < 0 || winnerIndex >= names.size()) {
            return;
        }

        Object[] options = {"Keep", "Remove"};
        int choice = JOptionPane.showOptionDialog(
                parent,
                names.get(winnerIndex),
                "Winner",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 1) {
            names.remove(winnerIndex);
            sidePanel.setNames(names);
            files.markUnsaved();
        }
    }
}