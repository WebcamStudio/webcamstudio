/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import webcamstudio.layout.Layout;
import webcamstudio.layout.LayoutItem;
import webcamstudio.media.Image;
import webcamstudio.sources.VideoSource;

/**
 *
 * @author patrick
 */
public class VideoMixer{

    private static VideoMixer instance = null;
    private static ArrayList<VideoListener> listeners = new ArrayList<VideoListener>();
    private int frameRate = 15;
    private int width = 320;
    private int height = 240;
    private Timer timer = null;
    private static long timecode = 0;

    private VideoMixer(int w, int h, int r) {
        width = w;
        height = h;
        frameRate = r;
        timer = new Timer("VideoMixer", true);
        timer.scheduleAtFixedRate(new VideoMixerBuilder(), 0, 1000/frameRate);

    }
    protected static void setTimeCode(long value){
        timecode=value;
    }
    public static long getTimeCode(){
        return timecode;
    }
    public void setFrameRate(int r) {
        frameRate = r;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer("VideoMixer", true);
        timer.scheduleAtFixedRate(new VideoMixerBuilder(), 0, 1000/frameRate);
    }

    public int getFrameRate() {
        return frameRate;
    }

    public int getWidth() {
        return width;
    }

    public int getHeigt() {
        return height;
    }

    public void setFormat(int width, int height, int rate) {
        this.width = width;
        this.height = height;
        this.frameRate = rate;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer("VideoMixer", true);
        timer.scheduleAtFixedRate(new VideoMixerBuilder(), 0, 1000/frameRate);
    }

    public void shutdown() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public static VideoMixer getNewInstance(int width, int height, int rate) {
        if (instance != null) {
            instance.shutdown();
            timecode = instance.getTimeCode();
            
        }
        instance = new VideoMixer(width, height, rate);
        instance.setTimeCode(timecode);
        return instance;
    }

    public static VideoMixer getInstance() {
        if (instance == null) {
            instance = new VideoMixer(320, 240, 15);
        }
        return instance;
    }

    public static void addListener(VideoListener l) {
        listeners.add(l);
    }

    public static void removeListener(VideoListener l) {
        listeners.remove(l);
    }

    protected void updateListeners(Image image) {
        for (VideoListener l : listeners) {
            l.newImage(image);
        }
    }
}

class VideoMixerBuilder extends TimerTask {

    @Override
    public void run() {
        VideoMixer mixer = VideoMixer.getInstance();
        
        if (mixer != null) {
            long timecode = mixer.getTimeCode();
            Layout currentLayout = Layout.getActiveLayout();
            BufferedImage tempImage = new BufferedImage(mixer.getWidth(), mixer.getHeigt(), BufferedImage.TYPE_INT_ARGB);
            if (currentLayout != null) {
                Graphics2D buffer = tempImage.createGraphics();
                for (LayoutItem item : currentLayout.getItems()) {
                    VideoSource  source = item.getSource();
                    buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, ((float) (source.getOpacity())) / 100F));
                    buffer.drawImage(source.getImage(), source.getShowAtX(), source.getShowAtY(), source.getOutputWidth(), source.getOutputHeight(), null);
                }
                buffer.dispose();
            }
            mixer.updateListeners(new Image(tempImage,timecode,100,0));
            timecode += ((44100*2*2)/mixer.getFrameRate());
            mixer.setTimeCode(timecode);
        }
        //System.out.println("Process Duration = " + (System.currentTimeMillis() - start));
    }
}