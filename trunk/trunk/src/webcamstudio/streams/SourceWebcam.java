/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import webcamstudio.externals.ProcessRenderer;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class SourceWebcam extends Stream {

    ProcessRenderer capture = null;
    BufferedImage lastPreview = null;
    boolean isPlaying = false;
    public SourceWebcam(File device) {
        super();
        rate = MasterMixer.getInstance().getRate();
        file = device;
        name = device.getName();


    }

    public SourceWebcam(String defaultName) {
        super();
        rate = MasterMixer.getInstance().getRate();
        name = defaultName;
    }

    @Override
    public void read() {
        isPlaying=true;
        lastPreview = new BufferedImage(captureWidth,captureHeight,BufferedImage.TYPE_INT_ARGB);
        rate = MasterMixer.getInstance().getRate();
        MasterFrameBuilder.register(this);
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "webcam");
        capture.read();
    }

    @Override
    public void stop() {
        isPlaying=false;
        if (capture != null) {
            capture.stop();
        }
        MasterFrameBuilder.unregister(this);
        lastPreview=null;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public BufferedImage getPreview() {
        return lastPreview;
    }

    @Override
    public synchronized Frame getFrame() {
        Frame f = null;
        if (capture != null) {
            f = capture.getFrame();
            if (f != null) {
                setAudioLevel(f);
                if (lastPreview!=null){
                    lastPreview.getGraphics().drawImage(f.getImage(), 0, 0, null);
                }
            }
        }
        return f;
    }
}
