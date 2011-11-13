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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import webcamstudio.controls.ControlAudio;
import webcamstudio.ffmpeg.FFMPEGCapture;
import webcamstudio.mixers.AudioMixer;


/**
 *
 * @author pballeux
 */
public class VideoSourceMusic extends VideoSource {

    FFMPEGCapture ffmpeg = new FFMPEGCapture("music",null,AudioMixer.getInstance());

    protected VideoSourceMusic() {
        captureWidth = 0;
        captureHeight = 0;
        opacity = 0;
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
        opacity = 0;
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
        opacity = 0;
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
        opacity = 0;
        image = null;
        controls.add(new ControlAudio(this));
    }

    public void setName(String n) {
        name = n;
    }

    public void stopSource() {
        stopMe = true;
        if (!ffmpeg.isStopped()) {
            ffmpeg.stop();
        }
        image = null;
        pixels = null;
        tempimage = null;
        isPlaying = false;
    }

    @Override
    public void startSource() {
        isPlaying=true;
        stopMe=false;
        ffmpeg.setVolume(volume);
        ffmpeg.setFile(new File(location));
        ffmpeg.setSeek(startingPosition);
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
        return isPlaying;
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
        return "Music: " + new java.io.File(location).getName();
    }

    @Override
    public boolean hasText() {
        return false;
    }
    private long duration = 0;
    private long startingPosition = 0;

    @Override
    public javax.swing.ImageIcon getThumbnail() {
        ImageIcon icon = getCachedThumbnail();
        if (icon == null) {
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
