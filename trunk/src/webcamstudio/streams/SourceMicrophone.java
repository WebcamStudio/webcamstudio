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
    boolean isPlaying = false;

    public SourceMicrophone() {
        super();
        rate = MasterMixer.getInstance().getRate();
        name = "Microphone";

    }

    @Override
    public void read() {
        isPlaying = true;
        MasterFrameBuilder.register(this);
        lastPreview = new BufferedImage(captureWidth,captureHeight,BufferedImage.TYPE_INT_ARGB);
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "mic", comm);
        capture.read();
    }

    @Override
    public void pause() {
        capture.pause();
    }
    
    @Override
    public void stop() {
        isPlaying = false;
        MasterFrameBuilder.unregister(this);
        if (capture != null) {
            capture.stop();
            capture = null;
        }
        if (this.getBackFF()){
            this.setComm("FF");
        }

    }
    @Override
    public boolean needSeek() {
            return needSeekCTRL=false;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
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
    public boolean hasFakeVideo(){
        return true;
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
        return true;
    }
    @Override
    public void readNext() {
         Frame f = null;
        if (capture != null) {
            f = capture.getFrame();
            if (f != null) {
                setAudioLevel(f);
                lastPreview.getGraphics().drawImage(f.getImage(), 0, 0, null);
            }
        }
        nextFrame=f;
    }

    @Override
    public void play() {
        capture.play();
    }
}
