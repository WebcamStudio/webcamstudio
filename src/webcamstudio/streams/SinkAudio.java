/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import static webcamstudio.WebcamStudio.outFFmpeg;
import webcamstudio.externals.ProcessRenderer;
import webcamstudio.mixers.Frame;
//import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author karl
 */
public class SinkAudio extends Stream {

    private ProcessRenderer capture = null;

    public SinkAudio() {
        name = "AudioOut";
        if (outFFmpeg){
            this.setComm("FF");
        } else {
            this.setComm("AV");
        }
    }

    @Override
    public void read() {
//        rate = MasterMixer.getInstance().getRate();
//        captureWidth = MasterMixer.getInstance().getWidth();
//        captureHeight = MasterMixer.getInstance().getHeight();
        //        rate = MasterMixer.getInstance().getRate();
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.OUTPUT, "spkAudioOut", comm); //"spkAudioOut"
        capture.writeCom();
    }

    @Override
    public void pause() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void stop() {
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
        if (capture != null) {
            return !capture.isStopped();
        } else {
            return false;
        }
    }

    @Override
    public BufferedImage getPreview() {
        return null;
    }
    
    @Override
    public Frame getFrame() {
        return null;
    }

    @Override
    public boolean hasAudio() {
        return true;
    }

    @Override
    public boolean hasVideo() {
        return false;
    }

    @Override
    public void readNext() {
        
    }

    @Override
    public void play() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
