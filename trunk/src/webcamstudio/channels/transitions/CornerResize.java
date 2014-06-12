/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.channels.transitions;

import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class CornerResize extends Transition{

    public CornerResize(Stream source){
        super(source);
    }
    @Override
    protected void execute() {
        int newW = channel.getWidth();
        int newH = channel.getHeight();
        int deltaW = newW;
        int deltaH = newH;
        int rate = source.getRate();
        int totalFrames = rate * 1;
        for (int i = 0; i<totalFrames;i++){
            source.setWidth(i*deltaW/totalFrames+1);
//            System.out.println("source W: "+i*deltaW/totalFrames+1);
            source.setHeight(i*deltaH/totalFrames+1);
//            System.out.println("source H: "+i*deltaH/totalFrames+1);
            source.setOpacity(i*100/totalFrames);
            Tools.sleep(1000/rate);
        }
    }
    
}
