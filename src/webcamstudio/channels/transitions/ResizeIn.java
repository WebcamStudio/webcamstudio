/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.channels.transitions;

import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;

/**
 *
 * @author pballeux (modified by karl)
 */
public class ResizeIn extends Transition {
    public ResizeIn(Stream source){
        super(source);
    }
    @Override
    protected void execute() {
            int rate = source.getRate();
            int frames = rate * 1;
            float deltaW = source.getWidth() / frames / 2F;
            float deltaH = source.getHeight() / frames / 2F;
            source.setX(channel.getX());
            source.setY(channel.getY());
            int x = channel.getX() + channel.getWidth()/2;
            int y = channel.getY() + channel.getHeight()/2;
            int w = channel.getWidth();
            int h = channel.getHeight();
            source.setHeight(channel.getHeight());
            source.setWidth(channel.getWidth());
            for (int i = 0; i < frames; i++) {
                
                    source.setX(x - (int) (i * deltaW));
                    source.setY(y - (int) (i * deltaH));
                    source.setWidth((int) (i * deltaW * 2F)+1);
                    source.setHeight((int) (i * deltaH * 2F)+1);
                    source.setOpacity(i*100/frames);
                    Tools.sleep(1000/rate);
            }
            source.setX(channel.getX());
            source.setY(channel.getY());
            source.setWidth(w);
            source.setHeight(h);
            source.setOpacity(100);
    }
}
