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
public class ShrinkIn extends Transition {

    @Override
    public void doTransition(final LayoutItem item,int sec) {
        VideoSource source = item.getSource();
        frames = sec * FPS;
        source.setVolume(item.getVolume());
        source.setOutputWidth(0);
        source.setOutputHeight(0);
        source.setOpacity(100);        
        float deltaW = (float)item.getWidth()/(float)frames/2F;
        float deltaH = (float)item.getHeight()/(float)frames/2F;
        int x = item.getX()+(item.getWidth()/2);
        int y = item.getY()+(item.getHeight()/2);
        source.setShowAtX(x);
        source.setShowAtY(y);
        if (!source.isPlaying()) {
            source.startSource();
        }

        for (float i = 0; i < frames; i++) {
            try {
                source.setShowAtX(x-(int)(i*deltaW));
                source.setShowAtY(y-(int)(i*deltaH));
                source.setOutputWidth((int)(deltaW*i*2F));
                source.setOutputHeight((int)(deltaH*i*2F));
                Thread.sleep(WAITTIME);
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
