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
import webcamstudio.mixers.PreviewFrameBuilder;

/**
 *
 * @author patrick
 */
public class SourceCustom extends Stream {

    ProcessRenderer capture = null;
    BufferedImage lastPreview = null;
    boolean isPlaying = false;

    public SourceCustom(File custom) {
        super();
        file = custom;
        name = file.getName();
    }

    @Override
    public void read() {
        isPlaying = true;
        lastPreview = new BufferedImage(captureWidth, captureHeight, BufferedImage.TYPE_INT_ARGB);
        rate = MasterMixer.getInstance().getRate();
        if (getPreView()){
            PreviewFrameBuilder.register(this);
        } else {
            MasterFrameBuilder.register(this);
        }
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "custom", comm);
        capture.readCustom();
    }

    @Override
    public void pause() {
        capture.pause();
    }
    
    @Override
    public void stop() {
        isPlaying = false;
        if (capture != null) {
            capture.stop();
        }
//        if (this.getBackFF()){
//            this.setComm("FF");
//        }
        if (getPreView()){
            PreviewFrameBuilder.unregister(this);
//            MasterFrameBuilder.register(this);
        } else {
            MasterFrameBuilder.unregister(this);
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
    public BufferedImage getPreview() {
        return lastPreview;
    }

    @Override
    public Frame getFrame() {
        return nextFrame;
    }
    
//    @Override
//    public void setFakeVideo(boolean hasIt) {
//        this.hasFakeVideo=hasIt;
//    }
//    
////    @Override
//    public void setFakeAudio(boolean hasIt) {
//        this.hasFakeVideo=hasIt;
//    }
//    
//    @Override
//    public boolean hasFakeVideo(){
//        return this.hasFakeVideo;
//    }
//    @Override
//    public boolean hasFakeAudio(){
//        return this.hasFakeAudio;
//    }
//    @Override
//    public boolean hasVideo() {
//        return hasVideo;
//    }
//    @Override
//    public boolean hasAudio() {
//        return hasAudio;
//    }
//    @Override
//    public void setHasAudio(boolean setHasAudio) {
//        hasAudio = setHasAudio;
//    }
//    @Override
//    public void setHasVideo(boolean setHasVideo) {
//        hasVideo = setHasVideo;
//    }
//    
    @Override
    public void readNext() {
        if (capture != null) {
            nextFrame = capture.getFrame();
            if (nextFrame != null) {
                setAudioLevel(nextFrame);
                lastPreview.getGraphics().drawImage(nextFrame.getImage(), 0, 0, null);
            }
        }
    }

    @Override
    public void play() {
        capture.play();
    }
}
