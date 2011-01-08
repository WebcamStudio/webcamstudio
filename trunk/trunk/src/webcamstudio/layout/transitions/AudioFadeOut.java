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
public class AudioFadeOut extends Transition {

    @Override
    public void doTransition(final LayoutItem item) {
        VideoSource source = item.getSource();
        source.setOpacity(0);
        float deltaAudio = ((float)source.getVolume()) / 20f;
        float volume = source.getVolume();
        source.fireSourceUpdated();
        for (int i = 0; i <= 20; i++) {
            try {
                volume-=deltaAudio;
                source.setVolume((int)volume);
                source.fireSourceUpdated();
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(LayoutItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        source.setVolume(0);
        if (source.isPlaying()){
            source.stopSource();
        }

    }
}
