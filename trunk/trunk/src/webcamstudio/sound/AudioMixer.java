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

import org.gstreamer.*;

public class AudioMixer {

    private int micVolume = 100;
    private int sysVolume = 100;
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
        if (!isActive) {
            new Thread(new Runnable() {

                public void run() {
                    try {
                        isActive = true;
                        java.io.File input = new java.io.File("/tmp/webcamstudio_audio");
                        elementSink.set("location", input.getAbsolutePath());
                        if (input.exists()) {
                            pipe = new Pipeline("WebcamStudio Mixer");
                            audioCaptureFilter.setCaps(Caps.fromString("audio/x-raw-int"));
                            audioOutputFilter.setCaps(Caps.fromString("audio/x-raw-int,rate=22050,signed=true,width=16,channels=1"));
                            elementSourceSystem.set("device", 1);
                            elementSourceMic.set("device", 0);
                            pipe.addMany(elementSourceSystem, elementSourceMic, audioMicVolume,audioSysVolume, audioAdder, audioCaptureFilter, audioOutputFilter, audioConvert, audioConvert2, audioEqualizer, elementSink);
                            Element.linkMany(elementSourceMic, audioCaptureFilter, audioConvert, audioMicVolume, audioEqualizer, audioAdder, audioConvert2, audioOutputFilter, elementSink);
                            elementSourceSystem.link(audioSysVolume);
                            audioSysVolume.link(audioAdder);
                            pipe.getBus().connect(new Bus.ERROR() {

                                public void errorMessage(GstObject arg0, int arg1, String arg2) {
                                    System.out.println("Error: " + arg0 + "," + arg1 + ", " + arg2);
                                    pipe.setState(State.NULL);
                                }
                            });

                            pipe.play();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void setLowFilter(int f) {
        low = f;
        audioEqualizer.set("band0", (float) f);
    }

    public void setMiddleFilter(int f) {
        middle = f;
        audioEqualizer.set("band1", (float) f);
    }

    public void setHighFilter(int f) {
        high = f;
        audioEqualizer.set("band2", (float) f);
    }

    public int getLowFilter() {
        return low;
    }

    public int getMiddleFilter() {
        return middle;
    }

    public int getHighFilter() {
        return high;
    }

    public void setMicVolume(int v) {
        micVolume = v;
        audioMicVolume.set("volume", (double) v / 100D);

    }
    public void setSysVolume(int v) {
        sysVolume = v;
        audioSysVolume.set("volume", (double) v / 100D);

    }

    public int getMicVolume() {
        return micVolume;
    }
    public int getSysVolume(){
        return sysVolume;
    }

    public void stop() {
        if (pipe != null && pipe.getState() == State.PLAYING) {
            //To be able to stop the pipeline, we must make sure that something as recorded
            // from the fifo_input
            Pipeline monitor = Pipeline.launch("pulsesrc device=fifo_input ! fakesink");
            monitor.play();
            pipe.stop();
            pipe.removeMany(audioEqualizer, audioMicVolume,audioSysVolume, elementSourceSystem, elementSourceMic, audioAdder, audioCaptureFilter, audioConvert, audioConvert2, audioOutputFilter, elementSink);
            pipe = null;
            monitor.stop();
            monitor = null;
        }
        isActive = false;
    }

    
    private Element elementSourceSystem = ElementFactory.make("pulsesrc", "pulsesrcSystem");
    private Element elementSourceMic = ElementFactory.make("pulsesrc", "pulsesrcMic");
    private Element elementSink = ElementFactory.make("filesink", "filesink");
    private Element audioCaptureFilter = ElementFactory.make("capsfilter", "audiocapturefilter");
    private Element audioOutputFilter = ElementFactory.make("capsfilter", "audiooutputfilter");
    private Element audioConvert = ElementFactory.make("audioconvert", "audioconvert");
    private Element audioConvert2 = ElementFactory.make("audioconvert", "audioconvert2");
    private Element audioEqualizer = ElementFactory.make("equalizer-3bands", "equalizer-3bands");
    private Element audioMicVolume = ElementFactory.make("volume", "micvolume");
    private Element audioSysVolume = ElementFactory.make("volume", "sysvolume");
    private Element audioAdder = ElementFactory.make("adder", "adder");
    private Pipeline pipe = null;

    public static void main(String[] args) {
        Gst.init();

    }
}
