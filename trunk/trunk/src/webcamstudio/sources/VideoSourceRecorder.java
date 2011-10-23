/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.components.Mixer;
import webcamstudio.ffmpeg.FFMPEGEncoder;

/**
 *
 * @author patrick
 */
public class VideoSourceRecorder extends VideoSource{
    FFMPEGEncoder ffmpeg = new FFMPEGEncoder();
    
    public VideoSourceRecorder(File output){
        location = output.getAbsolutePath();
        name = output.getName();
    }
    @Override
    public void startSource() {
        frameRate = Mixer.getFPS();
        ffmpeg.setCaptureHeight(Mixer.getHeight());
        ffmpeg.setCaptureWidth(Mixer.getWidth());
        ffmpeg.setHeight(outputHeight);
        ffmpeg.setWidth(outputWidth);
        ffmpeg.setOutput(location);
        ffmpeg.setRate(frameRate);
        ffmpeg.read();
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
        timer = new Timer("Encoder",true);
        timer.scheduleAtFixedRate(new encoderFeeder(this), 0, 1000/frameRate);
        
    }

    @Override
    public void stopSource() {
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
        ffmpeg.stop();
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
    
}

class encoderFeeder extends TimerTask{
    VideoSourceRecorder source=null;
    public encoderFeeder(VideoSourceRecorder source){
        this.source=source;
    }
    @Override
    public void run() {
        try {
            source.ffmpeg.pushData(Mixer.getData());
        } catch (IOException ex) {
            Logger.getLogger(encoderFeeder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}