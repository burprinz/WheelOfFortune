package de.luiskun.ui;

import de.luiskun.util.NameLoader;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

/**
 * Rechte Seitenleiste: Namensliste bearbeiten, Rad drehen, Ergebnis anzeigen.
 */
public class SidePanel extends JPanel {

    private final JTextArea namesArea = new JTextArea();
    private final JButton spinButton = new JButton("Spin");

    public SidePanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setPreferredSize(new Dimension(220, 0));

        add(new JLabel("Names (one per line)"), BorderLayout.NORTH);
        add(new JScrollPane(namesArea), BorderLayout.CENTER);


        JPanel bottom = new JPanel(new GridLayout(0, 1, 0, 8));
        bottom.add(spinButton);
        add(bottom, BorderLayout.SOUTH);
    }

    public void setNames(List<String> names) {
        namesArea.setText(String.join("\n", names));
    }

    public List<String> getNames() {
        return NameLoader.parse(namesArea.getText());
    }

    public void onNamesChanged(Runnable listener) {
        namesArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                listener.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                listener.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                listener.run();
            }
        });
    }

    public void onSpin(Runnable listener) {
        spinButton.addActionListener(e -> listener.run());
    }

    public void setResult(String text) {
        //resultLabel.setText(text.isEmpty() ? " " : text);
    }

    public void setSpinEnabled(boolean enabled) {
        spinButton.setEnabled(enabled);
    }

    public void setSpinning(boolean spinning) {
        spinButton.setEnabled(!spinning);
        namesArea.setEditable(!spinning);
    }
}