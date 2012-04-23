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
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;

/**
 *
 * @author patrick
 */
public class SourceDesktop extends Stream {

    BufferedImage image = null;
    boolean playing = true;
    boolean stop = false;
    Frame frame = null;
    Robot robot = null;
    protected int screenwidth = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
    protected int screenheight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;

    public SourceDesktop() {
        super();
        name = "Desktop";
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(SourceDesktop.class.getName()).log(Level.SEVERE, null, ex);
        }
        Thread capture = new Thread(new Runnable() {

            @Override
            public void run() {
                while (1 == 1) {
                    if (frame != null) {
                        frame.setOutputFormat(x, y, width, height, opacity, volume);
                        frame.setZOrder(zorder);
                        frame.setImage(capture());
                        image = frame.getImage();
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SourceDesktop.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        capture.setPriority(Thread.MIN_PRIORITY);
        capture.start();

    }

    private BufferedImage capture() {
        int mouseX = (int) java.awt.MouseInfo.getPointerInfo().getLocation().getX() - (captureWidth / 2);
        int mouseY = (int) java.awt.MouseInfo.getPointerInfo().getLocation().getY() - (captureHeight / 2);
        BufferedImage img = robot.createScreenCapture(new Rectangle(mouseX, mouseY, captureWidth, captureHeight));
        return img;
    }

    @Override
    public void read() {
        stop = false;
        try {
            frame = new Frame(uuid, image, null);
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
            MasterFrameBuilder.register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        stop = true;
        MasterFrameBuilder.unregister(this);
    }

    @Override
    public Frame getFrame() {
        return frame;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public BufferedImage getPreview() {
        return image;
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
