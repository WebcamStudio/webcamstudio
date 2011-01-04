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
public class SlideOut extends Transition {

    @Override
    public void doTransition(final LayoutItem item) {
        VideoSource source = item.getSource();
        int x = 0;
        int y = 0;
        if (source.getShowAtX() < source.getOutputWidth() / 2) {
            x = 0 - source.getOutputWidth() - 10;
        } else {
            x = (source.getOutputWidth() * 2) + 10;
        }
        if (source.getShowAtY() < source.getOutputHeight() / 2) {
            y = 0 - source.getOutputHeight() - 10;
        } else {
            y = (source.getOutputHeight() * 2) + 10;
        }
        int xDelta = (x - source.getShowAtX()) / 20;
        int yDelta = (y - source.getShowAtY()) / 20;

        for (int i = 0; i < 20; i++) {
            try {
                source.setShowAtX(source.getShowAtX() + xDelta);
                source.setShowAtY(source.getShowAtY() + yDelta);

                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(LayoutItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        source.setOutputWidth(item.getWidth());
        source.setOutputHeight(item.getHeight());
        source.fireSourceUpdated();
        if (source.isPlaying()) {
            source.stopSource();
        }

    }
}
