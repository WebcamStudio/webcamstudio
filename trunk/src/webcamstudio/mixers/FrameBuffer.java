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
    private ArrayList<Frame> buffer = new ArrayList<Frame>();
    private static final int BUFFER_SIZE = 30;
    private static final int BUFFER_THRESHOLD = 15;
    private boolean abort = false;
    public void push(Frame f){
        while (!abort && buffer.size()>=BUFFER_SIZE){
            Tools.sleep(30);
        }
        buffer.add(f);
    }
    public Frame pop(){
        Frame f = null;
        while (!abort && buffer.isEmpty()){
            Tools.sleep(10);
        }
        if (buffer.size()>0){
            f = buffer.remove(0);
        } 
        return f;
    }
    public boolean needData(){
        return buffer.size() < BUFFER_THRESHOLD;
    }
    public void clear(){
        abort=false;
        buffer.clear();
    }
    public void abort(){
        abort=true;
    }
}
