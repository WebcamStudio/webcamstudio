/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.SaltAndPepperNoise;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author karl
 */
public class SaltNPepper extends Effect {

    private final com.jhlabs.image.CircleFilter filter = new com.jhlabs.image.CircleFilter();
    private final RGB rgb = new RGB();
    @Override
    public void applyEffect(BufferedImage img) {
        FastBitmap imageIn = new FastBitmap(img);
        imageIn.toRGB();
        SaltAndPepperNoise sAndpNoise = new SaltAndPepperNoise();
        sAndpNoise.applyInPlace(imageIn);
        BufferedImage temp = imageIn.toBufferedImage();
        
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
        buffer.drawImage(temp, 0, 0, img.getWidth(), img.getHeight(), 0, 0, temp.getWidth(), temp.getHeight(), null);
        buffer.dispose();

    }
    
    @Override
    public JPanel getControl() {
        return null;
    }

    @Override
    public boolean needApply(){
        return needApply=true;
    }

    @Override
    public void resetFX() {
        // nothing here.
    }
}
