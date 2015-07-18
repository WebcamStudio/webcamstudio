/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import static webcamstudio.mixers.MasterMixer.BUFFER_SIZE;
import static webcamstudio.util.Tools.sleep;

/**
 *
 * @author patrick
 */
public class ImageBuffer {
    private final ArrayList<WSImage> buffer = new ArrayList<>();
    private int bufferSize = BUFFER_SIZE;
    private boolean abort = false;
    private int currentIndex = 0;
    private long framePushed = 0;
    private long framePopped = 0;
    
    
    public ImageBuffer(int w,int h){
        for (int i = 0;i<bufferSize;i++){
            WSImage img = new WSImage(w,h, TYPE_INT_RGB);
            buffer.add(img);
        }
    }
    public ImageBuffer(int w,int h,int bufferSize){
        this.bufferSize=bufferSize;
        for (int i = 0;i<bufferSize;i++){
            WSImage img = new WSImage(w,h, TYPE_INT_RGB);
            buffer.add(img);
        }
    }
    public void push(BufferedImage img){
        while(!abort && (framePushed - framePopped) >= bufferSize){
            sleep(30);
        }
        currentIndex++;
        currentIndex %= bufferSize;
        int[] data = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        WSImage image = buffer.get(currentIndex);
        image.setData(data);
        framePushed++;
    }
    public void doneUpdate(){
        currentIndex++;
        currentIndex %= bufferSize;
        framePushed++;
    }
    public WSImage getImageToUpdate(){
        while(!abort && (framePushed - framePopped) >= bufferSize){
            sleep(30);
        }
        return buffer.get((currentIndex+1)%bufferSize);
    }
    public WSImage pop(){
        while(!abort && framePopped >= framePushed){
            sleep(10);
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
