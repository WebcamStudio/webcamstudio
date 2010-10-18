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
public class Glow extends Effect{
    private com.jhlabs.image.GlowFilter filter = new com.jhlabs.image.GlowFilter();
    @Override
    public void applyEffect(BufferedImage img) {
        filter.setAmount(0.09f);
        Graphics2D buffer = img.createGraphics();
        BufferedImage temp = filter.filter(img, null);
        buffer.setBackground(new java.awt.Color(0,0,0,0));
        buffer.clearRect(0,0,img.getWidth(),img.getHeight());
        buffer.drawImage(temp, 0, 0,null);
        buffer.dispose();
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
