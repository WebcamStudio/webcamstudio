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
import webcamstudio.sources.effects.Effect;
import webcamstudio.util.Screen;
import webcamstudio.util.Tools;
import webcamstudio.util.Tools.OS;

/**
 *
 * @author patrick (modified by karl)
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
    protected String[] screenID = Screen.getSources();
    
    public SourceDesktop() {
        super();
        name = "Desktop";
        rate = this.getRate();
        desktopW = Screen.getWidth(screenID[0]);
        desktopH = Screen.getHeight(screenID[0]);
    }

    @Override
    public void pause() {
        capture.pause();
    }
    
    @Override
    public void read() {
        stop = false;
        isPlaying = true;
        rate = this.getRate();
        lastPreview = new BufferedImage(captureWidth,captureHeight,BufferedImage.TYPE_INT_ARGB);
        MasterFrameBuilder.register(this);
        if (os == OS.LINUX) {
            capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "desktop", comm);
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
        if (this.getBackFF()){
            this.setComm("FF");
        }
        MasterFrameBuilder.unregister(this);
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
    public void setIsPlaying(boolean setIsPlaying) {
        isPlaying = setIsPlaying;
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
            if (this.getEffects() != null) {
                for (Effect fxD : this.getEffects()) {
                    if (fxD.needApply() && nextFrame != null){   
                        fxD.applyEffect(nextFrame.getImage());
                    }
                }
            }
            if (nextFrame != null) {
                lastPreview = nextFrame.getImage();
            }
        } else if (defaultCapture != null) {
            frame.setImage(defaultCapture.createScreenCapture(area));
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
            applyEffects(frame.getImage());
            nextFrame=frame;
            lastPreview = frame.getImage();
        } 
    }

    @Override
    public void play() {
        capture.play();
    }
}
