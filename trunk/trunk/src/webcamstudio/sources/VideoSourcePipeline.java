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

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gstreamer.*;

/**
 *
 * @author pballeux
 */
public class VideoSourcePipeline extends VideoSource implements org.gstreamer.elements.RGBDataSink.Listener {

    private String gstPipeline = "videotestsrc ! video/x-raw-rgb,width=320,height=240 ! ffmpegcolorspace name=tosink";
    private String iconFile = null;

    public VideoSourcePipeline(File pluginFile) {

        location = pluginFile.getAbsolutePath();
        name = pluginFile.getName();
        getPipeline();
        frameRate = 15;
        outputWidth = 320;
        outputHeight = 240;
    }

    public VideoSourcePipeline() {
        frameRate = 15;
        outputWidth = 320;
        outputHeight = 240;
    }

    private String getPipeline() {
        String retValue = "";
        File pluginFile = new File(location);
        if (pluginFile.exists()) {
            try {
                java.util.Properties plugin = new java.util.Properties();
                plugin.load(pluginFile.toURI().toURL().openStream());
                name = plugin.getProperty("name", name);
                iconFile = plugin.getProperty("icon", null);
                retValue = plugin.getProperty("pipeline");
                hasSound = false;
            } catch (IOException ex) {
                Logger.getLogger(VideoSourcePipeline.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return retValue;
    }

    public void setName(String n) {
        name = n;
    }

    public VideoSourcePipeline(String pipeline) {
        location = "Auto Detect";
        name = location;
        frameRate = 15;
        outputWidth = 320;
        outputHeight = 240;
        gstPipeline = pipeline + "";
        hasSound = false;
    }

    @Override
    public boolean canUpdateSource() {
        return false;
    }

    @Override
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
        try {
            gstPipeline = getPipeline();
            elementSink = new org.gstreamer.elements.RGBDataSink("RGBDataSink" + uuId, this);
            System.out.println(gstPipeline);
            pipe = Pipeline.launch(gstPipeline);
            pipe.add(elementSink);
            Element e = pipe.getElementByName("tosink");
            e.link(elementSink);
            pipe.getBus().connect(new Bus.EOS() {

                @Override
                public void endOfStream(GstObject arg0) {
                    pipe.stop();
                    if (doLoop) {
                        pipe.setState(State.PLAYING);
                    }
                }
            });
            pipe.getBus().connect(new Bus.ERROR() {

                @Override
                public void errorMessage(GstObject arg0, int arg1, String arg2) {
                    error(name + " Error: " + arg0 + "," + arg1 + ", " + arg2);
                    stopSource();
                }
            });
            pipe.setState(State.PLAYING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void rgbFrame(int w, int h, java.nio.IntBuffer buffer) {
        captureWidth = w;
        captureHeight = h;
        if (!isRendering) {
            isRendering = true;
            tempimage = graphicConfiguration.createCompatibleImage(captureWidth, captureHeight, java.awt.image.BufferedImage.TRANSLUCENT);
            int[] array = buffer.array();
            for (int i = 0; i < array.length; i++) {
                array[i] = array[i] | 0xFF000000;
            }
            tempimage.setRGB(0, 0, captureWidth, captureHeight, array, 0, captureWidth);
            detectActivity(tempimage);
            applyEffects(tempimage);
            applyShape(tempimage);
            image = tempimage;
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

    @Override
    public void play() {
        if (pipe != null) {
            pipe.play();
        }
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
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
        list.add(new webcamstudio.controls.ControlShapes(this));
        list.add(new webcamstudio.controls.ControlEffects(this));
        list.add(new webcamstudio.controls.ControlActivity(this));
        list.add(new webcamstudio.controls.ControlFaceDetection(this));

        return list;
    }
    private org.gstreamer.elements.RGBDataSink elementSink = null;
    private Pipeline pipe = null;

    @Override
    public javax.swing.ImageIcon getThumbnail() {
        ImageIcon icon = super.getCachedThumbnail();
        if (icon == null) {
            if (iconFile == null) {
                icon = super.getThumbnail();
            } else {
                File resource = new File(iconFile);
                if (!resource.exists()){
                    //Try to find the image at the same place as the pipeline
                    resource = new File(new File(location).getParentFile(),iconFile);
                }
                try {
                    icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(resource.toURI().toURL()).getScaledInstance(32, 32, Image.SCALE_FAST));
                } catch (MalformedURLException ex) {
                    Logger.getLogger(VideoSourcePipeline.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                saveThumbnail(icon);
            } catch (IOException ex) {
                Logger.getLogger(VideoSourceWidget.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return icon;
    }
}
