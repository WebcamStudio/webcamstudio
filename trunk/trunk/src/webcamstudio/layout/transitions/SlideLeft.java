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
    public void doTransition(final LayoutItem item, int sec) {
        VideoSource source = item.getSource();
        frames = sec * FPS;
        if (frames >= 0) {
            int x = 0 - item.getWidth();
            int y = item.getY();
            source.setOutputWidth(item.getWidth());
            source.setOutputHeight(item.getHeight());
            source.setVolume(item.getVolume());
            source.setOpacity(100);
            float xDelta = (item.getWidth() + item.getX()) / (float) frames;
            source.setShowAtX(x);
            source.setShowAtY(y);
            if (!source.isPlaying()) {
                source.startSource();
            }
            for (float i = 0; i < frames; i++) {
                try {
                    source.setShowAtX(x + (int) (i * xDelta));
                    Thread.sleep(WAITTIME);
                } catch (InterruptedException ex) {
                    Logger.getLogger(getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (!source.isPlaying()) {
            source.startSource();
        }

        source.setShowAtX(item.getX());
        source.setShowAtY(item.getY());
        source.setOutputWidth(item.getWidth());
        source.setOutputHeight(item.getHeight());
        source.setVolume(item.getVolume());

    }
}
