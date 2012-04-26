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
    private static final int BUFFER_SIZE = 30;
    private boolean abort = false;
    int currentIndex = 0;
    long frameCounter = 0;

    public AudioBuffer(int rate) {
        for (int i = 0; i < BUFFER_SIZE; i++) {
            buffer.add(new byte[(44100 * 2 * 2) / rate]);
        }
    }

    public void push(byte[] data) {
        while (frameCounter >= BUFFER_SIZE) {
            Tools.sleep(30);
        }
        currentIndex++;
        currentIndex = currentIndex % BUFFER_SIZE;
        byte[] d = buffer.get(currentIndex);
        for (int i = 0; i < d.length; i++) {
            d[i] = data[i];
        }
        frameCounter++;

    }
    public void doneUpdate(){
        currentIndex++;
        currentIndex = currentIndex % BUFFER_SIZE;
        frameCounter++;
    }    
    public byte[] getAudioToUpdate(){
        while (frameCounter >= BUFFER_SIZE-1) {
            Tools.sleep(30);
        }
        return buffer.get((currentIndex+1)%BUFFER_SIZE);
    }
    public byte[] pop() {
        while (frameCounter < 1) {
            Tools.sleep(1);
        }
        frameCounter--;
        return buffer.get(currentIndex);
    }

    public void abort() {
        abort = true;
        currentIndex = 0;
    }

    public void clear() {
        abort = false;
        currentIndex = 0;
    }
}
