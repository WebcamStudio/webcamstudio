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
    boolean playing = false;
    boolean stop = false;
    Frame frame = null;
    Robot robot = null;
    protected int screenwidth = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
    protected int screenheight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
    int captureX = 0;
    int captureY = 0;
    boolean followMouse = false;
    public SourceDesktop() {
        super();
        name = "Desktop";
    }

    public boolean isFollowingMouse(){
        return followMouse;
    }
    public void setFollowMouse(boolean b){
        followMouse=b;
    }
    private BufferedImage capture() {
        if (followMouse){
            captureX = (int) java.awt.MouseInfo.getPointerInfo().getLocation().getX() - (captureWidth / 2);
            captureY = (int) java.awt.MouseInfo.getPointerInfo().getLocation().getY() - (captureHeight / 2);
        }
        BufferedImage img = robot.createScreenCapture(new Rectangle(captureX, captureY, captureWidth, captureHeight));
        return img;
    }

    @Override
    public void read() {
        stop = false;
        playing=true;
try {
            robot = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(SourceDesktop.class.getName()).log(Level.SEVERE, null, ex);
        }
        Thread capture = new Thread(new Runnable() {

            @Override
            public void run() {
                while (playing) {
                    if (frame != null) {
                        frame.setOutputFormat(x, y, width, height, opacity, volume);
                        frame.setZOrder(zorder);
                        frame.setImage(capture());
                        image = frame.getImage();
                    }
                    try {
                        Thread.sleep(1000/rate);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SourceDesktop.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        capture.setPriority(Thread.MIN_PRIORITY);
        capture.start();        
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
        playing=false;
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
