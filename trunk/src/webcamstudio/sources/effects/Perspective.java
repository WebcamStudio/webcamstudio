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
import webcamstudio.sources.effects.controls.PerspectiveControl;

/**
 *
 * @author pballeux
 */
public class Perspective extends Effect {

    private com.jhlabs.image.PerspectiveFilter filter = new com.jhlabs.image.PerspectiveFilter();
    private float x1 = 0;
    private float y1 = 0;
    private float x2 = 320;
    private float y2 = 0;
    private float x3 = 320;
    private float y3 = 240;
    private float x4 = 0;
    private float y4 = 240;

    @Override
    public void applyEffect(BufferedImage img) {
        filter.setCorners(x1, y1, x2, y2, x3, y3, x4, y4);
        Graphics2D buffer = img.createGraphics();
        BufferedImage temp = filter.filter(img, null);
        buffer.setBackground(new Color(0, 0, 0, 0));
        buffer.clearRect(0, 0, img.getWidth(), img.getHeight());
        buffer.drawImage(temp, 0, 0, img.getWidth(), img.getHeight(), 0, 0, temp.getWidth(), temp.getHeight(), null);
        buffer.dispose();
    }

    @Override
    public JPanel getControl() {
        return new PerspectiveControl(this);
    }

    @Override
    public void applyStudioConfig(Preferences prefs) {
        prefs.putFloat("x1", x1);
        prefs.putFloat("y1", y1);
        prefs.putFloat("x2", x2);
        prefs.putFloat("y2", y2);
        prefs.putFloat("x3", x3);
        prefs.putFloat("y3", y3);
        prefs.putFloat("x4", x4);
        prefs.putFloat("y4", y4);
    }

    @Override
    public boolean needApply(){
        return needApply=true;
    }
    @Override
    public void loadFromStudioConfig(Preferences prefs) {
        x1 = prefs.getFloat("x1", x1);
        y1 = prefs.getFloat("y1", y1);
        x2 = prefs.getFloat("x2", x2);
        y2 = prefs.getFloat("y2", y2);
        x3 = prefs.getFloat("x3", x3);
        y3 = prefs.getFloat("y3", y3);
        x4 = prefs.getFloat("x4", x4);
        y4 = prefs.getFloat("y4", y4);

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
     * @return the x2
     */
    public float getX2() {
        return x2;
    }

    /**
     * @param x2 the x2 to set
     */
    public void setX2(float x2) {
        this.x2 = x2;
    }

    /**
     * @return the y2
     */
    public float getY2() {
        return y2;
    }

    /**
     * @param y2 the y2 to set
     */
    public void setY2(float y2) {
        this.y2 = y2;
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

    /**
     * @return the x4
     */
    public float getX4() {
        return x4;
    }

    /**
     * @param x4 the x4 to set
     */
    public void setX4(float x4) {
        this.x4 = x4;
    }

    /**
     * @return the y4
     */
    public float getY4() {
        return y4;
    }

    /**
     * @param y4 the y4 to set
     */
    public void setY4(float y4) {
        this.y4 = y4;
    }
}
