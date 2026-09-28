package de.luiskun.ui;

import de.luiskun.util.FileManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Popups {

    public static boolean unsavedMessageDialog(MainFrame mainFrame, FileManager files) {
        if (!mainFrame.unsavedChanges) {
            return true;
        }

        boolean saved = false;
        while (!saved) {
            Object[] options = {"Cancel", "Abandon", "Save", "Save as"};
            int choice = JOptionPane.showOptionDialog(
                    mainFrame,
                    "You have unsaved changes. Save?",
                    "Unsaved changes",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == 1) {
                saved = true;
            } else if (choice == 2) {
                saved = files.save();
            } else if (choice == 3) {
                saved = files.saveAs();
            } else {
                return false;
            }
        }
        mainFrame.unsavedChanges = false;
        return true;
    }

    public static void showWinnerDialog(MainFrame mainFrame, SidePanel sidePanel, String winner) {
        Object[] options = {"Keep", "Remove"};
        int choice = JOptionPane.showOptionDialog(
                mainFrame,
                winner,
                "Winner",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 1) {
            List<String> names = new ArrayList<>(sidePanel.getNames());
            names.remove(winner);
            sidePanel.setNames(names);
            mainFrame.unsavedChanges = true;
        }
    }
}
