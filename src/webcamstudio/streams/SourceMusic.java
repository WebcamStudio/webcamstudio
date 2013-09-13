/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import webcamstudio.externals.ProcessRenderer;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class SourceMusic extends Stream {

    ProcessRenderer capture = null;
    BufferedImage lastPreview = null;
    boolean isPlaying = false;

    public SourceMusic(File music) {
        super();
        rate = MasterMixer.getInstance().getRate();
        file = music;
        name = music.getName();

    }

    @Override
    public void read() {
        isPlaying = true;
        MasterFrameBuilder.register(this);
        lastPreview = new BufferedImage(captureWidth,captureHeight,BufferedImage.TYPE_INT_ARGB);
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "music");
        capture.read();
    }

    @Override
    public void stop() {
        isPlaying = false;
        MasterFrameBuilder.unregister(this);
        if (capture != null) {
            capture.stop();
            capture = null;
        }

    }
    @Override
    public boolean needSeek() {
            return needSeekCTRL=true;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
/*        if (capture != null) {
            return !capture.isStopped();
        } else {
            return false;
        } */
    }
    @Override
    public void setIsPlaying(boolean setIsPlaying) {
        isPlaying = setIsPlaying;
    }
    @Override
    public BufferedImage getPreview() {
        return lastPreview;
    }

    @Override
    public Frame getFrame() {
       
        return nextFrame;
    }
    @Override
    public boolean hasAudio() {
        return true;
    }

    @Override
    public boolean hasVideo() {
        return false;
    }
    @Override
    public void readNext() {
         Frame f = null;
        if (capture != null) {
            f = capture.getFrame();
            if (f != null) {
                setAudioLevel(f);
            }
        }
        nextFrame=f;
    }
}
