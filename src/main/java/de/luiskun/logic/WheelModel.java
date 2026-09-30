package de.luiskun.logic;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Zustand und Physik eines Rads. Die Segmentgröße ist proportional zur Gewichtung der Einträge.
 * <p>
 * Der Endwinkel eines Drehs ist gleichverteilt, deshalb entspricht die Gewinnwahrscheinlichkeit
 * eines Eintrags genau seinem Anteil am Rad.
 */
public class WheelModel {

    private static final int MAX_SIMULATION_STEPS = 2_000_000;

    private List<WheelEntry> entries = List.of();
    private List<String> names = List.of();
    private double[] sliceStarts = new double[0];
    private double[] sliceExtents = new double[0];

    private double angle;
    private double speed;
    private double topSpeed;
    private double distanceScale = 1.0;
    private boolean braking;
    private boolean spinning;

    private double acceleration;
    private double friction;
    private double stopSpeed;

    private boolean winnerAnimation;

    public List<String> getNames() {
        return names;
    }

    public List<WheelEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<WheelEntry> newEntries) {
        this.entries = List.copyOf(newEntries);
        this.names = entries.stream().map(WheelEntry::name).toList();

        int n = entries.size();
        sliceStarts = new double[n];
        sliceExtents = new double[n];
        double total = 0;
        for (WheelEntry e : entries) {
            total += e.weight();
        }
        double start = 0;
        for (int i = 0; i < n; i++) {
            sliceStarts[i] = start;
            sliceExtents[i] = entries.get(i).weight() / total * 360.0;
            start += sliceExtents[i];
        }
    }

    /** Alle Einträge mit Gewichtung 1. */
    public void setNames(List<String> newNames) {
        setEntries(newNames.stream().map(name -> new WheelEntry(name, 1)).toList());
    }

    /** Startwinkel (Grad) des Segments, relativ zur Raddrehung. */
    public double getSliceStart(int index) {
        return sliceStarts[index];
    }

    public double getSliceExtent(int index) {
        return sliceExtents[index];
    }

    public double getAngle() {
        return angle;
    }

    public boolean isSpinning() {
        return spinning;
    }

    public int getPointerIndex() {
        return getWinnerIndex();
    }

    public void startWinnerAnimation() {
        winnerAnimation = true;
    }

    public void stopWinnerAnimation() {
        winnerAnimation = false;
    }

    public boolean isWinnerAnimation() {
        return winnerAnimation;
    }

    /**
     * Startet einen Dreh mit den aktuellen {@link WheelConstants}.
     *
     * @return false, wenn das Rad schon dreht oder keine Einträge hat
     */
    public boolean spin() {
        return spin(WheelConstants.MIN_TOP_SPEED, WheelConstants.MAX_TOP_SPEED,
                WheelConstants.ACCELERATION, WheelConstants.FRICTION, WheelConstants.STOP_SPEED);
    }

    /**
     * Startet einen Dreh mit den übergebenen Werten.
     * <p>
     * Die Höchstgeschwindigkeit wird zufällig gewählt. Damit der Endwinkel trotzdem exakt gleichverteilt
     * ist (sonst wären manche Segmente wahrscheinlicher), wird der Gesamtweg vorab berechnet und die
     * Drehung leicht gestreckt oder gestaucht, bis sie auf einem zufälligen Zielwinkel endet.
     *
     * @return false, wenn das Rad schon dreht oder keine Einträge hat
     */
    public boolean spin(double minTopSpeed, double maxTopSpeed, double acceleration, double friction, double stopSpeed) {
        if (spinning || entries.isEmpty()) {
            return false;
        }
        this.acceleration = acceleration;
        this.friction = friction;
        this.stopSpeed = stopSpeed;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        double lo = Math.min(minTopSpeed, maxTopSpeed);
        double hi = Math.max(minTopSpeed, maxTopSpeed);
        this.topSpeed = lo + random.nextDouble() * (hi - lo);

        double base = simulateDistance();
        this.distanceScale = 1.0;
        if (base > 0) {
            double target = random.nextDouble() * 360.0;
            double delta = mod(target - angle - base, 360.0);
            if (delta > 180.0) {
                delta -= 360.0;
            }
            double wanted = base + delta;
            if (wanted < base * 0.75) {
                wanted += 360.0;
            }
            this.distanceScale = wanted / base;
        }

        this.speed = 0;
        this.braking = false;
        this.spinning = true;
        return true;
    }

    /** Gesamtwinkel, den das Rad mit den aktuellen Werten bis zum Stillstand zurücklegt. */
    private double simulateDistance() {
        double total = 0;
        double v = 0;
        boolean brake = false;
        for (int i = 0; i < MAX_SIMULATION_STEPS; i++) {
            total += v;
            if (!brake) {
                v += acceleration;
                if (v >= topSpeed) {
                    v = topSpeed;
                    brake = true;
                }
            } else {
                v *= friction;
                if (v < stopSpeed) {
                    break;
                }
            }
        }
        return total;
    }

    /**
     * Ein Simulationsschritt.
     *
     * @return true, wenn das Rad in diesem Schritt zum Stillstand gekommen ist
     */
    public boolean update() {
        if (!spinning) {
            return false;
        }

        angle = (angle + speed * distanceScale) % 360;

        if (!braking) {
            speed += acceleration;
            if (speed >= topSpeed) {
                speed = topSpeed;
                braking = true;
            }
        } else {
            speed *= friction;
            if (speed < stopSpeed) {
                stop();
                return true;
            }
        }
        return false;
    }

    /**
     * Index des Segments unter dem Zeiger (oben, bei 90°) oder -1 ohne Einträge.
     */
    public int getWinnerIndex() {
        int n = entries.size();
        if (n == 0) {
            return -1;
        }
        double relativeAngle = mod(90.0 - angle, 360.0);
        for (int i = 0; i < n; i++) {
            if (relativeAngle < sliceStarts[i] + sliceExtents[i]) {
                return i;
            }
        }
        return n - 1;
    }

    public void stop() {
        speed = 0;
        spinning = false;
        braking = false;
    }

    private static double mod(double value, double m) {
        return ((value % m) + m) % m;
    }
}
