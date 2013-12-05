/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;

/**
 *
 * @author patrick
 */
public class SourceImage extends Stream{

    BufferedImage image = null;
    boolean playing = false;
    Frame frame = null;
    public SourceImage(File img){
        super();
        file=img;
        name = img.getName();
    }
    
    private void loadImage(File f) throws IOException{
        image = ImageIO.read(f);
        captureWidth = image.getWidth();
        captureHeight = image.getHeight();
    }
    
    @Override
    public void read() {
        playing = true;   
        try{
            loadImage(file);
            frame = new Frame(captureWidth,captureHeight,rate);
            frame.setImage(image);
            frame.setAudio(null);
            frame.setID(uuid);
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
            MasterFrameBuilder.register(this);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        playing = false;
        frame = null;
        MasterFrameBuilder.unregister(this);
    }
    @Override
    public boolean needSeek() {
            return needSeekCTRL=false;
    }
    @Override
    public Frame getFrame(){
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
        frame.setImage(image);
        applyEffects(frame.getImage());
        frame.setOutputFormat(x, y, width, height, opacity, volume);
        frame.setZOrder(zorder);
        nextFrame=frame;
    }

}
