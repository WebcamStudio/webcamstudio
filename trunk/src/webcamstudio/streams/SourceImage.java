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
    boolean playing = true;
    boolean stop = false;
    Frame frame = null;
    public SourceImage(File img){
        file=img;
        name = img.getName();
    }
    
    private void loadImage(File f) throws IOException{
        image = ImageIO.read(f);
    }
    
    @Override
    public void read() {
        stop=false;
        try{
            loadImage(file);
            frame = new Frame(uuid,image,null);
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
            MasterFrameBuilder.register(this);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        stop=true;
        MasterFrameBuilder.unregister(this);
    }

    @Override
    public Frame getFrame(){
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
