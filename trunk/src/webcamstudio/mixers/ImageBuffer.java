/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick
 */
public class ImageBuffer {
    private ArrayList<BufferedImage> buffer = new ArrayList<BufferedImage>();
    private static final int BUFFER_SIZE = 30;
    private static final int BUFFER_THRESHOLD = 15;
    private boolean abort = false;
    public void push(BufferedImage img){
        while (!abort && buffer.size()>=BUFFER_SIZE){
            Tools.sleep(10);
        }
        buffer.add(img);
    }
    public BufferedImage pop(){
        BufferedImage image = null;
        while(!abort && buffer.size()==0){
            //System.err.println("Waiting for images...");
            Tools.sleep(10);
        }
        if (!abort && buffer.size()>0){
            image = buffer.remove(0);
        }
        return image;
    }
    public void abort(){
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
