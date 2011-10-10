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
public class FocusIn extends Transition {

    @Override
    public void doTransition(final LayoutItem item) {
        VideoSource source = item.getSource();
        source.setOpacity(0);
        source.setShowAtX(item.getX());
        source.setShowAtY(item.getY());
        source.setOutputWidth(item.getWidth());
        source.setOutputHeight(item.getHeight());
        source.setVolume(item.getVolume());
        Block effect = new Block();
        effect.setSize(20);
        source.addEffect(effect);
        source.fireSourceUpdated();
        if (!source.isPlaying()) {
            source.startSource();
        }
        for (int i = 0; i <= 20; i++) {
            try {
                source.setOpacity(i * 5);
                source.fireSourceUpdated();
                effect.setSize(20-i+1);
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(LayoutItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        source.removeEffect(effect);
    }
}
