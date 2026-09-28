package de.luiskun.ui;

import de.luiskun.logic.Colors;
import de.luiskun.logic.SettingsWheelModel;
import de.luiskun.logic.WheelModel;
import de.luiskun.util.FileManager;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private static final int FRAME_DELAY_MS = 16; // ca. 60 FPS

    public static final int MODE_WHEEL = 0;
    public static final int MODE_CONSTANT_SETTINGS = 1;
    private int currentMode = MODE_WHEEL;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    // Wheel
    private final JSplitPane wheelSplit;
    // Constant Settings
    private final JSplitPane constantsSplit;

    private final WheelModel spinModel = new WheelModel();
    private final WheelPanel wheelPanel = new WheelPanel(spinModel);
    private final SidePanel sidePanel = new SidePanel(this);

    private final SettingsWheelModel constantWheelModel = new SettingsWheelModel();
    private final SettingsWheelPanel constantsWheelPanel = new SettingsWheelPanel(constantWheelModel);
    private final SettingsSidePanel constantsSidePanel = new SettingsSidePanel(this);

    private final Timer timer = new Timer(FRAME_DELAY_MS, e -> tick());
    private final FileManager fileManager = new FileManager(this, sidePanel);

    boolean unsavedChanges = false;

    public MainFrame() {
        super("Wheel of Fortune");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        MainFrame mainFrame = this;

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                Popups.unsavedMessageDialog(mainFrame, fileManager);
                fileManager.saveCurrentOpenedWheelAsDefault();
                System.exit(0);
            }
        });

        setLayout(new BorderLayout());

        wheelSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, wheelPanel, sidePanel);
        wheelSplit.setResizeWeight(1.0);
        wheelSplit.setContinuousLayout(true);
        wheelSplit.setBorder(null);

        constantsSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, constantsWheelPanel, constantsSidePanel);

        cardPanel.add(wheelSplit, String.valueOf(MODE_WHEEL));
        cardPanel.add(constantsSplit, String.valueOf(MODE_CONSTANT_SETTINGS));

        add(cardPanel, BorderLayout.CENTER);

        sidePanel.setNames(fileManager.loadLastOpenedWheel());
        applyNames();
        sidePanel.onNamesChanged(this::applyNames);
        sidePanel.onSpin(this::spin);

        // Test Spin Listener
        constantsSidePanel.onTestSpin((minTopSpeed, maxTopSpeed, acceleration, friction, stopSpeed) -> {
            boolean started = constantWheelModel.spin(minTopSpeed, maxTopSpeed, acceleration, friction, stopSpeed);
            if (started) {
                constantsSidePanel.setTestSpinEnabled(false);
                if (!timer.isRunning()) {
                    timer.start();
                }
            }
        });

        setJMenuBar(new MenuBar(this, fileManager));

        pack();
        setMinimumSize(new Dimension(500, 350));
        setLocationRelativeTo(null);

        changeColorMode();
    }

    public void switchMode(int mode) {
        if (timer.isRunning()) {
            timer.stop();
        }

        constantWheelModel.stop();
        constantsSidePanel.setTestSpinEnabled(true);

        this.currentMode = mode;
        cardLayout.show(cardPanel, String.valueOf(mode));

        if (mode == MODE_WHEEL) {
            // Falls Apply gedrückt wurde: Hauptrad stoppen und UI entsperren
            spinModel.stop();
            sidePanel.setSpinning(false);
            wheelPanel.repaint();
        } else if (mode == MODE_CONSTANT_SETTINGS) {
            constantsSidePanel.updateSlidersFromWheelConstants();
        }
    }

    public void changeColorMode() {
        wheelSplit.setUI(new BasicSplitPaneUI()
        {
            @Override
            public BasicSplitPaneDivider createDefaultDivider()
            {
                return new BasicSplitPaneDivider(this)
                {
                    @Override
                    public void paint(Graphics g)
                    {
                        g.setColor(Colors.backgroundColor);
                        g.fillRect(0, 0, getSize().width, getSize().height);
                        super.paint(g);
                    }
                };
            }
        });

        wheelSplit.setBorder(new LineBorder(Colors.textColor));

        wheelPanel.changeColorMode();
        sidePanel.changeColorMode();
        constantsWheelPanel.changeColorMode();
        constantsSidePanel.changeColorMode();
    }

    private void applyNames() {
        List<String> names = sidePanel.getNames();
        spinModel.setNames(names);
        sidePanel.setSpinEnabled(!names.isEmpty() && !spinModel.isSpinning());
        wheelPanel.repaint();
    }

    private void spin() {
        if (!spinModel.spin()) {
            return;
        }
        sidePanel.setSpinning(true);
        timer.start();
    }

    private void tick() {
        if (currentMode == MODE_WHEEL) {
            boolean finished = spinModel.update();
            wheelPanel.repaint();
            if (finished) {
                timer.stop();
                sidePanel.setSpinning(false);
                Popups.showWinnerDialog(this, sidePanel, spinModel.getWinner());
            }
        } else if (currentMode == MODE_CONSTANT_SETTINGS) {
            boolean finished = constantWheelModel.update();
            constantsWheelPanel.repaint();
            if (finished) {
                timer.stop();
                constantsSidePanel.setTestSpinEnabled(true);
            }
        }
    }
}