/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class SourceMicrophone extends Stream {

    private boolean isPlaying = false;
    private TargetDataLine line;
    private Frame frame = null;
    private byte[] audio = null;

    public SourceMicrophone() {
        super();
        rate = MasterMixer.getInstance().getRate();
        name = "Microphone";
    }

    @Override
    public void read() {
        isPlaying = true;
        rate = MasterMixer.getInstance().getRate();
        audio = new byte[44100 * 2 * 2 / rate];
        frame = new Frame(captureWidth, captureHeight, rate);
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // format is an AudioFormat object
                if (!AudioSystem.isLineSupported(info)) {
                    isPlaying = false;
                    System.out.println("Microphone not supported");
                }
                try {
                    line = (TargetDataLine) AudioSystem.getLine(info);
                    line.open(format);
                    line.start();
                } catch (Exception e) {
                    isPlaying = false;
                    stop();
                }

            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
        MasterFrameBuilder.register(this);
    }

    @Override
    public void stop() {
        isPlaying = false;
        if (line != null) {
            line.stop();
            line.close();
            line = null;
        }
        MasterFrameBuilder.unregister(this);
    }
    @Override
    public boolean needSeek() {
            return needSeekCTRL=false;
    }
    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public BufferedImage getPreview() {
        return null;
    }

    private byte[] getNextAudio() {
        if (line != null) {
            line.read(audio, 0, audio.length);
            return audio;
        } else {
            return null;
        }
    }

    @Override
    public Frame getFrame() {
        return nextFrame;
    }

    @Override
    public boolean hasAudio() {
        return true;
    }

    @Override
    public boolean hasVideo() {
        return false;
    }

    @Override
    public void readNext() {
        byte[] nextAudio = getNextAudio();
        frame.setAudio(nextAudio);
        frame.setOutputFormat(x, y, width, height, opacity, volume);
        frame.setZOrder(zorder);
        nextFrame=frame;
    }
}
