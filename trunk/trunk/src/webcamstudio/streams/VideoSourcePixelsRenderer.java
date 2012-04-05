/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.util.TimerTask;

/**
 *
 * @author patrick
 */
public class VideoSourcePixelsRenderer extends TimerTask {

    VideoSource source = null;
    BufferedImage imgBuffer = null;

    public VideoSourcePixelsRenderer(VideoSource s) {
        source = s;
    }

    @Override
    public void run() {
        if (source.pixels != null) {
            imgBuffer = new BufferedImage(source.captureWidth, source.captureHeight, BufferedImage.TYPE_INT_ARGB);
            imgBuffer.setRGB(0, 0, source.captureWidth, source.captureHeight, source.pixels, 0, source.captureWidth);
            source.updateOutputImage(imgBuffer);
        }
    }
}
