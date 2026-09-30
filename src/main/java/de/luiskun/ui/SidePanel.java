package de.luiskun.ui;

import de.luiskun.logic.Colors;
import de.luiskun.logic.WheelEntry;
import de.luiskun.util.NameLoader;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * Rechte Seitenleiste: Namensliste bearbeiten und Rad drehen.
 */
public class SidePanel extends JPanel {

    private final JLabel inputInfoLabel = new JLabel("Names (one per line)");
    private final JTextArea namesArea = new JTextArea();
    private final JScrollPane namesScroll = new JScrollPane(namesArea);
    private final AccentButton spinButton = new AccentButton("Spin", true);

    /** true, solange der Text per Code (und nicht vom Nutzer) geändert wird. */
    private boolean programmaticChange;

    public SidePanel() {
        super(new BorderLayout(8, 8));

        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setPreferredSize(new Dimension(220, 0));
        setMinimumSize(new Dimension(180, 0));

        namesArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        namesArea.setToolTipText("<html>One entry per line.<br>Optional weight: <b>Name;2</b></html>");

        add(inputInfoLabel, BorderLayout.NORTH);
        add(namesScroll, BorderLayout.CENTER);
        add(spinButton, BorderLayout.SOUTH);
    }

    public void changeColorMode() {
        setBackground(Colors.surfaceColor);

        inputInfoLabel.setForeground(Colors.textColor);

        namesArea.setForeground(Colors.textColor);
        namesArea.setBackground(Colors.backgroundColor);
        namesArea.setCaretColor(Colors.textColor);
        namesArea.setSelectionColor(Colors.accentColor);
        namesArea.setSelectedTextColor(Colors.accentTextColor);

        namesScroll.setBorder(new LineBorder(Colors.borderColor));
        namesScroll.getViewport().setBackground(Colors.backgroundColor);
        repaint();
    }

    /**
     * Setzt die Namen per Code. Löst {@link #onNamesChanged}, aber nicht {@link #onUserEdit} aus.
     */
    public void setNames(List<String> names) {
        programmaticChange = true;
        try {
            namesArea.setText(String.join("\n", names));
        } finally {
            programmaticChange = false;
        }
    }

    /** Die Zeilen der Liste (inklusive Gewichtung, z. B. "Anna;2"). */
    public List<String> getNames() {
        return NameLoader.parse(namesArea.getText());
    }

    /** Die Einträge mit erkannter Gewichtung; gleiche Reihenfolge wie {@link #getNames()}. */
    public List<WheelEntry> getEntries() {
        return NameLoader.parseEntries(namesArea.getText());
    }

    /** Wird bei jeder Änderung der Namensliste aufgerufen (auch bei Änderungen per Code). */
    public void onNamesChanged(Runnable listener) {
        addDocumentListener(listener, false);
    }

    /** Wird nur aufgerufen, wenn der Nutzer selbst tippt. */
    public void onUserEdit(Runnable listener) {
        addDocumentListener(listener, true);
    }

    public void onSpin(Runnable listener) {
        spinButton.addActionListener(e -> listener.run());
    }

    public void setSpinEnabled(boolean enabled) {
        spinButton.setEnabled(enabled);
    }

    public void setSpinning(boolean spinning) {
        spinButton.setEnabled(!spinning);
        namesArea.setEditable(!spinning);
    }

    private void addDocumentListener(Runnable listener, boolean userEditsOnly) {
        Runnable fire = () -> {
            if (!userEditsOnly || !programmaticChange) {
                listener.run();
            }
        };
        namesArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fire.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fire.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fire.run();
            }
        });
    }

    public void shuffleNames() {
        List<String> list = new ArrayList<>(getNames());
        Collections.shuffle(list);
        setNames(list);
    }

    public void sortNames() {
        List<String> list = new ArrayList<>(getNames());
        list.sort(String.CASE_INSENSITIVE_ORDER);
        setNames(list);
    }

    public void removeDuplicates() {
        List<String> unique = new ArrayList<>(
                new LinkedHashSet<>(getNames())
        );
        setNames(unique);
    }
}