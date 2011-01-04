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
public class ShrinkOut extends Transition {

    @Override
    public void doTransition(final LayoutItem item) {
        VideoSource source = item.getSource();
        source.setOpacity(100);
        
        source.fireSourceUpdated();
        int deltaW = source.getOutputWidth() / 20 /2 ;
        int deltaH = source.getOutputHeight() / 20 /2;
        source.setShowAtX(item.getX());
        source.setShowAtY(item.getY());
        source.setOutputHeight(item.getHeight());
        source.setOutputWidth(item.getWidth());
        for (int i = 20; i >= 0; i--) {
            try {
                source.setShowAtX(source.getShowAtX()+deltaW);
                source.setShowAtY(source.getShowAtY()+deltaH);
                source.setOutputWidth(source.getOutputWidth() - (deltaW*2));
                source.setOutputHeight(source.getOutputHeight() - (deltaH*2));
                source.fireSourceUpdated();
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(LayoutItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (source.isPlaying()) {
            source.stopSource();
        }
        source.setOutputHeight(item.getHeight());
        source.setOutputWidth(item.getWidth());
        source.setShowAtX(item.getX());
        source.setShowAtY(item.getY());


    }
}
