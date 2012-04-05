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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/**
 *
 * @author patrick
 */
public class MasterMixer {

    static protected int frameRate = 15;
    static protected int width = 320;
    static protected int height = 240;
    static private Timer renderer = new Timer();
    static TreeMap<String, List<Frame>> sources = new TreeMap<String, List<Frame>>();
    static List<Frame> renderedFrames = new ArrayList<Frame>();

    public static void setWidth(int w) {
        width = w;
    }

    public static void setHeight(int h) {
        height = h;
    }

    public static void setRate(int rate) {
        frameRate = rate;
    }

    public static int getRate() {
        return frameRate;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    static public void start() {
        renderer.scheduleAtFixedRate(new MasterRenderer(), 0, 1000 / frameRate);
    }

    static public void stop() {
        renderer.cancel();
    }

    static public void addSourceFrame(Frame frame, String sourceID) {
        if (sources.containsKey(sourceID)) {
            sources.get(sourceID).add(frame);

        } else {
            sources.put(sourceID, new ArrayList<Frame>());
            sources.get(sourceID).add(frame);
        }
        if (sources.get(sourceID).size() > 5*frameRate) {
            sources.get(sourceID).remove(0);
        }
    }

    protected static List<Frame> getSources() {
        ArrayList<Frame> currentSources = new ArrayList<Frame>();
        for (List<Frame> frames : sources.values()) {
            if (frames.size() > 0) {
                currentSources.add(frames.remove(0));
            }
        }
        return currentSources;
    }

    public static Frame getCurrentFrame() {
        if (renderedFrames.size() > 0) {
            return renderedFrames.get(0);
        } else {
            return null;
        }
    }
}

class MasterRenderer extends TimerTask {

    @Override
    public void run() {

        ArrayList<Frame> frames = new ArrayList<Frame>();
        frames.addAll(MasterMixer.getSources());
        if (frames.size() > 0) {

            Frame targetFrame = new Frame(MasterMixer.getWidth(),MasterMixer.getHeight(),MasterMixer.getRate());
            mixImages(frames, targetFrame);
            mixAudio(frames, targetFrame);
            MasterMixer.renderedFrames.add(targetFrame);
            if (MasterMixer.renderedFrames.size() > 5*MasterMixer.getRate()) {
                MasterMixer.renderedFrames.remove(0);
            }
        }

    }

    private void mixImages(List<Frame> frames, Frame targetFrame) {
        BufferedImage img = new BufferedImage(MasterMixer.width, MasterMixer.height, BufferedImage.TYPE_INT_ARGB);
        TreeMap<Integer,Frame> orderedFrame = new TreeMap<Integer,Frame>();
        for (Frame f : frames){
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

    private void mixAudio(List<Frame> frames, Frame targetFrame) {
        byte[] audioData = new byte[(44100 * 2 * 2) / MasterMixer.frameRate];
        ShortBuffer outputBuffer = ByteBuffer.wrap(audioData).asShortBuffer();
        for (Frame f : frames) {
            byte[] data = f.getAudioData();
            if (data != null) {
                ShortBuffer buffer = ByteBuffer.wrap(data).asShortBuffer();
                outputBuffer.rewind();
                while (buffer.hasRemaining()) {
                    float mix = (float)buffer.get() * f.getVolume();
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
            }
        }
        targetFrame.setAudio(audioData);
    }
}
