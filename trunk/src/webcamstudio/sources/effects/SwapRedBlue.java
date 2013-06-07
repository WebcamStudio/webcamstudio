/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;

/**
 *
 * @author pballeux
 */
public class SwapRedBlue extends Effect {

    

    @Override
    public void applyEffect(BufferedImage img) {
        
        int[] data = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int r, g, b, c,a;
        for (int i = 0; i < data.length; i++) {
            c = data[i];
            a = (c & 0xFF000000);
            r = (c & 0x00FF0000) >> 16;
            g = (c & 0x0000FF00);
            b = (c & 0x000000FF);
            data[i] = a + (b << 16) + g + r;
        }
    }

    @Override
    public boolean needApply(){
        return needApply=false;
    }
    @Override
    public JPanel getControl() {
        return null;
    }

    @Override
    public void applyStudioConfig(Preferences prefs) {

    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
        
    }

 
}
