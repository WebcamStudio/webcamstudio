/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author patrick
 */
public class SystemAudioPlayer {

    boolean stopMe = false;
    SourceDataLine source;

    public void play() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
        source = javax.sound.sampled.AudioSystem.getSourceDataLine(format);
        source.open();
        source.start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                Frame previousFrame = null;
                stopMe=false;
                while (!stopMe) {
                    Frame frame = MasterMixer.getCurrentFrame();
                    if (frame != null && !frame.equals(previousFrame)) {
                        byte[] data = frame.getAudioData();
                        if (data != null && data.length>0) {
                            source.write(data, 0, data.length);
                            previousFrame=frame;
                        } else {
                            System.out.println("No audio");
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(SystemAudioPlayer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } else {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(SystemAudioPlayer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                }
                source.stop();
                source.close();
                source=null;
                System.gc();
            }
        }).start();


    }

    public void stop() {
        stopMe = true;
    }
}
