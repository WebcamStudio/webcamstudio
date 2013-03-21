/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.config;

import java.util.ArrayList;

/**
 *
 * @author patrick
 */
public class Studioz {
    private String name;
    private ArrayList<Media> medias;
    private ArrayList<Channel> channels;
    private ArrayList<Event> events;
    private Icon icon;

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
     * @return the medias
     */
    public ArrayList<Media> getMedias() {
        return medias;
    }

    /**
     * @param medias the medias to set
     */
    public void setMedias(ArrayList<Media> medias) {
        this.medias = medias;
    }

    /**
     * @return the channels
     */
    public ArrayList<Channel> getChannels() {
        return channels;
    }

    /**
     * @param channels the channels to set
     */
    public void setChannels(ArrayList<Channel> channels) {
        this.channels = channels;
    }

    /**
     * @return the events
     */
    public ArrayList<Event> getEvents() {
        return events;
    }

    /**
     * @param events the events to set
     */
    public void setEvents(ArrayList<Event> events) {
        this.events = events;
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
