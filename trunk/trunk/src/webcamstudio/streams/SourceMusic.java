/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import webcamstudio.ffmpeg.FFMPEGRenderer;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class SourceMusic extends Stream {

    FFMPEGRenderer capture = null;
    BufferedImage lastPreview = null;
    
    public SourceMusic(File movie) {
        rate = MasterMixer.getRate();
        file=movie;
        name = movie.getName();
        capture = new FFMPEGRenderer(this,FFMPEGRenderer.ACTION.CAPTURE, "music");
    }


    @Override
    public void read() {
        MasterFrameBuilder.register(this);
        capture.read();
    }


    @Override
    public void stop() {
        MasterFrameBuilder.unregister(this);
        capture.stop();

    }

    @Override
    public boolean isPlaying() {
        return !capture.isStopped();
    }

    @Override
    public BufferedImage getPreview() {
        return lastPreview;
    }
    @Override
    public Frame getFrame(){
        Frame f = capture.getFrame();
        if (f!=null){
            setAudioLevel(f);
            lastPreview=f.getImage();
        }
        return f;
    }

    @Override
    public boolean hasAudio() {
        return true;
    }

    @Override
    public boolean hasVideo() {
        return false;
    }

}
