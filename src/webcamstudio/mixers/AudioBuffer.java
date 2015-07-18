/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import static java.lang.System.arraycopy;
import java.util.ArrayList;
import static webcamstudio.WebcamStudio.audioFreq;
import static webcamstudio.mixers.MasterMixer.BUFFER_SIZE;
import static webcamstudio.util.Tools.sleep;

/**
 *
 * @author patrick
 */
public class AudioBuffer {

    private final ArrayList<byte[]> buffer = new ArrayList<>();
    private int bufferSize = BUFFER_SIZE;
    private boolean abort = false;
    private int aFreq = audioFreq;
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
            sleep(30);
        }
        currentIndex++;
        currentIndex %= bufferSize;
        byte[] d = buffer.get(currentIndex);
        arraycopy(data, 0, d, 0, d.length);
        framePushed++;
        }
    
    public void doneUpdate(){
        currentIndex++;
        currentIndex %= bufferSize;
        framePushed++;
    }    
    public byte[] getAudioToUpdate(){
        while (!abort && (framePushed - framePopped) >= bufferSize) {
            sleep(30);
        }
        return buffer.get((currentIndex+1)%bufferSize);
    }
    public byte[] pop() {
        while (!abort && framePopped >= framePushed) {
            sleep(10);
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
