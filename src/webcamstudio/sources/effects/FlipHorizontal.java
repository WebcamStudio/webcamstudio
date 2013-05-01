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
public class FlipHorizontal extends Effect{

    @Override
    public void applyEffect(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        Graphics2D buffer = img.createGraphics();
        BufferedImage tempimage = cloneImage(img);
        buffer.setBackground(new java.awt.Color(0,0,0,0));
        buffer.clearRect(0,0,img.getWidth(),img.getHeight());
        buffer.drawImage(tempimage, w, 0, 0, h, 0, 0, w, h, null);
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
