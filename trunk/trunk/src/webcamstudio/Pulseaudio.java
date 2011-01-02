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
package webcamstudio;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.gstreamer.*;

public class Pulseaudio {
    // gst-launch gconfaudiosrc ! audio/x-raw-int,rate=44100,channels=2,signed=true,width=16 ! 
    // audioconvert ! ladspa-delay-5s Delay=$DEFAULT_DELAY Dry-Wet-Balance=1.0 ! audioconvert ! 
    // audio/x-raw-int,rate=44100,signed=true,width=16,channels=2 ! filesink location=$DEFAULT_FIFO
    public Pulseaudio() {
    }

    public void start() {
        new Thread(new Runnable() {

            public void run() {
                try {
                    java.io.File input = new java.io.File("/tmp/music.input");
                    if (!input.exists()) {
                        Process p = Runtime.getRuntime().exec("pactl load-module module-pipe-source");
                        p.waitFor();
                        p.destroy();
                        p = null;
                    }
                    elementSink.set("location", "/tmp/music.input");
                    if (input.exists()) {
                        pipe = new Pipeline("Microphone");
                        audioCaptureFilter.setCaps(Caps.fromString("audio/x-raw-int"));
                        audioOutputFilter.setCaps(Caps.fromString("audio/x-raw-int,rate=44100,signed=true,width=16,channels=2"));
                        audioDelay.set("Delay", 0.1F);
                        audioDelay.set("Dry-Wet-Balance", 1F);
                        setEcho(false);
                        pipe.addMany(elementSource, audioCaptureFilter, audioOutputFilter, audioConvert, audioPitch, audioConvert2, audioEqualizer, audioDelay, elementSink, audioEcho1, audioEcho2, audioEcho3);
                        Element.linkMany(elementSource, audioCaptureFilter, audioConvert, audioDelay, audioEqualizer, audioPitch, audioEcho1, audioEcho2, audioEcho3, audioConvert2, audioOutputFilter, elementSink);
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

    public void setEcho(boolean value) {
        if (value) {
            audioEcho1.set("Delay", 0.2F);
            audioEcho1.set("Dry-Wet-Balance", 0.3F);
            audioEcho2.set("Delay", 0.1F);
            audioEcho2.set("Dry-Wet-Balance", 0.2F);
            audioEcho3.set("Delay", 0.1F);
            audioEcho3.set("Dry-Wet-Balance", 0.1F);
        } else {
            audioEcho1.set("Delay", 0.01F);
            audioEcho1.set("Dry-Wet-Balance", 1F);
            audioEcho2.set("Delay", 0.01F);
            audioEcho2.set("Dry-Wet-Balance", 1F);
            audioEcho3.set("Delay", 0.01F);
            audioEcho3.set("Dry-Wet-Balance", 1F);
        }
    }

    public void setPitch(int pitch) {
        //waiting value from -10 to +10;
        if (pitch < 0) {
            audioPitch.set("pitch", ((float) (pitch + 10 + 1)) / 10.0F);
        } else if (pitch > 0) {
            audioPitch.set("pitch", ((float) (pitch + 10)) / 10.0F);
        } else {
            audioPitch.set("pitch", 1.0F);
        }

    }

    public void setDelay(int d) {
        // we do +1 to avoid 0...
        audioDelay.set("Delay", (float) (d + 1) / 1000F);
    }

    public void setLowFilter(int f) {
        audioEqualizer.set("band0", (float) f);
    }

    public void setMiddleFilter(int f) {
        audioEqualizer.set("band1", (float) f);
    }

    public void setHighFilter(int f) {
        audioEqualizer.set("band2", (float) f);
    }

    public void stop() {
        if (pipe != null && pipe.getState()==State.PLAYING) {
            pipe.setState(State.NULL);
            while (pipe.getState() == State.PLAYING) {
                pipe.getState();
            }
            pipe.removeMany(elementSource, audioCaptureFilter, audioConvert, audioDelay, audioConvert2, audioOutputFilter, elementSink);
            pipe = null;
        }
    }
    java.io.File configurationFile = null;
    Element elementSource = ElementFactory.make("gconfaudiosrc", "gconfaudiosrc");
    Element elementSink = ElementFactory.make("filesink", "filesink");
    Element audioCaptureFilter = ElementFactory.make("capsfilter", "audiocapturefilter");
    Element audioOutputFilter = ElementFactory.make("capsfilter", "audiooutputfilter");
    Element audioConvert = ElementFactory.make("audioconvert", "audioconvert");
    Element audioConvert2 = ElementFactory.make("audioconvert", "audioconvert2");
    Element audioDelay = ElementFactory.make("ladspa-delay-5s", "audiodelay");
    Element audioEcho1 = ElementFactory.make("ladspa-delay-5s", "audioecho1");
    Element audioEcho2 = ElementFactory.make("ladspa-delay-5s", "audioecho2");
    Element audioEcho3 = ElementFactory.make("ladspa-delay-5s", "audioecho3");
    Element audioPitch = ElementFactory.make("pitch", "audiopitch");
    Element audioEqualizer = ElementFactory.make("equalizer-3bands", "equalizer-3bands");
    Pipeline pipe = null;

    public static void main(String[] args){
        Gst.init();
        Pulseaudio p = new Pulseaudio();
        p.start();
        try {
            Thread.sleep(100000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Pulseaudio.class.getName()).log(Level.SEVERE, null, ex);
        }
        p.stop();
        p=null;
    }
}
