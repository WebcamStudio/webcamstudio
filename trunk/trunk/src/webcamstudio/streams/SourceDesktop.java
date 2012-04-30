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
    OS os = Tools.getOS();
    long timeCode = 0;

    public SourceDesktop() {
        super();
        name = "Desktop";
        rate = MasterMixer.getInstance().getRate();
    }

    @Override
    public void read() {
        isPlaying = true;
        rate = MasterMixer.getInstance().getRate();
        lastPreview = new BufferedImage(captureWidth, captureHeight, BufferedImage.TYPE_INT_ARGB);
        MasterFrameBuilder.register(this);
        if (os == OS.LINUX) {
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
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                timeCode = System.currentTimeMillis();
                long timeDelta = 1000/rate;
                while (isPlaying) {
                    timeCode += timeDelta;
                    if (os == OS.LINUX) {
                        frame = capture.getFrame();
                        if (frame!=null){
                            lastPreview = frame.getImage();
                        }
                    } else {
                        if (frame != null) {
                            frame.setOutputFormat(x, y, width, height, opacity, volume);
                            frame.setZOrder(zorder);
                            frame.setImage(defaultCapture.createScreenCapture(new Rectangle(desktopX, desktopY, desktopW, desktopH)));
                            lastPreview = frame.getImage();
                            Tools.sleep(990/rate);
                        }
                    }
                    Tools.sleep(10);
                }
            }
        });
        t.start();
    }

    @Override
    public void stop() {
        isPlaying = false;
        if (capture != null) {
            capture.stop();
            capture=null;
        }
        MasterFrameBuilder.unregister(this);
    }

    @Override
    public Frame getFrame() {
        return frame;
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
