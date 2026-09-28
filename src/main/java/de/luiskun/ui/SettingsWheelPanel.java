package de.luiskun.ui;

import de.luiskun.logic.Colors;
import de.luiskun.logic.SettingsWheelModel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.List;

public class SettingsWheelPanel extends JPanel {

    private static final int MARGIN = 24;

    private final SettingsWheelModel model;

    public SettingsWheelPanel(SettingsWheelModel model) {
        this.model = model;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 500));
        setMinimumSize(new Dimension(250, 250));
    }

    public void changeColorMode() {
        setBackground(Colors.backgroundColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int diameter = Math.min(getWidth(), getHeight()) - 2 * MARGIN;

        if (diameter <= 0) {
            g2.dispose();
            return;
        }

        List<String> names = model.getNames();
        int n = names.isEmpty() ? 4 : names.size();

        double r = diameter / 2.0;
        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;
        double sliceSize = 360.0 / n;
        double angle = model.getAngle();

        g2.setFont(getFont().deriveFont(Font.PLAIN, (float) Math.max(10, r * 0.08)));

        for (int i = 0; i < n; i++) {
            double start = angle + i * sliceSize;

            g2.setColor(Colors.colorFor(i));
            g2.fill(new Arc2D.Double(cx - r, cy - r, diameter, diameter, start, sliceSize, Arc2D.PIE));
        }
        g2.dispose();
    }
}