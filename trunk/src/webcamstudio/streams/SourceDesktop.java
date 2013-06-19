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
import webcamstudio.externals.ProcessRenderer;
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

    ProcessRenderer capture = null;
    Robot defaultCapture = null;
    Frame frame = null;
    boolean isPlaying = false;
    boolean stop = false;
    OS os = Tools.getOS();
    long timeCode = 0;
    Rectangle area = null;
    BufferedImage lastPreview = null;

    public SourceDesktop() {
        super();
        name = "Desktop";
        rate = MasterMixer.getInstance().getRate();
    }

    @Override
    public void read() {
        stop = false;
        isPlaying = true;
        rate = MasterMixer.getInstance().getRate();
        lastPreview = new BufferedImage(captureWidth,captureHeight,BufferedImage.TYPE_INT_ARGB);
        MasterFrameBuilder.register(this);
        if (os == OS.LINUX) {
            capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "desktop");
            capture.read();
        } else {
            try {
                defaultCapture = new Robot();
                frame = new Frame(desktopW, desktopH, rate);
                frame.setID(uuid);
                area = new Rectangle(desktopX, desktopY, desktopW, desktopH);
                frame.setOutputFormat(x, y, width, height, opacity, volume);
                frame.setZOrder(zorder);
            } catch (AWTException ex) {
                Logger.getLogger(SourceDesktop.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void stop() {
        stop = true;
        isPlaying = false;
        if (capture != null) {
            capture.stop();
            capture = null;
        }
        MasterFrameBuilder.unregister(this);
//        lastPreview=null;
    }
    @Override
    public boolean needSeek() {
            return needSeekCTRL=false;
    }

    @Override
    public Frame getFrame() {
       return nextFrame;
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

    @Override
    public void readNext() {
         if (capture != null) {
            nextFrame = capture.getFrame();
            if (nextFrame != null) {
                lastPreview = nextFrame.getImage();
            }
        } else if (defaultCapture != null) {
            frame.setImage(defaultCapture.createScreenCapture(area));
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
            nextFrame=frame;
            lastPreview = frame.getImage();
            
        } 
    }
}
