/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import webcamstudio.sources.effects.controls.PerspectiveControl;

/**
 *
 * @author pballeux (modified by karl)
 */
public class Perspective extends Effect {

    private final com.jhlabs.image.PerspectiveFilter filter = new com.jhlabs.image.PerspectiveFilter();
    private float x1 = 0;
    private float y1 = 0;
    private float x2 = 0;
    private float y2 = 0;
    private float x3 = 0;
    private float y3 = 0;
    private float x4 = 0;
    private float y4 = 0;

    @Override
    public void applyEffect(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        filter.setCorners(x1, y1, w+x2, y2, w+x3, h+y3, x4, h+y4);
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
        buffer.setBackground(new Color(0, 0, 0, 0));
        buffer.clearRect(0, 0, w, h);
        buffer.drawImage(temp, 0, 0, w, h, 0, 0, temp.getWidth(), temp.getHeight(), null);
        buffer.dispose();
    }

    @Override
    public JPanel getControl() {
        return new PerspectiveControl(this);
    }

    @Override
    public boolean needApply(){
        return needApply= true;
    }

    /**
     * @return the x1
     */
    public float getX1() {
        return x1;
    }

    /**
     * @param x1 the x1 to set
     */
    public void setX1(float x1) {
        this.x1 = x1;
    }

    /**
     * @return the y1
     */
    public float getY1() {
        return y1;
    }

    /**
     * @param y1 the y1 to set
     */
    public void setY1(float y1) {
        this.y1 = y1;
    }

    /**
     * @return the x3
     */
    public float getX3() {
        return x3;
    }

    /**
     * @param x3 the x3 to set
     */
    public void setX3(float x3) {
        this.x3 = x3;
    }

    /**
     * @return the y3
     */
    public float getY3() {
        return y3;
    }

    /**
     * @param y3 the y3 to set
     */
    public void setY3(float y3) {
        this.y3 = y3;
    }

    @Override
    public void resetFX() {
        // nothing here.
    }
}
