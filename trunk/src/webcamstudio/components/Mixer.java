/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.image.*;
import java.util.TimerTask;
import webcamstudio.exporter.VideoExporterStream;
import webcamstudio.exporter.vloopback.VideoOutput;
import webcamstudio.layout.Layout;
import webcamstudio.layout.LayoutItem;
import webcamstudio.sources.VideoSource;

/**
 *
 * @author pballeux
 */
public class Mixer {

    protected int frameRate = 15;
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
    private int x, y, w, h, o;
    private Image img = null;
    private GraphicsConfiguration graphicConfiguration = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    protected int frameCount = 0;
    java.util.Timer timer = null;

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
        setFramerate(frameRate);
        //new Thread(new imageMixer(this)).start();
    }

    public void setSize(int w, int h) {
        outputWidth = w;
        outputHeight = h;
    }

    public void setFramerate(int fps) {
        frameRate = fps;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new java.util.Timer("Mixer DrawImage", true);
        timer.scheduleAtFixedRate(new drawMixerImage(this), 0L, (long) (1000 / frameRate));
        timer.scheduleAtFixedRate(new outputMixerImage(this), 0L, (long) (1000 / frameRate));
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
        //if (image == null || image.getWidth() != outputWidth || image.getHeight() != outputHeight) {
            image = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_ARGB);
            buffer = image.createGraphics();
        //    dataImageMixer = ((java.awt.image.DataBufferInt) image.getRaster().getDataBuffer()).getData();
        //}
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


        try {
            Layout activeLayout = Layout.getActiveLayout();
            if (activeLayout != null) {
                Object[] li = activeLayout.getItems().toArray();
                activeLayout = null;
                for (int i = 0; i < li.length; i++) {
                    LayoutItem item = (LayoutItem) li[i];
                    VideoSource source = item.getSource();
                    if (source.getActivityDetection() == 0 || (source.getActivityDetection() > 0 && source.activityDetected())) {
                        if (source.getImage() != null) {
                            x = source.getShowAtX();
                            y = source.getShowAtY();
                            w = source.getOutputWidth();
                            h = source.getOutputHeight();
                            o = source.getOpacity();
                            img = source.getImage();
                            source = null;
                            //Don't do anything if there is no rotation to do...
                            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, ((float) (o)) / 100F));
                            //buffer.setClip(x1, y1, source.getOutputWidth(), source.getOutputHeight());
                            buffer.drawImage(img, x, y, w, h, null);
                            img = null;
                        }
                    }
                }
            }
            activeLayout = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //buffer.dispose();
        //buffer = null;
//        if (outputImage == null || outputImage.getWidth() != outputWidth || outputImage.getHeight() != outputHeight) {
//            outputImage = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_ARGB);
//            outputImage.getGraphics().clearRect(0, 0, outputWidth, outputHeight);
//            dataImageOutput = ((java.awt.image.DataBufferInt) outputImage.getRaster().getDataBuffer()).getData();
//        }
//        outputImage.setRGB(0, 0, outputWidth, outputHeight, dataImageMixer, 0, outputWidth);
          outputImage = image;
        image = null;
        dataImageOutput = ((java.awt.image.DataBufferInt) outputImage.getRaster().getDataBuffer()).getData();
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

class drawMixerImage extends TimerTask {

    Mixer mixer = null;

    public drawMixerImage(Mixer m) {
        mixer = m;
    }

    @Override
    public void run() {
        mixer.drawImage();
        mixer.frameCount++;
    }
}

class outputMixerImage extends TimerTask {

    private Mixer mixer = null;

    public outputMixerImage(Mixer m) {
        mixer = m;
    }

    @Override
    public void run() {
        if (mixer.outputDevice != null && mixer.dataImageOutput != null) {
            mixer.outputDevice.write(mixer.dataImageOutput);
        }
    }
}

class imageMixer implements Runnable {

    private Mixer mixer = null;

    public imageMixer(Mixer m) {
        mixer = m;
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!mixer.stopMe) {
                    //if (mixer.frameRate != mixer.frameCount) {
                    System.out.println("Mixer : " + mixer.frameCount + " fps");
                    //}
                    mixer.frameCount = 0;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(imageMixer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }

    @Override
    public void run() {
        long timestamp = 0;
        while (!mixer.stopMe) {
            if (!mixer.isDrawing) {
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

            }
            try {
                Thread.sleep(1000 / mixer.frameRate);
            } catch (InterruptedException ex) {
                Logger.getLogger(imageMixer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
