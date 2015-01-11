/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.util.ArrayList;

/**
 *
 * @author patrick
 */
public class PreviewMixer {
    private static PreviewMixer instance = null;
    public static int BUFFER_SIZE = 20;

    public static PreviewMixer getInstance() {
        if (instance==null){
            instance = new PreviewMixer();
        }
        return instance;
    }

    protected int frameRate = 25;
    protected int width = 320;
    protected int height = 240;
    protected ArrayList<SinkListener> listeners = null;
    private PreviewFrameBuilder builder = null;
    private int audioLevelLeft = 0;
    private int audioLevelRight = 0;
    private float avgFPS = 0;
    
    private PreviewMixer(){
        listeners = new ArrayList<>();
    }
    
    /**
     * @return the audioLevelLeft
     */
    public int getAudioLevelLeft(){
        return audioLevelLeft;
    }

    public void setFPS(float f) {
        avgFPS=f;
    }
    public float getFPS(){
        return avgFPS;
    }
    /**
     * @return the audioLevelRight
     */
    public int getAudioLevelRight(){
        return audioLevelRight;
    }

    public synchronized void register(SinkListener l) {
        listeners.add(l);
    }

    public synchronized void unregister(SinkListener l){
        listeners.remove(l);
    }
    public void setWidth(int w){
        width = w;
    }
    public void setHeight(int h) {
        height = h;
    }

    public void setRate(int rate) {
        frameRate = rate;
    }

    public int getRate() {
        return frameRate;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void start() {
        builder = new PreviewFrameBuilder(width,height,frameRate);
        new Thread(builder).start();
    }

    public void stop() {
        builder.stop();
    }

    public void setCurrentFrame(Frame f) {
        setAudioLevel(f);
        updateListeners(f);
    }
    private synchronized  void updateListeners(Frame f){
        for (SinkListener l : listeners){
            l.newPreFrame(f);
        }
    }
    protected void setAudioLevel(Frame f){
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

    public interface SinkListener {

        public void newPreFrame(Frame frame);
    }
}


