/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import javax.swing.JLabel;

/**
 *
 * @author patrick
 */
public class ResourceMonitorLabel extends JLabel {
    long endTime = 0;
    public ResourceMonitorLabel(long endTime,String text){
        this.endTime = endTime;
        setText(text);
    }
    public long getEndTime(){
        return endTime;
    }
}
