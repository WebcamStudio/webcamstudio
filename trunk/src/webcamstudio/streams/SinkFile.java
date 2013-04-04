/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import webcamstudio.externals.ProcessRenderer;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class SinkFile extends Stream {

    private ProcessRenderer capture = null;

    public SinkFile(File f) {
        file = f;
        name = f.getName();

    }

    @Override
    public void read() {
        rate = MasterMixer.getInstance().getRate();
        captureWidth = MasterMixer.getInstance().getWidth();
        captureHeight = MasterMixer.getInstance().getHeight();
        rate = MasterMixer.getInstance().getRate();
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.OUTPUT, "file");
        capture.write();
    }

    @Override
    public void stop() {
        if (capture != null) {
            capture.stop();
            capture = null;
        }
    }
    @Override
    public void fakeStop() {
            capture.fakeStop();
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
        return null;
    }

    @Override
    public Frame getFrame() {
        return null;
    }

    @Override
    public boolean hasAudio() {
        return true;
    }

    @Override
    public boolean hasVideo() {
        return true;
    }

    @Override
    public void readNext() {
        
    }
}
