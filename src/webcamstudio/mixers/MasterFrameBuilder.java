/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package webcamstudio.mixers;

import static java.awt.AlphaComposite.SRC_OVER;
import static java.awt.AlphaComposite.SRC;
import static java.awt.AlphaComposite.getInstance;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.VolatileImage;
import static java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION;
import static java.awt.RenderingHints.KEY_COLOR_RENDERING;
import static java.awt.RenderingHints.KEY_DITHERING;
import static java.awt.RenderingHints.KEY_FRACTIONALMETRICS;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED;
import static java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED;
import static java.awt.RenderingHints.VALUE_DITHER_DISABLE;
import static java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.RenderingHints.VALUE_RENDER_SPEED;
import java.awt.image.BufferedImage;
import static java.lang.Short.MAX_VALUE;
import static java.lang.Short.MIN_VALUE;
import static java.lang.System.currentTimeMillis;
import static java.nio.ByteBuffer.wrap;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.Executors.newCachedThreadPool;
import java.util.concurrent.Future;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.logging.Level;
import java.util.logging.Logger;
import static webcamstudio.components.MasterPanel.masterVolume;
import webcamstudio.streams.SourceDesktop;
import webcamstudio.streams.Stream;
import static webcamstudio.util.Tools.sleep;

/**
 *
 * @author patrick (modified by karl)
 */
public class MasterFrameBuilder implements Runnable {

    private static final ArrayList<Stream> streams = new ArrayList<>();// add private
    private static int fps = 0;
    public static synchronized void register(Stream s) {
        if (!streams.contains(s)) {
            streams.add(s);
//            System.out.println("Register Master Stream Size: "+streams.size());
            if (!(s instanceof SourceDesktop)) {
                s.setRate(MasterMixer.getInstance().getRate());
            }
        }
    }

    public static synchronized void unregister(Stream s) {
        streams.remove(s);
//        System.out.println("UnRegister Master Stream Size: "+streams.size());
        if (!(s instanceof SourceDesktop)) {
            s.setRate(PreviewMixer.getInstance().getRate());
        }
    }
    private Image imageF;
    private int imageX, imageY, imageW, imageH;
    private boolean stopMe = false;
    private long mark = currentTimeMillis();
    private FrameBuffer frameBuffer = null;
    private final TreeMap<Integer, Frame> orderedFrames = new TreeMap<>();

    public MasterFrameBuilder(int w, int h, int r) {
        frameBuffer = new FrameBuffer(w, h, r);
    }

    public void stop() {
        stopMe = true;
    }

    private void mixImages(Collection<Frame> frames, Frame targetFrame) {
        // This is sort of a hack to allow multiple frames to have the same ZOrder...
        // It drives me nuts when I activate a source and it makes another one with the same Z-Order disappear...   ---GEC
        int count = 0;
        for (Frame f : frames) {
            orderedFrames.put(((f.getZOrder() * 128) + count), f);
            count++;
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();

        BufferedImage image = targetFrame.getImage();
        if (image != null) {
            Graphics2D g = image.createGraphics();
            int width = image.getWidth();
            int height = image.getHeight();

            g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_SPEED);
            g.setRenderingHint(KEY_RENDERING, VALUE_RENDER_SPEED);
            g.setRenderingHint(KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_OFF);
            g.setRenderingHint(KEY_DITHERING, VALUE_DITHER_DISABLE);
            g.setRenderingHint(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_SPEED);
            g.setBackground(new Color(0, 0, 0, 0));
            g.clearRect(0, 0, width, height);

            for (Frame f : orderedFrames.values()) {
                imageF = f.getImage();
                imageX = f.getX();
                imageY = f.getY();
                imageW = f.getWidth();
                imageH = f.getHeight();

                g.setComposite(getInstance(SRC_OVER, f.getOpacity() / 100F));
                g.drawImage(imageF, imageX, imageY, imageW, imageH, null);
            }
            g.dispose();
        }
        orderedFrames.clear();
    }

    private void mixAudio(Collection<Frame> frames, Frame targetFrame) {
        byte[] audioData = targetFrame.getAudioData();
        ShortBuffer outputBuffer = wrap(audioData).asShortBuffer();
        for (int i = 0; i < audioData.length; i++) {
            audioData[i] = 0;
        }
        for (Frame f : frames) {
            byte[] data = f.getAudioData();
            if (data != null) {
                ShortBuffer buffer = wrap(data).asShortBuffer();
                outputBuffer.rewind();
                while (buffer.hasRemaining()) {
//                    System.out.println("Volume="+f.getVolume());
                    float volume = f.getVolume() + masterVolume;
                    if (volume < 0) {
                        volume = 0;
                    }
                    float mix = buffer.get() * (volume);
                    outputBuffer.mark();
                    if (outputBuffer.position() < outputBuffer.limit()){ //25fps IOException
                        mix += outputBuffer.get();
                    }
                    outputBuffer.reset();
                    if (mix > MAX_VALUE) {
                        mix = MAX_VALUE;
                    } else if (mix < MIN_VALUE) {
                        mix = MIN_VALUE;
                    }
                    if (outputBuffer.position() < outputBuffer.limit()){ //25fps IOException
                        outputBuffer.put((short) mix);
                    }
                }
                f.setAudio(null);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() throws NullPointerException{
        stopMe = false;
        ArrayList<Frame> frames = new ArrayList<>();
        mark = System.currentTimeMillis();
        int r = MasterMixer.getInstance().getRate();
        long frameDelay = 1000 / r;
        long timeCode = currentTimeMillis();
        long frameNum = 0;
        while (!stopMe) {
            timeCode += frameDelay;
            Frame targetFrame = frameBuffer.getFrameToUpdate();
            frames.clear();

            long captureTime = 0;
            long captureStartTime = System.nanoTime();

            // threaded capture mode runs frame capture for each source in a different thread
            // In principle it should be better but the overhead of threading appears to be more trouble than it's worth.
            boolean threadedCaptureMode = true;
            ExecutorService pool = newCachedThreadPool();
            if (threadedCaptureMode) {
                ArrayList<Future<Frame>> resultsT = new ArrayList<>();

                try {
                    resultsT = ((ArrayList)pool.invokeAll(streams, 5, SECONDS));
                } catch (InterruptedException ex) {
                    Logger.getLogger(MasterFrameBuilder.class.getName()).log(Level.SEVERE, null, ex);
                }
                ArrayList<Future<Frame>> results = resultsT;

                int i=0;
                Frame f;
                for (Future stream : results) {
                    try {
                        f = (Frame)stream.get();

                        if (f != null) {
                            frames.add(f);
                        }
                    } catch (CancellationException | InterruptedException | ExecutionException ex) {
                        Logger.getLogger(MasterFrameBuilder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
            for (int i = 0; i < streams.size(); i++) {
                Frame f;

                try {
                    Stream s = streams.get(i);
                    f = s.call();

                    // Due to race conditions when sources start up, a source may not really be ready to operate by the time it's active in MasterFrameBuilder. (Ultimately that should probably be fixed)
                    // For that reason, we guard against (f == null) here, so streams
                    if (f != null) {
                        frames.add(f);
                    }
                }
                catch (Exception e)
                {}
            }
            }

            long now = currentTimeMillis();
            captureTime = (now - captureStartTime);

            long sleepTime = (timeCode - now);

            // Drop frames if we're running behind - but no more than half of them
//            if ((sleepTime > 0) || ((frameNum % 2) != 0)) {
                fps++;

                mixAudio(frames, targetFrame);
                mixImages(frames, targetFrame);
                targetFrame = null;
                frameBuffer.doneUpdate();
                MasterMixer.getInstance().setCurrentFrame(frameBuffer.pop());
//            }

            float delta = (now - mark);
            if (delta >= 1000) {
                mark = now;
                MasterMixer.getInstance().setFPS(fps / (delta / 1000F));
                fps = 0;
            }

            //System.out.println("Capture time: " + (captureTime / 1000000.0) + "ms");
            //System.out.println("Timecode: " + timeCode + ", now: " + now + ", diff: " + sleepTime);

            if (sleepTime > 0) {
                sleep(sleepTime);
            }

            frameNum++;
        }
    }
}
