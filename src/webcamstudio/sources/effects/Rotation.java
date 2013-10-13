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
import webcamstudio.sources.effects.controls.RotationControl;

/**
 *
 * @author pballeux
 */
public class Rotation extends Effect{
    private com.jhlabs.image.RotateFilter filter = new com.jhlabs.image.RotateFilter();
    private float rotation = 0;
    @Override
    public void applyEffect(BufferedImage img) {
        filter.setAngle(rotation);
        filter.setEdgeAction(com.jhlabs.image.RotateFilter.ZERO);
        filter.setInterpolation(com.jhlabs.image.RotateFilter.BILINEAR);
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
        buffer.setBackground(new Color(0,0,0,0));
        buffer.clearRect(0,0,img.getWidth(),img.getHeight());
        buffer.drawImage(temp, 0, 0,img.getWidth(),img.getHeight(),0,0,temp.getWidth(),temp.getHeight(),null);
        buffer.dispose();
    }
    @Override
    public boolean needApply(){
        return needApply=true;
    }
   public void setRotation(int value){
       rotation=(float)Math.toRadians(value);
   }
   public int getRotation(){
       return (int)Math.toDegrees(rotation);
   }
    @Override
    public JPanel getControl() {
        return new RotationControl(this);
    }

    @Override
    public void applyStudioConfig(Preferences prefs) {
        prefs.putFloat("rotation", rotation);
    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
        rotation = prefs.getFloat("rotation", rotation);
    }

}
