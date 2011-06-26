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
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gstreamer.*;
import org.gstreamer.elements.AppSink;
import webcamstudio.controls.ControlRescale;
import webcamstudio.exporter.vloopback.VideoDevice;

/**
 *
 * @author pballeux
 */
public class VideoSourceV4L extends VideoSource {

    protected String source = "v4lsrc";
    private VideoSink videosink = null;
    private AppSink sink = null;

    public VideoSourceV4L() {
        outputWidth = 320;
        outputHeight = 240;
        frameRate = 15;
        captureWidth = 320;
        captureHeight = 240;
        doRescale = true;

    }

    public VideoSourceV4L(String deviceName, File fallbackDevice) {
        outputWidth = 320;
        outputHeight = 240;
        name = deviceName;
        location = getDeviceForName(deviceName, fallbackDevice).getAbsolutePath();
        //If the name has changed, it will be reaffected...

        if (deviceName.length() == 0) {
            name = "???";
        }
        frameRate = 15;
        captureWidth = 320;
        captureHeight = 240;
        doRescale = true;

    }

    protected File getDeviceForName(String deviceName, File fallbackDevice) {
        File f = null;
        for (VideoDevice v : VideoDevice.getDevices()) {
            if (v.getName().equals(deviceName)) {
                f = v.getFile();
                break;
            }
        }
        if (f == null) {
            f = fallbackDevice;
            for (VideoDevice v : VideoDevice.getDevices()) {
                if (f.getAbsolutePath().equals(v.getFile().getAbsoluteFile())) {
                    name = v.getName();
                    break;
                }
            }
        }

        return f;
    }

    @Override
    public boolean canUpdateSource() {
        return false;
    }

    public void stopSource() {
        stopMe = true;
        if (videosink!=null){
            videosink.stop();
            videosink=null;
        }
        if (pipe != null) {

            pipe.stop();
            pipe.getState();
            sink = null;
            pipe = null;
        }
        image = null;
        pixels = null;
        tempimage = null;
        isPlaying = false;
    }

    @Override
    public void startSource() {
        isPlaying = true;
        location = getDeviceForName(name, new File(location)).getAbsolutePath();
        try {
            sink = (AppSink) ElementFactory.make("appsink", "appsink"+uuId);
            String rescaling = "";
            if (doRescale) {
                rescaling = " ! videoscale ! video/x-raw-yuv,width=" + captureWidth + ",height=" + captureHeight + " ! videorate ! video/x-raw-yuv,framerate=" + frameRate + "/1";
            }

            if (activeEffect.length() == 0) {
                pipe = Pipeline.launch(source + " device=" + location + " " + rescaling + " ! ffmpegcolorspace ! video/x-raw-rgb,bpp=32,depth=24, red_mask=65280, green_mask=16711680, blue_mask=-16777216 ! ffmpegcolorspace name=tosink");
            } else {
                pipe = Pipeline.launch(source + " device=" + location + " " + rescaling + " ! ffmpegcolorspace ! " + activeEffect + " ! ffmpegcolorspace ! video/x-raw-rgb,bpp=32,depth=24 !  ffmpegcolorspace name=tosink");

            }
            pipe.add(sink);
            Element e = pipe.getElementByName("tosink");
            e.link(sink);

            pipe.getBus().connect(new Bus.SEGMENT_START() {

                public void segmentStart(GstObject arg0, Format arg1, long arg2) {
                    System.out.println("SEGMENT_START: " + arg0.toString() + " : " + arg1 + ", " + arg2);
                }
            });
            pipe.getBus().connect(new Bus.INFO() {

                @Override
                public void infoMessage(GstObject arg0, int arg1, String arg2) {
                    System.out.println("INFO: " + arg1 + ", " + arg2);
                }
            });
            pipe.getBus().connect(new Bus.BUFFERING() {

                @Override
                public void bufferingData(GstObject arg0, int arg1) {
                    System.out.println("BUFFERING: " + arg1);
                }
            });

            pipe.getBus().connect(new Bus.EOS() {

                @Override
                public void endOfStream(GstObject arg0) {
                    pipe.stop();
                }
            });
            pipe.getBus().connect(new Bus.ERROR() {

                @Override
                public void errorMessage(GstObject arg0, int arg1, String arg2) {
                    error(name + " Error: " + arg0 + "," + arg1 + ", " + arg2);
                }
            });
            pipe.setState(State.PLAYING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (videosink!=null){
            videosink.stop();
        }
        videosink = new VideoSink(this, sink);
        videosink.start();
    }

    
    @Override
    public void setImage(BufferedImage img) {
        applyFaceDetection(img);
        detectActivity(img);
        applyEffects(img);
        applyShape(img);
        image=img;
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

    public boolean isPaused() {
        boolean retValue = false;


        if (pipe != null) {
            retValue = (pipe.getState() == State.PAUSED);


        }
        return retValue;


    }

    public boolean hasText() {
        return false;


    }

    @Override
    public String toString() {
        return name + " (" + captureAtX + "," + captureAtY + ":" + captureWidth + "x" + captureHeight + ")";


    }
    private org.gstreamer.elements.RGBDataSink elementSink = null;
    private Pipeline pipe = null;

    @Override
    public java.util.Collection<JPanel> getControls() {
        java.util.Vector<JPanel> list = new java.util.Vector<JPanel>();
        list.add(new ControlRescale(this));
        list.add(new webcamstudio.controls.ControlShapes(this));
        list.add(new webcamstudio.controls.ControlEffects(this));
        list.add(new webcamstudio.controls.ControlGSTEffects(this));
        list.add(new webcamstudio.controls.ControlActivity(this));
        list.add(new webcamstudio.controls.ControlFaceDetection(this));


        return list;


    }

    @Override
    public javax.swing.ImageIcon getThumbnail() {
        ImageIcon icon = getCachedThumbnail();
        if (icon == null) {
            icon = new ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/camera-video.png"));
            try {
                saveThumbnail(icon);
            } catch (IOException ex) {
                Logger.getLogger(VideoSourceV4L.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return icon;

    }
}
