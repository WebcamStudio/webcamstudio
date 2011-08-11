/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.sources.*;
import java.awt.image.*;
import webcamstudio.exporter.VideoExporterStream;
import webcamstudio.exporter.vloopback.VideoOutput;
import webcamstudio.layout.Layout;
import webcamstudio.layout.LayoutItem;

/**
 *
 * @author pballeux
 */
public class Mixer {

    protected int frameRate = 20;
    protected int outputWidth = 320;
    protected int outputHeight = 240;
    protected java.awt.image.BufferedImage image = null;
    protected BufferedImage outputImage = null;
    protected boolean isDrawing = false;
    protected boolean lightMode = false;
    protected boolean stopMe = false;
    protected java.awt.image.BufferedImage paintImage = null;
    protected VideoOutput outputDevice = null;
    protected Image background = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/webcamstudio/resources/splash.jpg"));
    protected boolean activateOutputStream = false;
    protected VideoExporterStream outputStream = null;
    protected int outputStreamPort = 4888;
    protected java.awt.Graphics2D buffer = null;
    protected int[] dataImageOutput = null;
    protected int[] dataImageMixer = null;
    public enum MixerQuality {

        HIGH,
        GOOD,
        NORMAL,
        LOW
    };

    public void setBackground(Image img) {
        background = img;
    }
    private MixerQuality quality = MixerQuality.NORMAL;

    public Mixer() {
        new Thread(new imageMixer(this)).start();
    }

    public void setSize(int w, int h) {
        outputWidth = w;
        outputHeight = h;
    }

    public void setFramerate(int fps) {
        frameRate = fps;
    }

    public int getFramerate() {
        return frameRate;
    }

    public BufferedImage getImage() {
        return outputImage;
    }

    public void setPaintImage(java.awt.image.BufferedImage img) {
        paintImage = img;
    }

    public void setQuality(MixerQuality q) {
        quality = q;
    }

    protected void drawImage() {
        isDrawing = true;
        if (image == null || image.getWidth() != outputWidth || image.getHeight() != outputHeight) {
            image = new BufferedImage(outputWidth, outputHeight, BufferedImage.TRANSLUCENT);
            buffer = image.createGraphics();
            dataImageMixer = ((java.awt.image.DataBufferInt) image.getRaster().getDataBuffer()).getData();
        }
//        switch (quality) {
//            case HIGH:
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//
//                break;
//            case GOOD:
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_DEFAULT);
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//                break;
//            case NORMAL:
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_DEFAULT);
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED);
//                break;
//            case LOW:
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_SPEED);
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_DISABLE);
//                buffer.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED);
//                break;
//        }
        if (background == null) {
            buffer.setColor(java.awt.Color.DARK_GRAY);
            buffer.fillRect(0, 0, outputWidth, outputHeight);
        } else {
            buffer.drawImage(background, 0, 0, outputWidth, outputHeight, 0, 0, background.getWidth(null), background.getHeight(null), null);
        }

        Image img = null;
        try {
            Layout activeLayout = Layout.getActiveLayout();
            if (activeLayout != null) {
                for (LayoutItem item : activeLayout.getItems()) {
                    VideoSource source = item.getSource();
                    if (source.getActivityDetection() == 0 || (source.getActivityDetection() > 0 && source.activityDetected())) {

                        if (source.getImage() != null) {
                            img = source.getImage();
                            //Don't do anything if there is no rotation to do...
                            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, ((float) source.getOpacity()) / 100F));
                            //buffer.setClip(x1, y1, source.getOutputWidth(), source.getOutputHeight());
                            buffer.drawImage(img, source.getShowAtX(), source.getShowAtY(), source.getOutputWidth(), source.getOutputHeight(), null);
                            img = null;
                        }
                    }
                    source = null;
                }
            }
            activeLayout = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //buffer.dispose();
        //buffer = null;
        if (outputImage == null || outputImage.getWidth() != outputWidth || outputImage.getHeight() != outputHeight) {
            outputImage = new BufferedImage(outputWidth, outputHeight, BufferedImage.TRANSLUCENT);
            outputImage.getGraphics().clearRect(0, 0, outputWidth, outputHeight);
            dataImageOutput = ((java.awt.image.DataBufferInt) outputImage.getRaster().getDataBuffer()).getData();
        }
        System.arraycopy(dataImageMixer, 0, dataImageOutput, 0, dataImageOutput.length);
        //image = null;
        isDrawing = false;
    }

    public void setOutput(VideoOutput o) {
        outputDevice = o;
    }

    public VideoOutput getDevice() {
        return outputDevice;
    }

    public int getWidth() {
        return outputWidth;
    }

    public int getHeight() {
        return outputHeight;
    }

    public void activateStream(boolean activate, int port) {
        outputStreamPort = port;
        activateOutputStream = true;
    }

    public void stopStream() {
        activateOutputStream = false;
    }
}

class imageMixer implements Runnable {

    private Mixer mixer = null;

    public imageMixer(Mixer m) {
        mixer = m;
    }

    @Override
    public void run() {
        long timestamp = 0;
        while (!mixer.stopMe) {
            timestamp = System.currentTimeMillis();
            if (!mixer.isDrawing) {
                mixer.drawImage();
                if (mixer.activateOutputStream && mixer.outputStream == null) {
                    mixer.outputStream = new VideoExporterStream(mixer.outputStreamPort, mixer);
                    try {
                        mixer.outputStream.listen();
                    } catch (IOException ex) {
                        Logger.getLogger(Mixer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (!mixer.activateOutputStream && mixer.outputStream != null) {
                    mixer.outputStream.stop();
                }
                if (mixer.outputDevice != null) {
                    mixer.outputDevice.write(mixer.dataImageOutput);
                }
            }
            PreciseTimer.sleep(timestamp, 1000/mixer.frameRate);
        }

    }
}
