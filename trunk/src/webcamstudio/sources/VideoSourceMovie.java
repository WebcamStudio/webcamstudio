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
import java.util.ArrayList;
import javax.swing.ImageIcon;
import webcamstudio.controls.ControlAudio;
import webcamstudio.controls.ControlRescale;
import webcamstudio.ffmpeg.FFMPEGCapture;
import webcamstudio.media.Image;
import webcamstudio.mixers.AudioMixer;
import webcamstudio.mixers.VideoListener;
import webcamstudio.mixers.VideoMixer;

/**
 *
 * @author pballeux
 */
public class VideoSourceMovie extends VideoSource implements VideoListener {

    protected long startingPosition = 0;
    protected FFMPEGCapture ffmpeg = null;
    private ArrayList<Image> videoBuffer = new ArrayList<Image>();

    protected VideoSourceMovie() {
        frameRate = 24;
        captureWidth = 320;
        captureHeight = 240;
        doRescale = true;
        keepRatio = true;
        controls.add(new ControlRescale(this));
        controls.add(new webcamstudio.controls.ControlEffects(this));
        controls.add(new webcamstudio.controls.ControlShapes(this));
        controls.add(new webcamstudio.controls.ControlActivity(this));
        controls.add(new webcamstudio.controls.ControlFaceDetection(this));
        controls.add(new ControlAudio(this));

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
        controls.add(new ControlRescale(this));
        controls.add(new webcamstudio.controls.ControlEffects(this));
        controls.add(new webcamstudio.controls.ControlShapes(this));
        controls.add(new webcamstudio.controls.ControlActivity(this));
        controls.add(new webcamstudio.controls.ControlFaceDetection(this));
        controls.add(new ControlAudio(this));
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
        controls.add(new ControlRescale(this));
        controls.add(new webcamstudio.controls.ControlEffects(this));
        controls.add(new webcamstudio.controls.ControlShapes(this));
        controls.add(new webcamstudio.controls.ControlActivity(this));
        controls.add(new webcamstudio.controls.ControlFaceDetection(this));
        controls.add(new ControlAudio(this));
    }

    public void setName(String n) {
        name = n;
    }

    public void stopSource() {
        stopMe = true;
        if (ffmpeg != null && !ffmpeg.isStopped()) {
            ffmpeg.stop();
        }
        image = null;
        isPlaying = false;
    }

    @Override
    public void startSource() {
        ffmpeg = new FFMPEGCapture("movie", this, AudioMixer.getInstance());
        isPlaying = true;
        ffmpeg.setHeight(captureWidth);
        ffmpeg.setWidth(captureHeight);
        ffmpeg.setFile(new File(location));
        ffmpeg.setRate(frameRate);
        ffmpeg.setSeek(startingPosition);
        ffmpeg.setVolume(volume);
        ffmpeg.read();
    }

    public void seek(long secs) {
        startingPosition = secs;
    }

    public boolean canUpdateSource() {
        return false;
    }

    public long getSeekPosition() {
        long pos = 0;
        return pos;
    }

    public long getDuration() {
        duration = 0;
        return duration;
    }

    @Override
    public boolean isPlaying() {
        return (ffmpeg != null && !ffmpeg.isStopped());
    }

    @Override
    public void pause() {
    }

    public void play() {
    }

    @Override
    public void setVolume(int v) {
        volume = v;
    }

    @Override
    public boolean isPaused() {
        boolean retValue = false;
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
    private long duration = 0;

    @Override
    public ImageIcon getThumbnail() {
        ImageIcon icon = getCachedThumbnail();
        return icon;
    }

    @Override
    protected void updateOutputImage(BufferedImage img) {
        detectActivity(img);
        applyEffects(img);
        applyShape(img);
    }

    @Override
    public BufferedImage getImage() {
        BufferedImage img = getCurrentImage();
        if (img != null) {
            image = img;
        }
        return image;
    }

    @Override
    public void newImage(Image image) {
        updateOutputImage(image.getImage());
        videoBuffer.add(image);
    }

    private BufferedImage getCurrentImage() {
        BufferedImage img = null;
        long timecode = VideoMixer.getTimeCode();
        int loop = 0;
        if (videoBuffer.size() > 0) {
            Image image = videoBuffer.remove(0);
            while (image != null && (image.getTimeCode()) <= timecode) {
                loop++;
                img = image.getImage();
                if (videoBuffer.size() > 0) {
                    image = videoBuffer.remove(0);
                } else {
                    break;
                }
            }
        }
        return img;
    }
}
