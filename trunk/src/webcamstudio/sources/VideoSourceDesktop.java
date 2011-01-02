/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import webcamstudio.controls.ControlDesktop;

/**
 *
 * @author pballeux
 */
public class VideoSourceDesktop extends VideoSource {

    private Robot robot = null;

    public VideoSourceDesktop() {
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(VideoSourceDesktop.class.getName()).log(Level.SEVERE, null, ex);
        }
        location = "";
        name = "Desktop";
        frameRate = 15;
        showMouseCursor = true;
    }

    @Override
    public void startSource() {
        isPlaying=true;
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                stopMe = false;
                if (outputWidth == 0 && outputHeight == 0) {
                    outputWidth = 320;
                    outputHeight = 240;
                }
                int screenwidth = java.awt.MouseInfo.getPointerInfo().getDevice().getDisplayMode().getWidth();
                int screenheight = java.awt.MouseInfo.getPointerInfo().getDevice().getDisplayMode().getHeight();
                int x = 0;
                int y = 0;
                int mouseX = 0;
                int mouseY = 0;
                while (!stopMe) {
                    try {
                        x = captureAtX;
                        y = captureAtY;
                        if (image == null || (image.getWidth() != captureWidth) || (image.getHeight() != captureHeight)) {
                            image = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(captureWidth, captureHeight, java.awt.image.BufferedImage.TRANSLUCENT);
                        }
                        if (followMouseCursor) {
                            x = (int) java.awt.MouseInfo.getPointerInfo().getLocation().getX() - (captureWidth / 2);
                            y = (int) java.awt.MouseInfo.getPointerInfo().getLocation().getY() - (captureHeight / 2);
                        }
                        if (x < 0) {
                            x = 0;
                        }
                        if (y < 0) {
                            y = 0;
                        }
                        if (x > (screenwidth - captureWidth)) {
                            x = screenwidth - captureWidth - 1;
                        }
                        if (y > (screenheight - captureHeight)) {
                            y = screenheight - captureHeight - 1;
                        }
                        captureAtX = x;
                        captureAtY = y;
                        if (robot != null) {
                            tempimage = graphicConfiguration.createCompatibleImage(captureWidth, captureHeight, java.awt.image.BufferedImage.TRANSLUCENT);
                            java.awt.Graphics2D buffer = tempimage.createGraphics();
                            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 1.0F));
                            buffer.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                            buffer.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
                            buffer.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                            buffer.drawImage(robot.createScreenCapture(new java.awt.Rectangle(captureAtX, captureAtY, captureWidth, captureHeight)), 0, 0, captureWidth, captureHeight, null);
                            
                            if (showMouseCursor) {
                                mouseX = (int) java.awt.MouseInfo.getPointerInfo().getLocation().getX() - captureAtX;
                                mouseY = (int) java.awt.MouseInfo.getPointerInfo().getLocation().getY() - captureAtY;
                                buffer.setXORMode(java.awt.Color.BLACK);
                                buffer.fillOval(mouseX, mouseY, 15, 15);
                            }
                            buffer.dispose();
                            detectActivity(tempimage);
                            applyEffects(tempimage);
                            applyShape(tempimage);
                            image = tempimage;
                        }
                        Thread.sleep(1000 / frameRate);
                    } catch (Exception e) {
                        error("Desktop Error  : " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();

    }

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
        stopMe = true;
        image = null;
        isPlaying=false;
    }

    public boolean hasText() {
        return false;
    }

    public String toString() {
        return "Desktop: " + "(" + captureAtX + "," + captureAtY + ":" + captureWidth + "x" + captureHeight + ")";
    }

    @Override
    public java.util.Collection<JPanel> getControls() {
        java.util.Vector<JPanel> list = new java.util.Vector<JPanel>();
        list.add(new ControlDesktop(this));
        list.add(new webcamstudio.controls.ControlShapes(this));
        list.add(new webcamstudio.controls.ControlEffects(this));
        list.add(new webcamstudio.controls.ControlActivity(this));
        list.add(new webcamstudio.controls.ControlLayout(this));
        return list;
    }
}
