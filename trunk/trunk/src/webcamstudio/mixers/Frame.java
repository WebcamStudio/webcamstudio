/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.awt.image.BufferedImage;
import javax.sound.sampled.AudioFormat;

/**
 *
 * @author patrick
 */
public class Frame {
    private BufferedImage image;
    private int x = 0;
    private int y = 0;
    private int w = 320;
    private int h = 240;
    private int opacity = 100;
    private float audioVolume=1;
    private byte[] audioData;
    private long timecode = 0;
    private AudioFormat format;
    private int zOrder = 0;
    private String uuid = null;
    
    public Frame(String id,BufferedImage img, byte[] audio, long timeCode, AudioFormat f){
        image=img;
        audioData=audio;
        timecode=timeCode;
        format=f;
        uuid=id;
    }
    protected Frame(int w,int h,int rate){
        image=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        audioData= new byte[(44100 *2 *2) / rate];
    }
    public String getID(){
        return uuid;
    }
    public void setZOrder(int z){
        zOrder=z;
    }
    public int getZOrder(){
        return zOrder;
    }
    public void setImage(BufferedImage img){
        image=img;
    }
    public void setAudio(byte[] data){
        audioData = data;
    }
    public void setOutputFormat(int x, int y, int w, int h, int opacity,float volume){
        this.x=x;
        this.y=y;
        this.w=w;
        this.h=h;
        this.opacity=opacity;
        this.audioVolume=volume;
    }
    public BufferedImage getImage(){
        return image;
    }
     public byte[] getAudioData() {
        return audioData;
    }

    public long getTimeCode() {
        return timecode;
    }

    public void setTimeCode(long t){
        timecode=t;
    }
    public AudioFormat getFormat() {
        return format;
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public int getWidth(){
        return w;
    }
    public int getHeight(){
        return h;
    }
    public int getOpacity(){
        return opacity;
    }
    public float getVolume(){
        return audioVolume;
    }
}
