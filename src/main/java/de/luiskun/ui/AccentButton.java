package de.luiskun.ui;

import de.luiskun.logic.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Flacher Button mit abgerundeten Ecken, der die Farben aus {@link Colors} zur Paint-Zeit liest
 * (Theme-Wechsel braucht daher nur ein Repaint).
 */
public class AccentButton extends JButton {

    private static final int ARC = 10;

    private final boolean primary;

    public AccentButton(String text, boolean primary) {
        super(text);
        this.primary = primary;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        setFont(getFont().deriveFont(Font.BOLD, 13f));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        ButtonModel model = getModel();
        boolean enabled = isEnabled();
        boolean hover = enabled && model.isRollover();
        boolean pressed = enabled && model.isPressed();

        Color fill;
        Color textColor;
        if (primary) {
            if (!enabled) {
                fill = Colors.borderColor;
            } else if (pressed) {
                fill = mix(Colors.accentColor, Color.BLACK, 0.15f);
            } else if (hover) {
                fill = mix(Colors.accentColor, Color.WHITE, 0.15f);
            } else {
                fill = Colors.accentColor;
            }
            textColor = enabled ? Colors.accentTextColor : Colors.mutedTextColor;
        } else {
            fill = (hover || pressed) ? Colors.borderColor : Colors.surfaceColor;
            textColor = enabled ? Colors.textColor : Colors.mutedTextColor;
        }

        int w = getWidth();
        int h = getHeight();
        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, ARC, ARC));
        if (!primary) {
            g2.setColor(Colors.borderColor);
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, ARC, ARC));
        }

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (w - fm.stringWidth(getText())) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(textColor);
        g2.drawString(getText(), x, y);
        g2.dispose();
    }

    private static Color mix(Color a, Color b, float t) {
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }
}