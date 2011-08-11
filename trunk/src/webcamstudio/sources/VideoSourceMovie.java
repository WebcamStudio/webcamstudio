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
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gstreamer.*;
import org.gstreamer.elements.RGBDataSink;
import webcamstudio.controls.ControlAudio;
import webcamstudio.controls.ControlRescale;

/**
 *
 * @author pballeux
 */
public class VideoSourceMovie extends VideoSource implements RGBDataSink.Listener {

    private RGBDataSink sink = null;

    protected VideoSourceMovie() {
        frameRate = 24;
        captureWidth = 320;
        captureHeight = 240;
        doRescale = true;
        keepRatio = true;
    }

    public VideoSourceMovie(java.io.File loc) {

        location = loc.getAbsolutePath();
        name = loc.getName();
        hasSound = true;
        volume = 10;
        frameRate = 24;
        captureWidth = 320;
        captureHeight = 240;
        doRescale = true;
        keepRatio = true;
    }

    public VideoSourceMovie(java.net.URL loc) {

        location = loc.toString();
        name = loc.toString();
        hasSound = true;
        volume = 10;
        frameRate = 24;
        captureWidth = 320;
        captureHeight = 240;
        doRescale = true;
        keepRatio = true;
    }

    public VideoSourceMovie(String loc) {
        location = loc;
        name = loc;

        hasSound = true;
        volume = 10;
        frameRate = 24;
        captureWidth = 320;
        captureHeight = 240;
        doRescale = true;
        keepRatio = true;
    }

    public void setName(String n) {
        name = n;
    }

    public void stopSource() {
        stopMe = true;
        if (pipe != null) {

            pipe.stop();
            pipe.getState(100);
            sink = null;
            java.util.List<Element> list = pipe.getElements();
            for (int i = 0; i < list.size(); i++) {
                list.get(i).disown();
                pipe.remove(list.get(i));
            }
            pipe = null;
        }
        sink = null;
        image = null;
        pixels = null;
        tempimage = null;
        isPlaying = false;
    }

    @Override
    public void startSource() {
        String pipeline = "";
        try {
            isPlaying = true;
            if (location.toLowerCase().startsWith("http://") || location.toLowerCase().startsWith("https://")) {
                pipeline = "souphttpsrc location=\"" + location + "\" ! decodebin name=decode ";
                loadSound = true;
            } else if (location.toLowerCase().startsWith("rtsp://")) {
                pipeline = "rtspsrc location=\"" + location + "\" ! decodebin name=decode ";
                loadSound = true;
            } else if (location.toLowerCase().startsWith("tcp://")) {
                pipeline = "tcpclientsrc ";

                String[] addr = location.replaceFirst("tcp://", "").split(":");
                switch (addr.length) {
                    case 2:
                        pipeline += " host=" + addr[0];
                        pipeline += " port=" + addr[1];
                        break;
                    case 1:
                        pipeline += " host=" + addr[0];
                        pipeline += " port=4888";
                        break;
                }
                pipeline += " ! decodebin  name=decode ";
                loadSound = true;
            } else if (location.toLowerCase().startsWith("rfb://")) {

                pipeline = "rfbsrc view-only=true incremental=false";
                String[] addr = location.replaceFirst("rfb://", "").split(":");
                switch (addr.length) {
                    case 3:
                        pipeline += " host=" + addr[0];
                        pipeline += " port=" + addr[1];
                        pipeline += " password=" + addr[2];
                        break;
                    case 2:
                        pipeline += " host=" + addr[0];
                        pipeline += " port=" + addr[1];
                        break;
                    case 1:
                        pipeline += " host=" + addr[0];
                        break;
                }
                loadSound = false;
            } else {
                pipeline = "filesrc location=\"" + location + "\" ! decodebin  name=decode ";
                loadSound = true;
            }

            pipeline += " ! ffmpegcolorspace ";
            if (outputHeight == 0 && outputWidth == 0) {
                outputWidth = captureWidth;
                outputHeight = captureHeight;
            }
            if (doRescale) {
                pipeline += " ! videorate ! video/x-raw-yuv,framerate=" + frameRate + "/1 ! videoscale ! video/x-raw-yuv,width=" + captureWidth + ",height=" + captureHeight;
            }
            if (activeEffect.length() != 0) {
                pipeline += " ! ffmpegcolorspace ! " + activeEffect + " ! ffmpegcolorspace ";
            }
            pipeline += " ! alpha ! ffmpegcolorspace ! video/x-raw-rgb,bpp=32,depth=24, red_mask=65280, green_mask=16711680, blue_mask=-16777216 ! ffmpegcolorspace name=tosink";
            if (loadSound) {
                pipeline += " decode. ! queue ! volume name=volume volume=" + (double) volume / 100D + " ! audioconvert ! autoaudiosink ";
            }
            System.out.println(pipeline);
            pipe = Pipeline.launch(pipeline);
            sink = new RGBDataSink(name + uuId, this);
            pipe.add(sink);
            Element lastElement = pipe.getElementByName("tosink");
            lastElement.link(sink);
            if (loadSound) {
                elementAudioVolume = pipe.getElementByName("volume");
            }
            if (elementAudioVolume == null) {
                hasSound = false;
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
            new Thread(new Runnable() {

                @Override
                public void run() {
                    pipe.setState(State.PLAYING);
                    duration = pipe.queryDuration().toSeconds();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setImage(BufferedImage img) {
        detectActivity(img);
        applyEffects(img);
        applyShape(img);
        if (image == null || image.getWidth() != img.getWidth() || image.getHeight() != img.getHeight()) {
            image = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TRANSLUCENT);
            dataOutputImage = ((java.awt.image.DataBufferInt) image.getRaster().getDataBuffer()).getData();
        }
        image.setRGB(0, 0, captureWidth, captureHeight, dataInputImage, 0, captureWidth);
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
        if (pipe != null && (pipe.isPlaying() || pipe.getState(100) == State.PAUSED)) {
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
        if (elementAudioVolume != null) {
            elementAudioVolume.set("volume", (double) v / 100D);
        } else {
            hasSound = false;
        }

    }

    @Override
    public boolean isPaused() {
        boolean retValue = false;
        if (pipe != null) {
            retValue = (pipe.getState(100) == State.PAUSED);
        }
        return retValue;
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
        if (hasSound) {
            list.add(new ControlAudio(this));
        }
        return list;
    }
    private Element elementAudioVolume = null;
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

    @Override
    public void rgbFrame(int w, int h, IntBuffer buffer) {
        captureWidth = w;
        captureHeight = h;
        if (!isRendering) {
            isRendering = true;
            if (tempimage == null || tempimage.getWidth() != w || tempimage.getHeight() != h) {
                tempimage = graphicConfiguration.createCompatibleImage(captureWidth, captureHeight, java.awt.image.BufferedImage.TRANSLUCENT);
                dataInputImage = ((java.awt.image.DataBufferInt) tempimage.getRaster().getDataBuffer()).getData();
            }
            int[] array = buffer.array();
            tempimage.setRGB(0, 0, w, h, array, 0, w);
            setImage(tempimage);
            isRendering = false;
        }
    }
}
