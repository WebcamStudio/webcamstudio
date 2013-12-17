/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.channels.transitions;

import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;

/**
 *
 * @author pballeux
 */
public class AudioFadeIn extends Transition {
    protected AudioFadeIn(Stream source){
        super(source);
    }
    @Override
    public void execute() {
        int rate = source.getRate();
        float originalVolume = source.getVolume();
        source.setVolume(0f);
        for (int i = 0; i < rate ; i++) {     
                source.setVolume((i * originalVolume)/rate);
                Tools.sleep(1000/rate);     
        }
        source.setVolume(source.getVolume());
    }
}
