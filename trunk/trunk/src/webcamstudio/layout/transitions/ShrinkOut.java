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
    public void doTransition(final LayoutItem item, int sec) {
        VideoSource source = item.getSource();
        frames = sec * FPS;
        if (frames > 0) {
            source.setOpacity(100);
            source.setVolume(item.getVolume());
            float deltaW = (float) source.getOutputWidth() / (float) frames / 2F;
            float deltaH = (float) source.getOutputHeight() / (float) frames / 2F;
            source.setShowAtX(item.getX());
            source.setShowAtY(item.getY());
            int x = item.getX();
            int y = item.getY();
            int w = item.getWidth();
            int h = item.getHeight();
            source.setOutputHeight(item.getHeight());
            source.setOutputWidth(item.getWidth());
            for (int i = 0; i < frames; i++) {
                try {
                    source.setShowAtX(x + (int) (i * deltaW));
                    source.setShowAtY(y + (int) (i * deltaH));
                    source.setOutputWidth(w - (int) (i * deltaW * 2F));
                    source.setOutputHeight(h - (int) (i * deltaH * 2F));
                    Thread.sleep(WAITTIME);
                } catch (InterruptedException ex) {
                    Logger.getLogger(LayoutItem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (source.isPlaying()) {
            source.stopSource();
        }
        source.setOutputHeight(item.getHeight());
        source.setOutputWidth(item.getWidth());
        source.setShowAtX(item.getX());
        source.setShowAtY(item.getY());
        source.setVolume(item.getVolume());


    }
}
