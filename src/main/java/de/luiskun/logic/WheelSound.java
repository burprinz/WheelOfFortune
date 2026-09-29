package de.luiskun.logic;

import javax.sound.sampled.*;

public final class WheelSound {

    private WheelSound(){}

    public static void tick() {
        play(220, 18);
    }

    public static void win() {
        play(523, 120);
        sleep(40);
        play(659, 140);
        sleep(40);
        play(784, 180);
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    private static void play(int hz, int ms) {
        new Thread(() -> {
            try {
                float rate = 44100;
                byte[] buf = new byte[(int)(rate * ms / 1000)];

                for(int i=0;i<buf.length;i++){
                    double angle = i / (rate / hz) * 2.0 * Math.PI;
                    buf[i] = (byte)(Math.sin(angle) * 18);
                }

                AudioFormat fmt = new AudioFormat(rate,8,1,true,false);
                SourceDataLine line = AudioSystem.getSourceDataLine(fmt);
                line.open(fmt);
                line.start();
                line.write(buf,0,buf.length);
                line.drain();
                line.close();

            } catch(Exception ignored){}
        }).start();
    }
}