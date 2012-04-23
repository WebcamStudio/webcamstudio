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
import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick
 */
public class MasterFrameBuilder implements Runnable {

    static ArrayList<Stream> streams = new ArrayList<Stream>();
    private boolean stopMe = false;
    private static int fps = 0;
    private long mark = System.currentTimeMillis();
    BufferedImage workingImage = null;
    final static int RENDERED_IMAGES = 15;
    BufferedImage[] renderedImages = new BufferedImage[RENDERED_IMAGES];
    int curentRenderedImgeIndex = 0;

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

    private void mixImages(Collection<Frame> frames, Frame targetFrame) {
        TreeMap<Integer, Frame> orderedFrame = new TreeMap<Integer, Frame>();
        for (Frame f : frames) {
            orderedFrame.put(f.getZOrder(), f);
        }
        if (workingImage != null) {
            Graphics2D g = workingImage.createGraphics();
            g.clearRect(0, 0, workingImage.getWidth(), workingImage.getHeight());
            for (Frame f : orderedFrame.values()) {
                if (g != null) {
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float) f.getOpacity()) / 100F));
                    g.drawImage(f.getImage(), f.getX(), f.getY(), f.getWidth(), f.getHeight(), null);
                }
            }
            g.dispose();
            g = renderedImages[curentRenderedImgeIndex].createGraphics();
            g.drawImage(workingImage, 0, 0, null);
            targetFrame.setImage(renderedImages[curentRenderedImgeIndex]);
            curentRenderedImgeIndex++;
            curentRenderedImgeIndex = curentRenderedImgeIndex % renderedImages.length;
        }
    }

    private void mixAudio(Collection<Frame> frames, Frame targetFrame) {
        byte[] audioData = new byte[(44100 * 2 * 2) / MasterMixer.getInstance().getRate()];
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
        int w = MasterMixer.getInstance().getWidth();
        int h = MasterMixer.getInstance().getHeight();
        int r = MasterMixer.getInstance().getRate();
        workingImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < renderedImages.length; i++) {
            renderedImages[i] = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        long frameDelay = 1000 / r;
        long timeCode=System.currentTimeMillis();
        while (!stopMe) {
            long start = System.currentTimeMillis();
            timeCode+=frameDelay;
            Frame targetFrame = new Frame(w, h, r);
            frames = new ArrayList<Frame>();
            for (Stream s : streams) {
                Frame f = s.getFrame();
                if (f != null) {
                    frames.add(f);
                }
            }
            mixAudio(frames, targetFrame);
            SystemAudioPlayer.getInstance().addData(targetFrame.getAudioData());
            mixImages(frames, targetFrame);
            MasterMixer.getInstance().setCurrentFrame(targetFrame);
            targetFrame = null;
            fps++;
            float delta = System.currentTimeMillis() - mark;
            if (delta >= 1000) {
                mark = System.currentTimeMillis();
                MasterMixer.getInstance().setFPS((((float) fps) / (delta / 1000F)));
                fps = 0;
            }
            long sleepTime = timeCode-System.currentTimeMillis();
            if (sleepTime > 0){
                Tools.sleep(sleepTime+10);
            }
        }
    }
}
