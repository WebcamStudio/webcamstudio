/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class SourceMicrophone extends Stream {

    private boolean isPlaying = false;
    private TargetDataLine line;
    public SourceMicrophone() {
        super();
        rate = MasterMixer.getInstance().getRate();
        name = "Mic";
    }

    @Override
    public void read() {
        isPlaying = true;
        rate = MasterMixer.getInstance().getRate();
        
        AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class,format); // format is an AudioFormat object
        if (!AudioSystem.isLineSupported(info)) {
            isPlaying=false;
            System.out.println("Microphone not supported");
        }
        // Obtain and open the line.
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            MasterFrameBuilder.register(this);
        } catch (LineUnavailableException ex) {
            isPlaying=false;
        }
        
    }

    @Override
    public void stop() {
        isPlaying = false;
        if (line!=null){
            line.stop();
            line=null;
        }
        MasterFrameBuilder.unregister(this);
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public BufferedImage getPreview() {
        return null;
    }

    @Override
    public Frame getFrame() {
        Frame f = new Frame(uuid,null,null);
        int datasize = 44100*2*2/rate;
        byte[] data = new byte[datasize];
        line.read(data, 0, data.length);
        f.setAudio(data);
        this.setAudioLevel(f);
        f.setOutputFormat(x, y, width, height, opacity, volume);
        f.setZOrder(zorder);
        return f;
    }

    @Override
    public boolean hasAudio() {
        return true;
    }

    @Override
    public boolean hasVideo() {
        return false;
    }
}
