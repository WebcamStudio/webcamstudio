/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import webcamstudio.mixers.VideoMixer;

/**
 *
 * @author patrick
 */
public class VideoSourceRecorder extends VideoSource{
    
    
    public VideoSourceRecorder(File output){
        location = output.getAbsolutePath();
        name = output.getName();
    }
    @Override
    public void startSource() {
        frameRate = VideoMixer.getInstance().getFrameRate();
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
        return false;
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
        
    }
}