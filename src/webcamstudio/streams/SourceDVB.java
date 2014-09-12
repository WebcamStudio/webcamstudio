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
import webcamstudio.util.Tools;
/**
 *
 * @author karl
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
        if (getPreView()){
            PreviewFrameBuilder.register(this);
        } else {
            MasterFrameBuilder.register(this);
        }
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "DVB", comm);
//        Tools.sleep(200);
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
        Frame fDVB = null;
        if (capture != null) {
            fDVB = capture.getFrame();
            if (fDVB != null) {
                if (this.getEffects() != null) {
                    for (int fx = 0; fx < this.getEffects().size(); fx++) {
                        Effect fxT = this.getEffects().get(fx);
                        if (fxT.needApply()){
                            BufferedImage txImage = fDVB.getImage(); 
                            fxT.applyEffect(txImage);
                        }
                    }
                }
                setAudioLevel(fDVB);
                lastPreview.getGraphics().drawImage(fDVB.getImage(), 0, 0, null);
                nextFrame=fDVB;
            }
        }
    }

    @Override
    public void play() {
        capture.play();
    }

}
