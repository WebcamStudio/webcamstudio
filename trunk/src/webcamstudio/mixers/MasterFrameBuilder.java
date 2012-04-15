/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.streams.Stream;

/**
 *
 * @author patrick
 */
public class MasterFrameBuilder implements Runnable {

    static ArrayList<Stream> streams = new ArrayList<Stream>();
    private boolean stopMe = false;
    private int fps = 0;
    private long mark = System.currentTimeMillis();
    private long timeCode = System.currentTimeMillis();

    public static void register(Stream s) {
        if (!streams.contains(s)) {
            streams.add(s);
        }
    }

    public static void unregister(Stream s) {
        streams.remove(s);
    }

    public void stop() {
        stopMe = true;
    }

    private static void mixImages(Collection<Frame> frames, Frame targetFrame) {
        BufferedImage img = new BufferedImage(MasterMixer.width, MasterMixer.height, BufferedImage.TYPE_INT_ARGB);
        TreeMap<Integer, Frame> orderedFrame = new TreeMap<Integer, Frame>();
        for (Frame f : frames) {
            orderedFrame.put(f.getZOrder(), f);
        }
        if (img != null) {
            Graphics2D g = img.createGraphics();
            for (Frame f : orderedFrame.values()) {
                if (g != null) {
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float) f.getOpacity()) / 100F));
                    g.drawImage(f.getImage(), f.getX(), f.getX(), f.getWidth(), f.getHeight(), null);
                }
            }
            g.dispose();
            targetFrame.setImage(img);
        }
    }

    private static void mixAudio(Collection<Frame> frames, Frame targetFrame) {
        byte[] audioData = new byte[(44100 * 2 * 2) / MasterMixer.frameRate];
        ShortBuffer outputBuffer = ByteBuffer.wrap(audioData).asShortBuffer();
        for (Frame f : frames) {
            byte[] data = f.getAudioData();
            if (data != null) {
                ShortBuffer buffer = ByteBuffer.wrap(data).asShortBuffer();
                outputBuffer.rewind();
                while (buffer.hasRemaining()) {
                    float mix = (float) buffer.get() * f.getVolume();
                    outputBuffer.mark();
                    mix += outputBuffer.get();
                    outputBuffer.reset();
                    if (mix > Short.MAX_VALUE) {
                        mix = Short.MAX_VALUE;
                    } else if (mix < Short.MIN_VALUE) {
                        mix = Short.MIN_VALUE;
                    }
                    outputBuffer.put((short) mix);
                }
                f.setAudio(null);
            }
        }
        targetFrame.setAudio(audioData);
    }

    @Override
    public void run() {
        stopMe = false;
        ArrayList<Frame> frames = new ArrayList<Frame>();
        mark = System.currentTimeMillis();
        timeCode = System.currentTimeMillis();
        while (!stopMe) {
            timeCode = System.currentTimeMillis() + (1000 / MasterMixer.getRate());
            Frame targetFrame = new Frame(MasterMixer.getWidth(), MasterMixer.getHeight(), MasterMixer.getRate());
            frames.clear();
            for (Stream s : streams) {
                Frame f = s.getFrame();
                if (f != null) {
                    frames.add(f);
                }
            }
            mixAudio(frames, targetFrame);
            SystemAudioPlayer.getInstance().addData(targetFrame.getAudioData());
            mixImages(frames, targetFrame);
            MasterMixer.setCurrentFrame(targetFrame);
            fps++;
            float delta = System.currentTimeMillis() - mark;
            if (fps == 60) {
                System.out.println("Master Frame Builder: " + (60F / (delta / 1000F)) + " fps");
                mark = System.currentTimeMillis();
                fps = 0;
            }
            frames.clear();
            targetFrame = null;
            long waitTime = timeCode - System.currentTimeMillis();
            if (waitTime > 0) {
                try {
                    Thread.sleep(timeCode - System.currentTimeMillis());
                } catch (InterruptedException ex) {
                    Logger.getLogger(MasterFrameBuilder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }
}
