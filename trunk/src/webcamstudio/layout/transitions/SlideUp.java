/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.layout.transitions;

import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.layout.LayoutItem;
import webcamstudio.mixers.VideoMixer;
import webcamstudio.sources.VideoSource;

/**
 *
 * @author pballeux
 */
public class SlideUp extends Transition {

    @Override
    public void doTransition(final LayoutItem item, int sec) {
        VideoSource source = item.getSource();
        frames = sec * FPS;
        if (frames > 0) {
            int x = item.getX();
            int y = VideoMixer.getInstance().getHeigt();
            source.setOutputWidth(item.getWidth());
            source.setOutputHeight(item.getHeight());
            source.setVolume(item.getVolume());
            source.setOpacity(100);
            float yDelta = ((VideoMixer.getInstance().getHeigt() - item.getY()) / (float) frames);
            source.setShowAtX(x);
            source.setShowAtY(y);
            if (!source.isPlaying()) {
                source.startSource();
            }
            for (float i = 0; i < frames; i++) {
                try {
                    source.setShowAtY(y - (int) (i * yDelta));
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
