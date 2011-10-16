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
import webcamstudio.components.Mixer;
import webcamstudio.layout.Layout;
import webcamstudio.layout.LayoutItem;

/**
 *
 * @author patrick
 */
public class VideoSourceLayout extends VideoSource {

    protected String layoutUUID = null;
    protected java.util.AbstractMap<String, Layout> layouts = null;

    public VideoSourceLayout(String layoutUUID, java.util.AbstractMap<String, Layout> layouts) {
        this.layoutUUID = layoutUUID;
        location = layoutUUID;
        if (layouts.containsKey(layoutUUID)) {
            name = layouts.get(layoutUUID).toString();
        }
        this.layouts = layouts;
    }

    @Override
    public void startSource() {
        //Find the layout to use...
        isPlaying = true;
        stopMe = false;
        if (layouts.containsKey(layoutUUID)) {
            Layout layout = layouts.get(layoutUUID);
            name = layout.toString();
            if (!layout.isActive()) {
                layout.enterLayout(true);
            }

            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            timer = new Timer(name, true);
            frameRate=Mixer.getFPS();
            timer.scheduleAtFixedRate(new LayoutImage(this), 0, 1000/frameRate);
        }
    }

    @Override
    public void stopSource() {
        stopMe = true;
        if (layouts.containsKey(layoutUUID)) {
            layouts.get(layoutUUID).exitLayout(true);
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
        if (source.layouts.containsKey(source.layoutUUID)) {
            Layout layout = source.layouts.get(source.layoutUUID);
            source.captureHeight = Mixer.getHeight();
            source.captureWidth = Mixer.getWidth();
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