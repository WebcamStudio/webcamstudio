/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webcamstudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import webcamstudio.sources.effects.controls.WaterFxControl;

/**
 *
 * @author pballeux
 */
public class WaterFx extends Effect{
    final com.jhlabs.image.WaterFilter filter = new com.jhlabs.image.WaterFilter();
    private float radius = 320;//gain
    private float amplitude = 5;//bias
    private float wavelength = 50;
    @Override
    public void applyEffect(BufferedImage img) {
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
        filter.setRadius(radius);
        filter.setAmplitude(amplitude);
        filter.setWavelength(wavelength);
        BufferedImage temp = filter.filter(img, null);
        buffer.setBackground(new java.awt.Color(0,0,0,0));
        buffer.clearRect(0,0,img.getWidth(),img.getHeight());
        buffer.drawImage(temp, 0, 0,null);
        buffer.dispose();
    }
    @Override
    public JPanel getControl() {
        return new WaterFxControl(this);
    }
    
    public float getRadius() {
        return radius;
    }
    
    public float getAmplitude() {
        return amplitude;
    }
    
    public float getWavelength() {
        return wavelength;
    }
    
    public void setRadius(float r) {
        radius = r;
    }
    
    public void setAmplitude(float a) {
        amplitude = a;
    }
    
    public void setWavelength(float w) {
        wavelength = w;
    }
    
    @Override
    public void applyStudioConfig(Preferences prefs) {

    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
        
    }
    @Override
    public boolean needApply(){
        return needApply=false;
    }

}
