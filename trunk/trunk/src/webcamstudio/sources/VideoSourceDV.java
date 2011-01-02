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

import javax.swing.JPanel;
import webcamstudio.*;
import org.gstreamer.*;

/**
 *
 * @author pballeux
 */
public class VideoSourceDV extends VideoSource implements org.gstreamer.elements.RGBDataSink.Listener {

    public VideoSourceDV() {

        location = "DV1394";
        name = location;
        hasSound = true;
        frameRate = 15;
        captureWidth=320;
        captureHeight=240;
        doRescale=true;
    }

    public boolean canUpdateSource() {
        return false;
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
    }

    public void rgbFrame(int w, int h, java.nio.IntBuffer buffer) {
        captureWidth=w;
        captureHeight=h;
        if (!isRendering) {
            isRendering = true;
            tempimage = graphicConfiguration.createCompatibleImage(captureWidth, captureHeight, java.awt.image.BufferedImage.TRANSLUCENT);
            tempimage.setRGB(0, 0, captureWidth, captureHeight, buffer.array(), 0, captureWidth);
            detectActivity(tempimage);
            applyEffects(tempimage);
            applyShape(tempimage);
            image=tempimage;
            isRendering = false;
        }

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
    public java.util.Collection<JPanel> getControls() {
        java.util.Vector<JPanel> list = new java.util.Vector<JPanel>();
        list.add(new webcamstudio.controls.ControlRescale(this));
        list.add(new webcamstudio.controls.ControlShapes(this));
        list.add(new webcamstudio.controls.ControlEffects(this));
        list.add(new webcamstudio.controls.ControlActivity(this));
        list.add(new webcamstudio.controls.ControlFaceDetection(this));
        list.add(new webcamstudio.controls.ControlLayout(this));
        return list;
    }
    private org.gstreamer.elements.RGBDataSink elementSink = null;
    private Pipeline pipe = null;
    private VideoEffects objVideoEffects = new VideoEffects();
}
