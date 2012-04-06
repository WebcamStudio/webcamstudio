/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author patrick
 */
public class SystemAudioPlayer implements Runnable {

    boolean stopMe = false;
    private SourceDataLine source;
    private ExecutorService executor = null;
    private static SystemAudioPlayer instance = null;
    private ArrayList<byte[]> buffer = new ArrayList<byte[]>();
    
    private SystemAudioPlayer(){
    }
    public static SystemAudioPlayer getInstance(){
        if (instance==null){
            instance=new SystemAudioPlayer();
        }
        return instance;
    }
    public void addData(byte[] d){
       if (source!=null){
            buffer.add(d);
       }
    }
    public void play() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
        source = javax.sound.sampled.AudioSystem.getSourceDataLine(format);
        
        source.open();
        source.start();
        executor = java.util.concurrent.Executors.newCachedThreadPool();
        executor.submit(this);
        executor.shutdown();
    }

    @Override
    public void run() {
        stopMe = false;
        while (!stopMe) {
            if (buffer.size()>0){
                byte[] d = buffer.remove(0);
                source.write(d, 0, d.length);
            } else {
                try {
                    //System.out.println("No sound to play...");
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SystemAudioPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void stop() {
        stopMe = true;
        source.stop();
        source.close();
        source = null;
        executor = null;
        System.gc();
    }
}
