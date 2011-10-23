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
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import webcamstudio.controls.ControlAudio;
import webcamstudio.controls.ControlRescale;
import webcamstudio.ffmpeg.FFMPEGMovies;

/**
 *
 * @author pballeux
 */
public class VideoSourceMovie extends VideoSource {

    protected java.util.Timer timer = null;
    protected long startingPosition = 0;
    protected FFMPEGMovies ffmpeg = new FFMPEGMovies();
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
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (!ffmpeg.isStopped()){
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
        ffmpeg.setSeek(startingPosition);
        ffmpeg.setVolume(volume);
        ffmpeg.read();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer(name, true);
        timer.scheduleAtFixedRate(new imageMovie(this), 0, 1000 / frameRate);
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
        return isPlaying;
    }

    @Override
    public void pause() {
    }

    public void play() {
    }

    @Override
    public void setVolume(int v) {
        volume=v;
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
        image = img;
    }
}
class imageMovie extends TimerTask{

    VideoSourceMovie source = null;
    boolean isDrawing=false;
    public imageMovie(VideoSourceMovie m){
        source=m;
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