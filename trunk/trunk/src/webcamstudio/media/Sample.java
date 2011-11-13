/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media;

import javax.sound.sampled.AudioFormat;

/**
 *
 * @author patrick
 */
public class Sample {
private byte[] data;
    private long timecode = 0;
    private AudioFormat format;

    public Sample(byte[] data, long timecode, AudioFormat format) {
        this.data = data;
        this.timecode=timecode;
        this.format = format;
    }

    public byte[] getData() {
        return data;
    }

    public long getTimeCode() {
        return timecode;
    }

    public AudioFormat getFormat() {
        return format;
    }    
}
