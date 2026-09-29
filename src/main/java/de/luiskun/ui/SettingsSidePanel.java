package de.luiskun.ui;

import de.luiskun.logic.Colors;
import de.luiskun.logic.WheelConstants;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Locale;

public class SettingsSidePanel extends JPanel {

    private final MainFrame frame;

    private final JLabel titleLabel = new JLabel("Physics Settings");

    // Slider arbeiten mit ganzen Zahlen: Wert = Sliderwert / scale
    private final SliderRow minTopSpeed = new SliderRow("Min Top Speed", "%.1f", 10, 1.0, 100.0);
    private final SliderRow maxTopSpeed = new SliderRow("Max Top Speed", "%.1f", 10, 3.0, 300.0);
    private final SliderRow acceleration = new SliderRow("Acceleration", "%.2f", 100, 0.05, 5.0);
    // Skaliert die Bremsrate (1 - friction) von 0.1x bis 10x des Standards
    private final SliderRow friction = new SliderRow("Friction", "%.4f", 10000, 0.85, 0.9985);
    private final SliderRow stopSpeed = new SliderRow("Stop Speed", "%.3f", 1000, 0.01, 1.0);
    private final List<SliderRow> rows = List.of(minTopSpeed, maxTopSpeed, acceleration, friction, stopSpeed);

    private final AccentButton testSpinButton = new AccentButton("Test Spin", false);
    private final AccentButton applyChangesButton = new AccentButton("Apply Changes", true);
    private final AccentButton restoreDefaultButton = new AccentButton("Restore Default", false);
    private final AccentButton cancelButton = new AccentButton("Cancel", false);

    public SettingsSidePanel(MainFrame frame) {
        super(new BorderLayout(8, 8));
        this.frame = frame;

        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setPreferredSize(new Dimension(240, 0));
        setMinimumSize(new Dimension(200, 0));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(14));
        rows.forEach(row -> row.addTo(content));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(0, 1, 0, 8));
        bottom.setOpaque(false);
        bottom.add(testSpinButton);
        bottom.add(applyChangesButton);
        bottom.add(restoreDefaultButton);
        bottom.add(cancelButton);
        add(bottom, BorderLayout.SOUTH);

        applyChangesButton.addActionListener(e -> {
            WheelConstants.setNewValues(minTopSpeed.get(), maxTopSpeed.get(), acceleration.get(), friction.get(), stopSpeed.get());
            frame.switchMode(MainFrame.MODE_WHEEL);
        });
        restoreDefaultButton.addActionListener(e -> loadSliders(
                WheelConstants.DEFAULT_MIN_TOP_SPEED, WheelConstants.DEFAULT_MAX_TOP_SPEED,
                WheelConstants.DEFAULT_ACCELERATION, WheelConstants.DEFAULT_FRICTION, WheelConstants.DEFAULT_STOP_SPEED));
        cancelButton.addActionListener(e -> frame.switchMode(MainFrame.MODE_WHEEL));

        updateSlidersFromWheelConstants();
    }

    @FunctionalInterface
    public interface TestSpinListener {
        void onTestSpin(double minTopSpeed, double maxTopSpeed, double acceleration, double friction, double stopSpeed);
    }

    public void onTestSpin(TestSpinListener listener) {
        testSpinButton.addActionListener(e -> listener.onTestSpin(
                minTopSpeed.get(), maxTopSpeed.get(), acceleration.get(), friction.get(), stopSpeed.get()));
    }

    public void setTestSpinEnabled(boolean enabled) {
        testSpinButton.setEnabled(enabled);
    }

    public void updateSlidersFromWheelConstants() {
        loadSliders(WheelConstants.MIN_TOP_SPEED, WheelConstants.MAX_TOP_SPEED,
                WheelConstants.ACCELERATION, WheelConstants.FRICTION, WheelConstants.STOP_SPEED);
    }

    public void changeColorMode() {
        setBackground(Colors.surfaceColor);
        titleLabel.setForeground(Colors.textColor);
        rows.forEach(row -> row.label.setForeground(Colors.textColor));
        repaint();
    }

    private void loadSliders(double minTop, double maxTop, double acc, double fric, double stop) {
        minTopSpeed.set(minTop);
        maxTopSpeed.set(maxTop);
        acceleration.set(acc);
        friction.set(fric);
        stopSpeed.set(stop);
    }

    /**
     * Label + Slider mit Skalierung Ganzzahl <-> double.
     */
    private static final class SliderRow {
        private final String name;
        private final String format;
        private final int scale;

        final JLabel label = new JLabel();
        final JSlider slider;

        SliderRow(String name, String format, int scale, double min, double max) {
            this.name = name;
            this.format = format;
            this.scale = scale;

            slider = new JSlider((int) Math.round(min * scale), (int) Math.round(max * scale));
            slider.setOpaque(false);
            slider.setFocusable(false);
            slider.addChangeListener(e -> updateLabel());

            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            slider.setAlignmentX(Component.LEFT_ALIGNMENT);
            updateLabel();
        }

        double get() {
            return slider.getValue() / (double) scale;
        }

        void set(double value) {
            slider.setValue((int) Math.round(value * scale));
        }

        void addTo(JPanel container) {
            container.add(label);
            container.add(slider);
            container.add(Box.createVerticalStrut(10));
        }

        private void updateLabel() {
            label.setText(name + ": " + String.format(Locale.ROOT, format, get()));
        }
    }
}