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

    private final ArrayList<byte[]> buffer = new ArrayList<>();
    private int bufferSize = MasterMixer.BUFFER_SIZE;
    private boolean abort = false;
    private int aFreq = webcamstudio.WebcamStudio.audioFreq;
    int currentIndex = 0;
    long framePushed = 0;
    long framePopped = 0;

    public AudioBuffer(int rate) {
        for (int i = 0; i < bufferSize; i++) {
            buffer.add(new byte[(aFreq * 2 * 2) / rate]);
        }
    }
    public AudioBuffer(int rate,int bufferSize) {
        this.bufferSize=bufferSize;
        for (int i = 0; i < bufferSize; i++) {
            buffer.add(new byte[(aFreq * 2 * 2) / rate]);
        }
    }

    public void push(byte[] data) {
        while (!abort && (framePushed - framePopped) >= bufferSize) {
            Tools.sleep(30);
        }
        currentIndex++;
        currentIndex = currentIndex % bufferSize;
        byte[] d = buffer.get(currentIndex);
        System.arraycopy(data, 0, d, 0, d.length);
        framePushed++;

    }
    public void doneUpdate(){
        currentIndex++;
        currentIndex = currentIndex % bufferSize;
        framePushed++;
    }    
    public byte[] getAudioToUpdate(){
        while (!abort && (framePushed - framePopped) >= bufferSize) {
            Tools.sleep(30);
        }
        return buffer.get((currentIndex+1)%bufferSize);
    }
    public byte[] pop() {
        while (!abort && framePopped >= framePushed) {
            Tools.sleep(10);
        }
        framePopped++;
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
