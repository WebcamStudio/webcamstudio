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
public class SourceMovie extends Stream {

    ProcessRenderer capture = null;
    BufferedImage lastPreview = null;
    boolean isPlaying = false;

    public SourceMovie(File movie) {
        super();
        rate = MasterMixer.getInstance().getRate();
        file = movie;
        name = movie.getName();
    }

    @Override
    public void read() {
        isPlaying = true;
        rate = MasterMixer.getInstance().getRate();
        lastPreview = new BufferedImage(captureWidth,captureHeight,BufferedImage.TYPE_INT_ARGB);
        MasterFrameBuilder.register(this);
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "movie", comm);
        capture.read();
    }

    @Override
    public void pause() {
        isPlaying = false;
        capture.pause();
    }
    
    @Override
    public void stop() {
        if (loop){
            if (capture != null) {
                capture.stop();
                capture = null;
            }
            if (this.getBackFF()){
                this.setComm("FF");
            }
            this.read();
        } else {
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
    }
    @Override
    public boolean needSeek() {
            return needSeekCTRL=true;
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
    public boolean hasFakeVideo(){
        return true;
    }
    @Override
    public boolean hasFakeAudio(){
        return true;
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
    public void readNext() {
        Frame f = null;
        if (capture != null) {
            f = capture.getFrame();
            if (this.getEffects() != null) {
                for (int fx = 0; fx < this.getEffects().size(); fx++) {
                    if (f != null) {
                        Effect fxM = this.getEffects().get(fx);
                        if (fxM.needApply()){   
                            fxM.applyEffect(f.getImage());
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
        isPlaying = true;
        capture.play();
    }

}
