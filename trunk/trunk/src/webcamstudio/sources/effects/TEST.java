/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;

/**
 *
 * @author pballeux
 */
public class TEST extends Effect {

    private com.jhlabs.image.TwirlFilter filter = new com.jhlabs.image.TwirlFilter();

    @Override
    public void applyEffect(BufferedImage img) {
        //filter.setRange(10);
        //filter.setLevels(3);
        
        Graphics2D buffer = img.createGraphics();
        BufferedImage temp = filter.filter(img, null);
        buffer.setBackground(new Color(0, 0, 0, 0));
        buffer.clearRect(0, 0, img.getWidth(), img.getHeight());
        buffer.drawImage(temp, 0, 0, null);
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
