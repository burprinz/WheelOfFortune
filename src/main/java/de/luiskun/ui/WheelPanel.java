package de.luiskun.ui;

import de.luiskun.logic.Colors;
import de.luiskun.logic.WheelModel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.List;

/**
 * Zeichnet ein Rad (Hauptrad und Test-Rad der Physik-Einstellungen verwenden dieselbe Klasse).
 * Die Farben werden zur Paint-Zeit aus {@link Colors} gelesen, ein Theme-Wechsel braucht nur ein Repaint.
 */
public class WheelPanel extends JPanel {

    private static final int MARGIN = 28;
    private static final int POINTER_SIZE = 14;

    private final WheelModel model;

    private double pointerShake = 0;
    private Confetti[] confetti;

    public WheelPanel(WheelModel model) {
        this.model = model;
        setPreferredSize(new Dimension(600, 500));
        setMinimumSize(new Dimension(250, 250));
    }

    @Override
    protected void paintComponent(Graphics g) {
        pointerShake *= 0.82;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(Colors.backgroundColor);
        g2.fillRect(0, 0, getWidth(), getHeight());

        List<String> names = model.getNames();
        int diameter = Math.min(getWidth(), getHeight()) - 2 * MARGIN;

        if (diameter > 0) {
            if (names.isEmpty()) {
                drawCenteredMessage(g2, "No entries");
            } else {
                drawWheel(g2, names, diameter);
            }
        }

        if(confetti != null){

            for(Confetti c : confetti){

                c.x += c.vx;
                c.y += c.vy;

                c.vy += 0.12;
                c.r += 8;

                Graphics2D cg = (Graphics2D) g2.create();

                cg.translate(c.x,c.y);
                cg.rotate(Math.toRadians(c.r));

                cg.setColor(c.color);
                cg.fillRect(-(int)c.size/2,-(int)c.size/2,(int)c.size,(int)c.size);

                cg.dispose();
            }

            repaint();
        }

        g2.dispose();
    }

    private void drawWheel(Graphics2D g2, List<String> names, int diameter) {
        double r = diameter / 2.0;
        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;
        int n = names.size();
        double sliceSize = 360.0 / n;
        double angle = model.getAngle();

        // Segmente
        for (int i = 0; i < n; i++) {
            //g2.setColor(Colors.colorFor(i));
            Color c = Colors.colorFor(i);

            if(model.isWinnerAnimation()){

                if(i == model.getWinnerIndex()){
                    c = brighten(c);
                }else{
                    c = fade(c);
                }
            }

            g2.setColor(c);
            g2.fill(slice(cx, cy, r, angle + i * sliceSize, sliceSize));
        }

        // Trennlinien in Hintergrundfarbe
        g2.setColor(Colors.backgroundColor);
        g2.setStroke(new BasicStroke(2f));
        for (int i = 0; i < n; i++) {
            g2.draw(slice(cx, cy, r, angle + i * sliceSize, sliceSize));
        }

        drawLabels(g2, names, cx, cy, r, angle, sliceSize);

        // Außenring und Nabe
        g2.setColor(Colors.borderColor);
        g2.setStroke(new BasicStroke(3f));
        g2.draw(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));

        double hub = Math.max(6, r * 0.05);
        Ellipse2D hubShape = new Ellipse2D.Double(cx - hub, cy - hub, 2 * hub, 2 * hub);
        g2.setColor(Colors.backgroundColor);
        g2.fill(hubShape);
        g2.setColor(Colors.borderColor);
        g2.draw(hubShape);

        drawPointer(g2, (int) cx, (int) (cy - r));
    }

    private void drawLabels(Graphics2D g2, List<String> names, double cx, double cy, double r, double angle, double sliceSize) {
        g2.setFont(getFont().deriveFont(Font.BOLD, (float) Math.max(11, r * 0.08)));
        FontMetrics fm = g2.getFontMetrics();
        int maxTextWidth = (int) (r * 0.7);
        double textRadius = r * 0.6;

        for (int i = 0; i < names.size(); i++) {
            double mid = Math.toRadians(angle + i * sliceSize + sliceSize / 2.0);
            String text = fit(names.get(i), fm, maxTextWidth);

            Graphics2D textG = (Graphics2D) g2.create();
            textG.translate(cx + textRadius * Math.cos(mid), cy - textRadius * Math.sin(mid));
            textG.rotate(-mid);
            textG.setColor(Colors.SLICE_TEXT_COLOR);
            textG.drawString(text, -fm.stringWidth(text) / 2, (fm.getAscent() - fm.getDescent()) / 2);
            textG.dispose();
        }
    }

    private void drawPointer(Graphics2D g2, int x, int wheelTop) {
        /*
        Polygon pointer = new Polygon(
                new int[]{x - POINTER_SIZE, x + POINTER_SIZE, x},
                new int[]{wheelTop - POINTER_SIZE, wheelTop - POINTER_SIZE, wheelTop + POINTER_SIZE * 2},
                3);
        g2.setColor(Colors.selectorColor);
        g2.fill(pointer);
        g2.setColor(Colors.backgroundColor);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(pointer);
        */
        Graphics2D p = (Graphics2D) g2.create();

        p.translate(x,wheelTop);
        p.rotate(Math.toRadians(pointerShake*18));

        Polygon pointer = new Polygon(
                new int[]{-POINTER_SIZE,POINTER_SIZE,0},
                new int[]{-POINTER_SIZE,-POINTER_SIZE,POINTER_SIZE*2},
                3);

        p.setColor(Colors.selectorColor);
        p.fill(pointer);
        p.setColor(Colors.backgroundColor);
        p.setStroke(new BasicStroke(2));
        p.draw(pointer);

        p.dispose();
    }

    private void drawCenteredMessage(Graphics2D g2, String message) {
        g2.setColor(Colors.mutedTextColor);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(message,
                (getWidth() - fm.stringWidth(message)) / 2,
                (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
    }

    private static Arc2D slice(double cx, double cy, double r, double start, double extent) {
        return new Arc2D.Double(cx - r, cy - r, 2 * r, 2 * r, start, extent, Arc2D.PIE);
    }

    private static String fit(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        int end = text.length();
        while (end > 0 && fm.stringWidth(text.substring(0, end) + "...") > maxWidth) {
            end--;
        }
        // Keine Surrogate-Paare (z. B. Emojis) in der Mitte durchschneiden
        if (end > 0 && Character.isHighSurrogate(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, end) + "...";
    }

    private Color brighten(Color c){
        return new Color(
                Math.min(255,c.getRed()+70),
                Math.min(255,c.getGreen()+70),
                Math.min(255,c.getBlue()+70)
        );
    }

    private Color fade(Color c){
        return new Color(
                (int)(c.getRed()*0.45),
                (int)(c.getGreen()*0.45),
                (int)(c.getBlue()*0.45)
        );
    }

    public void pointerKick(){
        pointerShake = 1;
    }

    public void startConfetti(){

        confetti = new Confetti[140];

        for(int i=0;i<confetti.length;i++){

            Confetti c = new Confetti();

            c.x = getWidth()/2.0;
            c.y = getHeight()/2.0;

            double a = Math.random()*Math.PI*2;
            double s = 2+Math.random()*7;

            c.vx = Math.cos(a)*s;
            c.vy = Math.sin(a)*s;

            c.size = 4+Math.random()*6;
            c.r = Math.random()*360;

            c.color = Colors.colorFor(i);

            confetti[i]=c;
        }


        javax.swing.Timer clearTimer = new Timer(2000, null);
        clearTimer.addActionListener(ev -> {
            confetti = null;
            clearTimer.stop();
            repaint();
        });
        clearTimer.setRepeats(false);
        clearTimer.start();
    }

    private static class Confetti{
        double x,y,vx,vy,r,size;
        Color color;
    }
}

