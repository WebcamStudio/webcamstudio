/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webcamstudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import webcamstudio.sources.effects.controls.HSBControl;

/**
 *
 * @author pballeux (modified by karl)
 */
public class HSB extends Effect{
    private final com.jhlabs.image.HSBAdjustFilter filter = new com.jhlabs.image.HSBAdjustFilter();
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
     * @param hFactor
     */
    public void setHFactor(int hFactor) {
        this.hFactor = hFactor/ratio;
    }

    /**
     * @return the contrast
     */
    public int getSFactor() {
        return (int)(sFactor*ratio);
    }

    /**
     * @param sFactor
     */
    public void setSFactor(int sFactor) {
        this.sFactor = sFactor/ratio;
    }
    
    public int getBFactor() {
        return (int)(bFactor*ratio);
    }

    /**
     * @param bFactor
     */
    public void setBFactor(int bFactor) {
        this.bFactor = bFactor/ratio;
    }

    @Override
    public void resetFX() {
        // nothing here.
    }
}
