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
public class SlideLeft extends Transition {

    @Override
    public void doTransition(final LayoutItem item) {
        VideoSource source = item.getSource();
        int frames = 30;
        int x = 0-item.getWidth();
        int y = item.getY();
        source.setOutputWidth(item.getWidth());
        source.setOutputHeight(item.getHeight());
        source.setVolume(item.getVolume());
        source.setOpacity(100);
        int xDelta = (item.getWidth()+item.getX()) / frames;
        source.setShowAtX(x);
        source.setShowAtY(y);
        if (!source.isPlaying()) {
            source.startSource();
        }
        for (int i = 0; i < frames; i++) {
            try {
                source.setShowAtX(x + (i*xDelta));
                Thread.sleep(2000/frames);
            } catch (InterruptedException ex) {
                Logger.getLogger(getName()).log(Level.SEVERE, null, ex);
            }
        }
        source.setShowAtX(item.getX());
        source.setShowAtY(item.getY());
        source.setOutputWidth(item.getWidth());
        source.setOutputHeight(item.getHeight());
        source.fireSourceUpdated();

    }
}
