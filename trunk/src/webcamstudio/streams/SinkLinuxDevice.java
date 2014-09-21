/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import webcamstudio.exporter.vloopback.V4L2Loopback;
import webcamstudio.exporter.vloopback.VideoOutput;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class SinkLinuxDevice extends Stream implements MasterMixer.SinkListener {

    VideoOutput device;
    boolean stop = false;

    public SinkLinuxDevice(File f, String name) {
        file = f;
        device = new V4L2Loopback(null);
        this.name = name;
    }

    @Override
    public void read() {
        stop = false;
        rate = MasterMixer.getInstance().getRate();
        device.open(file.getAbsolutePath(), width, height, VideoOutput.RGB24);
        MasterMixer.getInstance().register(this);
    }

    @Override
    public void pause() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void stop() {
        stop = true;
        if (device != null) {
            device.close();
            device = null;
        }
        MasterMixer.getInstance().unregister(this);
    }
    @Override
    public boolean needSeek() {
            return needSeekCTRL=false;
    }

    @Override
    public boolean isPlaying() {
        return !stop;
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

    @Override
    public void newFrame(Frame frame) {
            if (frame != null) {
                BufferedImage image = frame.getImage();
                if (image != null) {
                    int[] imgData = ((java.awt.image.DataBufferInt) image.getRaster().getDataBuffer()).getData();
                    if (device != null) {
                        device.write(imgData);
                    }
                }
            }
        }

    @Override
    public void readNext() {
        
    }

    @Override
    public void play() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
