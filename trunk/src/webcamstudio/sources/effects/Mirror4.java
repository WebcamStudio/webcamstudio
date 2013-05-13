/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webcamstudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;

/**
 *
 * @author pballeux
 */
public class Mirror4 extends Effect{

    @Override
    public void applyEffect(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        Graphics2D buffer = img.createGraphics();
        BufferedImage original = cloneImage(img);
        buffer.setBackground(new java.awt.Color(0,0,0,0));
        buffer.clearRect(0, h/2, w, 0);
        buffer.drawImage(original, 0, h/2, w, 0, 0, h/2, w, h, null);
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
    @Override
    public boolean needApply(){
        return needApply=true;
    }

}
