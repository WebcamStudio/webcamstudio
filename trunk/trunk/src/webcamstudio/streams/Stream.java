/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import webcamstudio.channels.MasterChannels;
import webcamstudio.channels.transitions.Transition;
import webcamstudio.mixers.Frame;
import webcamstudio.sources.effects.Effect;

/**
 *
 * @author patrick
 */
public abstract class Stream {

    protected String uuid = java.util.UUID.randomUUID().toString();
    protected int captureWidth = 320;
    protected int captureHeight = 240;
    protected int width = 320;
    protected int height = 240;
    protected int x = 0;
    protected int y = 0;
    protected float volume = 0.5f;
    protected int opacity = 100;
    protected int rate = 15;
    protected int seek = 0;
    protected int zorder = 0;
    protected File file = null;
    protected String name = "Default";
    protected String url = null;
    protected int audioLevelLeft = 0;
    protected int audioLevelRight = 0;
    protected ArrayList<Effect> effects = new ArrayList<Effect>();
    protected ArrayList<SourceChannel> channels = new ArrayList<SourceChannel>();
    protected int desktopX = 0;
    protected int desktopY = 0;
    protected int desktopW = 1024;
    protected int desktopH = 768;
    ArrayList<Transition> startTransitions = new ArrayList<Transition>();
    ArrayList<Transition> endTransitions = new ArrayList<Transition>();
    Listener listener = null;

    protected Stream() {
        MasterChannels.getInstance().register(this);
    }

    public void addStartTransition(Transition t) {
        startTransitions.add(t);
    }

    public void addEndTransition(Transition t) {
        endTransitions.add(t);
    }

    public void removeStartTransition(Transition t) {
        startTransitions.remove(t);
    }

    public void removeEndTransition(Transition t) {
        endTransitions.remove(t);
    }

    public ArrayList<Transition> getStartTransitions() {
        return startTransitions;
    }

    public ArrayList<Transition> getEndTransitions() {
        return endTransitions;
    }

    /**
     * @return the desktopX
     */
    public int getDesktopX() {
        return desktopX;
    }

    /**
     * @param desktopX the desktopX to set
     */
    public void setDesktopX(int desktopX) {
        this.desktopX = desktopX;
    }

    /**
     * @return the desktopY
     */
    public int getDesktopY() {
        return desktopY;
    }

    /**
     * @param desktopY the desktopY to set
     */
    public void setDesktopY(int desktopY) {
        this.desktopY = desktopY;
    }

    public int getDesktopEndX(){
        return desktopX + desktopW;
    }
    public int getDesktopEndY(){
        return desktopY+desktopH;
    }
    /**
     * @return the desktopW
     */
    public int getDesktopW() {
        return desktopW;
    }

    /**
     * @param desktopW the desktopW to set
     */
    public void setDesktopW(int desktopW) {
        this.desktopW = desktopW;
    }

    /**
     * @return the desktopH
     */
    public int getDesktopH() {
        return desktopH;
    }

    /**
     * @param desktopH the desktopH to set
     */
    public void setDesktopH(int desktopH) {
        this.desktopH = desktopH;
    }

    public interface Listener {

        public void sourceUpdated(Stream stream);
    }

    public void setListener(Listener l) {
        listener = l;
    }

    public void updateStatus() {
        if (listener != null) {
            listener.sourceUpdated(this);
        }
    }

    public void destroy() {
        stop();
        MasterChannels.getInstance().unregister(this);
    }

    public abstract void read();

    public abstract void stop();

    public abstract boolean isPlaying();

    public abstract BufferedImage getPreview();

    public abstract boolean hasAudio();

    public abstract boolean hasVideo();

    public int getAudioLevelLeft() {
        return audioLevelLeft;
    }

    public int getAudioLevelRight() {
        return audioLevelRight;
    }

    public void addChannel(SourceChannel sc) {
        channels.add(sc);
    }

    public void removeChannel(SourceChannel sc) {
        channels.remove(sc);
    }

    public void selectChannel(String name) {
        for (SourceChannel sc : channels) {
            if (sc.getName().equals(name)) {
                sc.apply(this);
                break;
            }
        }
    }

    public ArrayList<SourceChannel> getChannels() {
        return channels;
    }

    public void setName(String n) {
        name = n;
    }

    public ArrayList<Effect> getEffects() {
        return effects;
    }

    public synchronized void setEffects(ArrayList<Effect> list) {
        effects = list;
    }

    public synchronized void addEffect(Effect e) {
        effects.add(e);
    }

    public synchronized void removeEffect(Effect e) {
        effects.remove(e);
    }

    public synchronized void applyEffects(BufferedImage img) {
        ArrayList<Effect> temp = new ArrayList<Effect>();
        temp.addAll(effects);
        for (Effect e : temp) {
            e.applyEffect(img);
        }
    }

    protected void setAudioLevel(Frame f) {
        if (f != null) {
            byte[] data = f.getAudioData();
            if (data != null) {
                audioLevelLeft = 0;
                audioLevelRight = 0;
                int tempValue = 0;
                for (int i = 0; i < data.length; i += 4) {
                    tempValue = (data[i] << 8 & (data[i + 1])) / 256;
                    if (tempValue < 0) {
                        tempValue *= -1;
                    }
                    if (audioLevelLeft < tempValue) {
                        audioLevelLeft = tempValue;
                    }
                    tempValue = (data[i + 2] << 8 & (data[i + 3])) / 256;

                    if (tempValue < 0) {
                        tempValue *= -1;
                    }
                    if (audioLevelRight < tempValue) {
                        audioLevelRight = tempValue;
                    }
                }
                audioLevelLeft = (int) (audioLevelLeft * volume);
                audioLevelRight = (int) (audioLevelRight * volume);
            }
        }
    }

    public String getURL() {
        return url;
    }

    public String getName() {
        return name;
    }

    public Frame getFrame() {
        Frame f = null;
        return f;
    }

    public String getID() {
        return uuid;
    }

    public File getFile() {
        return file;
    }

    public void setZOrder(int z) {
        zorder = z;
    }

    public int getZOrder() {
        return zorder;
    }

    /**
     * @return the captureWidth
     */
    public int getCaptureWidth() {
        return captureWidth;
    }

    /**
     * @param captureWidth the captureWidth to set
     */
    public void setCaptureWidth(int captureWidth) {
        this.captureWidth = captureWidth;
    }

    /**
     * @return the captureHeight
     */
    public int getCaptureHeight() {
        return captureHeight;
    }

    /**
     * @param captureHeight the captureHeight to set
     */
    public void setCaptureHeight(int captureHeight) {
        this.captureHeight = captureHeight;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return the volume
     */
    public float getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(float volume) {
        this.volume = volume;
    }

    /**
     * @return the opacity
     */
    public int getOpacity() {
        return opacity;
    }

    /**
     * @param opacity the opacity to set
     */
    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    /**
     * @return the rate
     */
    public int getRate() {
        return rate;
    }

    /**
     * @param rate the rate to set
     */
    public void setRate(int rate) {
        this.rate = rate;
    }

    /**
     * @return the seek
     */
    public int getSeek() {
        return seek;
    }

    /**
     * @param seek the seek to set
     */
    public void setSeek(int seek) {
        this.seek = seek;
    }

    public static Stream getInstance(File file) {
        Stream stream = null;
        String ext = file.getName().toLowerCase().trim();
        System.out.println("DEBUG EXT: " + ext);
        if (ext.endsWith(".avi")
                || ext.endsWith(".ogg")
                || ext.endsWith(".ogv")
                || ext.endsWith(".mp4")
                || ext.endsWith(".m4v")
                || ext.endsWith(".mpg")
                || ext.endsWith(".divx")
                || ext.endsWith(".wmv")
                || ext.endsWith(".flv")
                || ext.endsWith(".mov")
                || ext.endsWith(".vob")) {
            stream = new SourceMovie(file);
        } else if (file.getAbsolutePath().toLowerCase().startsWith("/dev/video")) {
            stream = new SourceWebcam(file);
        } else if (ext.endsWith(".jpg")
                || ext.endsWith(".bmp")
                || ext.endsWith(".png")) {
            stream = new SourceImage(file);
        } else if (ext.endsWith(".gif")) {
            stream = new SourceImageGif(file);
        } else if (ext.endsWith(".mp3")
                || ext.endsWith(".wav")
                || ext.endsWith(".wma")
                || ext.endsWith(".m4a")
                || ext.endsWith(".mp2")) {
            stream = new SourceMusic(file);
        } else if (ext.endsWith(".wss")){
            stream = new SourceCustom(file);
        }
        return stream;
    }
}
