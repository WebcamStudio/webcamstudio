/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.util.ArrayList;
import static webcamstudio.WebcamStudio.audioFreq;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick
 */
public class AudioBuffer {

    private final ArrayList<byte[]> buffer = new ArrayList<>();
    private int bufferSize = MasterMixer.BUFFER_SIZE;
    private boolean abort = false;
    private int aFreq = audioFreq;
    byte[] beep;
    int beepMillisec = 250;
    int currentIndex = 0;
    long framePushed = 0;
    long framePopped = 0;

    public AudioBuffer(int rate) {
        for (int i = 0; i < bufferSize; i++) {
            buffer.add(new byte[(aFreq * 2 * 2) / rate]);
        }
    }
    
    public byte[] Beep() {
        beep = new byte[beepMillisec*44]; // 44 is samples per msec
      double omega = 2 * Math.PI / 100; // 100 is samples per tone cycle
      for(int i=0; i<beep.length; i++) {
         beep[i] = (byte)(80 * Math.sin(omega*i)); // 80 is amplitude, < 128
      }
      return beep;
    }
    
    public AudioBuffer(int rate,int bufferSize) {
        this.bufferSize=bufferSize;
        for (int i = 0; i < bufferSize; i++) {
            buffer.add(new byte[(aFreq * 2 * 2) / rate]);
        }
    }

    public void push(byte[] data) {
        while (!abort && (framePushed - framePopped) >= bufferSize) {
//            currentIndex++;
//            currentIndex %= bufferSize;
//            byte[] d = Beep();
//            System.arraycopy(data, 0, d, 0, d.length);
//            framePushed++;
//            System.out.println("Beep Pushed.");
            Tools.sleep(30);
        }
        currentIndex++;
        currentIndex %= bufferSize;
        byte[] d = buffer.get(currentIndex);
        System.arraycopy(data, 0, d, 0, d.length);
        framePushed++;
        }
    
    public void doneUpdate(){
        currentIndex++;
        currentIndex %= bufferSize;
        framePushed++;
    }    
    public byte[] getAudioToUpdate(){
        while (!abort && (framePushed - framePopped) >= bufferSize) {
            System.out.println("AudioUpdate Sleep.");
            Tools.sleep(30);
        }
        System.out.println("AudioUpdate.");
        return buffer.get((currentIndex+1)%bufferSize);
    }
    public byte[] pop() {
        while (!abort && framePopped >= framePushed) {
//            framePopped++;
//            System.out.println("Beep Pop.");
//            return Beep();
            Tools.sleep(30);
        }
        framePopped++;
//        System.out.println("Buffer Poppeed.");
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
