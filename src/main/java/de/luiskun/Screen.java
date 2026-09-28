package de.luiskun;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Screen extends JPanel implements Runnable{

    public static final int W = 800, H = 500;

    int fps = 60;
    Thread thread;

    static String[] names = null;

    double angle = 0;
    double maxSpeed = 0;
    double currentSpeed = 0;
    double angleAcceleration = 0;
    boolean maxSpeedReached = false;
    boolean running = false;

    public Screen() {
        this.setPreferredSize(new Dimension(W, H));
        this.setDoubleBuffered(true);
        this.setLayout(null);

        readNames();

        start();
    }

    public void update() {

        if (running) {
            angle += currentSpeed;
            if (!maxSpeedReached) {
                currentSpeed += angleAcceleration;
                angleAcceleration += 0.1;
                if (currentSpeed > maxSpeed) {
                    maxSpeedReached = true;
                    maxSpeed = currentSpeed;
                }
            } else {
                currentSpeed -= 1;
                if (currentSpeed <= 0) {
                    currentSpeed = 0;
                    running = false;
                    System.out.println(getTopString());
                }
            }

        }

    }

    private void start() {
        angle = 0;
        currentSpeed = 0;
        angleAcceleration = 0;
        maxSpeed = getRandomMaxSpeed();
        maxSpeedReached = false;

        running = true;
    }

    private double getRandomMaxSpeed() {
        return Math.random()*1000;
    }

    private String getTopString() {
        int N = names.length;
        double sliceSize = 360.0 / N;

        double relAng = 90.0 - angle;

        relAng = ((relAng % 360) + 360) % 360;

        int idx = (int) (relAng / sliceSize) % N;
        return names[idx];
    }

    private static Color getColor(int num, int N) {
        return Color.getHSBColor((num * 0.618033988749895f) % 1.0f, 0.65f, 0.9f);
    }

    private static String getName(String text, FontMetrics fm, int width) {
        if (fm.stringWidth(text) < width) {
            return text;
        }
        String res = "";
        for (int i = 0; i < text.length(); i++) {
            String pot = res + text.charAt(i) + "...";
            if (fm.stringWidth(pot) > width) {
                return res + "...";
            }
            res += text.charAt(i);
        }
        return res;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, W, H);

        int wheelsize = (int)(H*0.8);
        double r = wheelsize / 2.0;
        int x = 10;
        int y = H / 2 - wheelsize / 2;

        double cx = x + r;
        double cy = y + r;

        int N = names.length;
        double sliceSize = 360.0 / N;

        for (int i = 0; i < N; i++) {
            double angleStart = angle + i * sliceSize;
            g2.setColor(getColor(i, N));
            g2.fill(new Arc2D.Double(x, y, wheelsize, wheelsize, angleStart, sliceSize, Arc2D.PIE));


            double midPoint = Math.toRadians(angleStart + sliceSize / 2.0);
            int textR = (int) (r * 0.6);
            int textX = (int) (cx + textR * Math.cos(midPoint));
            int textY = (int) (cy - textR * Math.sin(midPoint));
            FontMetrics fm = g2.getFontMetrics();
            String text = getName(names[i], fm, (int)(r * 0.7));
            int textWidth = fm.stringWidth(text);

            Graphics2D textG2 = (Graphics2D) g.create();
            textG2.translate(textX, textY);
            textG2.rotate(-midPoint);
            textG2.setColor(Color.BLACK);
            textG2.drawString(text, -textWidth / 2, (fm.getAscent() - fm.getDescent()) / 2);
            textG2.dispose();
        }

        g2.setColor(Color.BLACK);
        g2.fillPolygon(new int[]{(int)(cx) - 10, (int)(cx) + 10, (int)(cx)}, new int[]{10, 10, 50}, 3);

    }

    private static void readNames() {
        try {
            String inp = new String(Files.readAllBytes(Paths.get("src/main/resources/input.txt")));
            names = inp.split("\n");
        } catch (IOException e) {
            names = new String[] {"Error reading input"};
        }
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / fps; // 0.0166s
        double delta = 0;
        double lastTime = System.nanoTime();
        double currentTime;
        while (thread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void startThread() {
        thread = new Thread(this);
        thread.start();
    }
}
