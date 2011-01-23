/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.awt.Image;
import java.awt.Toolkit;
import webcamstudio.sources.*;
import webcamstudio.VirtualHost;
import java.awt.image.*;
import webcamstudio.exporter.vloopback.VideoOutput;
import webcamstudio.layout.Layout;
import webcamstudio.layout.LayoutItem;

/**
 *
 * @author pballeux
 */
public class Mixer implements java.lang.Runnable {

    protected int frameRate = 15;
    protected int outputWidth = 320;
    protected int outputHeight = 240;
    protected static java.awt.GraphicsConfiguration graphicConfiguration = null;
    protected java.awt.image.BufferedImage image = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(outputWidth, outputHeight);
    private BufferedImage outputImage = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(outputWidth, outputHeight);
    private boolean isDrawing = false;
    protected boolean lightMode = false;
    private VirtualHost virtualHost = new VirtualHost();
    private boolean stopMe = false;
    private java.awt.image.BufferedImage paintImage = null;
    private VideoOutput outputDevice = null;
    private Image background = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/webcamstudio/resources/splash.jpg"));

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
        new Thread(this).start();
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

    private void drawImage() {
        isDrawing = true;
        if (image == null || (outputWidth != image.getWidth())) {
            image = graphicConfiguration.createCompatibleImage(outputWidth, outputHeight, java.awt.image.BufferedImage.TRANSLUCENT);
            outputImage = graphicConfiguration.createCompatibleImage(outputWidth, outputHeight, java.awt.image.BufferedImage.TRANSLUCENT);
        }
        java.awt.Graphics2D buffer = image.createGraphics();
        int x1, x2, x3, x4;
        int y1, y2, y3, y4;

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

        BufferedImage img = null;
        try {
            Layout activeLayout = Layout.getActiveLayout();
            if (activeLayout != null) {
                for (LayoutItem item : activeLayout.getItems()) {
                    VideoSource source = item.getSource();
                    if (source.getActivityDetection() == 0 || (source.getActivityDetection() > 0 && source.activityDetected())) {
                        virtualHost.put(source.getKeywords(), source);
                        img = source.getImage();
                        if (img != null) {
                            //Don't do anything if there is no rotation to do...

                            x1 = source.getShowAtX();
                            y1 = source.getShowAtY();
                            x2 = x1 + source.getOutputWidth();
                            y2 = y1 + source.getOutputHeight();
                            x3 = 0;
                            y3 = 0;
                            x4 = source.getCaptureWidth();
                            y4 = source.getCaptureHeight();
                            float opacity = (float) source.getOpacity();
                            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, opacity / 100F));
                            //buffer.setClip(x1, y1, source.getOutputWidth(), source.getOutputHeight());
                            buffer.drawImage(img, x1, y1, x2, y2, x3, y3, x4, y4, null);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        buffer.dispose();
        outputImage.getGraphics().drawImage(image, 0, 0, null);
        isDrawing = false;
    }

    public void setOutput(VideoOutput o) {
        outputDevice = o;
    }


    @Override
    public void run() {
        graphicConfiguration = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        while (!stopMe) {
            try {
                drawImage();
                if (outputDevice != null) {
                    outputDevice.write(outputImage);
                }
                Thread.sleep(1000 / frameRate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
