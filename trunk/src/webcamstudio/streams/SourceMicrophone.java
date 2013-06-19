/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import webcamstudio.externals.ProcessRenderer;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick (modified by Karl)
 */
public class SourceMicrophone extends Stream {

    ProcessRenderer capture = null;
    BufferedImage lastPreview = null;

    public SourceMicrophone() {
        super();
        rate = MasterMixer.getInstance().getRate();
        name = "Microphone";

    }

    @Override
    public void read() {
        MasterFrameBuilder.register(this);
        lastPreview = new BufferedImage(captureWidth,captureHeight,BufferedImage.TYPE_INT_ARGB);
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "mic");
        capture.read();
    }

    @Override
    public void stop() {
        MasterFrameBuilder.unregister(this);
        if (capture != null) {
            capture.stop();
            capture = null;
        }

    }
    @Override
    public boolean needSeek() {
            return needSeekCTRL=false;
    }

    @Override
    public boolean isPlaying() {
        if (capture != null) {
            return !capture.isStopped();
        } else {
            return false;
        }

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
    public boolean hasFakeVideo(){
        return false;
    }
    @Override
    public boolean hasFakeAudio(){
        return true;
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
