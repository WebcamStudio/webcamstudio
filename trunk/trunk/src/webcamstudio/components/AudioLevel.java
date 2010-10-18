/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author pballeux
 */
public class AudioLevel implements Runnable {

    private TargetDataLine tdl = null;
    private AudioFormat format = new AudioFormat(44100, 8, 1, true, false);
    private int audioLevel = 0;
    private boolean stopMe = false;
    private boolean formatIsSigned = true;

    public AudioLevel() {
        boolean foundFormat = false;
        boolean pulseAudioFound = false;
        DataLine.Info dlinfo = new DataLine.Info(TargetDataLine.class, format);
        //Try to find a suitable format
        //List all detected mixer
        for (javax.sound.sampled.Mixer.Info mixer : AudioSystem.getMixerInfo()) {
            System.out.println("Mixer: " + mixer.getName());
            if (mixer.getName().toLowerCase().indexOf("pulseaudio")!=-1){
                pulseAudioFound = true;
            }
        }


        if (AudioSystem.isLineSupported(dlinfo)) {
            foundFormat = true;
        } else {
            format = new AudioFormat(44100, 8, 1, false, false);
            dlinfo = new DataLine.Info(TargetDataLine.class, format);
            if (AudioSystem.isLineSupported(dlinfo)) {
                foundFormat = true;
                formatIsSigned = false;

            } else {
                format = new AudioFormat(44100, 8, 1, true, false);
                dlinfo = new DataLine.Info(TargetDataLine.class, format);
                if (AudioSystem.isLineSupported(dlinfo)) {
                    foundFormat = true;
                    formatIsSigned = true;

                } else {
                    format = new AudioFormat(44100, 8, 1, false, false);
                    dlinfo = new DataLine.Info(TargetDataLine.class, format);
                    if (AudioSystem.isLineSupported(dlinfo)) {
                        foundFormat = true;
                        formatIsSigned = false;
                    }

                }
            }
        }
        if (foundFormat && pulseAudioFound) {
            System.out.println(dlinfo.toString());
            try {
                tdl = AudioSystem.getTargetDataLine(format);

                Thread t = new Thread(this);
                t.setPriority(Thread.MIN_PRIORITY);
                t.start();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No format found!");
        }

    }

    public int getLevel() {
        return audioLevel;
    }

    public void stop() {
        stopMe = true;
    }

    @Override
    public void run() {
        System.out.println("Monitoring sound...");
        byte lastHighByte = 0;
        byte lastLowByte = 0;
        while (!stopMe) {
            lastHighByte = 0;
            lastLowByte = 0;

            try {
                tdl.open(format);
                tdl.start();
                byte[] buffer = new byte[11025];

                tdl.read(buffer, 0, buffer.length);
                for (byte b : buffer) {
                    if (b > lastHighByte) {
                        lastHighByte = b;
                    }
                    if (b < lastLowByte) {
                        lastLowByte = b;
                    }
                }
                if (formatIsSigned) {
                    audioLevel = lastHighByte;

                } else {

                    audioLevel = lastLowByte;

                }
                Thread.sleep(1);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        tdl.stop();
        tdl.close();
    }
}
