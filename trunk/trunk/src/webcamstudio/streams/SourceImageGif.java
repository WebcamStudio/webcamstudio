/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.components.GifDecoder;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;

/**
 *
 * @author patrick
 */
public class SourceImageGif extends Stream {

    BufferedImage image = null;
    boolean playing = true;
    boolean stop = false;
    Frame frame = null;
    GifDecoder decoder = new GifDecoder();
    
    public SourceImageGif(File img) {
        super();
        file = img;
        name = img.getName();
    }
    public SourceImageGif(String name,URL url){
        this.url = url;
        this.name=name;
    }
    
    private void loadImage(File f) throws IOException {
        if (file!=null){
            decoder.read(file.toURI().toURL().openStream());
        } else if (url!=null){
            decoder.read(url.openStream());
        }
        image = new BufferedImage(decoder.getImage().getWidth(),decoder.getImage().getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
        System.out.println("Image Count: " + decoder.getFrameCount());
        new Thread(new Runnable() {

            @Override
            public void run() {
                updateImage();
            }
        }).start();
    }

    private void updateImage() {
        if (decoder != null) {
            int index = 0;
            while (!stop) {
                image = decoder.getFrame(index);
//                image.getGraphics().setColor(new Color(0,0,0,0));
//                image.getGraphics().clearRect(0, 0, image.getWidth(), image.getHeight());
//                image.getGraphics().drawImage(tmpImage, 0, 0, null);
                if (image != null) {
                    captureWidth = image.getWidth();
                    captureHeight = image.getHeight();
                    try {
                        Thread.sleep(decoder.getDelay(index));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SourceImageGif.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    index++;
                    if (index >= decoder.getFrameCount()) {
                        index = 0;
                    }
                    if (frame != null) {
                        frame.setImage(image);
                        frame.setOutputFormat(x, y, width, height, opacity, volume);
                        frame.setZOrder(zorder);
                    }
                } else {
                    stop=true;
                }
            }
        }
    }

    @Override
    public void read() {
        stop = false;
        try {
            loadImage(file);
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
        frame = null;
        MasterFrameBuilder.unregister(this);
    }

    @Override
    public Frame getFrame() {
        frame.setOutputFormat(x, y, width, height, opacity, volume);
        frame.setZOrder(zorder);
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
