/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.nio.IntBuffer;
import java.util.Collection;
import javax.swing.JPanel;
import org.gstreamer.Bus;
import org.gstreamer.Element;
import org.gstreamer.GstObject;
import org.gstreamer.Pipeline;
import org.gstreamer.elements.RGBDataSink;
import webcamstudio.controls.ControlRescale;

/**
 *
 * @author patrick
 */
public class VideoSourceFullDesktop extends VideoSource implements RGBDataSink.Listener {

    private Pipeline pipe = null;
    private RGBDataSink sink = null;

    public VideoSourceFullDesktop() {
        name = "Desktop";
        location = "";

    }

    @Override
    public void stopSource() {
        if (pipe != null) {
            pipe.stop();
            pipe = null;
        }
        image = null;
    }

    @Override
    public Collection<JPanel> getControls() {
        java.util.Vector<JPanel> list = new java.util.Vector<JPanel>();
        list.add(new ControlRescale(this));
        list.add(new webcamstudio.controls.ControlActivity(this));
        list.add(new webcamstudio.controls.ControlIdentity(this));
        return list;
    }

    @Override
    public boolean canUpdateSource() {
        return false;
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isPlaying() {
        return (pipe != null);
    }

    @Override
    public void pause() {
    }

    @Override
    public void play() {
        if (pipe != null) {
            pipe.play();
        }
    }

    @Override
    public void startSource() {
        String pipeline = "ximagesrc use-damage=false remote=false ! video/x-raw-rgb,framerate=" + frameRate + "/1 ! ffmpegcolorspace ! videoscale ! video/x-raw-rgb,width=" + captureWidth + ",height=" + captureHeight + ",bpp=32,depth=24, red_mask=65280, green_mask=16711680, blue_mask=-16777216,endianness=4321 ! ffmpegcolorspace name=tosink";
        pipe = Pipeline.launch(pipeline);
        sink = new RGBDataSink("desktopsink", this);
        Element end = pipe.getElementByName("tosink");
        pipe.add(sink);
        end.link(sink);
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
                stopSource();
            }
        });
        pipe.play();
    }

    @Override
    public void rgbFrame(int w, int h, IntBuffer buffer) {
        captureWidth = w;
        captureHeight = h;
        if (!isRendering) {
            isRendering = true;
            tempimage = graphicConfiguration.createCompatibleImage(captureWidth, captureHeight, java.awt.image.BufferedImage.OPAQUE);
            int[] array = buffer.array();
            tempimage.setRGB(0, 0, captureWidth, captureHeight, array, 0, captureWidth);
            detectActivity(tempimage);
            image = tempimage;
            isRendering = false;
        }
    }

  
}
