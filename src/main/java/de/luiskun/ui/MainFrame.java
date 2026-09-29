package de.luiskun.ui;

import de.luiskun.logic.Colors;
import de.luiskun.logic.RemovalHistory;
import de.luiskun.logic.RemovalHistory.Removal;
import de.luiskun.logic.WheelModel;
import de.luiskun.logic.WheelSound;
import de.luiskun.util.FileManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
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

    private final RemovalHistory removalHistory = new RemovalHistory();

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

        // Strg+Space for Spinning
        KeyStroke ks = KeyStroke.getKeyStroke(
                KeyEvent.VK_SPACE,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
        );
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(ks,"spin");
        getRootPane().getActionMap().put("spin", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentMode == MODE_WHEEL){
                    spin();
                }
            }
        });

        sidePanel.setNames(fileManager.loadLastOpenedWheel());
        applyNames();
        sidePanel.onNamesChanged(this::applyNames);
        sidePanel.onUserEdit(fileManager::markUnsaved);
        sidePanel.onSpin(this::spin);
        fileManager.onWheelReplaced(() -> {
            removalHistory.clear();
            updateHistoryMenu();
        });

        settingsSidePanel.onTestSpin(this::testSpin);

        setJMenuBar(menuBar);

        pack();
        setMinimumSize(new Dimension(500, 350));
        setLocationRelativeTo(null);

        changeColorMode();

        // Wenn TXT reingezogen wird
        setTransferHandler(new TransferHandler(){
            @Override
            public boolean canImport(TransferSupport s){
                return s.isDataFlavorSupported(
                        DataFlavor.javaFileListFlavor
                );
            }

            @Override
            public boolean importData(TransferSupport s){
                if(!canImport(s))
                    return false;
                try{
                    List<File> files = (List<File>)
                            s.getTransferable().getTransferData(
                                    DataFlavor.javaFileListFlavor
                            );
                    if(files.isEmpty())
                        return false;
                    if(!Popups.unsavedMessageDialog(MainFrame.this,fileManager))
                        return false;
                    fileManager.load(files.get(0).toPath());
                    return true;
                }catch(Exception e){
                    return false;
                }
            }
        });
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

        // Segmentwechsel für Tick-Sound erkennen
        int before = model.getPointerIndex();
        boolean finished = model.update();
        int after = model.getPointerIndex();

        if (!testMode && before != after && model.isSpinning()) {
            WheelSound.tick();
            wheelPanel.pointerKick();
        }

        (testMode ? testWheelPanel : wheelPanel).repaint();

        if (!finished) {
            return;
        }

        timer.stop();

        if (testMode) {
            settingsSidePanel.setTestSpinEnabled(true);
        } else {
            setSpinning(false);

            spinModel.startWinnerAnimation();
            wheelPanel.startConfetti();
            WheelSound.win();
            wheelPanel.repaint();

            applyNames();
            offerWinnerRemoval();

            spinModel.stopWinnerAnimation();
            wheelPanel.repaint();
        }
    }

    /** Fragt nach dem Spin, ob der Gewinner behalten oder entfernt werden soll. */
    private void offerWinnerRemoval() {
        List<String> names = new ArrayList<>(sidePanel.getNames());
        int index = spinModel.getWinnerIndex();
        if (index < 0 || index >= names.size()) {
            return;
        }

        String winner = names.get(index);
        if (!Popups.showWinnerDialog(this, winner)) {
            return;
        }


        names.remove(index);
        sidePanel.setNames(names);
        removalHistory.record(new Removal(index, winner));
        fileManager.markUnsaved();
        updateHistoryMenu();
    }

    /** Fügt den zuletzt entfernten Eintrag an seiner alten Position wieder ein. */
    public void undoRemoval() {
        Removal removal = removalHistory.popUndo();
        if (removal == null) {
            return;
        }

        List<String> names = new ArrayList<>(sidePanel.getNames());
        int index = Math.min(removal.index(), names.size());
        names.add(index, removal.name());
        sidePanel.setNames(names);

        removalHistory.pushRedo(new Removal(index, removal.name()));
        fileManager.markUnsaved();
        updateHistoryMenu();
    }

    /** Entfernt den zuletzt wiederhergestellten Eintrag erneut. */
    public void redoRemoval() {
        Removal removal = removalHistory.popRedo();
        if (removal == null) {
            return;
        }

        List<String> names = new ArrayList<>(sidePanel.getNames());
        boolean sameSpot = removal.index() < names.size() && names.get(removal.index()).equals(removal.name());
        int index = sameSpot ? removal.index() : names.indexOf(removal.name());

        // Wurde der Eintrag zwischenzeitlich von Hand gelöscht, gibt es nichts mehr zu entfernen
        if (index >= 0) {
            names.remove(index);
            sidePanel.setNames(names);
            removalHistory.pushUndo(new Removal(index, removal.name()));
            fileManager.markUnsaved();
        }
        updateHistoryMenu();
    }

    private void updateHistoryMenu() {
        menuBar.setHistoryState(removalHistory.canUndo(), removalHistory.canRedo());
    }

    private void closeApplication() {
        if (!Popups.unsavedMessageDialog(this, fileManager)) {
            return;
        }
        fileManager.saveCurrentOpenedWheelAsDefault();
        System.exit(0);
    }

    public void shuffleNames(){
        sidePanel.shuffleNames();
        fileManager.markUnsaved();
    }

    public void sortNames(){
        sidePanel.sortNames();
        fileManager.markUnsaved();
    }

    public void removeDuplicates(){
        sidePanel.removeDuplicates();
        fileManager.markUnsaved();
    }
}