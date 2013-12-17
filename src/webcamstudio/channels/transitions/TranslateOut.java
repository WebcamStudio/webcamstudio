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
public class TranslateOut extends Transition{
    public TranslateOut(Stream source){
        super(source);
    }
    @Override
    protected void execute() {
        int oldX = -source.getX();
        int oldY = -source.getY();
        int newX = source.getCaptureWidth();
        int newY = source.getCaptureHeight();
        int deltaX = newX - oldX;
        int deltaY = newY - oldY;
        int rate = source.getRate();
        int totalFrames = rate * 1;
        int o = rate;
        for (int i = 0; i<totalFrames;i++){
            source.setX(oldX + (i*deltaX/totalFrames));
            source.setY(oldY+(i*deltaY/totalFrames));
            source.setOpacity(o*100/totalFrames);
            o--;
            Tools.sleep(1000/rate);
        }
        source.setX(oldX);
        source.setY(oldY);
        
    }
    
}
