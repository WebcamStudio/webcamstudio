/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.layout.transitions;

import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.layout.LayoutItem;
import webcamstudio.sources.VideoSource;

/**
 *
 * @author pballeux
 */
public class FadeIn extends Transition {

    @Override
    public void doTransition(final LayoutItem item,int sec) {
        VideoSource source = item.getSource();
        frames = sec * FPS;
        source.setOpacity(0);
        source.setShowAtX(item.getX());
        source.setShowAtY(item.getY());
        source.setOutputWidth(item.getWidth());
        source.setOutputHeight(item.getHeight());
        source.setVolume(item.getVolume());
        if (!source.isPlaying()) {
            source.startSource();
        }
        for (int i = 0; i < frames; i++) {
            try {
                source.setOpacity((i * 100 / frames));
                Thread.sleep(WAITTIME);
            } catch (InterruptedException ex) {
                Logger.getLogger(LayoutItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
