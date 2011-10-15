/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.layout.transitions;

import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.layout.LayoutItem;
import webcamstudio.sources.VideoSource;
import webcamstudio.sources.effects.Block;

/**
 *
 * @author pballeux
 */
public class FocusOut extends Transition {

    @Override
    public void doTransition(final LayoutItem item,int sec) {
        VideoSource source = item.getSource();
        frames=sec*FPS;
        source.setOpacity(0);
        source.setShowAtX(item.getX());
        source.setShowAtY(item.getY());
        source.setOutputWidth(item.getWidth());
        source.setOutputHeight(item.getHeight());
        Block effect = new Block();
        effect.setSize(1);
        source.addEffect(effect);
        source.setVolume(item.getVolume());
        source.fireSourceUpdated();
        for (int i = 0; i < frames; i++) {
            try {
                source.setOpacity(100-(i * 100 / frames));
                effect.setSize((i * 20 / frames));
                Thread.sleep(WAITTIME);
            } catch (InterruptedException ex) {
                Logger.getLogger(LayoutItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        source.removeEffect(effect);
        if (source.isPlaying()) {
            source.stopSource();
        }

    }
}
