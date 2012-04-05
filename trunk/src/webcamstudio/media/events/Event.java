/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.events;

import java.util.TimerTask;
import webcamstudio.media.config.EventType;

/**
 *
 * @author patrick
 */
public abstract class Event extends TimerTask{
    public Event getInstance(EventType type){
        Event event = null;
        switch(type){
            default:
                event = null;
                break;
        }
        return event;
    }
}
