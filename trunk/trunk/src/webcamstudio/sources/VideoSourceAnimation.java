/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 *
 * @author pballeux
 */
public class VideoSourceAnimation extends VideoSource {

    public VideoSourceAnimation(java.io.File loc) {

        location = loc.getAbsolutePath();
        name = loc.getName();
        frameRate = 5;
        controls.add(new webcamstudio.controls.ControlShapes(this));
        controls.add(new webcamstudio.controls.ControlEffects(this));
    }

    public VideoSourceAnimation(java.net.URL loc) {

        location = loc.toString();
        name = loc.getFile();
        frameRate = 5;
        controls.add(new webcamstudio.controls.ControlShapes(this));
        controls.add(new webcamstudio.controls.ControlEffects(this));
    }

    private java.io.File getJarFromWeb(String loc) throws IOException {
        java.io.File file = java.io.File.createTempFile("ANM", ".jar");
        file.setWritable(true);
        file.setReadable(true);
        URL url = new URL(loc);
        InputStream is = url.openStream();
        java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
        java.io.BufferedOutputStream bout = new java.io.BufferedOutputStream(fos, 65000);
        byte[] data = new byte[65000];
        int count = -1;
        count = is.read(data);
        while (count != -1) {
            if (count != -1) {
                bout.write(data, 0, count);
            }
            count = is.read(data);
        }
        is.close();
        bout.close();
        fos.close();
        file.deleteOnExit();
        return file;
    }

    public void setAudioLevel(int l) {
        if (animator != null) {
            animator.setAudioLevel(l);
        }
    }

    @Override
    public void startSource() {
        stopMe = false;

        if (animator == null) {
            animator = new Animator(this);
            try {
                if (location.toLowerCase().startsWith("http://") || location.toLowerCase().startsWith("https://")) {
                    java.io.File file = getJarFromWeb(location);
                    animator.loadAnimation(file);
                } else {
                    animator.loadAnimation(new java.io.File(location));
                }
                captureWidth = animator.getWidth();
                captureHeight = animator.getHeight();
                frameRate = 1000 / animator.getTimeLapse();
                if (outputHeight == 0 && outputWidth == 0) {
                    outputWidth = 320;
                    outputHeight = 240;
                }
            } catch (Exception e) {
                error("Animation Error  : " + e.getMessage());
            }
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer(name, true);
        timer.scheduleAtFixedRate(new imageAnimation(this), 0, 1000 / frameRate);

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
    public boolean isPlaying() {
        return (animator != null);
    }

    @Override
    public void pause() {
    }

    @Override
    public void play() {
    }

    @Override
    public boolean isPaused() {
        return (animator != null && animator.isStopped());
    }

    @Override
    public void stopSource() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        stopMe = true;
        image = null;
    }

    @Override
    public String toString() {
        return "Animation: " + new java.io.File(location).getName();
    }
    protected Animator animator = null;

    @Override
    public ImageIcon getThumbnail() {
        ImageIcon icon = getCachedThumbnail();
        if (icon == null) {
            try {
                icon = new ImageIcon(Animator.getThumbnail(new File(location)).getScaledInstance(32, 32, BufferedImage.SCALE_FAST));
            } catch (Exception ex) {
                Logger.getLogger(VideoSourceAnimation.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                saveThumbnail(icon);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                Logger.getLogger(VideoSourceAnimation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return icon;
    }
}

class imageAnimation extends TimerTask {

    private VideoSourceAnimation animation = null;

    public imageAnimation(VideoSourceAnimation a) {
        animation = a;
    }

    @Override
    public void run() {

        if (animation.animator != null) {
            animation.captureWidth = animation.animator.getWidth();
            animation.captureHeight = animation.animator.getHeight();

            animation.tempimage = animation.graphicConfiguration.createCompatibleImage(animation.captureWidth, animation.captureHeight, java.awt.image.BufferedImage.TRANSLUCENT);
            Graphics2D buffer = animation.tempimage.createGraphics();
            buffer.drawImage(animation.animator.getCurrentImage(), 0, 0, null);
            buffer.dispose();
            animation.applyEffects(animation.tempimage);
            animation.applyShape(animation.tempimage);
            animation.image = animation.tempimage;
        }

    }
}
