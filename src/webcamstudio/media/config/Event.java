/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.config;

/**
 *
 * @author patrick
 */
public class Event {
    private EventType type;
    private long timecode;
    private long duration;
    private long initialValue;
    private long endingValue;
    private long restartDelay;
    private String mediaGUID;

    /**
     * @return the type
     */
    public EventType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(EventType type) {
        this.type = type;
    }

    /**
     * @return the timecode
     */
    public long getTimecode() {
        return timecode;
    }

    /**
     * @param timecode the timecode to set
     */
    public void setTimecode(long timecode) {
        this.timecode = timecode;
    }

    /**
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * @return the initialValue
     */
    public long getInitialValue() {
        return initialValue;
    }

    /**
     * @param initialValue the initialValue to set
     */
    public void setInitialValue(long initialValue) {
        this.initialValue = initialValue;
    }

    /**
     * @return the endingValue
     */
    public long getEndingValue() {
        return endingValue;
    }

    /**
     * @param endingValue the endingValue to set
     */
    public void setEndingValue(long endingValue) {
        this.endingValue = endingValue;
    }

    /**
     * @return the restartDelay
     */
    public long getRestartDelay() {
        return restartDelay;
    }

    /**
     * @param restartDelay the restartDelay to set
     */
    public void setRestartDelay(long restartDelay) {
        this.restartDelay = restartDelay;
    }

    /**
     * @return the mediaGUID
     */
    public String getMediaGUID() {
        return mediaGUID;
    }

    /**
     * @param mediaGUID the mediaGUID to set
     */
    public void setMediaGUID(String mediaGUID) {
        this.mediaGUID = mediaGUID;
    }
}
