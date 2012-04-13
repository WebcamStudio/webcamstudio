/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import webcamstudio.ffmpeg.FFMPEGRenderer;
import webcamstudio.mixers.MasterFrameBuilder;

/**
 *
 * @author patrick
 */
public class SourceWebcam extends Stream{

    FFMPEGRenderer capture = null;
    public SourceWebcam(File device){
       capture = new FFMPEGRenderer(this,FFMPEGRenderer.ACTION.CAPTURE,"webcam");
       file=device;
       name = device.getName();
       
    }

    @Override
    public void read() {
        MasterFrameBuilder.register(this);
        capture.read();
    }

    @Override
    public void stop() {
        capture.stop();
        MasterFrameBuilder.unregister(this);
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
    
}
