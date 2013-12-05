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
import webcamstudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class SourceDVB extends Stream {

    ProcessRenderer capture = null;
    BufferedImage lastPreview = null;
    boolean isPlaying = false;

    public SourceDVB() {
        super();
        if (this.getChName() != null){
        name = this.getChName();
        } else {
        name = "DVB-T";  
        }
        rate = MasterMixer.getInstance().getRate();
    }

    @Override
    public void read() {
        isPlaying = true;
        rate = MasterMixer.getInstance().getRate();
        lastPreview = new BufferedImage(captureWidth,captureHeight,BufferedImage.TYPE_INT_ARGB);
        MasterFrameBuilder.register(this);
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "DVB");
        Tools.sleep(10);
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
        Frame fDVB = null;
        if (capture != null) {
            fDVB = capture.getFrame();
                for (Effect fxDVB : this.getEffects()) {
                    if (fDVB != null) {
                        if (fxDVB.needApply()){
                            fxDVB.applyEffect(fDVB.getImage());
                        }
                    }
                }
            
            if (fDVB != null) {
                setAudioLevel(fDVB);
                lastPreview.getGraphics().drawImage(fDVB.getImage(), 0, 0, null);
            }
        }
        nextFrame=fDVB;
    }

}
