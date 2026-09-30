package de.luiskun.ui;

import de.luiskun.logic.Colors;

import javax.swing.*;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Nicht-modales Hilfefenster, das alle Funktionen erklärt. Die Farben folgen dem aktuellen Theme.
 */
public class HelpDialog extends JDialog {

    private static final String MOD =
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() == InputEvent.META_DOWN_MASK ? "Cmd" : "Ctrl";



    private final JEditorPane pane = new JEditorPane();
    private final JScrollPane scroll = new JScrollPane(pane);

    public HelpDialog(Frame owner) {
        super(owner, "Help – Wheel of Fortune", false);

        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        scroll.setBorder(null);
        add(scroll);

        getRootPane().registerKeyboardAction(e -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        setSize(580, 640);
        setLocationRelativeTo(owner);
        refresh();
    }

    /** Baut Farben und Text neu auf (nach einem Theme-Wechsel oder vor dem Anzeigen). */
    public final void refresh() {
        pane.setBackground(Colors.backgroundColor);
        scroll.getViewport().setBackground(Colors.backgroundColor);
        getContentPane().setBackground(Colors.backgroundColor);
        pane.setText(buildHtml());
        pane.setCaretPosition(0);
    }

    private static String hex(Color c) {
        return String.format("#%06X", c.getRGB() & 0xFFFFFF);
    }

    private static String buildHtml() {
        String css = """
                body { font-family: SansSerif; font-size: 13pt; color: %TEXT%; background-color: %BG%; }
                h1 { font-size: 20pt; color: %ACCENT%; }
                h2 { font-size: 15pt; color: %ACCENT%; margin-top: 14px; }
                code { font-family: Monospaced; color: %ACCENT%; }
                .muted { color: %MUTED%; }
                """
                .replace("%TEXT%", hex(Colors.textColor))
                .replace("%BG%", hex(Colors.backgroundColor))
                .replace("%ACCENT%", hex(Colors.accentColor))
                .replace("%MUTED%", hex(Colors.mutedTextColor));

        String body = """
                <h1>Wheel of Fortune</h1>

                <h2>Spinning</h2>
                <ul>
                  <li>Type one entry per line into the list on the right, then click <b>Spin</b>
                      or press <code>MOD+Space</code>.</li>
                  <li>The winner is the segment below the pointer at the top.</li>
                  <li>After each spin you can <b>keep</b> the winner or <b>remove</b> it from the entries.</li>
                  <li>While the wheel spins, most things like the entry field and some menu fields are locked.</li>
                </ul>

                <h2>Weights</h2>
                <ul>
                  <li>Write a <b>whole number after the last semicolon</b> of a line to give that entry a weight,
                      e.g. <code>Niklas;3</code>. A larger weight means a larger segment and a higher chance.</li>
                  <li>Lines without a valid number have weight <b>1</b>. You can freely mix weighted
                      and unweighted lines.</li>
                  <li>Only positive whole numbers (&ge 1) count. Something like <code>Niklas;0</code>,
                      <code>Hauke;x</code> or <code>Joshi;2.5</code> gets the weight 1.</li>
                  <li>Semicolons inside a name are fine: <code>Hello; World;2</code> is the entry
                      "Hello; World" with weight 2.</li>
                  <li>Example: <code>Niklas;2</code>, <code>Hauke</code>, <code>Joshi</code> gives Niklas 50%,
                      Hauke 25% and Joshi 25%.</li>
                  <li>Weights are saved in the file and are not shown on the wheel &ndash; the size of the
                      segment shows them.</li>
                </ul>

                <h2>Wheels menu (files)</h2>
                <ul>
                  <li><b>Create</b> (<code>MOD+N</code>): new empty wheel.</li>
                  <li><b>Load</b> (<code>MOD+L</code>) / <b>Save</b> (<code>MOD+S</code>) /
                      <b>Save as</b> (<code>MOD+Shift+S</code>): wheels are plain text files
                      (<code>.txt</code>), one entry per line.</li>
                  <li><b>Recent</b>: quickly reopen your last opened wheels.</li>
                  <li>You can also <b>drag and drop</b> a <code>.txt</code> file onto the window.</li>
                  <li>A star in the title marks unsaved changes, but you will also get asked if you
                      want to save them before they get lost.</li>
                  <li>The last opened wheel is loaded on the next start.</li>
                </ul>

                <h2>Removing winners</h2>
                <ul>
                  <li><b>Settings &rarr; Undo removal</b> (<code>MOD+Z</code>) puts the last removed entry back
                      at its old position, <b>Redo removal</b> (<code>MOD+Shift+Z</code>) removes it again.</li>
                </ul>

                <h2>Tools menu</h2>
                <ul>
                  <li><b>Shuffle</b>: random order of the list.</li>
                  <li><b>Sort A-Z</b>: alphabetical order.</li>
                  <li><b>Remove duplicates</b>: deletes identical lines and keeps the first one.</li>
                </ul>

                <h2>Appearance</h2>
                <ul>
                  <li><b>Dark mode</b> switches the colors. Your choice is remembered.</li>
                </ul>

                <h2>Physics settings</h2>
                <p>Open <b>Settings &rarr; Change wheel constants</b>:</p>
                <ul>
                  <li><b>Min / Max Top Speed</b>: every spin picks a random top speed between these values.</li>
                  <li><b>Acceleration</b>: how fast the wheel picks up speed.</li>
                  <li><b>Friction</b>: closer to 1 means the wheel runs longer before it stops.</li>
                  <li><b>Stop Speed</b>: the wheel stops once it is slower than this.</li>
                  <li><b>Test Spin</b> tries the current slider values on a test wheel,
                      <b>Apply Changes</b> saves them, <b>Restore Default</b> resets the sliders,
                      <b>Cancel</b> discards the changes.</li>
                </ul>

                <h2>Shortcuts</h2>
                <ul>
                  <li><code>MOD+Space</code> spin &nbsp</li>
                  <li><code>MOD+N</code> create &nbsp</li>
                  <li><code>MOD+L</code> load &nbsp</li>
                  <li><code>MOD+S</code> save &nbsp</li>
                  <li><code>MOD+Shift+S</code> save as &nbsp</li>
                  <li><code>MOD+Z</code> undo removal &nbsp</li>
                  <li><code>MOD+Shift+Z</code> redo removal &nbsp</li>
                  <li><code>F1</code> this info</li>
                </ul>
                """.replace("MOD", MOD);

        return "<html><head><style type=\"text/css\">" + css + "</style></head><body>" + body + "</body></html>";
    }
}
