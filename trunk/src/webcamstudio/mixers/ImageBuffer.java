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
    private int bufferSize = 10;
    private static final long TIMEOUT=5000;
    private boolean abort = false;
    private int currentIndex = 0;
    private long framePushed = 0;
    private long framePopped = 0;
    
    
    public ImageBuffer(int w,int h){
        for (int i = 0;i<bufferSize;i++){
            BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
            buffer.add(img);
        }
    }
    public ImageBuffer(int w,int h,int bufferSize){
        this.bufferSize=bufferSize;
        for (int i = 0;i<bufferSize;i++){
            BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
            buffer.add(img);
        }
    }
    public void push(BufferedImage img){
        while(!abort && (framePushed - framePopped) >= bufferSize){
            Tools.sleep(30);
        }
        currentIndex++;
        currentIndex = currentIndex % bufferSize;
        buffer.get(currentIndex).getGraphics().drawImage(img, 0, 0, null);
        framePushed++;
    }
    public void doneUpdate(){
        currentIndex++;
        currentIndex = currentIndex % bufferSize;
        framePushed++;
    }
    public BufferedImage getImageToUpdate(){
        while(!abort && (framePushed - framePopped) >= bufferSize){
            Tools.sleep(30);
        }
        return buffer.get((currentIndex+1)%bufferSize);
    }
    public BufferedImage pop(){
        long mark = System.currentTimeMillis();
        while(!abort && framePopped >= framePushed){
//            if (System.currentTimeMillis()-mark >= TIMEOUT){
//                //Resetting everyting;
//                System.err.println("Resetting video!");
//                currentIndex=0;
//                framePopped=0;
//                framePushed=0;
//                break;
//            }
            Tools.sleep(10);
        }
        framePopped++;
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
