package de.luiskun.logic;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Synthetisierte Sounds: ein weiches "Tock" pro Segmentwechsel und ein kurzer Glockenklang-Jingle
 * für den Gewinner. Die Klänge werden einmal vorberechnet und über wiederverwendete Clips abgespielt
 * (kein Thread und kein neues Audiogerät pro Ton, keine Aussetzer im UI).
 */
public final class WheelSound {

    private static final float RATE = 44_100f;
    private static final AudioFormat FORMAT = new AudioFormat(RATE, 16, 1, true, false);

    /** Leicht unterschiedliche Tonhöhen, damit das Ticken lebendiger klingt. */
    private static final double[] TICK_PITCHES = {760, 820, 880};
    private static final int TICK_CLIPS = 6;

    private static Clip[] tickClips = new Clip[0];
    private static Clip winClip;
    private static int nextTick;
    private static boolean loading;
    private static boolean ready;

    private WheelSound() {
    }

    /** Bereitet die Sounds im Hintergrund vor, damit der erste Ton nicht ruckelt. */
    public static synchronized void preload() {
        if (loading) {
            return;
        }
        loading = true;
        Thread thread = new Thread(WheelSound::load, "wheel-sound-loader");
        thread.setDaemon(true);
        thread.start();
    }

    public static synchronized void tick() {
        if (!ready) {
            return;
        }
        restart(tickClips[nextTick]);
        nextTick = (nextTick + 1) % tickClips.length;
    }

    public static synchronized void win() {
        if (ready) {
            restart(winClip);
        }
    }

    private static void restart(Clip clip) {
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    private static void load() {
        try {
            Clip[] clips = new Clip[TICK_CLIPS];
            for (int i = 0; i < clips.length; i++) {
                clips[i] = openClip(renderTick(TICK_PITCHES[i % TICK_PITCHES.length]), -4f);
            }
            Clip win = openClip(renderWin(), -3f);
            synchronized (WheelSound.class) {
                tickClips = clips;
                winClip = win;
                ready = true;
            }
        } catch (Exception | LinkageError e) {
            // Kein Audiogerät verfügbar: die App läuft einfach ohne Ton weiter
            System.err.println("Sound disabled: " + e.getMessage());
        }
    }

    private static Clip openClip(byte[] pcm, float gainDb) throws Exception {
        Clip clip = AudioSystem.getClip();
        clip.open(FORMAT, pcm, 0, pcm.length);
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), gainDb)));
        }
        return clip;
    }

    /** Kurzes, holziges "Tock": gedämpfter Sinus mit leiser tieferer Schicht und winzigem Anschlag. */
    private static byte[] renderTick(double pitch) {
        double[] buf = new double[(int) (RATE * 0.07)];
        for (int i = 0; i < buf.length; i++) {
            double t = i / RATE;
            double body = Math.sin(2 * Math.PI * pitch * t) * Math.exp(-t * 75);
            double low = Math.sin(2 * Math.PI * pitch * 0.5 * t) * Math.exp(-t * 55) * 0.5;
            double attack = Math.min(1.0, t / 0.001);
            buf[i] = (body + low) * attack;
        }
        return toPcm(buf, 0.45);
    }

    /** Aufsteigender Dur-Akkord aus glockenartigen Tönen (C-Dur, C5 bis C6). */
    private static byte[] renderWin() {
        double[] buf = new double[(int) (RATE * 1.6)];
        double[] notes = {523.25, 659.25, 783.99, 1046.50};
        for (int i = 0; i < notes.length; i++) {
            addBell(buf, 0.11 * i, notes[i], i == notes.length - 1 ? 1.0 : 0.85);
        }
        return toPcm(buf, 0.6);
    }

    private static void addBell(double[] buf, double startSec, double freq, double gain) {
        int start = (int) (startSec * RATE);
        for (int i = 0; start + i < buf.length; i++) {
            double t = i / RATE;
            double sample = Math.sin(2 * Math.PI * freq * t) * Math.exp(-t * 3.2)
                    + 0.30 * Math.sin(2 * Math.PI * freq * 2.0 * t) * Math.exp(-t * 5.5)
                    + 0.12 * Math.sin(2 * Math.PI * freq * 3.01 * t) * Math.exp(-t * 9.0);
            double attack = Math.min(1.0, t / 0.006);
            buf[start + i] += sample * attack * gain;
        }
    }

    /** Normalisiert auf die Zielhöhe, blendet das Ende aus und wandelt in 16-Bit-PCM (little endian). */
    private static byte[] toPcm(double[] buf, double peak) {
        double max = 1e-9;
        for (double v : buf) {
            max = Math.max(max, Math.abs(v));
        }
        int fade = Math.min(buf.length, (int) (RATE * 0.02));
        byte[] out = new byte[buf.length * 2];
        for (int i = 0; i < buf.length; i++) {
            double v = buf[i] / max * peak;
            int remaining = buf.length - i;
            if (remaining < fade) {
                v *= remaining / (double) fade;
            }
            short s = (short) Math.round(Math.max(-1.0, Math.min(1.0, v)) * 32767);
            out[2 * i] = (byte) (s & 0xFF);
            out[2 * i + 1] = (byte) ((s >> 8) & 0xFF);
        }
        return out;
    }
}
