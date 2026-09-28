package de.luiskun.ui;

import de.luiskun.logic.WheelModel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.List;


public class WheelPanel extends JPanel {

    private static final int MARGIN = 24;
    private static final int POINTER_SIZE = 14;

    private final WheelModel model;

    public WheelPanel(WheelModel model) {
        this.model = model;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 500));
        setMinimumSize(new Dimension(250, 250));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        List<String> names = model.getNames();
        int diameter = Math.min(getWidth(), getHeight()) - 2 * MARGIN;

        if (diameter <= 0) {
            g2.dispose();
            return;
        }
        if (names.isEmpty()) {
            drawCenteredMessage(g2, "Keine Namen vorhanden");
            g2.dispose();
            return;
        }

        double r = diameter / 2.0;
        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;
        int n = names.size();
        double sliceSize = 360.0 / n;
        double angle = model.getAngle();

        g2.setFont(getFont().deriveFont(Font.PLAIN, (float) Math.max(10, r * 0.08)));
        FontMetrics fm = g2.getFontMetrics();
        int maxTextWidth = (int) (r * 0.7);
        double textRadius = r * 0.6;

        for (int i = 0; i < n; i++) {
            double start = angle + i * sliceSize;

            g2.setColor(colorFor(i));
            g2.fill(new Arc2D.Double(cx - r, cy - r, diameter, diameter, start, sliceSize, Arc2D.PIE));

            double mid = Math.toRadians(start + sliceSize / 2.0);
            String text = fit(names.get(i), fm, maxTextWidth);

            Graphics2D textG = (Graphics2D) g2.create();
            textG.translate(cx + textRadius * Math.cos(mid), cy - textRadius * Math.sin(mid));
            textG.rotate(-mid);
            textG.setColor(Color.BLACK);
            textG.drawString(text, -fm.stringWidth(text) / 2, (fm.getAscent() - fm.getDescent()) / 2);
            textG.dispose();
        }

        drawPointer(g2, (int) cx, (int) (cy - r));
        g2.dispose();
    }

    private void drawPointer(Graphics2D g2, int x, int wheelTop) {
        g2.setColor(Color.BLACK);
        g2.fillPolygon(
                new int[]{x - POINTER_SIZE, x + POINTER_SIZE, x},
                new int[]{wheelTop - POINTER_SIZE, wheelTop - POINTER_SIZE, wheelTop + POINTER_SIZE * 2},
                3);
    }

    private void drawCenteredMessage(Graphics2D g2, String message) {
        g2.setColor(Color.GRAY);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(message,
                (getWidth() - fm.stringWidth(message)) / 2,
                (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
    }

    private static Color colorFor(int index) {
        return Color.getHSBColor((index * 0.618033988749895f) % 1.0f, 0.65f, 0.9f);
    }

    private static String fit(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (fm.stringWidth(sb.toString() + c + "...") > maxWidth) {
                break;
            }
            sb.append(c);
        }
        return sb + "...";
    }
}