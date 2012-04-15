/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import webcamstudio.ffmpeg.FFMPEGRenderer;
import webcamstudio.mixers.Frame;

/**
 *
 * @author patrick
 */
public class SinkFile extends Stream {

    private FFMPEGRenderer capture = null;

    public SinkFile(File f) {
        capture = new FFMPEGRenderer(this, FFMPEGRenderer.ACTION.OUTPUT, "file");
        file = f;
        name = f.getName();
    }

    @Override
    public void read() {
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
}
