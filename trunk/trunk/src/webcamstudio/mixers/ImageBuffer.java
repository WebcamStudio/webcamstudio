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
    private static final int BUFFER_SIZE = 3;
    private boolean abort = false;
    private int currentIndex = 0;
    private long frameCounter = 0;
    
    
    public ImageBuffer(int w,int h){
        for (int i = 0;i<BUFFER_SIZE;i++){
            BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
            buffer.add(img);
        }
    }
    public void push(BufferedImage img){
        while(frameCounter >0){
            Tools.sleep(30);
        }
        currentIndex++;
        currentIndex = currentIndex % BUFFER_SIZE;
        buffer.get(currentIndex).getGraphics().drawImage(img, 0, 0, null);
        frameCounter++;
    }
    public void doneUpdate(){
        currentIndex++;
        currentIndex = currentIndex % BUFFER_SIZE;
        frameCounter++;
    }
    public BufferedImage getImageToUpdate(){
        while(frameCounter >0){
            Tools.sleep(30);
        }
        return buffer.get((currentIndex+1)%BUFFER_SIZE);
    }
    public BufferedImage pop(){
        while(frameCounter < 1){
            Tools.sleep(10);
        }
        frameCounter--;
        return buffer.get(currentIndex);
    }
    public void abort(){
        abort=true;
        currentIndex=0;
    }
    public void clear(){
        abort=false;
        currentIndex=0;
    }
}
