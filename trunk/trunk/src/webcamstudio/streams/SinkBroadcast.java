/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import webcamstudio.ffmpeg.FFMPEGRenderer;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class SinkBroadcast extends Stream {

    private FFMPEGRenderer capture = null;
    
    public SinkBroadcast(String url,String name) {
        this.url=url;
        this.name=name;
    }
    @Override
    public String getName(){
        return name;
    }
    @Override
    public void read() {
        rate = MasterMixer.getInstance().getRate();
        capture = new FFMPEGRenderer(this,FFMPEGRenderer.ACTION.OUTPUT,"broadcast");
        capture.write();
    }

    @Override
    public void stop() {
        if  (capture!=null){
            capture.stop();
            capture=null;
        }
    }

    @Override
    public boolean isPlaying() {
        return capture!=null && !capture.isStopped();
    }

    @Override
    public BufferedImage getPreview() {
        return null;
    }

    @Override
    public boolean hasAudio() {
        return true;
    }

    @Override
    public boolean hasVideo() {
        return true;
    }
}
