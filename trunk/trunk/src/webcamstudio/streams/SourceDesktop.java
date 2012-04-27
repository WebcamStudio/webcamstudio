/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.ffmpeg.FFMPEGRenderer;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.util.Tools;
import webcamstudio.util.Tools.OS;

/**
 *
 * @author patrick
 */
public class SourceDesktop extends Stream {

    FFMPEGRenderer capture = null;
    Robot defaultCapture = null;
    Frame frame = null;
    BufferedImage lastPreview = null;
    boolean isPlaying = false;

    public SourceDesktop() {
        super();
        name = "Desktop";
        rate = MasterMixer.getInstance().getRate();
    }

    @Override
    public void read() {
        isPlaying = true;
        rate = MasterMixer.getInstance().getRate();
        MasterFrameBuilder.register(this);
        if (Tools.getOS() == OS.LINUX) {
            capture = new FFMPEGRenderer(this, FFMPEGRenderer.ACTION.CAPTURE, "desktop");
            capture.read();
        } else {
            try {
                defaultCapture = new Robot();
                frame = new Frame(uuid, null, null);
                frame.setOutputFormat(x, y, width, height, opacity, volume);
                frame.setZOrder(zorder);
            } catch (AWTException ex) {
                Logger.getLogger(SourceDesktop.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
    public Frame getFrame() {
        Frame f = null;
        if (capture != null) {
            f = capture.getFrame();
            if (f != null) {
                lastPreview = f.getImage();
            }
        } else {
            if (frame != null) {
                f = frame;
                frame.setOutputFormat(x, y, width, height, opacity, volume);
                frame.setZOrder(zorder);
                frame.setImage(defaultCapture.createScreenCapture(new Rectangle(desktopX, desktopY, desktopW, desktopH)));
                lastPreview = frame.getImage();
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
