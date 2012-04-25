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
public class AudioBuffer {
    private ArrayList<byte[]> buffer = new ArrayList<byte[]>();
    private static final int BUFFER_SIZE = MasterMixer.getInstance().getRate()*10;
    private static final int BUFFER_THRESHOLD = 15;
    private boolean abort = false;
    public void push(byte[] data){
        while (!abort && buffer.size()>=BUFFER_SIZE){
            Tools.sleep(30);
        }
        buffer.add(data);
    }
    public byte[] pop(){
        byte[] data = null;
        while(!abort && buffer.size()==0){
            //System.err.println("Waiting for images...");
            Tools.sleep(10);
        }
        if (!abort && buffer.size()>0){
            data = buffer.remove(0);
        }
        return data;
    }
    public byte[] popNoWait(){
        byte[] data = null;
        if (!abort && buffer.size()>0){
            data = buffer.remove(0);
        }
        return data;
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
