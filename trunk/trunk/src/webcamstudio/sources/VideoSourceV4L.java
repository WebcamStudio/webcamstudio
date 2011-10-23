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
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import webcamstudio.controls.ControlRescale;
import webcamstudio.exporter.vloopback.VideoDevice;
import webcamstudio.ffmpeg.FFMPEGV4L2;

/**
 *
 * @author pballeux
 */
public class VideoSourceV4L extends VideoSource {

    private java.util.Timer timer = new Timer("V4L", true);
    protected FFMPEGV4L2 ffmpeg = new FFMPEGV4L2();

    public VideoSourceV4L() {
        outputWidth = 320;
        outputHeight = 240;
        frameRate = 15;
        captureWidth = 320;
        captureHeight = 240;
        doRescale = true;
        controls.add(new ControlRescale(this));
        controls.add(new webcamstudio.controls.ControlShapes(this));
        controls.add(new webcamstudio.controls.ControlEffects(this));
        controls.add(new webcamstudio.controls.ControlGSTEffects(this));
        controls.add(new webcamstudio.controls.ControlActivity(this));
        controls.add(new webcamstudio.controls.ControlFaceDetection(this));

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
        controls.add(new ControlRescale(this));
        controls.add(new webcamstudio.controls.ControlShapes(this));
        controls.add(new webcamstudio.controls.ControlEffects(this));
        controls.add(new webcamstudio.controls.ControlGSTEffects(this));
        controls.add(new webcamstudio.controls.ControlActivity(this));
        controls.add(new webcamstudio.controls.ControlFaceDetection(this));

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
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (!ffmpeg.isStopped()) {
            ffmpeg.stop();
        }
        image = null;
        isPlaying = false;
    }

    @Override
    public void startSource() {
        isPlaying = true;
        ffmpeg.setHeight(captureWidth);
        ffmpeg.setWidth(captureHeight);
        ffmpeg.setInput(location);
        ffmpeg.setRate(frameRate);
        ffmpeg.read();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer(name, true);
        timer.scheduleAtFixedRate(new imageV4L(this), 0, 1000 / frameRate);
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;


    }

    @Override
    public void pause() {
    }

    public void play() {
    }

    public boolean isPaused() {
        return false;
    }

    public boolean hasText() {
        return false;
    }

    @Override
    public String toString() {
        return name + " (" + captureAtX + "," + captureAtY + ":" + captureWidth + "x" + captureHeight + ")";


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

    protected void updateOutputImage(BufferedImage img) {
        if (img != null) {
            applyFaceDetection(img);
            detectActivity(img);
            applyEffects(img);
            applyShape(img);
            image = img;
        }
    }
}

class imageV4L extends TimerTask {

    VideoSourceV4L source = null;
    boolean isDrawing = false;

    public imageV4L(VideoSourceV4L source) {
        this.source = source;
    }

    @Override
    public void run() {
        if (!isDrawing) {
            isDrawing = true;
            BufferedImage img = source.ffmpeg.getImage();
            source.updateOutputImage(img);
            isDrawing = false;
        }
    }
}
