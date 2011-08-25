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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import webcamstudio.*;
import org.gstreamer.*;

/**
 *
 * @author pballeux
 */
public class VideoSourceDV extends VideoSource implements org.gstreamer.elements.RGBDataSink.Listener {

    private Timer timer = null;
    public VideoSourceDV() {

        location = "DV1394";
        name = location;
        hasSound = true;
        frameRate = 15;
        captureWidth=320;
        captureHeight=240;
        doRescale=true;
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
        stopMe = true;
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
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
        image = null;
        pixels = null;
        tempimage = null;
        isPlaying = false;
    }

    @Override
    public void startSource() {
        isPlaying = true;
        elementSink = new org.gstreamer.elements.RGBDataSink("RGBDataSink" + uuId, this);
        String rescaling = "";
        if (doRescale){
            rescaling = "! videoscale ! video/x-raw-yuv,width="+captureWidth+",height="+captureHeight+" ! videorate ! video/x-raw-yuv,framerate="+frameRate+"/1";
        }
        pipe = Pipeline.launch("dv1394src ! queue ! dvdemux name=d ! dvdec quality=4 ! queue ! deinterlace "+rescaling+" ! ffmpegcolorspace ! video/x-raw-rgb,bpp=32,depth=24, red_mask=65280, green_mask=16711680, blue_mask=-16777216 ! ffmpegcolorspace name=tosink");
        pipe.add(elementSink);
        Element e = pipe.getElementByName("tosink");
        e.link(elementSink);
        pipe.getBus().connect(new Bus.EOS() {

            public void endOfStream(GstObject arg0) {
                pipe.stop();
                if (doLoop) {
                    pipe.setState(State.PLAYING);
                }
            }
        });
        pipe.getBus().connect(new Bus.ERROR() {

            public void errorMessage(GstObject arg0, int arg1, String arg2) {
                error(name + " Error: " + arg0 + "," + arg1 + ", " + arg2);
                stopSource();
            }
        });
        pipe.setState(State.PLAYING);
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
        timer = new Timer(name,true);
        timer.scheduleAtFixedRate(new VideoSourcePixelsRenderer(this), 0,1000/frameRate);
    }
    protected void updateOutputImage(BufferedImage img) {
        detectActivity(img);
        applyEffects(img);
        applyShape(img);
        image=img;
    }

    public void rgbFrame(int w, int h, java.nio.IntBuffer buffer) {
        captureWidth=w;
        captureHeight=h;
        int[] array = new int[w*h];
        buffer.get(array);
        pixels=array;

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

    public boolean hasText() {
        return false;
    }

    public boolean isPaused() {
        boolean retValue = false;
        if (pipe != null) {
            retValue = (pipe.getState() == State.PAUSED);
        }
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

    private org.gstreamer.elements.RGBDataSink elementSink = null;
    private Pipeline pipe = null;
    private VideoEffects objVideoEffects = new VideoEffects();
}
