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
public class ShrinkIn extends Transition {

    @Override
    public void doTransition(final LayoutItem item) {
        VideoSource source = item.getSource();
        if (!source.isPlaying()) {
            source.startSource();
        }
        
        source.setOutputWidth(0);
        source.setOutputHeight(0);
        source.setOpacity(100);        
        int deltaW = item.getWidth() / 20/2;
        int deltaH = item.getHeight() / 20/2;
        source.setShowAtX(item.getX()+(item.getWidth()/2) + deltaW);
        source.setShowAtY(item.getY()+(item.getHeight()/2) + deltaH);

        for (int i = 0; i <= 20; i++) {
            try {
                source.setShowAtX(source.getShowAtX()-deltaW);
                source.setShowAtY(source.getShowAtY()-deltaH);
                source.setOutputWidth(deltaW*i*2);
                source.setOutputHeight(deltaH*i*2);
                source.fireSourceUpdated();
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(LayoutItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        source.setShowAtX(item.getX());
        source.setShowAtY(item.getY());
        source.setOutputWidth(item.getWidth());
        source.setOutputHeight(item.getHeight());
        source.fireSourceUpdated();

    }
}
