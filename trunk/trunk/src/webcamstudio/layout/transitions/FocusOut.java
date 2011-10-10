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
    public void doTransition(final LayoutItem item) {
        VideoSource source = item.getSource();
        source.setOpacity(0);
        source.setShowAtX(item.getX());
        source.setShowAtY(item.getY());
        source.setOutputWidth(item.getWidth());
        source.setOutputHeight(item.getHeight());
        Block effect = new Block();
        effect.setSize(20);
        source.addEffect(effect);
        source.setVolume(item.getVolume());
        source.fireSourceUpdated();
        for (int i = 20; i >= 0; i--) {
            try {
                source.setOpacity(i * 5);
                effect.setSize(20-i+1);
                source.fireSourceUpdated();
                Thread.sleep(100);
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
