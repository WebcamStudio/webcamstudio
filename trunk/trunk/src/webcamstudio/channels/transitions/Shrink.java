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
public class Shrink extends Transition{

    public Shrink(Stream source){
        super(source);
    }
    @Override
    protected void execute() {
        int oldW = source.getWidth();
        int oldH = source.getHeight();
        int newW = channel.getWidth();
        int newH = channel.getHeight();
        int deltaW = newW - oldW;
        int deltaH = newH - oldH;
        int rate = source.getRate();
        int totalFrames = rate * 1;
        
        //System.out.println(source.getWidth() + "x" + source.getHeight());
        for (int i = 0; i<totalFrames;i++){
            source.setWidth(oldW + ((i*deltaW/totalFrames)));
            source.setHeight(oldH + ((i*deltaH/totalFrames)));
            Tools.sleep(1000/rate);
            
        }
    }
    
}
