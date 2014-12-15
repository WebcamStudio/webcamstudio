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
import webcamstudio.mixers.PreviewFrameBuilder;
import webcamstudio.sources.effects.Effect;
/**
 *
 * @author karl
 */
public class SourceDV extends Stream {

    ProcessRenderer capture = null;
    BufferedImage lastPreview = null;
    boolean isPlaying = false;

    public SourceDV() {
        super();
        if (this.getChName() != null){
        name = this.getChName();
        } else {
        name = "DVCam";  
        }
        rate = MasterMixer.getInstance().getRate();
    }

    @Override
    public void read() {      
        if (getPreView()){
            PreviewFrameBuilder.register(this);
        } else {
            MasterFrameBuilder.register(this);
        }
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "DV", comm);
        capture.readCustom();
        lastPreview = new BufferedImage(captureWidth,captureHeight,BufferedImage.TYPE_INT_ARGB);
        isPlaying = true;
    }

    @Override
    public void pause() {
        isPaused = true;
        capture.pause();
    }
    
    @Override
    public void stop() {
        for (int fx = 0; fx < this.getEffects().size(); fx++) {
            Effect fxT = this.getEffects().get(fx);
            if (fxT.getName().endsWith("Stretch") || fxT.getName().endsWith("Crop")) {
                // do nothing.
            } else {
                fxT.resetFX();
            }
        }
        isPlaying = false;
        if (getPreView()){
            PreviewFrameBuilder.unregister(this);
        } else {
            MasterFrameBuilder.unregister(this);
        }
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
    public boolean hasAudio() {
        return true;
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
        return true;
    }
    @Override
    public void readNext() {
        Frame f = null;
        if (capture != null) {
            f = capture.getFrame();
            if (f != null) {
                BufferedImage img = f.getImage(); 
                applyEffects(img);
            }
            if (f != null) {
                setAudioLevel(f);
                lastPreview.getGraphics().drawImage(f.getImage(), 0, 0, null);
                nextFrame=f;
            }
        }
    }

    @Override
    public void play() {
        isPaused = false;
        capture.play();
    }

}
