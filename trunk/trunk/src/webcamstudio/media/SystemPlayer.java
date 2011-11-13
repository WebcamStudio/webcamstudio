/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import webcamstudio.mixers.AudioMixer;
import webcamstudio.mixers.AudioListener;
import webcamstudio.mixers.VideoListener;
import webcamstudio.mixers.VideoMixer;

/**
 *
 * @author patrick
 */
public class SystemPlayer implements AudioListener, VideoListener {
    //Make sure that mixer is initialized...

    private static AudioMixer amixer = AudioMixer.getInstance();
    private static VideoMixer vmixer = VideoMixer.getInstance();
    private static SystemPlayer instance = null;
    private static ArrayList<Sample> audioBuffer = new ArrayList<Sample>();
    private static ArrayList<Image> videoBuffer = new ArrayList<Image>();
    private static ArrayList<VideoListener> vListeners = new ArrayList<VideoListener>();
    private static boolean isPlaying = false;
    private static boolean stopMe = false;
    private static long startingTimecode = 0;
    private static long endingTimecode = 0;
    private Timer timer = null;

    public static SystemPlayer getInstance() {
        if (instance == null) {
            instance = new SystemPlayer();
            amixer.addListener(instance);
            vmixer.addListener(instance);
        }
        return instance;
    }

    protected static long getStartingTimeCode() {
        return startingTimecode;
    }

    protected static long getEndingTimeCode() {
        return endingTimecode;
    }

    protected void setTimeCode(long starting, long ending) {
        startingTimecode = starting;
        endingTimecode = ending;
    }

    protected static ArrayList<Image> getVideoBuffer() {
        return videoBuffer;
    }

    protected static ArrayList<Sample> getAudioBuffer() {
        return audioBuffer;
    }

    protected static Sample getAudio() {
        Sample s = null;
        if (audioBuffer.size() > 0) {
            s = audioBuffer.remove(0);
            startingTimecode = s.getTimeCode();
            endingTimecode = s.getTimeCode() + s.getData().length;
        }
        return s;
    }

    private SystemPlayer() {
        timer = new Timer("System Player", false);
        new Thread(new AudioPlayer()).start();
        new Thread(new VideoPlayer()).start();
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        vListeners.clear();
        AudioMixer.getInstance().removeListener(this);
        VideoMixer.getInstance().removeListener(this);
    }

    public void addListener(VideoListener l) {
        vListeners.add(l);
    }

    public void removeListener(VideoListener l) {
        vListeners.remove(l);
    }

    @Override
    public void newSample(Sample sample) {
        audioBuffer.add(sample);
    }

    @Override
    public void newImage(Image image) {
        videoBuffer.add(image);
    }

    protected static void updateVideoListeners(Image img) {
        for (VideoListener l : vListeners) {
            l.newImage(img);
        }
    }
}

class VideoPlayer extends TimerTask {

    @Override
    public void run() {
        while (true) {
            ArrayList<Image> list = new ArrayList<Image>();
            list.addAll(SystemPlayer.getVideoBuffer());
            long starting = SystemPlayer.getStartingTimeCode();
            long ending = SystemPlayer.getEndingTimeCode();
            for (Image img : list) {
                if (img.getTimeCode() >= starting && img.getTimeCode() <= ending) {
                    //We got an image to send
                    SystemPlayer.updateVideoListeners(img);
                    SystemPlayer.getVideoBuffer().remove(img);
                    break;
                } else if (img.getTimeCode() < starting) {
                    //Clean up old images...
                    SystemPlayer.getVideoBuffer().remove(img);
                }
            }
            try {
                Thread.sleep(1000/VideoMixer.getInstance().getFrameRate());
            } catch (InterruptedException ex) {
                Logger.getLogger(VideoPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

class AudioPlayer extends TimerTask {

    private AudioFormat format = AudioMixer.getFormat();
    private DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
    private SourceDataLine speaker = null;
    private boolean isPlaying = false;

    protected AudioPlayer() {
        try {
            speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open();
            speaker.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        while (true) {
            Sample sample = SystemPlayer.getAudio();
            if (sample != null) {
                byte[] data = sample.getData();
                sample = null;
                speaker.write(data, 0, data.length);
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(AudioPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}