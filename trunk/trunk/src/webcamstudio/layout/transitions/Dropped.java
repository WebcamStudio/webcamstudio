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
public class Dropped extends Transition {

    @Override
    public void doTransition(final LayoutItem item, int sec) {
        VideoSource source = item.getSource();
        frames = sec * FPS;
        if (frames > 0) {
            int x = item.getX();
            int y = 0 - (item.getHeight());
            source.setOutputWidth(item.getWidth());
            source.setOutputHeight(item.getHeight());
            source.setVolume(item.getVolume());
            source.setOpacity(100);
            float yDelta = (item.getHeight() + item.getY()) / ((float) frames / 2F);
            source.setShowAtX(x);
            source.setShowAtY(y);
            if (!source.isPlaying()) {
                source.startSource();
            }
            for (int i = 0; i < (frames / 2); i++) {
                try {
                    source.setShowAtY(y + (int) (i * yDelta));
                    Thread.sleep(WAITTIME);
                } catch (InterruptedException ex) {
                    Logger.getLogger(getName()).log(Level.SEVERE, null, ex);
                }
            }
            source.setShowAtX(item.getX());
            source.setShowAtY(item.getY());
            source.setOutputWidth(item.getWidth());
            source.setOutputHeight(item.getHeight());
            x = item.getX();
            y = item.getY();
            int w = item.getWidth();
            int h = item.getHeight();
            for (int i = 0; i < (frames / 4); i++) {
                try {
                    source.setOutputWidth(w + i);
                    source.setShowAtX(x - (i / 2));
                    source.setOutputHeight(h - i);
                    source.setShowAtY(y + i);
                    Thread.sleep(WAITTIME);
                } catch (InterruptedException ex) {
                    Logger.getLogger(getName()).log(Level.SEVERE, null, ex);
                }
            }
            w = source.getOutputWidth();
            h = source.getOutputHeight();
            x = source.getShowAtX();
            y = source.getShowAtY();
            for (int i = 0; i < frames / 4; i++) {
                try {
                    source.setOutputWidth(w - i);
                    source.setShowAtX(x + (i / 2));
                    source.setOutputHeight(h + i);
                    source.setShowAtY(y - i);
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
        source.setOpacity(100);

    }
}
