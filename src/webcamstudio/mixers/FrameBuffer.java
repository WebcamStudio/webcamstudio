/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.util.ArrayList;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick
 */
public class FrameBuffer {
    private final ArrayList<Frame> buffer = new ArrayList<>();
    private static final int BUFFER_SIZE = MasterMixer.BUFFER_SIZE;
    private boolean abort = false;
    private int currentIndex = 0;
    private long frameCounter=0;
    
    public FrameBuffer(int imgWidth, int imgHeight,int rate){
        for (int i = 0;i<BUFFER_SIZE;i++){
            Frame frame = new Frame(imgWidth,imgHeight,rate);
            buffer.add(frame);
        }
    }
//    public void push(Frame f){
//        while (!abort && frameCounter >0) {
//            Tools.sleep(30);
//        }
//        currentIndex++;
//        currentIndex = currentIndex % BUFFER_SIZE;
//        buffer.get(currentIndex); //.copyFrame(f)
//        frameCounter++;
//    }
    public void doneUpdate(){
        currentIndex++;
        currentIndex = currentIndex % BUFFER_SIZE;
        frameCounter++;
    }    
    public Frame getFrameToUpdate(){
        while (!abort && frameCounter >0) {
            Tools.sleep(30);
        }
        return buffer.get((currentIndex+1)%BUFFER_SIZE);
    }
    public Frame pop(){
        while(!abort && frameCounter < 1){
            Tools.sleep(10);
        }
        frameCounter--;
        return buffer.get(currentIndex);
    }
    public void clear(){
        abort=false;
        currentIndex=0;
    }
    public void abort(){
        abort=true;
    }
}
