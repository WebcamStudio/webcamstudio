/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.exporter.vloopback.V4L2Loopback;
import webcamstudio.exporter.vloopback.VideoDevice;
import webcamstudio.exporter.vloopback.VideoOutput;
import webcamstudio.ffmpeg.FFMPEGRenderer;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class SinkLinuxDevice extends Stream {

    VideoOutput device;
    boolean stop = false;
    
    public SinkLinuxDevice(File f,String name) {
        file = f;
        device = new V4L2Loopback(null);
        this.name = name;
    }

    @Override
    public void read() {
        stop=false;
        new Thread(new Runnable(){

            @Override
            public void run() {
                device.open(file.getAbsolutePath(), width, height, VideoOutput.RGB24);
                while (!stop){
                    Frame frame = MasterMixer.getCurrentFrame();
                    if (frame!=null){
                    BufferedImage image = frame.getImage();
                    if (image!=null){
                        int[] imgData = ((java.awt.image.DataBufferInt) image.getRaster().getDataBuffer()).getData();
                        device.write(imgData);
                    }
                    }
                    try {
                        Thread.sleep(1000/rate);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SinkLinuxDevice.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }

    @Override
    public void stop() {
        stop=true;
        device.close();
        device=null;
    }

    @Override
    public boolean isPlaying() {
        return false;
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
        return false;
    }

    @Override
    public boolean hasVideo() {
        return true;
    }
}
