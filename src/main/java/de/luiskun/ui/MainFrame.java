package de.luiskun.ui;

import de.luiskun.logic.WheelModel;
import de.luiskun.util.NameLoader;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private static final int FRAME_DELAY_MS = 16; // ca. 60 FPS

    private final WheelModel model = new WheelModel();
    private final WheelPanel wheelPanel = new WheelPanel(model);
    private final SidePanel sidePanel = new SidePanel();
    private final Timer timer = new Timer(FRAME_DELAY_MS, e -> tick());

    public MainFrame() {
        super("Glücksrad");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, wheelPanel, sidePanel);
        split.setResizeWeight(1.0);
        split.setContinuousLayout(true);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
        //add(wheelPanel, BorderLayout.CENTER);
        //add(sidePanel, BorderLayout.EAST);

        sidePanel.setNames(NameLoader.loadDefault());
        applyNames();
        sidePanel.onNamesChanged(this::applyNames);
        sidePanel.onSpin(this::spin);
        setJMenuBar(new MenuBar());

        pack();
        setMinimumSize(new Dimension(500, 350));
        setLocationRelativeTo(null);
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
        //sidePanel.setResult("");
        sidePanel.setSpinning(true);
        timer.start();
    }

    private void tick() {
        boolean finished = model.update();
        wheelPanel.repaint();
        if (finished) {
            timer.stop();
            //sidePanel.setResult(model.getWinner());
            sidePanel.setSpinning(false);
            showWinnerDialog(model.getWinner());
        }
    }

    private void showWinnerDialog(String winner) {
        Object[] options = {"Keep", "Remove"};
        int choice = JOptionPane.showOptionDialog(
                this,
                winner,
                "Winner",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 1) {
            List<String> names = new ArrayList<>(sidePanel.getNames());
            names.remove(winner);
            sidePanel.setNames(names);
        }
    }
}