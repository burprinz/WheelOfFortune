package de.luiskun.ui;

import de.luiskun.logic.Colors;
import de.luiskun.logic.WheelConstants;

import javax.swing.*;
import java.awt.*;

public class SettingsSidePanel extends JPanel {


    private final MainFrame frame;

    // UI-Elemente
    private final JLabel titleLabel = new JLabel("Physics Settings");

    private final JLabel minTopSpeedLabel = new JLabel();
    // Range: 1.0 bis 100.0 (Scaled x10: 10 bis 1000, Default: 100 -> 10.0)
    private final JSlider minTopSpeedSlider = createSlider(10, 1000, 100);

    private final JLabel maxTopSpeedLabel = new JLabel();
    // Range: 3.0 bis 300.0 (Scaled x10: 30 bis 3000, Default: 300 -> 30.0)
    private final JSlider maxTopSpeedSlider = createSlider(30, 3000, 300);

    private final JLabel accelerationLabel = new JLabel();
    // Range: 0.05 bis 5.00 (Scaled x100: 5 bis 500, Default: 50 -> 0.50)
    private final JSlider accelerationSlider = createSlider(5, 500, 50);

    private final JLabel frictionLabel = new JLabel();
    // Skaliert die Bremsrate (1 - friction) von 0.1x bis 10x
    // Range: 0.8500 bis 0.9985 (Scaled x10000: 8500 bis 9985, Default: 9850 -> 0.9850)
    private final JSlider frictionSlider = createSlider(8500, 9985, 9850);

    private final JLabel stopSpeedLabel = new JLabel();
    // Range: 0.010 bis 1.000 (Scaled x1000: 10 bis 1000, Default: 100 -> 0.100)
    private final JSlider stopSpeedSlider = createSlider(10, 1000, 100);

    // Buttons
    private final JButton testSpinButton = new JButton("Test Spin");
    private final JButton applyChangesButton = new JButton("Apply Changes");
    private final JButton restoreDefaultButton = new JButton("Restore Default");
    private final JButton cancelButton = new JButton("Cancel");

    public SettingsSidePanel(MainFrame frame) {
        super(new BorderLayout(8, 8));
        this.frame = frame;

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setPreferredSize(new Dimension(240, 0));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(12));

        // Slider-Gruppen mit Text-Update hinzufügen
        addSliderGroup(contentPanel, minTopSpeedLabel, minTopSpeedSlider, val -> {
            double value = val / 10.0;
            minTopSpeedLabel.setText(String.format("Min Top Speed: %.1f", value));
        });

        addSliderGroup(contentPanel, maxTopSpeedLabel, maxTopSpeedSlider, val -> {
            double value = val / 10.0;
            maxTopSpeedLabel.setText(String.format("Max Top Speed: %.1f", value));
        });

        addSliderGroup(contentPanel, accelerationLabel, accelerationSlider, val -> {
            double value = val / 100.0;
            accelerationLabel.setText(String.format("Acceleration: %.2f", value));
        });

        addSliderGroup(contentPanel, frictionLabel, frictionSlider, val -> {
            double value = val / 10000.0;
            frictionLabel.setText(String.format("Friction: %.4f", value));
        });

        addSliderGroup(contentPanel, stopSpeedLabel, stopSpeedSlider, val -> {
            double value = val / 1000.0;
            stopSpeedLabel.setText(String.format("Stop Speed: %.3f", value));
        });

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons unten platzieren
        JPanel bottomPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        bottomPanel.setOpaque(false);
        bottomPanel.add(testSpinButton);
        bottomPanel.add(applyChangesButton);
        bottomPanel.add(restoreDefaultButton);
        bottomPanel.add(cancelButton);
        add(bottomPanel, BorderLayout.SOUTH);

        applyChangesButton.addActionListener(e -> {
            WheelConstants.setNewValues(getMinTopSpeed(), getMaxTopSpeed(), getAcceleration(), getFriction(), getStopSpeed());
            frame.switchMode(MainFrame.MODE_WHEEL);
        });

        restoreDefaultButton.addActionListener(e -> {
            setMinTopSpeed(WheelConstants.DEFAULT_MIN_TOP_SPEED);
            setMaxTopSpeed(WheelConstants.DEFAULT_MAX_TOP_SPEED);
            setAcceleration(WheelConstants.DEFAULT_ACCELERATION);
            setFriction(WheelConstants.DEFAULT_FRICTION);
            setStopSpeed(WheelConstants.DEFAULT_STOP_SPEED);
        });

        cancelButton.addActionListener(e -> frame.switchMode(MainFrame.MODE_WHEEL));
    }

    private JSlider createSlider(int min, int max, int value) {
        JSlider slider = new JSlider(min, max, value);
        slider.setOpaque(false);
        slider.setFocusable(false);
        return slider;
    }

    private void addSliderGroup(JPanel container, JLabel label, JSlider slider, java.util.function.Consumer<Integer> onUpdate) {
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);

        onUpdate.accept(slider.getValue());

        slider.addChangeListener(e -> onUpdate.accept(slider.getValue()));

        container.add(label);
        container.add(slider);
        container.add(Box.createVerticalStrut(10));
    }

    @FunctionalInterface
    public interface TestSpinListener {
        void onTestSpin(double minTopSpeed, double maxTopSpeed, double acceleration, double friction, double stopSpeed);
    }

    public void onTestSpin(TestSpinListener listener) {
        testSpinButton.addActionListener(e -> {
            if (listener != null) {
                listener.onTestSpin(
                        getMinTopSpeed(),
                        getMaxTopSpeed(),
                        getAcceleration(),
                        getFriction(),
                        getStopSpeed()
                );
            }
        });
    }

    public void setTestSpinEnabled(boolean enabled) {
        testSpinButton.setEnabled(enabled);
    }

    public double getMinTopSpeed() {
        return minTopSpeedSlider.getValue() / 10.0;
    }

    public double getMaxTopSpeed() {
        return maxTopSpeedSlider.getValue() / 10.0;
    }

    public double getAcceleration() {
        return accelerationSlider.getValue() / 100.0;
    }

    public double getFriction() {
        return frictionSlider.getValue() / 10000.0;
    }

    public double getStopSpeed() {
        return stopSpeedSlider.getValue() / 1000.0;
    }


    public void setMinTopSpeed(double value) {
        minTopSpeedSlider.setValue((int) Math.round(value * 10.0));
    }

    public void setMaxTopSpeed(double value) {
        maxTopSpeedSlider.setValue((int) Math.round(value * 10.0));
    }

    public void setAcceleration(double value) {
        accelerationSlider.setValue((int) Math.round(value * 100.0));
    }

    public void setFriction(double value) {
        frictionSlider.setValue((int) Math.round(value * 10000.0));
    }

    public void setStopSpeed(double value) {
        stopSpeedSlider.setValue((int) Math.round(value * 1000.0));
    }

    public void changeColorMode() {
        setBackground(Colors.backgroundColor);

        titleLabel.setForeground(Colors.textColor);

        minTopSpeedLabel.setForeground(Colors.textColor);
        maxTopSpeedLabel.setForeground(Colors.textColor);
        accelerationLabel.setForeground(Colors.textColor);
        frictionLabel.setForeground(Colors.textColor);
        stopSpeedLabel.setForeground(Colors.textColor);

        minTopSpeedSlider.setBackground(Colors.backgroundColor);
        maxTopSpeedSlider.setBackground(Colors.backgroundColor);
        accelerationSlider.setBackground(Colors.backgroundColor);
        frictionSlider.setBackground(Colors.backgroundColor);
        stopSpeedSlider.setBackground(Colors.backgroundColor);
    }

    public void updateSlidersFromWheelConstants() {
        setMinTopSpeed(WheelConstants.MIN_TOP_SPEED);
        setMaxTopSpeed(WheelConstants.MAX_TOP_SPEED);
        setAcceleration(WheelConstants.ACCELERATION);
        setFriction(WheelConstants.FRICTION);
        setStopSpeed(WheelConstants.STOP_SPEED);
    }
}