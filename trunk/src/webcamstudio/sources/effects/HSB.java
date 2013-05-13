/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webcamstudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import webcamstudio.sources.effects.controls.HSBControl;

/**
 *
 * @author pballeux
 */
public class HSB extends Effect{
    private com.jhlabs.image.HSBAdjustFilter filter = new com.jhlabs.image.HSBAdjustFilter();
    private final float  ratio = 100f;
    private float hFactor = 0f/ratio;//gain
    private float sFactor = 0f/ratio;//bias
    private float bFactor = 0f/ratio;

    @Override
    public void applyEffect(BufferedImage img) {
        filter.setHFactor(hFactor);
        filter.setSFactor(sFactor);
        filter.setBFactor(bFactor);
        Graphics2D buffer = img.createGraphics();
        BufferedImage temp = filter.filter(img, null);
        buffer.setBackground(new java.awt.Color(0,0,0,0));
        buffer.clearRect(0,0,img.getWidth(),img.getHeight());
        buffer.drawImage(temp, 0, 0,null);
        buffer.dispose();
    }
    @Override
    public boolean needApply(){
        return needApply=true;
    }
    @Override
    public JPanel getControl() {
        return new HSBControl(this);
    }

    /**
     * @return the brightness
     */
    public int getHFactor() {
        return (int)(hFactor*ratio);
    }

    /**
     * @param brightness the brightness to set
     */
    public void setHFactor(int hFactor) {
        this.hFactor = ((float)hFactor)/ratio;
    }

    /**
     * @return the contrast
     */
    public int getSFactor() {
        return (int)(sFactor*ratio);
    }

    /**
     * @param contrast the contrast to set
     */
    public void setSFactor(int sFactor) {
        this.sFactor = ((float)sFactor)/ratio;
    }
    
    public int getBFactor() {
        return (int)(bFactor*ratio);
    }

    /**
     * @param contrast the contrast to set
     */
    public void setBFactor(int bFactor) {
        this.bFactor = ((float)bFactor)/ratio;
    }

    @Override
    public void applyStudioConfig(Preferences prefs) {
        prefs.putFloat("hFactor", hFactor);
        prefs.putFloat("sFactor", sFactor);
        prefs.putFloat("bFactor", bFactor);
        
    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
        hFactor=prefs.getFloat("hFactor", hFactor);
        sFactor=prefs.getFloat("sFactor", sFactor);
        bFactor=prefs.getFloat("bFactor", bFactor);
    }

}
