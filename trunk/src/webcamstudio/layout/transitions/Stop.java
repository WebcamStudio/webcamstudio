/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.layout.transitions;

import webcamstudio.layout.LayoutItem;
import webcamstudio.sources.VideoSource;

/**
 *
 * @author pballeux
 */
public class Stop extends Transition {

    @Override
    public void doTransition(LayoutItem item, int sec) {
        VideoSource source = item.getSource();
        try{
            Thread.sleep(1000*sec);
        } catch (Exception e){
        }
        source.setVolume(item.getVolume());
        source.setOpacity(0);
        source.setShowAtX(item.getX());
        source.setShowAtY(item.getY());
        source.setOutputWidth(item.getWidth());
        source.setOutputHeight(item.getHeight());
        if (source.isPlaying()) {
            source.stopSource();
        }
    }
}
