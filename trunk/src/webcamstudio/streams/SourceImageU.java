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
public class SourceImageU extends Stream {

    ProcessRenderer capture = null;
    BufferedImage lastPreview = null;
    boolean isPlaying = false;

    public SourceImageU(File img) {
        super();
        file = img;
        name = img.getName();
    }

    @Override
    public void read() {
        isPlaying = true;
            rate = MasterMixer.getInstance().getRate();
            lastPreview = new BufferedImage(captureWidth,captureHeight,BufferedImage.TYPE_INT_ARGB);
            MasterFrameBuilder.register(this); 
            capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "image", comm);
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
            return false;
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
    public boolean hasAudio() {
        return false;
    }
    @Override
    public boolean hasVideo() {
        return true;
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
    public void readNext() {
        Frame f = null;
        if (capture != null) {
            f = capture.getFrame();
            if (this.getEffects() != null) {
                for (Effect fxI : this.getEffects()) {
                    if (f != null) {
                        if (fxI.needApply()){
                            fxI.applyEffect(f.getImage());
                        }
                    }
                }
            }
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
