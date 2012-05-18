/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import webcamstudio.externals.ProcessRenderer;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class SourceCustom extends Stream {

    ProcessRenderer capture = null;
    BufferedImage lastPreview = null;
    boolean isPlaying = false;

    public SourceCustom(File custom) {
        super();
        file = custom;
        name = file.getName();
    }

    @Override
    public void read() {
        isPlaying = true;
        lastPreview = new BufferedImage(captureWidth, captureHeight, BufferedImage.TYPE_INT_ARGB);
        rate = MasterMixer.getInstance().getRate();
        MasterFrameBuilder.register(this);
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "custom");
        capture.read();
    }

    @Override
    public void stop() {
        isPlaying = false;
        if (capture != null) {
            capture.stop();
        }
        MasterFrameBuilder.unregister(this);
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public BufferedImage getPreview() {
        return lastPreview;
    }

    @Override
    public Frame getFrame() {
        return nextFrame;
    }

    @Override
    public void readNext() {
        if (capture != null) {
            nextFrame = capture.getFrame();
            if (nextFrame != null) {
                setAudioLevel(nextFrame);
                lastPreview.getGraphics().drawImage(nextFrame.getImage(), 0, 0, null);
            }
        }
    }
}
