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
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class SourceImage extends Stream{

    File sourceFile = null;
    BufferedImage image = null;
    boolean playing = true;
    boolean stop = false;
    public SourceImage(File img){
        sourceFile=img;
    }
    
    private void loadImage(File f) throws IOException{
        image = ImageIO.read(f);
    }
    
    @Override
    public void read() {
        stop=false;
        try{
            loadImage(sourceFile);
            new Thread(new Runnable(){

                @Override
                public void run() {
                    long timeCode = 0;
                    playing=true;
                    while(!stop){
                        try{
                            timeCode += ((44100 * 2 * 2) / rate);
                            Frame frame = new Frame(image,null,timeCode,null);
                            frame.setOutputFormat(x, y, width, height, opacity, volume);
                            frame.setZOrder(zorder);
                            MasterMixer.addSourceFrame(frame, uuid);
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
    public String getName() {
        return sourceFile.getName();
    }
    
}
