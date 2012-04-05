/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pballeux
 */
public class VideoSourceDesktop extends VideoSource {

    protected Robot robot = null;
    protected int screenwidth = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
    protected int screenheight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
    protected int frameCount = 0;

    public VideoSourceDesktop() {
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(VideoSourceDesktop.class.getName()).log(Level.SEVERE, null, ex);
        }
        name = "Desktop";
        location = name;
        frameRate = 15;
        showMouseCursor = true;
    }

    @Override
    public void startSource() {
        isPlaying = true;
        stopMe = false;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer(name, true);
        timer.scheduleAtFixedRate(new imageDesktop(this), 0, 1000 / frameRate);
    }

    @Override
    public void setFrameRate(int r) {
        frameRate = r;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer(name, true);
        timer.scheduleAtFixedRate(new imageDesktop(this), 0, 1000 / frameRate);

    }

    @Override
    public boolean canUpdateSource() {
        return false;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void pause() {
    }

    @Override
    public void play() {
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public void stopSource() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        stopMe = true;
        image = null;
        isPlaying = false;
    }

    public boolean hasText() {
        return false;
    }

    public String toString() {
        return "Desktop: " + "(" + captureAtX + "," + captureAtY + ":" + captureWidth + "x" + captureHeight + ")";
    }
}

class imageDesktop extends TimerTask {

    private Robot robot = null;
    VideoSourceDesktop desktop = null;

    public imageDesktop(VideoSourceDesktop d) {
        robot = d.robot;
        desktop = d;
    }

    @Override
    public void run() {
        if (desktop.getOutputWidth() == 0 && desktop.getOutputHeight() == 0) {
            desktop.setOutputWidth(320);
            desktop.setOutputHeight(240);
        }
        int x = 0;
        int y = 0;
        int mouseX = 0;
        int mouseY = 0;
        x = desktop.getCaptureAtX();
        y = desktop.getCaptureAtY();
        if (desktop.getImage() == null || (desktop.image.getWidth() != desktop.getCaptureWidth()) || (desktop.image.getHeight() != desktop.getCaptureHeight())) {
            desktop.image = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(desktop.getCaptureWidth(), desktop.getCaptureHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        }
        if (desktop.isFollowingMouse()) {
            x = (int) java.awt.MouseInfo.getPointerInfo().getLocation().getX() - (desktop.getCaptureWidth() / 2);
            y = (int) java.awt.MouseInfo.getPointerInfo().getLocation().getY() - (desktop.getCaptureHeight() / 2);
        }
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (x > (desktop.screenwidth - desktop.captureWidth)) {
            x = desktop.screenwidth - desktop.captureWidth - 1;
        }
        if (y > (desktop.screenheight - desktop.captureHeight)) {
            y = desktop.screenheight - desktop.captureHeight - 1;
        }
        desktop.captureAtX = x;
        desktop.captureAtY = y;
        if (robot != null) {
            desktop.tempimage = new BufferedImage(desktop.captureWidth, desktop.captureHeight, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D buffer = desktop.tempimage.createGraphics();
            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 1.0F));
            buffer.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            buffer.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
            buffer.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            buffer.drawImage(robot.createScreenCapture(new java.awt.Rectangle(desktop.captureAtX, desktop.captureAtY, desktop.captureWidth, desktop.captureHeight)), 0, 0, desktop.captureWidth, desktop.captureHeight, null);

            if (desktop.showMouseCursor) {
                mouseX = (int) java.awt.MouseInfo.getPointerInfo().getLocation().getX() - desktop.captureAtX;
                mouseY = (int) java.awt.MouseInfo.getPointerInfo().getLocation().getY() - desktop.captureAtY;
                buffer.setXORMode(java.awt.Color.BLACK);
                buffer.fillOval(mouseX, mouseY, 15, 15);
            }
            buffer.dispose();
            desktop.detectActivity(desktop.tempimage);
            desktop.applyEffects(desktop.tempimage);
            desktop.applyShape(desktop.tempimage);
            desktop.image = desktop.tempimage;
        }
    }
}
