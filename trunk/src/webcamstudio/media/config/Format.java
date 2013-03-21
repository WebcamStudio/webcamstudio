/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.config;

/**
 *
 * @author patrick
 */
public class Format {

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the file
     */
    public String getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * @return the tcp
     */
    public String getTcp() {
        return tcp;
    }

    /**
     * @param tcp the tcp to set
     */
    public void setTcp(String tcp) {
        this.tcp = tcp;
    }

    /**
     * @return the device
     */
    public String getDevice() {
        return device;
    }

    /**
     * @param device the device to set
     */
    public void setDevice(String device) {
        this.device = device;
    }

    /**
     * @return the volume
     */
    public int getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(int volume) {
        this.volume = volume;
    }

    /**
     * @return the seek
     */
    public int getSeek() {
        return seek;
    }

    /**
     * @param seek the seek to set
     */
    public void setSeek(int seek) {
        this.seek = seek;
    }

    /**
     * @return the audioFrequency
     */
    public int getAudioFrequency() {
        return audioFrequency;
    }

    /**
     * @param audioFrequency the audioFrequency to set
     */
    public void setAudioFrequency(int audioFrequency) {
        this.audioFrequency = audioFrequency;
    }

    /**
     * @return the audioBitSize
     */
    public int getAudioBitSize() {
        return audioBitSize;
    }

    /**
     * @param audioBitSize the audioBitSize to set
     */
    public void setAudioBitSize(int audioBitSize) {
        this.audioBitSize = audioBitSize;
    }

    /**
     * @return the audioChannels
     */
    public int getAudioChannels() {
        return audioChannels;
    }

    /**
     * @param audioChannels the audioChannels to set
     */
    public void setAudioChannels(int audioChannels) {
        this.audioChannels = audioChannels;
    }
    public enum Type{
        FILE,
        HTTP,
        TCP,
        DEVICE
    }
    private int width;
    private int height;
    private int x;
    private int y;
    private String url;
    private String file;
    private String tcp;
    private String device;
    private int volume;
    private int seek;
    private int audioFrequency;
    private int audioBitSize;
    private int audioChannels;
    
            
}
