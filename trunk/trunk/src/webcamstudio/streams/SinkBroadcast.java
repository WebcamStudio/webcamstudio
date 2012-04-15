/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import webcamstudio.ffmpeg.FFMPEGRenderer;

/**
 *
 * @author patrick
 */
public class SinkBroadcast extends Stream {

    private FFMPEGRenderer capture = null;
    
    public SinkBroadcast(String url,String name) {
        this.url=url;
        capture = new FFMPEGRenderer(this,FFMPEGRenderer.ACTION.OUTPUT,"broadcast");
        this.name=name;
    }
    @Override
    public String getName(){
        return name;
    }
    @Override
    public void read() {
        capture.write();
    }

    @Override
    public void stop() {
        capture.stop();
    }

    @Override
    public boolean isPlaying() {
        return !capture.isStopped();
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
