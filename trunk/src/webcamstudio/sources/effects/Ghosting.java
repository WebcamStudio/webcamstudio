/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import Catalano.Imaging.FastBitmap;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.JPanel;

/**
 *
 * @author pballeux
 */
public class Ghosting extends Effect {

    private ArrayList<FastBitmap> previusImagesIn = new ArrayList<>();
    private int imageCount = 0;
    FastBitmap previusImageIn;
    @Override
    public void applyEffect(BufferedImage img) {
        
        FastBitmap imageIn = new FastBitmap(img);
        previusImagesIn.add(imageIn);
        if (imageCount > 25){
            previusImageIn = previusImagesIn.get(1);
            previusImagesIn.remove(0);
        } else {
            previusImageIn = imageIn;
        }
        BufferedImage temp = previusImageIn.toBufferedImage();

        Graphics2D buffer = img.createGraphics();
        buffer.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                           java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
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
        
        buffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 90 / 100F));
        buffer.drawImage(temp, 0, 0, null);
        buffer.dispose();
        imageCount ++ ;
    }
    
    @Override
    public boolean needApply(){
        return needApply=true;
    }
    
    @Override
    public JPanel getControl() {
        JPanel dummy = new JPanel();
        return dummy;
//        return new TwirlControl(this);
    }

    @Override
    public void applyStudioConfig(Preferences prefs) {
    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) { 
    }

    /**
     * @return the radius
     */
//    public float getRadius() {
//        return radius;
//    }
//
//    /**
//     * @param radius the radius to set
//     */
//    public void setRadius(float radius) {
//        this.radius = radius;
//    }

   
//    /**
//     * @return the angle
//     */
//    public float getAngle() {
//        return angle;
//    }
//
//    /**
//     * @param angle the angle to set
//     */
//    public void setAngle(float angle) {
//        this.angle = angle;
//    }

}
