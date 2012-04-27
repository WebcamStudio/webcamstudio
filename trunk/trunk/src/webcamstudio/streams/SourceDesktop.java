/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import webcamstudio.ffmpeg.FFMPEGRenderer;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class SourceDesktop extends Stream {

    FFMPEGRenderer capture = null;
    BufferedImage lastPreview = null;
    boolean isPlaying = false;
    boolean followMouse = false;

    public SourceDesktop() {
        super();
        name = "Desktop";
        rate = MasterMixer.getInstance().getRate();
    }

    public boolean isFollowingMouse() {
        return followMouse;
    }

    public void setFollowMouse(boolean b) {
        followMouse = b;
    }

    @Override
    public void read() {
        isPlaying=true;
        rate = MasterMixer.getInstance().getRate();
        MasterFrameBuilder.register(this);
        capture = new FFMPEGRenderer(this, FFMPEGRenderer.ACTION.CAPTURE, "desktop");
        capture.read();
    }

    @Override
    public void stop() {
       isPlaying=false;
        if (capture != null) {
            capture.stop();
        }
        MasterFrameBuilder.unregister(this);
    }

    @Override
    public Frame getFrame() {
        Frame f = null;
        if (capture != null) {
            f = capture.getFrame();
            if (f != null) {
                lastPreview = f.getImage();
            }
        }
        return f;
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
    public boolean hasAudio() {
        return false;
    }

    @Override
    public boolean hasVideo() {
        return true;
    }
}
