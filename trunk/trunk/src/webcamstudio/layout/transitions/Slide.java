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
public class Slide extends Transition {

    @Override
    public void doTransition(final LayoutItem item) {
        VideoSource source = item.getSource();
        if (!source.isPlaying()) {
            source.startSource();
        }
        int xDelta = (item.getX() - source.getShowAtX()) / 10;
        int yDelta = (item.getY() - source.getShowAtY()) / 10;
        int wDelta = (item.getWidth() - source.getOutputWidth()) / 10;
        int hDelta = (item.getHeight() - source.getOutputHeight()) / 10;
        for (int i = 0; i < 10; i++) {
            try {
                source.setOutputWidth(source.getOutputWidth() + wDelta);
                source.setOutputHeight(source.getOutputHeight() + hDelta);
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
