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
    public void doTransition(final LayoutItem item, int sec) {
        VideoSource source = item.getSource();
        frames = sec * FPS;
        if (frames > 0) {
            item.setActive(true);
            source.setVolume(item.getVolume());
            float xDelta = (item.getX() - source.getShowAtX()) / (float) frames;
            float yDelta = (item.getY() - source.getShowAtY()) / (float) frames;
            float wDelta = (item.getWidth() - source.getOutputWidth()) / (float) frames;
            float hDelta = (item.getHeight() - source.getOutputHeight()) / (float) frames;
            int x = source.getShowAtX();
            int y = source.getShowAtY();
            int w = source.getOutputWidth();
            int h = source.getOutputHeight();
            if (!source.isPlaying()) {
                source.startSource();

            }
            for (float i = 0; i < frames; i++) {
                try {
                    source.setShowAtX(x + (int) (i * xDelta));
                    source.setShowAtY(y + (int) (i * yDelta));
                    source.setOutputWidth(w + (int) (i * wDelta));
                    source.setOutputHeight(h + (int) (i * hDelta));
                    Thread.sleep(WAITTIME);
                } catch (InterruptedException ex) {
                    Logger.getLogger(LayoutItem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        source.setShowAtX(item.getX());
        source.setShowAtY(item.getY());
        source.setOutputWidth(item.getWidth());
        source.setOutputHeight(item.getHeight());
        source.setVolume(item.getVolume());

    }
}
