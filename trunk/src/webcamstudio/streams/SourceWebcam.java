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
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class SourceWebcam extends Stream {

    FFMPEGRenderer capture = null;
    BufferedImage lastPreview = null;

    public SourceWebcam(File device) {
        super();
        rate = MasterMixer.getInstance().getRate();
        file = device;
        name = device.getName();


    }

    public SourceWebcam(String defaultName) {
        super();
        rate = MasterMixer.getInstance().getRate();
        name = defaultName;
    }

    @Override
    public void read() {
        MasterFrameBuilder.register(this);
        capture = new FFMPEGRenderer(this, FFMPEGRenderer.ACTION.CAPTURE, "webcam");
        capture.read();
    }

    @Override
    public void stop() {
        if (capture != null) {
            capture.stop();
        }
        MasterFrameBuilder.unregister(this);
    }

    @Override
    public boolean isPlaying() {
        if (capture != null) {
            return !capture.isStopped();
        } else {
            return false;
        }
    }

    @Override
    public BufferedImage getPreview() {
        return lastPreview;
    }

    @Override
    public Frame getFrame() {
        Frame f = null;
        if (capture != null) {
            f = capture.getFrame();
            if (f != null) {
                setAudioLevel(f);
                lastPreview = f.getImage();
            }
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
