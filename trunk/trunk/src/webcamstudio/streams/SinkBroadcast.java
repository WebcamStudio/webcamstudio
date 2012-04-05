/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import webcamstudio.ffmpeg.FFMPEGRenderer;

/**
 *
 * @author patrick
 */
public class SinkBroadcast extends Stream {

    private FFMPEGRenderer capture = null;
    private String name = "sink";
    public SinkBroadcast() {
        capture = new FFMPEGRenderer("broadcast");
        capture.updateFormat(x, y, width, height, opacity, volume);
        name = "Justin.tv";
    }
    @Override
    public String getName(){
        return name;
    }
    @Override
    public void read() {
        capture.updateFormat(x, y, width, height, opacity, volume);
        capture.setRate(rate);
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
        return capture.getPreview();
    }
}
