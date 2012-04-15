/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import webcamstudio.ffmpeg.FFMPEGRenderer;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;

/**
 *
 * @author patrick
 */
public class SourceWebcam extends Stream {

    FFMPEGRenderer capture = null;
    BufferedImage lastPreview = null;

    public SourceWebcam(File device) {
        capture = new FFMPEGRenderer(this, FFMPEGRenderer.ACTION.CAPTURE, "webcam");
        file = device;
        name = device.getName();

    }

    @Override
    public void read() {
        MasterFrameBuilder.register(this);
        capture.read();
    }

    @Override
    public void stop() {
        System.out.println("BeforeStop");
        capture.stop();
        System.out.println("AfterStop");
        MasterFrameBuilder.unregister(this);
        System.out.println("AfterUnregister");
    }

    @Override
    public boolean isPlaying() {
        return !capture.isStopped();
    }

    @Override
    public BufferedImage getPreview() {
        return lastPreview;
    }

    
    @Override
    public Frame getFrame() {
        Frame f = capture.getFrame();
        if (f != null) {
            setAudioLevel(f);
            lastPreview = f.getImage();
        }
        return f;
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
