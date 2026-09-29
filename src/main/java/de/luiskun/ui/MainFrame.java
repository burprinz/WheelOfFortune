package de.luiskun.ui;

import de.luiskun.logic.Colors;
import de.luiskun.logic.WheelModel;
import de.luiskun.util.FileManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class MainFrame extends JFrame {

    private static final int FRAME_DELAY_MS = 16; // ca. 60 FPS

    public static final int MODE_WHEEL = 0;
    public static final int MODE_CONSTANT_SETTINGS = 1;
    private int currentMode = MODE_WHEEL;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    // Hauptrad
    private final WheelModel spinModel = new WheelModel();
    private final WheelPanel wheelPanel = new WheelPanel(spinModel);
    private final SidePanel sidePanel = new SidePanel();

    // Test-Rad der Physik-Einstellungen
    private final WheelModel testModel = new WheelModel();
    private final WheelPanel testWheelPanel = new WheelPanel(testModel);
    private final SettingsSidePanel settingsSidePanel = new SettingsSidePanel(this);

    private final Timer timer = new Timer(FRAME_DELAY_MS, e -> tick());
    private final FileManager fileManager = new FileManager(this, sidePanel);
    private final MenuBar menuBar = new MenuBar(this, fileManager);

    public MainFrame() {
        super("Wheel of Fortune");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeApplication();
            }
        });

        testModel.setNames(List.of("Option 1", "Option 2", "Option 3", "Option 4"));

        cardPanel.add(createSplit(wheelPanel, sidePanel), String.valueOf(MODE_WHEEL));
        cardPanel.add(createSplit(testWheelPanel, settingsSidePanel), String.valueOf(MODE_CONSTANT_SETTINGS));
        add(cardPanel);

        sidePanel.setNames(fileManager.loadLastOpenedWheel());
        applyNames();
        sidePanel.onNamesChanged(this::applyNames);
        sidePanel.onUserEdit(fileManager::markUnsaved);
        sidePanel.onSpin(this::spin);

        settingsSidePanel.onTestSpin(this::testSpin);

        setJMenuBar(menuBar);

        pack();
        setMinimumSize(new Dimension(500, 350));
        setLocationRelativeTo(null);

        changeColorMode();
    }

    public void switchMode(int mode) {
        timer.stop();
        testModel.stop();
        settingsSidePanel.setTestSpinEnabled(true);

        currentMode = mode;
        cardLayout.show(cardPanel, String.valueOf(mode));

        if (mode == MODE_CONSTANT_SETTINGS) {
            settingsSidePanel.updateSlidersFromWheelConstants();
        }
    }

    public void changeColorMode() {
        getContentPane().setBackground(Colors.backgroundColor);
        menuBar.changeColorMode();
        sidePanel.changeColorMode();
        settingsSidePanel.changeColorMode();
        repaint();
    }

    /**
     * Gemeinsames Aussehen beider Split-Panes (Divider reagiert dynamisch auf das Theme).
     */
    private static JSplitPane createSplit(JComponent left, JComponent right) {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(Colors.backgroundColor);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(Colors.borderColor);
                        g.fillRect(getWidth() / 2, 0, 1, getHeight());
                    }
                };
            }
        });
        split.setBorder(null);
        split.setDividerSize(7);
        split.setResizeWeight(1.0);
        split.setContinuousLayout(true);
        return split;
    }

    private void applyNames() {
        spinModel.setNames(sidePanel.getNames());
        sidePanel.setSpinEnabled(!spinModel.getNames().isEmpty() && !spinModel.isSpinning());
        wheelPanel.repaint();
    }

    private void spin() {
        if (!spinModel.spin()) {
            return;
        }
        setSpinning(true);
        timer.start();
    }

    private void testSpin(double minTopSpeed, double maxTopSpeed, double acceleration, double friction, double stopSpeed) {
        if (testModel.spin(minTopSpeed, maxTopSpeed, acceleration, friction, stopSpeed)) {
            settingsSidePanel.setTestSpinEnabled(false);
            timer.start();
        }
    }

    /** Sperrt Namensliste, Spin-Button und Menü, solange das Hauptrad dreht. */
    private void setSpinning(boolean spinning) {
        sidePanel.setSpinning(spinning);
        menuBar.setSpinLock(spinning);
    }

    private void tick() {
        boolean testMode = currentMode == MODE_CONSTANT_SETTINGS;
        WheelModel model = testMode ? testModel : spinModel;

        boolean finished = model.update();
        (testMode ? testWheelPanel : wheelPanel).repaint();
        if (!finished) {
            return;
        }

        timer.stop();
        if (testMode) {
            settingsSidePanel.setTestSpinEnabled(true);
        } else {
            setSpinning(false);
            applyNames();
            Popups.showWinnerDialog(this, sidePanel, fileManager, spinModel.getWinnerIndex());
        }
    }

    private void closeApplication() {
        if (!Popups.unsavedMessageDialog(this, fileManager)) {
            return;
        }
        fileManager.saveCurrentOpenedWheelAsDefault();
        System.exit(0);
    }
}