/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import webcamstudio.components.GifDecoder;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;
import webcamstudio.sources.effects.Effect;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick
 */
public class SourceImageGif extends Stream {

    BufferedImage image = null;
    boolean playing = false;
    boolean stop = true;
    Frame frame = null;
    GifDecoder decoder = new GifDecoder();
    int index = 0;

    public SourceImageGif(File img) {
        super();
        file = img;
        name = img.getName();
    }

    public SourceImageGif(String name, URL url) {
        this.url = url.toString();
        this.name = name;
    }

    private void loadImage(File f) throws IOException {
        if (file != null) {
            decoder.read(file.toURI().toURL().openStream());
        } else if (url != null) {
            decoder.read(new URL(url).openStream());
        }
        image = new BufferedImage(decoder.getImage().getWidth(), decoder.getImage().getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        new Thread(new Runnable() {

            @Override
            public void run() {
                updateImage();
            }
        }).start();
    }

    private void updateImage() {
        if (decoder != null) {
            while (!stop) {
                index++;
                if (index >= decoder.getFrameCount()) {
                    index = 0;
                }
                Tools.sleep(decoder.getDelay(index));
            }
        }
    }

    @Override
    public void read() {
        playing=true;
        stop = false;
        try {
            loadImage(file);
            frame = new Frame(uuid, image, null);
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
            MasterFrameBuilder.register(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        playing=false;
        stop = true;
        MasterFrameBuilder.unregister(this);
    }
    @Override
    public boolean needSeek() {
            return needSeekCTRL=false;
    }
    @Override
    public Frame getFrame() {
        return nextFrame;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }
    @Override
    public void setIsPlaying(boolean setIsPlaying) {
        playing = setIsPlaying;
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

    @Override
    public void readNext() {
        image = decoder.getFrame(index);
        frame = new Frame(uuid, image, null);
        for (Effect fxG : this.getEffects()) {
            if (frame != null) {
                if (fxG.needApply()){   
                    fxG.applyEffect(frame.getImage());
                }
            }
        }
        frame.setOutputFormat(x, y, width, height, opacity, volume);
        frame.setZOrder(zorder);
        nextFrame=frame;
    }
}
