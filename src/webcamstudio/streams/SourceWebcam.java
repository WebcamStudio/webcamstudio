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
import webcamstudio.sources.effects.Effect;

/**
 *
 * @author patrick (modified by karl)
 */
public class SourceWebcam extends Stream {

    ProcessRenderer capture = null;
    BufferedImage lastPreview = null;
    boolean isPlaying = false;
    public SourceWebcam(File device) {
        super();
        rate = MasterMixer.getInstance().getRate();
        file = device;
        name = device.getName();
    }

    public SourceWebcam(String defaultName) {
        super();
        rate = MasterMixer.getInstance().getRate();
        name = defaultName;
    }

    @Override
    public void read() {
        isPlaying=true;
        lastPreview = new BufferedImage(captureWidth,captureHeight,BufferedImage.TYPE_INT_ARGB);
        rate = MasterMixer.getInstance().getRate();
        MasterFrameBuilder.register(this);
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "webcam", comm);
        capture.read();
    }
    
    @Override
    public void pause() {
        capture.pause();
    }
    
    @Override
    public void play() {
        capture.play();
    }
    
    @Override
    public void stop() {
        isPlaying=false;
        if (capture != null) {
            capture.stop();
        }
        if (this.getBackFF()){
            this.setComm("FF");
        }
        MasterFrameBuilder.unregister(this);
    }
    @Override
    public boolean hasFakeVideo(){
        return true;
    }
    @Override
    public boolean hasFakeAudio(){
        return false;
    }
    @Override
    public boolean hasVideo() {
        return true;
    }
    @Override
    public boolean hasAudio() {
        return false;
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
    public synchronized Frame getFrame() {
        return nextFrame;
    }

    @Override
    public void readNext() {
        Frame f = null;
        if (capture != null) {
            f = capture.getFrame();
            if (this.getEffects() != null) {
                for (Effect fxW : this.getEffects()) {
                    if (f != null && fxW.needApply()) {                    
                        fxW.applyEffect(f.getImage());
                    }
                }
            }
            if (f != null) {
                setAudioLevel(f);
                lastPreview=f.getImage();
            }
        }
        nextFrame=f;
    }
}
