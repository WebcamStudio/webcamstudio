/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import webcamstudio.layout.Layout;
import webcamstudio.layout.LayoutItem;
import webcamstudio.mixers.VideoMixer;

/**
 *
 * @author patrick
 */
public class VideoSourceLayout extends VideoSource {


    public VideoSourceLayout(String layoutUUID) {
        location = layoutUUID;
        if (Layout.getLayouts().containsKey(layoutUUID)) {
            name = Layout.getLayouts().get(layoutUUID).toString();
        }
    }

    @Override
    public void startSource() {
        //Find the layout to use...
        isPlaying = true;
        stopMe = false;
        if (Layout.getLayouts().containsKey(location)) {
            Layout layout = Layout.getLayouts().get(location);
            name = layout.toString();
            if (!layout.isActive()) {
                layout.enterLayout(true);
            }

            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            timer = new Timer(name, true);
            frameRate=VideoMixer.getInstance().getFrameRate();
            timer.scheduleAtFixedRate(new LayoutImage(this), 0, 1000/frameRate);
        } else {
            System.out.println("Could not find layout");
        }
    }

    @Override
    public void stopSource() {
        stopMe = true;
        if (Layout.getLayouts().containsKey(location)) {
            Layout.getLayouts().get(location).exitLayout(true);
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        isPlaying = false;
    }

    @Override
    public boolean canUpdateSource() {
        return false;
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public boolean isPaused() {
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
}

class LayoutImage extends TimerTask {

    VideoSourceLayout source = null;

    public LayoutImage(VideoSourceLayout source) {
        this.source = source;
    }

    @Override
    public void run() {
        if (Layout.getLayouts().containsKey(source.location)) {
            Layout layout = Layout.getLayouts().get(source.location);
            source.captureHeight = VideoMixer.getInstance().getHeigt();
            source.captureWidth = VideoMixer.getInstance().getWidth();
            source.tempimage = new BufferedImage(source.captureWidth, source.captureHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D buffer = source.tempimage.createGraphics();
            for (LayoutItem item : layout.getItems()) {
                Image image = item.getSource().getImage();
                if (image != null) {
                    buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, ((float) item.getSource().getOpacity()) / 100F));
                    buffer.drawImage(image, item.getSource().getShowAtX(), item.getSource().getShowAtY(), item.getSource().getOutputWidth() + item.getSource().getShowAtX(), item.getSource().getOutputHeight() + item.getSource().getShowAtY(), 0, 0, item.getSource().getCaptureWidth(), item.getSource().getCaptureHeight(), null);
                }
            }
            buffer.dispose();
            source.image = source.tempimage;
        } 
    }
    
}