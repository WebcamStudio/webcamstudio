/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.config;

/**
 *
 * @author patrick
 */
public class Media {

    /**
     * @return the guid
     */
    public String getGuid() {
        return guid;
    }

    /**
     * @param guid the guid to set
     */
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the handleSample
     */
    public boolean isHandleSample() {
        return handleSample;
    }

    /**
     * @param handleSample the handleSample to set
     */
    public void setHandleSample(boolean handleSample) {
        this.handleSample = handleSample;
    }

    /**
     * @return the handleImage
     */
    public boolean isHandleImage() {
        return handleImage;
    }

    /**
     * @param handleImage the handleImage to set
     */
    public void setHandleImage(boolean handleImage) {
        this.handleImage = handleImage;
    }


    public enum Type{
        Source,
        Sink
    }
    private String guid;
    private String name = null;
    private boolean handleSample = false;
    private boolean handleImage = false;
    private Type type = null;
    private Format sourceFormat = null;
    private Format sinkFormat = null;
    private String launchConfig = null;
    private Icon icon;
    private int fps;
    
    /**
     * @return the FPS
     */
    public int getFps() {
        return fps;
    }

    /**
     * @param fps the FPS to set
     */
    public void setFps(int fps) {
        this.fps = fps;
    }    
    
    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return the sourceFormat
     */
    public Format getSourceFormat() {
        return sourceFormat;
    }

    /**
     * @param sourceFormat the sourceFormat to set
     */
    public void setSourceFormat(Format sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    /**
     * @return the sinkFormat
     */
    public Format getSinkFormat() {
        return sinkFormat;
    }

    /**
     * @param sinkFormat the sinkFormat to set
     */
    public void setSinkFormat(Format sinkFormat) {
        this.sinkFormat = sinkFormat;
    }

    /**
     * @return the launchConfig
     */
    public String getLaunchConfig() {
        return launchConfig;
    }

    /**
     * @param launchConfig the launchConfig to set
     */
    public void setLaunchConfig(String launchConfig) {
        this.launchConfig = launchConfig;
    }

    /**
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }
    
    
}
