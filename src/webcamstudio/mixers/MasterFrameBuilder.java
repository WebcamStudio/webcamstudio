/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
//import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class MasterFrameBuilder implements Runnable {

    private static ArrayList<Stream> streams = new ArrayList<Stream>();// add private
    private static int fps = 0;
    private Image imageF;
    private int imageX, imageY, imageW, imageH;
    private GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private GraphicsDevice gs = ge.getDefaultScreenDevice();
    private GraphicsConfiguration gc = gs.getDefaultConfiguration();
    private BufferedImage imageC;
    public synchronized static void register(Stream s) {
        if (!streams.contains(s)) {
            streams.add(s);
        }
    }
    public synchronized static void unregister(Stream s) {
        streams.remove(s);
    }
    private boolean stopMe = false;
    private long mark = System.currentTimeMillis();
    private FrameBuffer frameBuffer = null;
    private TreeMap<Integer, Frame> orderedFrames = new TreeMap<Integer, Frame>();

    public MasterFrameBuilder(int w, int h, int r) {
        frameBuffer = new FrameBuffer(w, h, r);
    }

    public void stop() {
        stopMe = true;
    }
    /*BufferedImage createCompatibleImage(BufferedImage image)
     * {
     * GraphicsConfiguration gc = GraphicsEnvironment.
     * getLocalGraphicsEnvironment().
     * getDefaultScreenDevice().
     * getDefaultConfiguration();
     * 
     * BufferedImage newImage = gc.createCompatibleImage(
     * image.getWidth(),
     * image.getHeight(),
     * Transparency.TRANSLUCENT);
     * 
     * Graphics2D g = newImage.createGraphics();
     * g.drawImage(image, 0, 0, null);
     * g.dispose();
     * 
     * return newImage;
     * }
     * private BufferedImage CreateCompatibleImage(BufferedImage img){
     * //        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
     * //        GraphicsDevice gs = ge.getDefaultScreenDevice();
     * //        GraphicsConfiguration gc = gs.getDefaultConfiguration();
     * imageC = gc.createCompatibleImage(img.getWidth(), img.getHeight(), Transparency.TRANSLUCENT);
     * Graphics2D g;
     * g = imageC.createGraphics();
     * g.drawImage(img, 0, 0, null);
     * g.dispose();
     * return imageC;
     * }*/
    private void mixImages(Collection<Frame> frames, Frame targetFrame) {
        for (Frame f : frames) {       
            orderedFrames.put(f.getZOrder(), f);         
            }
        
        BufferedImage image = targetFrame.getImage();
        if (image != null) {
            Graphics2D g = image.createGraphics();
//            g.setBackground(new Color(0, 0, 0, 0));
            g.clearRect(0, 0, image.getWidth(), image.getHeight());
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_SPEED);
//            g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS,java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            g.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_DISABLE);
            g.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED);
            for (Frame f : orderedFrames.values()) {
                imageF = f.getImage();
                imageX = f.getX();
                imageY = f.getY();
                imageW = f.getWidth();
                imageH = f.getHeight();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float) f.getOpacity()) / 100F));
                g.drawImage(imageF, imageX, imageY, imageW, imageH, null);
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

//    @SuppressWarnings("all")
    @SuppressWarnings("unchecked")  
    @Override
    public void run() {
        stopMe = false;
        ArrayList<Frame> frames = new ArrayList<Frame>();
        ArrayList<Future<Frame>> resultsT = new ArrayList<Future<Frame>>();
        mark = System.currentTimeMillis();
        int r = MasterMixer.getInstance().getRate();
        long frameDelay = 1000 / r;
        long timeCode = System.currentTimeMillis();
        ExecutorService pool = java.util.concurrent.Executors.newCachedThreadPool();
        while (!stopMe) {
            timeCode += frameDelay;
            Frame targetFrame = frameBuffer.getFrameToUpdate();
            frames.clear();
            try {
                resultsT = ((ArrayList) pool.invokeAll(streams, 1, TimeUnit.SECONDS));
                ArrayList<Future<Frame>> results = resultsT;
                for (Future stream : results) {
                    if ((Frame)stream.get() != null) {
                        Frame f = (Frame)stream.get();
//                        if (f != null) {
                            frames.add(f);
//                        } 
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
