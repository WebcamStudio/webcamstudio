/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.awt.image.BufferedImage;
import webcamstudio.controls.ControlRescale;
import webcamstudio.ffmpeg.FFMPEGCapture;
import webcamstudio.media.Image;
import webcamstudio.mixers.VideoListener;


/**
 *
 * @author patrick
 */
public class VideoSourceFullDesktop extends VideoSource implements VideoListener {

    
    protected FFMPEGCapture ffmpeg = null;
    public VideoSourceFullDesktop() {
        name = "Full Desktop";
        location = "";
        controls.add(new ControlRescale(this));
        controls.add(new webcamstudio.controls.ControlIdentity(this));

    }

    @Override
    public void stopSource() {
        if (ffmpeg != null && !ffmpeg.isStopped()){
            ffmpeg.stop();
        }
        isPlaying=false;
        image = null;
    }

    @Override
    public boolean canUpdateSource() {
        return false;
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isPlaying() {
        return !ffmpeg.isStopped();
    }

    @Override
    public void pause() {
    }

    @Override
    public void play() {
    }

    @Override
    public void startSource() {
        isPlaying=true;
        ffmpeg = new FFMPEGCapture("desktop",this,null);
        frameRate=5;
        captureWidth = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth();
        captureHeight = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
        ffmpeg.setCaptureWidth(captureWidth);
        ffmpeg.setCaptureHeight(captureHeight);
        ffmpeg.setWidth(outputWidth);
        ffmpeg.setHeight(outputHeight);
        ffmpeg.setRate(frameRate);
        ffmpeg.read();
        
    }

    protected void updateOutputImage(BufferedImage img){
    }

    @Override
    public void newImage(Image image) {
        updateOutputImage(image.getImage());
        this.image = image.getImage();
    }

}
