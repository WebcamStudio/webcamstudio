/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.channels.transitions;

import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick
 */
public class HideLeft extends Transition{

    public HideLeft(Stream source){
        super(source);
    }
    @Override
    protected void execute() {
        
        int newW = channel.getWidth();
        int rate = source.getRate();
        int frames = rate * 1;
        for (int i = 0;i<frames;i++){
            source.setWidth(newW - i*newW/frames);
            source.setOpacity(100-i*100/frames);
            Tools.sleep(1000/rate);
        }
        source.setWidth(newW);
    }
    
}
