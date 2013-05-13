/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import webcamstudio.sources.effects.controls.BlockControl;

/**
 *
 * @author pballeux
 */
public class Blink extends Effect {

    long mark = System.currentTimeMillis();
    boolean blink = false;

    @Override
    public void applyEffect(BufferedImage img) {
        if (blink) {
            Graphics2D buffer = img.createGraphics();
            buffer.setBackground(new java.awt.Color(0, 0, 0, 0));
            buffer.clearRect(0, 0, img.getWidth(), img.getHeight());
            buffer.dispose();
        }
        if (System.currentTimeMillis() - mark > 1000) {
            blink = !blink;
            mark = System.currentTimeMillis();
        }

    }

    @Override
    public boolean needApply(){
        return needApply=true;
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
