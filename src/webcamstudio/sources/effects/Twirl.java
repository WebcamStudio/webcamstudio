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
import webcamstudio.sources.effects.controls.TwirlControl;

/**
 *
 * @author pballeux
 */
public class Twirl extends Effect {

    private com.jhlabs.image.TwirlFilter filter = new com.jhlabs.image.TwirlFilter();
    private float radius = 160;
    private float angle = (float)Math.toRadians(90f);
    @Override
    public void applyEffect(BufferedImage img) {
        filter.setRadius(radius);
        filter.setAngle(angle);
        Graphics2D buffer = img.createGraphics();
        BufferedImage temp = filter.filter(img, null);
        buffer.setBackground(new Color(0, 0, 0, 0));
        buffer.clearRect(0, 0, img.getWidth(), img.getHeight());
        buffer.drawImage(temp, 0, 0, img.getWidth(), img.getHeight(), 0, 0, temp.getWidth(), temp.getHeight(), null);
        buffer.dispose();
    }

    @Override
    public boolean needApply(){
        return needApply=true;
    }
    @Override
    public JPanel getControl() {
        return new TwirlControl(this);
    }

    @Override
    public void applyStudioConfig(Preferences prefs) {
        prefs.putFloat("radius",getRadius());
        prefs.putFloat("angle",getAngle());
    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
        setRadius(prefs.getFloat("radius", getRadius()));
        setAngle(prefs.getFloat("angle", getAngle()));
    }

    /**
     * @return the radius
     */
    public float getRadius() {
        return radius;
    }

    /**
     * @param radius the radius to set
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

   
    /**
     * @return the angle
     */
    public float getAngle() {
        return angle;
    }

    /**
     * @param angle the angle to set
     */
    public void setAngle(float angle) {
        this.angle = angle;
    }

}
