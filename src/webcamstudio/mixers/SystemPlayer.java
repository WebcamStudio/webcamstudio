/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
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
    private ArrayList<byte[]> buffer = new ArrayList<byte[]>();
    private FrameBuffer frames = null;
    private Viewer viewer = null;

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
        viewer.setImage(frame.getImage());
        viewer.setAudioLevel(MasterMixer.getInstance().getAudioLevelLeft(), MasterMixer.getInstance().getAudioLevelRight());
        viewer.repaint();
        if (source != null) {
            frames.push(frame);
        }
    }

    public void play() throws LineUnavailableException {
        frames = new FrameBuffer(MasterMixer.getInstance().getWidth(), MasterMixer.getInstance().getHeight(), MasterMixer.getInstance().getRate());
        AudioFormat format = new AudioFormat(22050, 16, 2, true, true);
        source = javax.sound.sampled.AudioSystem.getSourceDataLine(format);
        source.open();
        source.start();
        executor = java.util.concurrent.Executors.newCachedThreadPool();
        executor.submit(this);
        executor.shutdown();
    }

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
        Tools.sleep(30);
        executor = null;
        System.gc();
        webcamstudio.components.MasterPanel.tglSound.setSelected(false);
    }
}
