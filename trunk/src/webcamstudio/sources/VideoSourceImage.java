/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import webcamstudio.components.GifDecoder;

public class VideoSourceImage extends VideoSource {

    public VideoSourceImage(java.io.File loc) {

        if (loc != null) {
            try {
                location = loc.toURI().toURL().toString();
                thumbnail = Toolkit.getDefaultToolkit().createImage(loc.toURI().toURL()).getScaledInstance(32, 32, Image.SCALE_FAST);
                locations.add(location);
            } catch (Exception e) {
                error("Image Error:  " + e.getMessage());
            }
            name = loc.getName();
        }
    }

    public VideoSourceImage(java.io.File[] locs) {
        location = "";
        for (int i = 0; i < locs.length; i++) {
            try {
                if (locs[i].exists()) {
                    location += locs[i].toURI().toURL().toString() + ",";
                    thumbnail = Toolkit.getDefaultToolkit().createImage(locs[i].toURI().toURL()).getScaledInstance(32, 32, Image.SCALE_FAST);
                    name += locs[i].getName() + " ";
                }
            } catch (Exception e) {
                error("Image Error:  " + e.getMessage());
            }
        }
        if (locs.length > 0) {
            location = location.substring(0, location.length() - 1);
        }
    }

    public VideoSourceImage(java.net.URL loc) {
        if (loc != null) {
            location = loc.toString();
            thumbnail = Toolkit.getDefaultToolkit().createImage(loc).getScaledInstance(32, 32, Image.SCALE_FAST);
            locations.add(location);
            name = loc.toString();
        }
    }

    @Override
    public void startSource() {
        isPlaying = true;
        loadImage();
        Thread t = new Thread(new Runnable() {

            public void run() {
                stopMe = false;
                long lastTimeStamp = System.currentTimeMillis();
                while (!stopMe) {
                    try {
                        if (updateTimeLaspe > 0 && System.currentTimeMillis() - lastTimeStamp > updateTimeLaspe) {
                            loadImage();
                            lastTimeStamp = System.currentTimeMillis();
                        }
                        if (images.size() > 0) {
                            animatedIndex++;
                            if (animatedIndex >= images.size()) {
                                animatedIndex = 0;
                            }
                            if (animatedIndex == 0) {
                                captureHeight = images.get(animatedIndex).getHeight();
                                captureWidth = images.get(animatedIndex).getWidth();
                            }
                            tempimage = graphicConfiguration.createCompatibleImage(captureWidth, captureHeight, java.awt.image.BufferedImage.TRANSLUCENT);
                            java.awt.Graphics2D g = tempimage.createGraphics();
                            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 1));
                            g.drawImage(images.get(animatedIndex), 0, 0, null);
                            applyEffects(tempimage);
                            applyShape(tempimage);
                            g.dispose();
                            image = tempimage;
                        }
                        Thread.sleep(delays.get(animatedIndex));
                    } catch (Exception e) {
                        error("Image Error:  " + e.getMessage());
                    }

                }
            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

    }

    private void loadImage() {
        try {
            if (locations.size() == 0) {
                String[] locs = location.split(",");
                for (int i = 0; i < locs.length; i++) {
                    locations.add(locs[i]);
                }
            }

            if (index >= locations.size()) {
                index = 0;
            }

            java.net.URL url = new java.net.URL(locations.get(index++));
            if (url.getPath().toUpperCase().endsWith(".GIF")) {
                GifDecoder decoder = new GifDecoder();
                decoder.read(url.openStream());
                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    images.add(decoder.getFrame(i));
                    delays.add(decoder.getDelay(i));
                }
            } else {
                ImageInputStream stream = ImageIO.createImageInputStream(url.openStream());
                Iterator readers = ImageIO.getImageReaders(stream);
                if (readers.hasNext()) {
                    ImageReader reader = (ImageReader) readers.next();
                    reader.setInput(stream); // don't omit this line!
                    int n = reader.getNumImages(true); // don't use false!

                    for (int i = 0; i < n; i++) {
                        System.out.println("Loading image " + i);
                        BufferedImage img = reader.read(i);
                        images.add(img);
                        delays.add(200);
                    }

                    stream.close();
                } else {
                    stream.close();
                    tempimage = ImageIO.read(url);
                    images.add(tempimage);
                    delays.add(200);
                    frameRate = 1;
                }
            }
            if (images.size() > 0) {
                tempimage = images.get(0);
                if (outputHeight == 0 && outputWidth == 0) {
                    outputWidth = tempimage.getWidth();
                    outputHeight = tempimage.getHeight();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            error("Image Error  : " + e.getMessage());
        }

    }

    @Override
    public boolean canUpdateSource() {
        return true;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void pause() {
        //Do nothing...
    }

    @Override
    public void play() {
        //Do nothing;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public void stopSource() {
        isPlaying = false;
        stopMe = true;
        image = null;
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public String toString() {
        return "Image: " + name;
    }

    public Image getThumnail() {
        return thumbnail;
    }
    private int index = 0;
    private java.util.Vector<String> locations = new java.util.Vector<String>();
    private int animatedIndex = 0;
    private java.util.Vector<BufferedImage> images = new java.util.Vector<BufferedImage>();
    private java.util.Vector<Integer> delays = new java.util.Vector<Integer>();
    private Image thumbnail = null;

    @Override
    public java.util.Collection<JPanel> getControls() {
        java.util.Vector<JPanel> list = new java.util.Vector<JPanel>();
        list.add(new webcamstudio.controls.ControlEffects(this));
        list.add(new webcamstudio.controls.ControlShapes(this));
        list.add(new webcamstudio.controls.ControlReload(this));
        return list;
    }

    @Override
    public ImageIcon getThumbnail() {
        ImageIcon icon = getCachedThumbnail();
        if (icon == null) {
            try {
                tempimage = javax.imageio.ImageIO.read(new URL(location));
                if (tempimage != null) {
                    icon = new ImageIcon(tempimage.getScaledInstance(32, 32, BufferedImage.SCALE_FAST));
                } else {
                    icon = super.getThumbnail();
                }
            } catch (IOException ex) {
                Logger.getLogger(VideoSourceImage.class.getName()).log(Level.SEVERE, null, ex);
                icon = super.getThumbnail();
            }
            try {
                saveThumbnail(new ImageIcon(tempimage.getScaledInstance(128, 128, BufferedImage.SCALE_FAST)));
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
                Logger.getLogger(VideoSourceImage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return icon;
    }
}
