/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webcamstudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import webcamstudio.sources.effects.controls.ContrastControl;

/**
 *
 * @author pballeux
 */
public class Contrast extends Effect{
    private com.jhlabs.image.ContrastFilter filter = new com.jhlabs.image.ContrastFilter();
    private final float  ratio = 100f;
    private float brightness = 100f/ratio;
    private float contrast = 100f/ratio;

    @Override
    public void applyEffect(BufferedImage img) {
        filter.setBrightness(brightness);
        filter.setContrast(contrast);
        Graphics2D buffer = img.createGraphics();
        BufferedImage temp = filter.filter(img, null);
        buffer.setBackground(new java.awt.Color(0,0,0,0));
        buffer.clearRect(0,0,img.getWidth(),img.getHeight());
        buffer.drawImage(temp, 0, 0,null);
        buffer.dispose();
    }

    @Override
    public JPanel getControl() {
        return new ContrastControl(this);
    }

    /**
     * @return the brightness
     */
    public int getBrightness() {
        return (int)(brightness*ratio);
    }

    /**
     * @param brightness the brightness to set
     */
    public void setBrightness(int brightness) {
        this.brightness = ((float)brightness)/ratio;
    }

    /**
     * @return the contrast
     */
    public int getContrast() {
        return (int)(contrast*ratio);
    }

    /**
     * @param contrast the contrast to set
     */
    public void setContrast(int contrast) {
        this.contrast = ((float)contrast)/ratio;
    }

    @Override
    public void applyStudioConfig(Preferences prefs) {
        prefs.putFloat("brightness", brightness);
        prefs.putFloat("contrast", contrast);
    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
        brightness=prefs.getFloat("brightness", brightness);
        contrast=prefs.getFloat("contrast", contrast);

    }

}
