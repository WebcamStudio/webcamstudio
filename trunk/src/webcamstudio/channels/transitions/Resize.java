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
public class Resize extends Transition{

    public Resize(Stream source){
        super(source);
    }
    @Override
    protected void execute() {
        int oldW = 0;
        int oldH = 0;
        
        
        int newW = channel.getWidth();
        int newH = channel.getHeight();
//        System.out.println("NewW: "+newW+" NewH: "+newH);
        int deltaW = newW - oldW;
        int deltaH = newH - oldH;
//        System.out.println("DeltaW: "+deltaW+" DeltaH: "+deltaH);
        int rate = source.getRate();
        int totalFrames = rate * 1;
        
        //System.out.println(source.getWidth() + "x" + source.getHeight());
        for (int i = 0; i<totalFrames;i++){
            source.setWidth(oldW + ((i*deltaW/totalFrames)));
            source.setHeight(oldH + ((i*deltaH/totalFrames)));
            source.setOpacity(i*100/totalFrames);
            Tools.sleep(1000/rate);
            
        }
    }
    
}
