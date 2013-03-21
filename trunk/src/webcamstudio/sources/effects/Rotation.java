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
        BufferedImage temp = filter.filter(img, null);
        buffer.setBackground(new Color(0,0,0,0));
        buffer.clearRect(0,0,img.getWidth(),img.getHeight());
        buffer.drawImage(temp, 0, 0,img.getWidth(),img.getHeight(),0,0,temp.getWidth(),temp.getHeight(),null);
        buffer.dispose();
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
