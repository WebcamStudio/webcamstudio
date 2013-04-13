/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick
 */
public class MasterFrameBuilder implements Runnable {

    private static ArrayList<Stream> streams = new ArrayList<Stream>();// add private
    private boolean stopMe = false;
    private static int fps = 0;
    private long mark = System.currentTimeMillis();
    private FrameBuffer frameBuffer = null;
    private TreeMap<Integer, Frame> orderedFrames = new TreeMap<Integer, Frame>();

    public MasterFrameBuilder(int w, int h, int r) {
        frameBuffer = new FrameBuffer(w, h, r);
    }

    public synchronized static void register(Stream s) {
        if (!streams.contains(s)) {
            streams.add(s);
        }
    }

    public synchronized static void unregister(Stream s) {
        streams.remove(s);
    }

    public void stop() {
        stopMe = true;
    }

    private void mixImages(Collection<Frame> frames, Frame targetFrame) {
        for (Frame f : frames) {       
            orderedFrames.put(f.getZOrder(), f);         
            }        
        BufferedImage image = targetFrame.getImage();
        if (image != null) {
            Graphics2D g = image.createGraphics();
            g.setBackground(new Color(0, 0, 0, 0));
            g.clearRect(0, 0, image.getWidth(), image.getHeight());
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
            g.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            for (Frame f : orderedFrames.values()) {                    
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float) f.getOpacity()) / 100F));
                g.drawImage(f.getImage(), f.getX(), f.getY(), f.getWidth(), f.getHeight(), null);
            }
            g.dispose();

        }
        orderedFrames.clear();
    }

    private void mixAudio(Collection<Frame> frames, Frame targetFrame) {
        byte[] audioData = targetFrame.getAudioData();
        ShortBuffer outputBuffer = ByteBuffer.wrap(audioData).asShortBuffer();
        for (int i = 0; i < audioData.length; i++) {
            audioData[i] = 0;
        }
        for (Iterator<Frame> it = frames.iterator(); it.hasNext();) {
            Frame f = it.next();
            byte[] data = f.getAudioData();
            if (data != null) {
                ShortBuffer buffer = ByteBuffer.wrap(data).asShortBuffer();
                outputBuffer.rewind();
                while (buffer.hasRemaining()) {
                    float mix = (float) buffer.get() * f.getVolume();
                    outputBuffer.mark();
                    if (outputBuffer.position()< outputBuffer.limit()){ //25fps IOException                     
                      mix += outputBuffer.get();             
                    }                    
                outputBuffer.reset();
                    if (mix > Short.MAX_VALUE) {
                        mix = Short.MAX_VALUE;
                    } else if (mix < Short.MIN_VALUE) {
                        mix = Short.MIN_VALUE;
                    }
                    if (outputBuffer.position()< outputBuffer.limit()){ //25fps IOException                          
                    outputBuffer.put((short) mix);      
                    }
                }                
                f.setAudio(null);              
            }
        }
    }

    @Override
    public void run() { //synchronized
        stopMe = false;
        ArrayList<Frame> frames = new ArrayList<Frame>();
        mark = System.currentTimeMillis();
        int w = MasterMixer.getInstance().getWidth();
        int h = MasterMixer.getInstance().getHeight();
        int r = MasterMixer.getInstance().getRate();
        long frameDelay = 1000 / r;
        long timeCode = System.currentTimeMillis();
        ExecutorService pool = java.util.concurrent.Executors.newCachedThreadPool();
        while (!stopMe) {
            long start = System.currentTimeMillis();
            timeCode += frameDelay;
            Frame targetFrame = frameBuffer.getFrameToUpdate();
            frames.clear();
            try {
//              Tools.sleep(10);
                List<Future<Frame>> results = pool.invokeAll(streams);
                for (Future stream : results) {
                    Frame f = (Frame)stream.get();
                    if (f != null) {
                        frames.add(f);
                    } 
                }
                mixAudio(frames, targetFrame);            
                mixImages(frames, targetFrame);
                targetFrame = null;
                frameBuffer.doneUpdate();
                MasterMixer.getInstance().setCurrentFrame(frameBuffer.pop());
                fps++;
                float delta = System.currentTimeMillis() - mark;
                    if (delta >= 1000) {
                    mark = System.currentTimeMillis();
                    MasterMixer.getInstance().setFPS((((float) fps) / (delta / 1000F)));
                    fps = 0;
                }
                long sleepTime = timeCode - System.currentTimeMillis();
                if (sleepTime > 0) {
                    Tools.sleep(sleepTime + 10);
                }           
            } catch (Exception ex) {
                Logger.getLogger(MasterFrameBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
