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
import webcamstudio.util.Tools;

/**
 *
 * @author karl
 */
public class SourceURL extends Stream {
    private static final String userHomeDir = Tools.getUserHome();

    ProcessRenderer capture = null;
    BufferedImage lastPreview = null;
    boolean isPlaying = false;
    
    public SourceURL() {
        super();
        name = "URL";
        rate = MasterMixer.getInstance().getRate();
    }

    @Override
    public void read() {
        isPlaying = true;
        rate = MasterMixer.getInstance().getRate();
        lastPreview = new BufferedImage(captureWidth,captureHeight,BufferedImage.TYPE_INT_ARGB);
        MasterFrameBuilder.register(this);
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "url", comm);
        capture.readCom();           
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
            File directory = new File(userHomeDir+"/.webcamstudio");
            for(File f: directory.listFiles()) {
                if(f.getName().startsWith("WSFrom")) {
                    f.delete();
                }
            }
        }
        if (this.getBackFF()){
            this.setComm("FF");
        }
        isStillPicture = false;
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
    public boolean isIPCam() {
        return isIPCam;
    }
    @Override
    public void setIsIPCam(boolean setIsIPCam) {
        isIPCam = setIsIPCam;
    }
    @Override
    public boolean isStillPicture() {
        return isStillPicture;
    }
    @Override
    public void setIsStillPicture(boolean setIsStillPicture) {
        isStillPicture = setIsStillPicture;
    }
    @Override
    public boolean hasAudio() {
        return hasAudio;
    }
    @Override
    public void setHasAudio(boolean setHasAudio) {
        hasAudio = setHasAudio;
    }
    @Override
    public boolean hasVideo() {
        return hasVideo;
    }
    @Override
    public void setHasVideo(boolean setHasVideo) {
        hasVideo = setHasVideo;
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
            if (this.getEffects() != null) {
                for (Effect fxUR : this.getEffects()) {
                    if (f != null && fxUR.needApply()) {   
                        fxUR.applyEffect(f.getImage());
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
