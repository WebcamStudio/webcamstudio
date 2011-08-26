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
package webcamstudio.sources;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gstreamer.*;
import webcamstudio.controls.ControlAudio;

/**
 *
 * @author pballeux
 */
public class VideoSourceMusic extends VideoSource {

    protected VideoSourceMusic() {
        captureWidth = 0;
        captureHeight = 0;
        opacity=0;
        image = null;
        controls.add(new ControlAudio(this));
    }

    public VideoSourceMusic(java.io.File loc) {

        location = loc.getAbsolutePath();
        name = loc.getName();
        hasSound = true;
        volume = 10;
        captureWidth = 0;
        captureHeight = 0;
        opacity=0;
        image = null;
        controls.add(new ControlAudio(this));
    }

    public VideoSourceMusic(java.net.URL loc) {

        location = loc.toString();
        name = loc.toString();
        hasSound = true;
        volume = 10;
        captureWidth = 0;
        captureHeight = 0;
        opacity=0;
        image = null;
        controls.add(new ControlAudio(this));
    }

    public VideoSourceMusic(String loc) {
        location = loc;
        name = loc;
        hasSound = true;
        volume = 10;
        captureWidth = 0;
        captureHeight = 0;
        opacity=0;
        image = null;
        controls.add(new ControlAudio(this));
    }

    public void setName(String n) {
        name = n;
    }

    public void stopSource() {
        stopMe = true;
        if (pipe != null) {

            pipe.stop();
            pipe.getState();
            java.util.List<Element> list = pipe.getElements();
            for (int i = 0; i < list.size(); i++) {
                list.get(i).disown();
                pipe.remove(list.get(i));
            }
            pipe = null;
        }
        elementSource = null;
        image = null;
        pixels = null;
        tempimage = null;
        isPlaying = false;
    }

    @Override
    public void startSource() {
        try {
            stopMe=false;
            isPlaying = true;
            loadSound = true;
            if (location.toLowerCase().startsWith("http://") || location.toLowerCase().startsWith("https://")) {
                elementSource = ElementFactory.make(GST_SOUPHTTPSRC, GST_SOUPHTTPSRC + uuId);
                elementSource.set("location", location);
            } else if (location.toLowerCase().startsWith("rtsp://")) {
                elementSource = ElementFactory.make(GST_RTSPSRC, GST_RTSPSRC + uuId);
                elementSource.set("location", location);
            } else if (location.toLowerCase().startsWith("tcp://")) {
                System.out.println("Connection with TCP");
                elementSource = ElementFactory.make(GST_TCPCLIENTSRC, GST_TCPCLIENTSRC + uuId);
                String[] addr = location.replaceFirst("tcp://", "").split(":");
                switch (addr.length) {
                    case 2:
                        elementSource.set("host", addr[0]);
                        elementSource.set("port", new Integer(addr[1]).intValue());
                        break;
                    case 1:
                        elementSource.set("host", addr[0]);
                        elementSource.set("port", 4888);
                        break;
                }
            } else {
                elementSource = ElementFactory.make(GST_FILESRC, GST_FILESRC + uuId);
                elementSource.set("location", location);
            }


            elementAudioSink = ElementFactory.make(audioSink, name + " (" + uuId + ")");

            if (outputHeight == 0 && outputWidth == 0) {
                outputWidth = captureWidth;
                outputHeight = captureHeight;
            }
            pipe = new Pipeline("WebcamStudio-" + uuId);
            //Adding elements to pipe
            pipe.add(elementSource);
                pipe.add(elementDecoder);
                    pipe.add(elementQueueAudio);
                    pipe.add(elementAudioSink);
                    elementAudioVolume.set("volume", (double) volume / 100D);
                    pipe.add(elementAudioVolume);
                    pipe.add(elementAudioRate);
                    pipe.add(elementAudioConvert);

            // Linking everything together

            if (elementDecoder != null && elementDecoder instanceof org.gstreamer.elements.DecodeBin) {
                elementSource.link(elementDecoder);
                ((org.gstreamer.elements.DecodeBin) elementDecoder).connect(new org.gstreamer.elements.DecodeBin.NEW_DECODED_PAD() {

                    public void newDecodedPad(Element elem, Pad pad, boolean last) {
                        /* only link once */
                        if (pad.isLinked()) {
                            return;
                        }
                        /* check media type */
                        Caps caps = pad.getCaps();
                        Structure struct = caps.getStructure(0);
                        if (struct.getName().startsWith("audio/") && loadSound) {
                            System.out.println("Audio pad: " + struct.getName());
                            pad.link(elementQueueAudio.getStaticPad("sink"));
                            elementQueueAudio.link(elementAudioRate);
                            elementAudioRate.link(elementAudioVolume);
                            elementAudioVolume.link(elementAudioConvert);
                            elementAudioConvert.link(elementAudioSink);
                        } else if (struct.getName().startsWith("video/")) {
                            System.out.println("Linking video pad: " + struct.getName());
                        } else {
                            System.out.println("Unknown pad [" + struct.getName() + "]");
                        }
                    }
                });
            } 

            pipe.getBus().connect(new Bus.EOS() {

                @Override
                public void endOfStream(GstObject arg0) {
                    pipe.stop();
                    if (doLoop) {
                        pipe.setState(State.PLAYING);
                    } else {
                        info("ENDOFSTREAM");
                    }
                }
            });
            pipe.getBus().connect(new Bus.ERROR() {

                @Override
                public void errorMessage(GstObject arg0, int arg1, String arg2) {
                    System.out.println("Movie Error:  " + arg0 + "," + arg1 + ", " + arg2);
                    error("Movie Error:  " + arg0 + "," + arg1 + ", " + arg2);
                    doLoop = false;
                }
            });
            pipe.setState(State.PLAYING);
            duration = pipe.queryDuration().toSeconds();
            if (startingPosition > 0){
                while(!stopMe && !pipe.isPlaying()){
                    Thread.sleep(100);
                }
                pipe.pause();
                pipe.seek(ClockTime.fromSeconds(startingPosition));
                pipe.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void seek(long secs) {
        startingPosition = secs;
        if (secs >= 0  && pipe != null && pipe.isPlaying()) {
            pipe.pause();
            pipe.seek(ClockTime.fromSeconds(secs));
            pipe.play();
        }
    }

    public boolean canUpdateSource() {
        return false;
    }

    public long getSeekPosition() {
        long pos = 0;
        if (pipe != null && (pipe.isPlaying() || pipe.getState() == State.PAUSED)) {
            pos = pipe.queryPosition().toSeconds();
        }
        return pos;
    }

    public long getDuration() {
        duration = 0;
        if (pipe != null) {
            duration = pipe.queryDuration().toSeconds();
        }
        return duration;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void pause() {
        if (pipe != null) {
            pipe.pause();
        }
    }

    public void play() {
        if (pipe != null) {
            pipe.play();
        }
    }

    @Override
    public void setVolume(int v) {
        volume = v;
        elementAudioVolume.set("volume", (double) v / 100D);

    }

    @Override
    public boolean isPaused() {
        boolean retValue = false;
        if (pipe != null) {
            retValue = (pipe.getState() == State.PAUSED);
        }
        return retValue;
    }

    public void activateEffect(Pipeline pipe) {
    }

    @Override
    public String toString() {
        return "Movie: " + new java.io.File(location).getName();
    }

    @Override
    public boolean hasText() {
        return false;
    }

    private static final String GST_FILESRC = "filesrc";
    private static final String GST_SOUPHTTPSRC = "souphttpsrc";
    private static final String GST_RTSPSRC = "rtspsrc";
    private static final String GST_TCPCLIENTSRC = "tcpclientsrc";
    protected static final String GST_DECODEBIN = "decodebin";
    private static final String GST_QUEUE = "queue";
    private static final String GST_VOLUME = "volume";
    private static final String GST_AUDIORATE = "audiorate";
    private static final String GST_AUDIOCONVERT = "audioconvert";
    protected Element elementSource = null;
    protected Element elementDecoder = new org.gstreamer.elements.DecodeBin(GST_DECODEBIN + uuId);
    protected Element elementAudioSink = null;
    private Element elementQueueAudio = ElementFactory.make(GST_QUEUE, GST_QUEUE + "A_" + uuId);
    private Element elementAudioRate = ElementFactory.make(GST_AUDIORATE, GST_AUDIORATE + uuId);
    private Element elementAudioVolume = ElementFactory.make(GST_VOLUME, GST_VOLUME + uuId);
    private Element elementAudioConvert = ElementFactory.make(GST_AUDIOCONVERT, GST_AUDIOCONVERT + uuId);
    private Pipeline pipe = null;
    private long duration = 0;
    private long startingPosition = 0;

    @Override
        public javax.swing.ImageIcon getThumbnail() {
        ImageIcon icon = getCachedThumbnail();
        if (icon==null){
            icon = new ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/audio-card.png"));
            try {
                saveThumbnail(icon);
            } catch (IOException ex) {
                Logger.getLogger(VideoSourceV4L.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return icon;
    }
}
