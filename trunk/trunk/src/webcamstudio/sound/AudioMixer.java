/**
 *  WebcamStudio for GNU/Linux
 *  Copyright (C) 2008  Patrick Balleux
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * 
 */
package webcamstudio.sound;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AudioMixer implements Runnable {
    // gst-launch gconfaudiosrc ! audio/x-raw-int,rate=44100,channels=2,signed=true,width=16 ! 
    // audioconvert ! ladspa-delay-5s Delay=$DEFAULT_DELAY Dry-Wet-Balance=1.0 ! audioconvert ! 
    // audio/x-raw-int,rate=44100,signed=true,width=16,channels=2 ! filesink location=$DEFAULT_FIFO
    // This class will play any sound at 44100 kh, 2 channles, bits 16

    private java.io.File input = new java.io.File("/tmp/music.input");
    private boolean stopMe = false;

    public AudioMixer() {
        if (!input.exists()) {
            try {
                Process p = Runtime.getRuntime().exec("pactl load-module module-pipe-source rate=44100 format=s16 channels=1");
                p.waitFor();
                p.destroy();
                p = null;
            } catch (Exception ex) {
                Logger.getLogger(AudioMixer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void stop(){
        stopMe=true;
    }
    @Override
    public void run() {
        java.io.FileOutputStream fout = null;
        byte[] buffer = new byte[44100];
        double count = 0;
        double ang = 44100 * Math.PI * 2;
        for (int i = 0;i<buffer.length;i++){
            buffer[i] = (byte)(0 + (i%35));
        }
        try {
            fout = new java.io.FileOutputStream(input);
            while (!stopMe && count !=-1) {
                fout.write(buffer);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(AudioMixer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AudioMixer.class.getName()).log(Level.SEVERE, null, ex);
            stopMe=true;
        } catch (IOException ex) {
            Logger.getLogger(AudioMixer.class.getName()).log(Level.SEVERE, null, ex);
            stopMe=true;
        } finally {
            try {
                fout.close();
            } catch (IOException ex) {
                Logger.getLogger(AudioMixer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static void main(String[] args) {
        AudioMixer p = new AudioMixer();
        new Thread(p).start();
    }
}
