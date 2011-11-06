/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media;

import java.awt.image.BufferedImage;

/**
 *
 * @author patrick
 */
public class Image  {
    private BufferedImage image = null;
    private long timeStamp = 0;
    private byte[] sample = null;
    public Image(BufferedImage img,byte[] soundSample, long stamp){
        image=img;
        timeStamp=stamp;
        sample = soundSample;
    }
    public void updateAudio(byte[] audio){
        sample=audio;
    }
    public void updateImage(BufferedImage img){
        image=img;
    }
    public long getTimeStamp(){
        return timeStamp;
    }
    public BufferedImage getImage(){
        return image;
    }
    public byte[] getSound(){
        return sample;
    }
}
