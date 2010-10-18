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
public class SlideIn extends Transition {

    @Override
    public void doTransition(final LayoutItem item) {
        VideoSource source = item.getSource();
        int x = 0;
        int y = 0;
        source.setOutputWidth(item.getWidth());
        source.setOutputHeight(item.getHeight());
        if (!source.isPlaying()) {
            source.startSource();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(SlideIn.class.getName()).log(Level.SEVERE, null, ex);
        }
        int xDelta = (0 - item.getX()) / 10;
        int yDelta = (0 - item.getY()) / 10;
        for (int i = 0; i < 10; i++) {
            try {
                source.setShowAtX(source.getShowAtX() + xDelta);
                source.setShowAtY(source.getShowAtY() + yDelta);

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
