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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.gstreamer.*;

public class AudioMixer {
    private int volume = 100;
    private int low = 0;
    private int middle = 0;
    private int high = 0;
    private boolean isActive = false;

    // gst-launch gconfaudiosrc ! audio/x-raw-int,rate=44100,channels=2,signed=true,width=16 ! 
    // audioconvert ! ladspa-delay-5s Delay=$DEFAULT_DELAY Dry-Wet-Balance=1.0 ! audioconvert ! 
    // audio/x-raw-int,rate=44100,signed=true,width=16,channels=2 ! filesink location=$DEFAULT_FIFO
    public AudioMixer() {
    }

    public void start() {
        if (!isActive){
        new Thread(new Runnable() {
            public void run() {
                try {
                    isActive=true;
                    java.io.File input = new java.io.File("/tmp/music.input");
                    if (!input.exists()) {
                        Process p = Runtime.getRuntime().exec("pactl load-module module-pipe-source");
                        p.waitFor();
                        p.destroy();
                        p = null;
                    }
                    elementSink.set("location", "/tmp/music.input");
                    if (input.exists()) {
                        pipe = new Pipeline("WebcamStudio Mixer");
                        audioCaptureFilter.setCaps(Caps.fromString("audio/x-raw-int"));
                        audioOutputFilter.setCaps(Caps.fromString("audio/x-raw-int,rate=44100,signed=true,width=16,channels=2"));
                        pipe.addMany(elementSourceSystem,elementSourceMic,audioVolume,audioAdder, audioCaptureFilter, audioOutputFilter, audioConvert, audioConvert2, audioEqualizer, elementSink);
                        Element.linkMany(elementSourceMic, audioCaptureFilter, audioConvert,audioVolume, audioEqualizer, audioAdder,audioConvert2, audioOutputFilter, elementSink);
                        elementSourceSystem.link(audioAdder);
                        pipe.getBus().connect(new Bus.ERROR() {

                            public void errorMessage(GstObject arg0, int arg1, String arg2) {
                                System.out.println("Error: " + arg0 + "," + arg1 + ", " + arg2);
                                pipe.setState(State.NULL);
                            }
                        });

                        pipe.setState(State.PLAYING);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        }
    }

    public boolean isActive(){
        return isActive;
    }
    public void setLowFilter(int f) {
        low=f;
        audioEqualizer.set("band0", (float) f);
    }

    public void setMiddleFilter(int f) {
        middle=f;
        audioEqualizer.set("band1", (float) f);
    }

    public void setHighFilter(int f) {
        high=f;
        audioEqualizer.set("band2", (float) f);
    }
    public int getLowFilter(){
        return low;
    }
    public int getMiddleFilter(){
        return middle;
    }
    public int getHighFilter(){
        return high;
    }
    public void setVolume(int v) {
        volume = v;
        audioVolume.set("volume", (double) v / 100D);

    }

    public int getVolume(){
        return volume;
    }
    public void stop() {
        if (pipe != null && pipe.getState()==State.PLAYING) {
            pipe.stop();
            pipe.removeMany(audioEqualizer,audioVolume,elementSourceSystem,elementSourceMic,audioAdder, audioCaptureFilter, audioConvert, audioConvert2, audioOutputFilter, elementSink);
            pipe = null;
        }
        isActive=false;
    }
    java.io.File configurationFile = null;
    Element elementSourceSystem = ElementFactory.make("pulsesrc", "pulsesrcSystem");
    Element elementSourceMic = ElementFactory.make("pulsesrc", "pulsesrcMic");
    Element elementSink = ElementFactory.make("filesink", "filesink");
    Element audioCaptureFilter = ElementFactory.make("capsfilter", "audiocapturefilter");
    Element audioOutputFilter = ElementFactory.make("capsfilter", "audiooutputfilter");
    Element audioConvert = ElementFactory.make("audioconvert", "audioconvert");
    Element audioConvert2 = ElementFactory.make("audioconvert", "audioconvert2");
    Element audioEqualizer = ElementFactory.make("equalizer-3bands", "equalizer-3bands");
    Element audioVolume = ElementFactory.make("volume", "volume");
    Element audioAdder = ElementFactory.make("adder", "adder");
    Pipeline pipe = null;

    public static void main(String[] args){
        Gst.init();
        AudioMixer p = new AudioMixer();
        p.start();
        try {
            Thread.sleep(100000);
        } catch (InterruptedException ex) {
            Logger.getLogger(AudioMixer.class.getName()).log(Level.SEVERE, null, ex);
        }
        p.stop();
        p=null;
    }
}
