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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 * @author pballeux
 */
public class VideoSourceAnimation extends VideoSource {

    public VideoSourceAnimation(java.io.File loc) {

        location = loc.getAbsolutePath();
        name = loc.getName();
        frameRate = 5;
    }

    public VideoSourceAnimation(java.net.URL loc) {

        location = loc.toString();
        name = loc.getFile();
        frameRate = 5;
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
                if (outputHeight == 0 && outputWidth == 0) {
                    outputWidth = 320;
                    outputHeight = 240;
                }
            } catch (Exception e) {
                error("Animation Error  : " + e.getMessage());
            }
        } else {
            animator.play();
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                stopMe = false;
                captureWidth = animator.getWidth();
                captureHeight = animator.getHeight();
                while (!stopMe) {
                    tempimage = graphicConfiguration.createCompatibleImage(captureWidth, captureHeight, java.awt.image.BufferedImage.TRANSLUCENT);
                    Graphics2D buffer = tempimage.createGraphics();
                    buffer.drawImage(animator.getCurrentImage(), 0, 0, null);
                    buffer.dispose();
                    applyEffects(tempimage);
                    applyShape(tempimage);
                    image = tempimage;
                    try {
                        Thread.sleep(animator.getTimeLapse());
                    } catch (InterruptedException ex) {
                        Logger.getLogger(VideoSourceAnimation.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();

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
        return (animator != null && !animator.isStopped());
    }

    @Override
    public void pause() {
        if (animator != null) {
            animator.stopMe();
        }
    }

    @Override
    public void play() {
        if (animator != null && animator.isStopped()) {
            animator.play();
        }
    }

    @Override
    public boolean isPaused() {
        return (animator != null && animator.isStopped());
    }

    @Override
    public void stopSource() {
        stopMe = true;
        if (animator != null) {
            animator.stopMe();
            animator = null;
        }
        image = null;
    }

    @Override
    public String toString() {
        return "Animation: " + new java.io.File(location).getName();
    }

    @Override
    public java.util.Collection<JPanel> getControls() {
        java.util.Vector<JPanel> list = new java.util.Vector<JPanel>();
        list.add(new webcamstudio.controls.ControlShapes(this));
        list.add(new webcamstudio.controls.ControlEffects(this));
        return list;
    }
    private Animator animator = null;

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
