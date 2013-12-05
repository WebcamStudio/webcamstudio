/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author patrick
 */
public class Frame {
    private final BufferedImage image;
    private int x = 0;
    private int y = 0;
    private int w = 320;
    private int h = 240;
    private int opacity = 100;
    private float audioVolume=1;
    private byte[] audioData;
    private int zOrder = 0;
    private String uuid = null;
    private long frameNb = 0;
    private final int aFreq = webcamstudio.WebcamStudio.audioFreq;
    
   public Frame(String id,BufferedImage img, byte[] audio){
        image=img;
        audioData=audio;
        uuid=id;
    }
   
    public void setFrameNumber(long n){
        frameNb=n;
    }
    public long getFrameNumber(){
        return frameNb;
    }
    public Frame(int w,int h,int rate){
        this.w=w;
        this.h=h;
        image = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        audioData= new byte[(aFreq *2 *2) / rate];
    }
    public void copyFrame(Frame frame){
        
        BufferedImage imageSrc = frame.getImage();
        byte[] audioSrc = frame.getAudioData();
        if (imageSrc!=null){
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                               java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                               java.awt.RenderingHints.VALUE_RENDER_SPEED);
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                               java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                               java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS,
                               java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            g.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING,
                               java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED);
            g.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING,
                               java.awt.RenderingHints.VALUE_DITHER_DISABLE);
//            g.setBackground(new Color(0,0,0,0));
//            g.clearRect(0, 0, w, h);
            g.drawImage(imageSrc, 0, 0, null);
            g.dispose();
        }
        if (audioSrc != null && audioSrc.length==audioData.length){
            System.arraycopy(audioSrc, 0, audioData, 0, audioSrc.length);
        }
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
    public void setID(String id){
        uuid=id;
    }
    public void setImage(BufferedImage img){
        if (img != null){
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                               java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                               java.awt.RenderingHints.VALUE_RENDER_SPEED);
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                               java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                               java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS,
                               java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            g.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING,
                               java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED);
            g.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING,
                               java.awt.RenderingHints.VALUE_DITHER_DISABLE);
            g.setBackground(new Color(0,0,0,0));
            g.clearRect(0, 0, w, h);
            g.drawImage(img, 0, 0, null);
            g.dispose();
        } 
    }
    public void setAudio(byte[] data){
        if (data!=null){
            audioData = data.clone();
        } 
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
