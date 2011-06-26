/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import org.gstreamer.elements.AppSink;

/**
 *
 * @author patrick
 */
public class VideoSink {

    private boolean stopMe = false;
    private VideoSource source = null;
    private AppSink sink = null;

    public VideoSink(VideoSource source, AppSink sink) {
        this.source = source;
        this.sink = sink;
    }

    public void start() {
        stopMe = false;
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stopMe) {
                    org.gstreamer.Buffer b = sink.pullBuffer();
                    if (b != null && b.getSize() > 0) {
                        IntBuffer ints = b.getByteBuffer().asIntBuffer();
                        int[] array = new int[b.getSize() / 4];
                        ints.get(array);
                        b.dispose();
                        BufferedImage img = new BufferedImage(source.getCaptureWidth(), source.getCaptureHeight(), BufferedImage.TRANSLUCENT);
                        if (!stopMe && array.length>0) {
                            img.setRGB(0, 0, img.getWidth(), img.getHeight(), array, 0, img.getWidth());
                            source.setImage(img);
                        }
                    }
                }
            }
        }).start();
    }

    public void stop() {
        stopMe = true;
    }
}
