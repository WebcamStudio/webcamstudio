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
        MasterFrameBuilder.register(this);
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "DV", comm);
        capture.read();
        lastPreview = new BufferedImage(captureWidth,captureHeight,BufferedImage.TYPE_INT_ARGB);
        isPlaying = true;
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
        Frame fDV = null;
        if (capture != null) {
            fDV = capture.getFrame();
            if (fDV != null) {
                if (this.getEffects() != null) {
                    for (int fx = 0; fx < this.getEffects().size(); fx++) {
                        Effect fxT = this.getEffects().get(fx);
                        if (fxT.needApply()){
                            BufferedImage txImage = fDV.getImage(); 
                            fxT.applyEffect(txImage);
                        }
                    }
                }
                setAudioLevel(fDV);
                lastPreview.getGraphics().drawImage(fDV.getImage(), 0, 0, null);
                nextFrame=fDV;
            }
        }
    }

    @Override
    public void play() {
        capture.play();
    }

}
