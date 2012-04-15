/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import webcamstudio.mixers.Frame;

/**
 *
 * @author patrick
 */
public abstract class Stream {
    protected String uuid = java.util.UUID.randomUUID().toString();
    protected int captureWidth=320;
    protected int captureHeight= 240;
    protected int width=320;
    protected int height=240;
    protected int x = 0;
    protected int y = 0;
    protected float volume = 0.5f;
    protected int opacity = 100;
    protected int rate = 15;
    protected int seek = 0;
    protected int zorder = 0;
    protected File file = null;
    protected String name = null;
    protected String url = null;
    protected int audioLevelLeft = 0;
    protected int audioLevelRight = 0;
    public abstract void read();
    public abstract void stop();
    public abstract boolean isPlaying();
    public abstract BufferedImage getPreview();
    
    public int getAudioLevelLeft(){
        return audioLevelLeft;
    }
    public int getAudioLevelRight(){
        return audioLevelRight;
    }
    protected void setAudioLevel(Frame f) {
        byte[] data = f.getAudioData();
        if (data != null) {
            audioLevelLeft = 0;
            audioLevelRight = 0;
            int tempValue = 0;
            for (int i = 0; i < data.length; i += 4) {
                tempValue = (data[i]<<8 & (data[i + 1]))/256;
                if (tempValue<0){
                    tempValue *=-1;
                }
                if (audioLevelLeft < tempValue) {
                    audioLevelLeft = tempValue;
                }
                tempValue = (data[i + 2]<<8 & (data[i + 3]))/256;
               
                if (tempValue<0){
                    tempValue *=-1;
                }
                if (audioLevelRight < tempValue) {
                    audioLevelRight = tempValue;
                }
            }
        }
    }

    public String getURL(){
    return url;
    }
    
    public String getName(){
        return name;
    }
    public Frame getFrame(){
        Frame f = null;
        return f;
    }
    
    public String getID(){
        return uuid;
    }
    
    public File getFile(){
        return file;
    }
    public void save(XMLStreamWriter writer) throws XMLStreamException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException{
        writer.writeStartElement(getClass().getCanonicalName());
        for (Field f : getClass().getSuperclass().getDeclaredFields()){
            if (f.getType() == String.class){
                f.setAccessible(true);
                writer.writeStartElement(f.getName());
                writer.writeCharacters(f.get(this).toString());
                writer.writeEndElement();
            }
        }
        for (Field f : getClass().getDeclaredFields()){
            if (f.getType() == String.class){
                f.setAccessible(true);
                writer.writeStartElement(f.getName());
                writer.writeCharacters(f.get(this).toString());
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
    }
    
    public void load(XMLStreamReader reader) throws XMLStreamException, NoSuchFieldException, IllegalAccessException{
        boolean elementReading = true;
        while (elementReading && reader.hasNext()){
            int event = reader.getEventType();
            switch(event){
                case XMLStreamConstants.START_ELEMENT:
                    String property = reader.getName().getLocalPart();
                    String value = reader.getText();
                    Field f = getClass().getField(property);
                    f.setAccessible(true);
                    f.set(this, value);
                    break;
               case XMLStreamConstants.END_ELEMENT:
                   if (reader.getName().equals(getClass().getCanonicalName())){
                       elementReading=false;
                   }
                    break;
            }
        }
    }
    public void setZOrder(int z){
        zorder=z;
    }
    public int getZOrder(){
        return zorder;
    }

    /**
     * @return the captureWidth
     */
    public int getCaptureWidth() {
        return captureWidth;
    }

    /**
     * @param captureWidth the captureWidth to set
     */
    public void setCaptureWidth(int captureWidth) {
        this.captureWidth = captureWidth;
    }

    /**
     * @return the captureHeight
     */
    public int getCaptureHeight() {
        return captureHeight;
    }

    /**
     * @param captureHeight the captureHeight to set
     */
    public void setCaptureHeight(int captureHeight) {
        this.captureHeight = captureHeight;
    }

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
     * @return the volume
     */
    public float getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(float volume) {
        this.volume = volume;
    }

    /**
     * @return the opacity
     */
    public int getOpacity() {
        return opacity;
    }

    /**
     * @param opacity the opacity to set
     */
    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    /**
     * @return the rate
     */
    public int getRate() {
        return rate;
    }

    /**
     * @param rate the rate to set
     */
    public void setRate(int rate) {
        this.rate = rate;
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
    public static Stream getInstance(File file){
        Stream stream = null;
        String ext = file.getName().toLowerCase().trim();
        System.out.println("DEBUG EXT: " + ext);
        if (ext.endsWith(".avi") ||
                ext.endsWith(".ogg") ||
                ext.endsWith(".ogv") ||
                ext.endsWith(".mp4") ||
                ext.endsWith(".m4v") ||
                ext.endsWith(".mpg") ||
                ext.endsWith(".divx") ||
                ext.endsWith(".vob")){
            stream = new SourceMovie(file);
        } else if (file.getAbsolutePath().toLowerCase().startsWith("/dev/video")){
            stream = new SourceWebcam(file);
        } else if (ext.endsWith(".jpg")||
                ext.endsWith(".bmp")||
                ext.endsWith(".gif")||
                ext.endsWith(".png")
                ){
            stream = new SourceImage(file);
        }
        return stream;
    }
}
