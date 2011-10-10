/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.Timer;
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
    private Timer timer = null;

    public VideoSourceFullDesktop() {
        name = "Full Desktop";
        location = "";
        controls.add(new ControlRescale(this));
        controls.add(new webcamstudio.controls.ControlIdentity(this));

    }

    @Override
    public void stopSource() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (pipe != null) {
            pipe.stop();
            pipe = null;
        }
        image = null;
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
        String pipeline = "ximagesrc use-damage=true remote=false ! video/x-raw-rgb,framerate=" + frameRate + "/1 ! alpha ! ffmpegcolorspace ! videoscale method=2 ! video/x-raw-rgb,width=" + outputWidth + ",height=" + outputHeight + ",bpp=32,depth=24, red_mask=65280, green_mask=16711680, blue_mask=-16777216,alpha_mask=255,endianness=4321 ! ffmpegcolorspace name=tosink";
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
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer(name, true);
        timer.scheduleAtFixedRate(new VideoSourcePixelsRenderer(this), 0, 1000 / frameRate);
    }

    protected void updateOutputImage(BufferedImage img){
        image=img;
    }

    @Override
    public void rgbFrame(int w, int h, IntBuffer buffer) {
        captureWidth = w;
        captureHeight = h;
        int[] array = new int[w * h];
        buffer.get(array);
        pixels = array;
    }
}
