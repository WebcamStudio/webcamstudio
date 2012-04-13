/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import webcamstudio.ffmpeg.FFMPEGRenderer;
import webcamstudio.mixers.Frame;

/**
 *
 * @author patrick
 */
public class SinkBroadcast extends Stream {

    private FFMPEGRenderer capture = null;
    public SinkBroadcast() {
        capture = new FFMPEGRenderer(this,FFMPEGRenderer.ACTION.OUTPUT,"broadcast");
        name = "Justin.tv";
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
        if (frames.size()>0){
            return frames.get(0).getImage();
        } else {
            return null;
        }
    }

    @Override
    public Frame getFrame() {
        return null;
    }
}
