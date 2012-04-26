/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick
 */
public class ImageBuffer {
    private ArrayList<BufferedImage> buffer = new ArrayList<BufferedImage>();
    private static final int BUFFER_SIZE = 100;
    private static final int BUFFER_THRESHOLD = 1;
    private boolean abort = false;
    long count = 0;
    public void push(BufferedImage img){
        while (!abort && buffer.size()>=BUFFER_SIZE){
            Tools.sleep(30);
        }
        buffer.add(img);
        if (count==0){
            try {
                javax.imageio.ImageIO.write(img, "png", new File("/home/patrick/Desktop/test.png"));
            } catch (IOException ex) {
                Logger.getLogger(ImageBuffer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        count++;
    }
    public BufferedImage pop(){
        BufferedImage image = null;
        while(!abort && buffer.isEmpty()){
            //System.err.println("Waiting for images...");
            Tools.sleep(10);
        }
        if (!abort && buffer.size()>0){
            image = buffer.remove(0);
        }
        return image;
    }
    public BufferedImage popNoWait(){
        BufferedImage image = null;
        if (!abort && buffer.size()>0){
            image = buffer.remove(0);
        }
        return image;
    }
    public void abort(){
        System.out.println("Video: " + count);
        abort=true;
        buffer.clear();
    }
    public boolean needData(){
        return buffer.size() < BUFFER_THRESHOLD;
    }
    public void clear(){
        abort=false;
        buffer.clear();
    }
}
