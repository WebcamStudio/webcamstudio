/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import webcamstudio.controls.ControlRescale;
import webcamstudio.ffmpeg.FFMPEGFullDesktop;

/**
 *
 * @author patrick
 */
public class VideoSourceFullDesktop extends VideoSource {

    private Timer timer = null;
    protected FFMPEGFullDesktop ffmpeg = new FFMPEGFullDesktop();
    public VideoSourceFullDesktop() {
        name = "Full Desktop";
        location = "";
        controls.add(new ControlRescale(this));
        controls.add(new webcamstudio.controls.ControlIdentity(this));

    }

    @Override
    public void stopSource() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (!ffmpeg.isStopped()){
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
        return isPlaying;
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
        frameRate=5;
        captureWidth = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth();
        captureHeight = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
        ffmpeg.setCaptureWidth(captureWidth);
        ffmpeg.setCaptureHeight(captureHeight);
        ffmpeg.setWidth(outputWidth);
        ffmpeg.setHeight(outputHeight);
        ffmpeg.setRate(frameRate);
        ffmpeg.read();
        
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer(name, true);
        timer.scheduleAtFixedRate(new imageFDesktop(this), 0, 1000 / frameRate);
    }

    protected void updateOutputImage(BufferedImage img){
        if (img!=null){
            image=img;
        }
    }

}

class imageFDesktop extends TimerTask{
    VideoSourceFullDesktop source = null;
    public imageFDesktop(VideoSourceFullDesktop s){
        source=s;
    }

    @Override
    public void run() {
        source.updateOutputImage(source.ffmpeg.getImage());
    }
}