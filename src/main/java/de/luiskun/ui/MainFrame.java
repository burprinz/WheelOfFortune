package de.luiskun.ui;

import de.luiskun.logic.Colors;
import de.luiskun.logic.WheelModel;
import de.luiskun.util.FileManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private static final int FRAME_DELAY_MS = 16; // ca. 60 FPS

    private final JSplitPane split;
    private final WheelModel model = new WheelModel();
    private final WheelPanel wheelPanel = new WheelPanel(model);
    private final SidePanel sidePanel = new SidePanel(this);
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

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, wheelPanel, sidePanel);
        split.setResizeWeight(1.0);
        split.setContinuousLayout(true);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        sidePanel.setNames(fileManager.loadLastOpenedWheel());
        applyNames();
        sidePanel.onNamesChanged(this::applyNames);
        sidePanel.onSpin(this::spin);
        setJMenuBar(new MenuBar(this, fileManager));

        pack();
        setMinimumSize(new Dimension(500, 350));
        setLocationRelativeTo(null);

        changeColorMode();
    }

    public void changeColorMode() {
        split.setUI(new BasicSplitPaneUI()
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

        split.setBorder(new LineBorder(Colors.textColor));

        wheelPanel.changeColorMode();
        sidePanel.changeColorMode();

    }

    private void applyNames() {
        List<String> names = sidePanel.getNames();
        model.setNames(names);
        sidePanel.setSpinEnabled(!names.isEmpty());
        wheelPanel.repaint();
    }

    private void spin() {
        if (!model.spin()) {
            return;
        }
        sidePanel.setSpinning(true);
        timer.start();
    }

    private void tick() {
        boolean finished = model.update();
        wheelPanel.repaint();
        if (finished) {
            timer.stop();
            sidePanel.setSpinning(false);
            Popups.showWinnerDialog(this, sidePanel, model.getWinner());
        }
    }

}