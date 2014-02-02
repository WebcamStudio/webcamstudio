/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import javax.sound.sampled.SourceDataLine;
import webcamstudio.components.Viewer;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class SystemPlayer implements Runnable {

    boolean stopMe = false;
    public boolean stopMePub = stopMe;
    private SourceDataLine source;
    private ExecutorService executor = null;
    private static SystemPlayer instance = null;
    private FrameBuffer frames = null;
    private Viewer viewer = null;
    private int aFreq = webcamstudio.WebcamStudio.audioFreq;

    private SystemPlayer(Viewer viewer) {
        this.viewer = viewer;
    }

    public static SystemPlayer getInstance(Viewer viewer) {
        if (instance == null) {
            instance = new SystemPlayer(viewer);
        }
        return instance;
    }

    public void addFrame(Frame frame) {
        BufferedImage fImage = frame.getImage();
        viewer.setImage(fImage);
        int lAL = MasterMixer.getInstance().getAudioLevelLeft();
        int lAR = MasterMixer.getInstance().getAudioLevelRight();
        viewer.setAudioLevel(lAL, lAR);
        viewer.repaint();
//        if (source != null) {
//            frames.push(frame);
//        }
    }

//    public void play() throws LineUnavailableException {
//        aFreq = webcamstudio.WebcamStudio.audioFreq;
//        int fWidth = MasterMixer.getInstance().getWidth();
//        int fHeight = MasterMixer.getInstance().getHeight();
//        int fRate = MasterMixer.getInstance().getRate();
//        frames = new FrameBuffer(fWidth, fHeight, fRate);
//        AudioFormat format = new AudioFormat(aFreq, 16, 2, true, true);
////        System.out.println("SysPlayer AFreq: "+aFreq);
//        source = javax.sound.sampled.AudioSystem.getSourceDataLine(format);
//        Tools.sleep(20);
//        source.open(format, 2048);
//        source.start();
//        executor = java.util.concurrent.Executors.newCachedThreadPool();
//        executor.submit(this);
//        Tools.sleep(20);
//        executor.shutdown();
//    }

    @Override
    public void run() {
        stopMe = false;
        frames.clear();
        while (!stopMe) {
            Frame frame = frames.pop();
            byte[] d = frame.getAudioData();
            if (d != null) {
                source.write(d, 0, d.length);
            }
        }
    }

    public void stop() {
        stopMe = true;
        if (frames != null) {
            Tools.sleep(30);
            frames.abort();
        }
        if (source != null) {
            Tools.sleep(30);
            source.stop();
            Tools.sleep(30);
            source.close();
            Tools.sleep(30);
            source = null;
        }
        Tools.sleep(20);
        executor = null;
        webcamstudio.components.MasterPanel.tglSound.setSelected(false);
    }
}
