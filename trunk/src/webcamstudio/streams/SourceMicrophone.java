/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import webcamstudio.mixers.AudioBuffer;
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
    private AudioBuffer audioBuffer = null;
    private Frame frame = null;

    public SourceMicrophone() {
        super();
        rate = MasterMixer.getInstance().getRate();
        name = "Microphone";
    }

    @Override
    public void read() {
        isPlaying = true;
        rate = MasterMixer.getInstance().getRate();
        audioBuffer = new AudioBuffer(rate);

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // format is an AudioFormat object
                if (!AudioSystem.isLineSupported(info)) {
                    isPlaying = false;
                    System.out.println("Microphone not supported");
                }
                try {
                    line = (TargetDataLine) AudioSystem.getLine(info);
                    line.open(format);
                    line.start();
                    frame = new Frame(uuid, null, null);
                    audioBuffer.clear();
                    //long mark = 0;
                    while (isPlaying && line.isOpen()) {
                        //mark= System.currentTimeMillis();
                        byte[] data = audioBuffer.getAudioToUpdate();
                        line.read(data, 0, data.length);
                        audioBuffer.doneUpdate();
                        //System.out.println("delta= " + (System.currentTimeMillis()-mark));
                    }
                } catch (Exception e) {
                    isPlaying = false;
                    stop();
                }
                frame = null;
            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
        MasterFrameBuilder.register(this);
    }

    @Override
    public void stop() {
        isPlaying = false;
        audioBuffer.abort();
        if (line != null) {
            line.stop();
            line.close();
            line = null;
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
        if (frame != null) {
            frame.setAudio(audioBuffer.pop());
            this.setAudioLevel(frame);
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
        }
        return frame;
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
