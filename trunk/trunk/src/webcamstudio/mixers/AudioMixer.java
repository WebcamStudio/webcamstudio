/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.sampled.AudioFormat;
import webcamstudio.media.Sample;

/**
 *
 * @author patrick
 */
public class AudioMixer implements AudioListener {

    private static AudioMixer instance = null;
    final public static int BUFFER_LENGTH_SEC = 1;
    private ArrayList<Sample> samples = new ArrayList<Sample>();
    private ArrayList<AudioListener> listeners = new ArrayList<AudioListener>();
    private int bufferLength = 0;
    private static AudioFormat format = null;
    private Timer bufferBuilder = null;
    private static long timecode = 0;

    private AudioMixer() {

        this.format = new AudioFormat(44100, 16, 2, true, true);
        bufferLength = format.getChannels() * (format.getSampleSizeInBits() / 8) * (int) format.getFrameRate() * BUFFER_LENGTH_SEC;
        bufferBuilder = new Timer("Audio Builder", true);
        bufferBuilder.scheduleAtFixedRate(new AudioBufferBuilder(this), 0, 1000);
        timecode = 0;
    }

    protected void setTimeCode(long value) {
        timecode = value;
    }

    public static long getTimeCode() {
        return timecode;
    }

    public static AudioMixer getInstance() {
        if (instance == null) {
            instance = new AudioMixer();
        }
        return instance;
    }

    public void addListener(AudioListener listener) {
        listeners.add(listener);
    }

    public void removeListener(AudioListener listener) {
        listeners.remove(listener);
    }

    public void shutdown() {
        listeners.clear();
        if (bufferBuilder != null) {
            bufferBuilder.cancel();
            bufferBuilder = null;
        }
    }

    public static AudioFormat getFormat() {
        return format;
    }

    public ArrayList<Sample> getSamples() {
        return samples;
    }

    public void updateCurrentBuffer(byte[] data, long timeCode) {
        for (AudioListener l : listeners) {
            l.newSample(new Sample(data, timeCode, format));
        }
    }

    public int getBufferLength() {
        return bufferLength;
    }

    @Override
    public void newSample(Sample sample) {
        samples.add(sample);
    }
}

class AudioBufferBuilder extends TimerTask {

    AudioMixer mixer;
    byte[] tempBuffer = null;
    int startingBufferIndex = 0;

    public AudioBufferBuilder(AudioMixer m) {
        mixer = m;
        tempBuffer = new byte[mixer.getBufferLength()];
    }

    @Override
    public void run() {
        long timecode = mixer.getTimeCode();
        VideoMixer.setTimeCode(timecode);
        long entryPoint = 0;
        long maxSampleLength = 0;
        ArrayList<Sample> samples = new ArrayList<Sample>();
        samples.addAll(mixer.getSamples());

        long bufferLength = tempBuffer.length - startingBufferIndex;
        for (Sample sample : samples) {
            if (sample != null && sample.getTimeCode() >= timecode && sample.getTimeCode() < (timecode + tempBuffer.length)) {
                entryPoint = (sample.getTimeCode() - timecode);
                mixAudio(sample.getData(), (int) entryPoint, sample.getData().length);
                mixer.getSamples().remove(sample);
            }
        }

        startingBufferIndex += (tempBuffer.length / AudioMixer.BUFFER_LENGTH_SEC);
        if ((startingBufferIndex % tempBuffer.length) == 0) {
            startingBufferIndex = 0;
            mixer.updateCurrentBuffer(tempBuffer, timecode);
            tempBuffer = new byte[mixer.getBufferLength()];
        }
        timecode += (44100 * 2 * 2);
        mixer.setTimeCode(timecode);

    }

    private void mixAudio(byte[] data, int entryPoint, int length) {
        int step = (mixer.getFormat().getSampleSizeInBits() / 8);
        int sample1, sample2, mixedSample = 0;
        int startingSampleIndex = 0;
        if (entryPoint < 0) {
            startingSampleIndex = data.length + entryPoint;
            entryPoint = 0;
        }
        for (int i = startingSampleIndex; startingSampleIndex >= 0 && i < length && i < data.length; i += step) {
//            tempBuffer[i + entryPoint] = data[i];
//            if (step == 2) {
//                tempBuffer[i + entryPoint + 1] = data[i + 1];
//            }
            sample1 = tempBuffer[i + entryPoint];
            sample2 = data[i];
            if (step == 2) {
                sample1 = (sample1 << 8) + (tempBuffer[i + entryPoint + 1]);
                sample2 = (sample2 << 8) + (data[i] + 1);
            }
            mixedSample = (sample1 + sample2);
            tempBuffer[i + entryPoint] = (byte) (mixedSample & 0xFF);
            if (step == 2) {
                tempBuffer[i + entryPoint + 1] = (byte) ((mixedSample >> 8) & 0xFF);
            }
        }
    }
}
