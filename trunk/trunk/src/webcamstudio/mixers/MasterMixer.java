/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author patrick
 */
public class MasterMixer {

    static protected int frameRate = 15;
    static protected int width = 720;
    static protected int height = 480;
    static protected ArrayList<SinkListener> listeners = new ArrayList<SinkListener>();
    static private MasterFrameBuilder builder = new MasterFrameBuilder();
    static private int audioLevelLeft = 0;
    static private int audioLevelRight = 0;
    
    static Frame currentFrame = null;

    /**
     * @return the audioLevelLeft
     */
    public static int getAudioLevelLeft() {
        return audioLevelLeft;
    }

    /**
     * @return the audioLevelRight
     */
    public static int getAudioLevelRight() {
        return audioLevelRight;
    }

    public interface SinkListener{
        public void newFrame(Frame frame);
    }
    
    public synchronized static void register(SinkListener l){
        listeners.add(l);
    }
    public synchronized static void unregister(SinkListener l){
        listeners.remove(l);
    }
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
        builder = new MasterFrameBuilder();
        new Thread(builder).start();
    }

    static public void stop() {
        builder.stop();
    }
    public static void setCurrentFrame(Frame f ){
        currentFrame=f;
        setAudioLevel(f);
        updateListeners(f);
    }
    private synchronized static void updateListeners(Frame f){
        for (SinkListener l : listeners){
            l.newFrame(f);
        }
    }
    public static void setCurrentFrame(BufferedImage img,byte[] audio){
        Frame f = new Frame("",img, audio);
        currentFrame=f;
        setAudioLevel(f);
        updateListeners(f);
    }
    protected static void setAudioLevel(Frame f) {
        byte[] data = f.getAudioData();
        if (data != null) {
            audioLevelLeft = 0;
            audioLevelRight = 0;
            int tempValue = 0;
            for (int i = 0; i < data.length; i += 4) {
                tempValue = (data[i]<<8 & (data[i + 1]))/256;
                if (tempValue<0){
                    tempValue *=-1;
                }
                if (getAudioLevelLeft() < tempValue) {
                    audioLevelLeft = tempValue;
                }
                tempValue = (data[i + 2]<<8 & (data[i + 3]))/256;
               
                if (tempValue<0){
                    tempValue *=-1;
                }
                if (getAudioLevelRight() < tempValue) {
                    audioLevelRight = tempValue;
                }
            }
        }
    }
}


