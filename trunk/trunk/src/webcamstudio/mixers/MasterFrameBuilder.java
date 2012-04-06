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

    //private static TreeMap<String, Frame> frames = new TreeMap<String, Frame>();
    static ArrayList<Frame> frames = new ArrayList<Frame>();
    private boolean stopMe = false;

    public static void unregister(Stream s) {
        //frames.remove(s.getID());
    }

    public void stop(){
        stopMe=true;
    }
    public static void addFrame(String uuid, Frame f) {
        frames.add(f);
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
        stopMe=false;
        TreeMap<String,Frame> list = new TreeMap<String,Frame>();
        while (!stopMe) {
            Frame targetFrame = new Frame(MasterMixer.getWidth(), MasterMixer.getHeight(), MasterMixer.getRate());
            for (int i = 0;i<frames.size();i++){
                Frame f = frames.get(i);
                if (!list.containsKey(f.getID())){
                    list.put(f.getID(), f);
                }
            }
            frames.removeAll(list.values());
            mixAudio(list.values(), targetFrame);
            SystemAudioPlayer.getInstance().addData(targetFrame.getAudioData());
            mixImages(list.values(), targetFrame);
            MasterMixer.setCurrentFrame(targetFrame);
            list.clear();
            targetFrame = null;
            try {
                Thread.sleep(1000/15);
            } catch (InterruptedException ex) {
                Logger.getLogger(MasterFrameBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
