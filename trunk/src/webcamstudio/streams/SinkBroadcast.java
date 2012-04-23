/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.ffmpeg.FFMPEGRenderer;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class SinkBroadcast extends Stream {

    private FFMPEGRenderer capture = null;
    
    public SinkBroadcast(String url,String name) {
        try {
            this.url=new URL(url);
        } catch (MalformedURLException ex) {
            Logger.getLogger(SinkBroadcast.class.getName()).log(Level.SEVERE, null, ex);
        }
        
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
