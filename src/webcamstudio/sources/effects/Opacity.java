/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import webcamstudio.sources.effects.controls.OpacityControl;

/**
 *
 * @author pballeux (modified by karl)
 */
public class Opacity extends Effect {

    private int opacity = 100;

    @Override
    public void applyEffect(BufferedImage img) {
        BufferedImage temp = cloneImage(img);
        Graphics2D buffer = img.createGraphics();
        buffer.setRenderingHint(RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_SPEED);
        buffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_OFF);
        buffer.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        buffer.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                           RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        buffer.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                           RenderingHints.VALUE_COLOR_RENDER_SPEED);
        buffer.setRenderingHint(RenderingHints.KEY_DITHERING,
                           RenderingHints.VALUE_DITHER_DISABLE);
        buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, opacity / 100F));
        buffer.setBackground(new Color(0,0,0,0));
        buffer.clearRect(0,0,img.getWidth(),img.getHeight());
        buffer.drawImage(temp, 0, 0,null);
        buffer.dispose();
    }

    public void setOpacity(int o){
        opacity=o;
    }
    public int getOpacity(){
        return opacity;
    }
    @Override
    public JPanel getControl() {
        return new OpacityControl(this);
    }

    @Override
    public void applyStudioConfig(Preferences prefs) {
        prefs.putInt("opacity", opacity);
    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
        opacity=prefs.getInt("opacity", opacity);
    }
    @Override
    public boolean needApply(){
        return needApply=true;
    }

 
}
