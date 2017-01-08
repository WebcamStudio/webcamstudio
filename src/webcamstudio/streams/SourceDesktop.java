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
import webcamstudio.mixers.PreviewFrameBuilder;
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

    protected String desktopN = "0";
    protected int deskN = 0;
    protected int desktopX = 0;
    protected int desktopY = 0;
    protected int desktopW = 0;
    protected int desktopH = 0;
    protected int windowX = 0;
    protected int windowY = 0;
    protected int windowW = 0;
    protected int windowH = 0;
    protected String desktopXid = "";
    protected String elementXid = "";
    protected boolean singleWindow = false;

    public SourceDesktop() {
        super();
        name = "Desktop";
        rate = MasterMixer.getInstance().getRate();
        desktopW = Screen.getWidth(screenID[0]);
        desktopH = Screen.getHeight(screenID[0]);
        windowX = Screen.getX(screenID[0]);
        windowY = Screen.getY(screenID[0]);
        windowW = Screen.getWidth(screenID[0]);
        windowH = Screen.getHeight(screenID[0]);
    }

    @Override
    public void pause() {
        isPaused = true;
        capture.pause();
    }

    @Override
    public void read() {
        stop = false;
        isPlaying = true;
        rate = this.getRate();
        lastPreview = new BufferedImage(captureWidth,captureHeight,BufferedImage.TYPE_INT_ARGB);
        if (getPreView()){
            PreviewFrameBuilder.register(this);
        } else {
            MasterFrameBuilder.register(this);
        }
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
        for (int fx = 0; fx < this.getEffects().size(); fx++) {
            Effect fxT = this.getEffects().get(fx);
            if (fxT.getName().endsWith("Stretch") || fxT.getName().endsWith("Crop")) {
                // do nothing.
            } else {
                fxT.resetFX();
            }
        }
        stop = true;
        isPlaying = false;
        if (capture != null) {
            capture.stop();
            capture = null;
        }
        if (this.getBackFF()){
            this.setComm("FF");
        }
        if (getPreView()){
            PreviewFrameBuilder.unregister(this);
        } else {
            MasterFrameBuilder.unregister(this);
        }
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
        Frame f = null;
        if (capture != null) {
            f = capture.getFrame();
            if (f != null) {
                BufferedImage img = f.getImage();
                applyEffects(img);
            }
            if (f != null) {
                lastPreview = f.getImage();
            }
        } else if (defaultCapture != null) {
            f.setImage(defaultCapture.createScreenCapture(area));
            f.setOutputFormat(x, y, width, height, opacity, volume);
            f.setZOrder(zorder);
            applyEffects(f.getImage());
            lastPreview = f.getImage();
        }
        nextFrame=f;
    }

    @Override
    public void play() {
        isPaused = false;
        capture.play();
    }

    public String getDesktopN() {
        return desktopN;
    }

    public void setDesktopN(String desktopN) {
        this.desktopN = desktopN;
    }

    public int getDeskN() {
        return deskN;
    }

    public void setDeskN(int desktopN) {
        this.deskN = desktopN;
    }

    public int getDesktopX() {
        return desktopX;
    }

    public String getDesktopXid() {
        return desktopXid;
    }

    public void setDesktopXid(String dXid) {
        this.desktopXid = dXid;
    }

    public String getElementXid() {
        return elementXid;
    }

    public void setElementXid(String eXid) {
        this.elementXid = eXid;
    }

    public boolean getSingleWindow() {
        return singleWindow;
    }

    public void setSingleWindow(boolean sWin) {
        this.singleWindow = sWin;
    }

    /**
     * @param desktopX the desktopX to set
     */
    public void setDesktopX(int desktopX) {
        this.desktopX = desktopX;
    }

    /**
     * @return the desktopY
     */
    public int getDesktopY() {
        return desktopY;
    }

    /**
     * @param desktopY the desktopY to set
     */
    public void setDesktopY(int desktopY) {
        this.desktopY = desktopY;
    }

    public int getDesktopEndX(){
        return desktopX + desktopW - 1;
    }

    public int getDesktopEndY(){
        return desktopY + desktopH - 1;
    }

    /**
     * @return the desktopW
     */
    public int getDesktopW() {
        return desktopW;
    }

    /**
     * @param desktopW the desktopW to set
     */
    public void setDesktopW(int desktopW) {
        this.desktopW = desktopW;
    }

    /**
     * @return the desktopH
     */
    public int getDesktopH() {
        return desktopH;
    }

    /**
     * @param desktopH the desktopH to set
     */
    public void setDesktopH(int desktopH) {
        this.desktopH = desktopH;
    }

    public void setWindowX(int windowX) {
        this.windowX = windowX;
    }

    public int getWindowX() {
        return this.windowX;
    }

    /**
     * @param windowY
     */
    public void setWindowY(int windowY) {
        this.windowY = windowY;
    }

    /**
     * @return the desktopY
     */
    public int getWindowY() {
        return this.windowY;
    }

    public int getWindowEndX(){
        return windowX + windowW - 1;
    }

    public int getWindowEndY(){
        return windowY + windowH - 1;
    }

    /**
     * @return the desktopW
     */
    public int getWindowW() {
        return windowW;
    }

    /**
     * @param windowW
     */
    public void setWindowW(int windowW) {
        this.windowW = windowW;
    }

    /**
     * @return the desktopH
     */
    public int getWindowH() {
        return windowH;
    }

    /**
     * @param windowH
     */
    public void setWindowH(int windowH) {
        this.windowH = windowH;
    }
}
