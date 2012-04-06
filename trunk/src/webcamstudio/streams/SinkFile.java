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
public class SinkFile extends Stream {

    private FFMPEGRenderer capture = null;
    private String name = "sink";
    public SinkFile(File f) {
        capture = new FFMPEGRenderer(uuid,"outputfile");
        capture.updateFormat(x, y, width, height, opacity, volume);
        capture.setFile(f);
        name = f.getName();
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
