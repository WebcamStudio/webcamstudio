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
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import webcamstudio.ffmpeg.FFMPEGDV;

/**
 *
 * @author pballeux
 */
public class VideoSourceDV extends VideoSource {

    private Timer timer = null;
    protected FFMPEGDV ffmpeg = new FFMPEGDV();

    public VideoSourceDV() {

        location = "";
        name = location;
        hasSound = true;
        frameRate = 15;
        captureWidth = 320;
        captureHeight = 240;
        doRescale = true;
        controls.add(new webcamstudio.controls.ControlRescale(this));
        controls.add(new webcamstudio.controls.ControlShapes(this));
        controls.add(new webcamstudio.controls.ControlEffects(this));
        controls.add(new webcamstudio.controls.ControlActivity(this));
        controls.add(new webcamstudio.controls.ControlFaceDetection(this));
    }

    public boolean canUpdateSource() {
        return false;
    }

    public void stopSource() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (!ffmpeg.isStopped()) {
        }
        image = null;
        pixels = null;
        tempimage = null;
        isPlaying = false;
    }

    @Override
    public void startSource() {
        isPlaying = true;
        ffmpeg.setRate(frameRate);
        ffmpeg.setWidth(outputWidth);
        ffmpeg.setHeight(outputHeight);
        ffmpeg.read();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer(name, true);
        timer.scheduleAtFixedRate(new imageDV(this), 0, 1000 / frameRate);
    }

    protected void updateOutputImage(BufferedImage img) {
        if (img != null) {
            detectActivity(img);
            applyEffects(img);
            applyShape(img);
            image = img;
        }
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

    public boolean hasText() {
        return false;
    }

    public boolean isPaused() {
        boolean retValue = false;
        return retValue;
    }

    @Override
    public String toString() {
        return name;
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

class imageDV extends TimerTask {

    VideoSourceDV source = null;

    public imageDV(VideoSourceDV s) {
        source = s;
    }

    @Override
    public void run() {
        source.updateOutputImage(source.ffmpeg.getImage());
    }
}