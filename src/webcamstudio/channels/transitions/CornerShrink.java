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
public class CornerShrink extends Transition{

    public CornerShrink(Stream source){
        super(source);
    }
    @Override
    protected void execute() {
        int oldW = 0;
        int oldH = 0;
        int x = channel.getX();
        int y = channel.getY();        
        int newW = channel.getWidth();
        int newH = channel.getHeight();
        int deltaW = newW - oldW;
        int deltaH = newH - oldH;
        int rate = source.getRate();
        int totalFrames = rate * 1;
        for (int i = 0; i<totalFrames;i++){
            source.setWidth(newW - i*deltaW/totalFrames);
            source.setX(x + i*deltaW/totalFrames);
            source.setHeight(newH - i*deltaH/totalFrames);
            source.setY(y + i*deltaH/totalFrames);
            source.setOpacity(100 - i*100/totalFrames);
            Tools.sleep(1000/rate);
        }
        source.setX(x);
        source.setY(y);
        source.setWidth(newW);
        source.setHeight(newH);
        source.setOpacity(100);
    }
    
}
