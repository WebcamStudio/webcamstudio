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
import webcamstudio.controls.ControlRescale;

/**
 *
 * @author pballeux
 */
public class VideoSourceMovie extends VideoSource implements org.gstreamer.elements.RGBDataSink.Listener {

    protected VideoSourceMovie() {
        frameRate = 24;
        captureWidth = 320;
        captureHeight = 240;
        doRescale = true;
    }

    public VideoSourceMovie(java.io.File loc) {

        location = loc.getAbsolutePath();
        name = loc.getName();
        colorspace = GST_COLORSPACE_YUV;
        hasSound = true;
        volume = 10;
        frameRate = 24;
        captureWidth = 320;
        captureHeight = 240;
        doRescale = true;
    }

    public VideoSourceMovie(java.net.URL loc) {

        location = loc.toString();
        name = loc.toString();
        colorspace = GST_COLORSPACE_YUV;
        hasSound = true;
        volume = 10;
        frameRate = 24;
        captureWidth = 320;
        captureHeight = 240;
        doRescale = true;
    }

    public VideoSourceMovie(String loc) {
        location = loc;
        name = loc;
        colorspace = GST_COLORSPACE_YUV;
        hasSound = true;
        volume = 10;
        frameRate = 24;
        captureWidth = 320;
        captureHeight = 240;
        doRescale = true;
    }

    public void setName(String n) {
        name = n;
    }

    public void stopSource() {
        stopMe = true;
        if (pipe != null) {

            pipe.stop();
            pipe.getState();
            elementSink = null;
            java.util.List<Element> list = pipe.getElements();
            for (int i = 0; i < list.size(); i++) {
                list.get(i).disown();
                pipe.remove(list.get(i));
            }
            pipe = null;
        }
        elementSink = null;
        elementSource = null;
        image = null;
        pixels = null;
        tempimage = null;
        isPlaying = false;
    }

    @Override
    public void startSource() {
        try {
            isPlaying = true;
            if (location.toLowerCase().startsWith("http://") || location.toLowerCase().startsWith("https://")) {
                elementSource = ElementFactory.make(GST_SOUPHTTPSRC, GST_SOUPHTTPSRC + uuId);
                elementSource.set("location", location);
            } else if (location.toLowerCase().startsWith("rtsp://")) {
                elementSource = ElementFactory.make(GST_RTSPSRC, GST_RTSPSRC + uuId);
                elementSource.set("location", location);
                loadSound = true;
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
            } else if (location.toLowerCase().startsWith("rfb://")) {
                System.out.println("Connection with RFB");
                elementSource = ElementFactory.make("rfbsrc", "rfbsrc" + uuId);
                elementDecoder = null;
                String[] addr = location.replaceFirst("rfb://", "").split(":");
                frameRate = 5;
                elementSource.set("view-only", true);
                elementSource.set("incremental", false);
                switch (addr.length) {
                    case 3:
                        elementSource.set("host", addr[0]);
                        elementSource.set("port", new Integer(addr[1]).intValue());
                        elementSource.set("password", addr[2]);
                        break;
                    case 2:
                        elementSource.set("host", addr[0]);
                        elementSource.set("port", new Integer(addr[1]).intValue());
                        break;
                    case 1:
                        elementSource.set("host", addr[0]);
                        elementSource.set("port", 5900);
                        break;
                }
                loadSound = false;
            } else {
                elementSource = ElementFactory.make(GST_FILESRC, GST_FILESRC + uuId);
                elementSource.set("location", location);
            }


            elementAudioSink = ElementFactory.make(audioSink, name + " (" + uuId + ")");

            if (outputHeight == 0 && outputWidth == 0) {
                outputWidth = captureWidth;
                outputHeight = captureHeight;
            }
            setOpacity(opacity);
            pipe = new Pipeline("WebcamStudio-" + uuId);
            elementSink = new org.gstreamer.elements.RGBDataSink("RGBDataSink" + uuId, this);
            elementSink.setPassDirectBuffer(true);
            //Adding elements to pipe
            pipe.add(elementQueueVideo);
            pipe.add(elementSource);
            if (elementDecoder != null) {
                pipe.add(elementDecoder);
                if (loadSound) {
                    pipe.add(elementQueueAudio);
                    pipe.add(elementAudioSink);
                    elementAudioVolume.set("volume", (double) volume / 100D);
                    pipe.add(elementAudioVolume);
                    pipe.add(elementAudioRate);
                    pipe.add(elementAudioConvert);
                }
            }
            pipe.add(elementFFMpegColorspace1);
            pipe.add(elementFFMpegColorspace2);
            this.effectSource = elementFFMpegColorspace2;
            this.effectSink = elementFFMpegColorspace2;

            pipe.add(elementSink);

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
                            pad.link(elementQueueVideo.getStaticPad("sink"));
                            elementQueueVideo.link(elementFFMpegColorspace1);
                        } else {
                            System.out.println("Unknown pad [" + struct.getName() + "]");
                        }
                    }
                });
            } else if (elementDecoder != null) {
                elementSource.link(elementDecoder);
                elementDecoder.link(elementQueueVideo);
                elementQueueVideo.link(elementFFMpegColorspace1);
            } else {
                elementSource.link(elementFFMpegColorspace1);
            }

            if (doRescale) {
                Element videorate = ElementFactory.make("videorate", "videorate" + uuId);
                Element videorateCaps = ElementFactory.make("capsfilter", "videoratecaps" + uuId);
                videorateCaps.setCaps(Caps.fromString("video/x-raw-yuv,framerate=" + frameRate + "/1"));
                Element videoscale = ElementFactory.make("videoscale", "videoscale" + uuId);
                Element videoscaleCaps = ElementFactory.make("capsfilter", "videoscalecaps" + uuId);
                videoscaleCaps.setCaps(Caps.fromString("video/x-raw-yuv,width=" + captureWidth + ",height=" + captureHeight));
                pipe.add(videorate);
                pipe.add(videorateCaps);
                pipe.add(videoscale);
                pipe.add(videoscaleCaps);
                elementFFMpegColorspace1.link(videoscale);
                videoscale.link(videoscaleCaps);
                videoscaleCaps.link(videorate);
                videorate.link(videorateCaps);
                videorateCaps.link(elementFFMpegColorspace2);
            } else {
                elementFFMpegColorspace1.link(elementFFMpegColorspace2);
            }
            activateEffect(pipe);
            Element sinkCaps = ElementFactory.make("capsfilter", "sinkCaps" + uuId);
            sinkCaps.setCaps(Caps.fromString("video/x-raw-rgb,bpp=32,depth=24, red_mask=65280, green_mask=16711680, blue_mask=-16777216"));
            pipe.add(sinkCaps);
            if (currentEffect != null) {
                pipe.add(currentEffect);
                pipe.add(elementFFMpegColorspace3);
                elementFFMpegColorspace2.link(currentEffect);
                currentEffect.link(elementFFMpegColorspace3);
                elementFFMpegColorspace3.link(sinkCaps);
                sinkCaps.link(elementSink);
            } else {
                elementFFMpegColorspace2.setCaps(Caps.fromString("video/x-raw-rgb,bpp=32,depth=24, red_mask=65280, green_mask=16711680, blue_mask=-16777216"));
                elementFFMpegColorspace2.link(sinkCaps);
                sinkCaps.link(elementSink);
            }
            pipe.getBus().connect(new Bus.EOS() {

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

                public void errorMessage(GstObject arg0, int arg1, String arg2) {
                    System.out.println("Movie Error:  " + arg0 + "," + arg1 + ", " + arg2);
                    error("Movie Error:  " + arg0 + "," + arg1 + ", " + arg2);
                    doLoop = false;
                }
            });
            pipe.setState(State.PLAYING);
            duration = pipe.queryDuration().toSeconds();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void rgbFrame(int w, int h, java.nio.IntBuffer buffer) {
        captureWidth = w;
        captureHeight = h;
        if (!isRendering) {
            isRendering = true;
            int[] array = new int[w * h];
            buffer.get(array);
            tempimage = graphicConfiguration.createCompatibleImage(captureWidth, captureHeight, java.awt.image.BufferedImage.TRANSLUCENT);
            if (activeEffect.equals("vertigotv") || activeEffect.equals("shagadelictv")) {
                for (int i = 0; i < array.length; i++) {
                    array[i] = array[i] | 0xFF000000;
                }
            }
            tempimage.setRGB(0, 0, captureWidth, captureHeight, array, 0, captureWidth);
            detectActivity(tempimage);
            applyEffects(tempimage);
            applyShape(tempimage);
            image = tempimage;
            isRendering = false;
        }

    }

    public void seek(long secs) {
        if (secs >= 0 && secs <= duration && pipe != null && pipe.isPlaying()) {
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
        if (activeEffect.length() == 0) {
            currentEffect = null;
        } else {
            currentEffect = ElementFactory.make(activeEffect, activeEffect + uuId);
        }
    }

    @Override
    public String toString() {
        return "Movie: " + new java.io.File(location).getName();
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public java.util.Collection<JPanel> getControls() {
        java.util.Vector<JPanel> list = new java.util.Vector<JPanel>();
        list.add(new ControlRescale(this));
        list.add(new webcamstudio.controls.ControlEffects(this));
        list.add(new webcamstudio.controls.ControlShapes(this));
        list.add(new webcamstudio.controls.ControlGSTEffects(this));
        list.add(new webcamstudio.controls.ControlActivity(this));
        list.add(new webcamstudio.controls.ControlFaceDetection(this));
        list.add(new ControlAudio(this));
        return list;
    }
    private static final String GST_FFMPEGCOLORSPACE = "ffmpegcolorspace";
    private static final String GST_FILESRC = "filesrc";
    private static final String GST_SOUPHTTPSRC = "souphttpsrc";
    private static final String GST_RTSPSRC = "rtspsrc";
    private static final String GST_TCPCLIENTSRC = "tcpclientsrc";
    private static final String GST_VIDEORATE = "videorate";
    private static final String GST_VIDEOSCALE = "videoscale";
    private static final String GST_CAPSFILTER = "capsfilter";
    protected static final String GST_DECODEBIN = "decodebin";
    private static final String GST_VIDEOBALANCE = "videobalance";
    private static final String GST_GAMMA = "gamma";
    protected static final String GST_COLORSPACE_YUV = "video/x-raw-yuv";
    protected static final String GST_COLORSPACE_RGB = "video/x-raw-rgb";
    private static final String GST_QUEUE = "queue";
    private static final String GST_VOLUME = "volume";
    private static final String GST_AUDIORATE = "audiorate";
    private static final String GST_AUDIOCONVERT = "audioconvert";
    protected String colorspace = GST_COLORSPACE_YUV;
    private Element effectSource = null;
    private Element currentEffect = null;
    private Element effectSink = null;
    protected Element elementSource = null;
    protected Element elementDecoder = new org.gstreamer.elements.DecodeBin(GST_DECODEBIN + uuId);
    private org.gstreamer.elements.RGBDataSink elementSink = null;
    private Element elementFFMpegColorspace1 = ElementFactory.make(GST_FFMPEGCOLORSPACE, "ffmpegcolorspace1" + uuId);
    private Element elementFFMpegColorspace2 = ElementFactory.make(GST_FFMPEGCOLORSPACE, "ffmpegcolorspace2" + uuId);
    private Element elementFFMpegColorspace3 = ElementFactory.make(GST_FFMPEGCOLORSPACE, "ffmpegcolorspace3" + uuId);
    protected Element elementAudioSink = null;
    private Element elementQueueAudio = ElementFactory.make(GST_QUEUE, GST_QUEUE + "A_" + uuId);
    private Element elementQueueVideo = ElementFactory.make(GST_QUEUE, GST_QUEUE + "V_" + uuId);
    private Element elementAudioRate = ElementFactory.make(GST_AUDIORATE, GST_AUDIORATE + uuId);
    private Element elementAudioVolume = ElementFactory.make(GST_VOLUME, GST_VOLUME + uuId);
    private Element elementAudioConvert = ElementFactory.make(GST_AUDIOCONVERT, GST_AUDIOCONVERT + uuId);
    private Pipeline pipe = null;
    private long duration = 0;

    @Override
    public ImageIcon getThumbnail() {
        ImageIcon icon = getCachedThumbnail();
        if (icon == null) {
            loadSound = false;
            startSource();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(VideoSourceMovie.class.getName()).log(Level.SEVERE, null, ex);
            }
            pipe.seek(ClockTime.fromSeconds(300));
            image = null;
            while (image == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(VideoSourceMovie.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            icon = new ImageIcon(image.getScaledInstance(32, 32, BufferedImage.SCALE_FAST));



            try {
                saveThumbnail(new ImageIcon(image.getScaledInstance(128, 128, BufferedImage.SCALE_FAST)));
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
                Logger.getLogger(VideoSourceMovie.class.getName()).log(Level.SEVERE, null, ex);
            }
            stopSource();
            loadSound = true;
        }
        return icon;
    }
}
