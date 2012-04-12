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
            Frame frame = new Frame(uuid,image,null);
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
            frames.add(frame);
            MasterFrameBuilder.register(this);
            new Thread(new Runnable(){
                @Override
                public void run() {
                    playing=true;
                    frames.get(0).setOutputFormat(x, y, width, height, opacity, volume);
                    frames.get(0).setZOrder(zorder);
                    while(!stop){
                        try{
                            Thread.sleep(1000/rate);
                        } catch(Exception e){
                        }
                    }
                    playing=false;
                }
            }).start();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        stop=true;
        frames.clear();
        MasterFrameBuilder.unregister(this);
    }

    @Override
    public Frame getFrame(){
        Frame f = null;
        if (frames.size()>0){
            f= frames.get(0);
        }
        return f;
    }
    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public BufferedImage getPreview() {
        return image;
    }
    
}
