/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Rotate;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;

/**
 *
 * @author karl
 */
public class ComboGhost extends Effect {
    private int angle = 0;
    private int scale = 1;
    private int x, y = 0;
    
    @Override
    public void applyEffect(BufferedImage img) {
        FastBitmap imageIn = new FastBitmap(img);
        int width  = img.getWidth();
        int height = img.getHeight();
        //Convolution process.
        Rotate.Algorithm algorithm = Rotate.Algorithm.BILINEAR;
        Rotate c = new Rotate(angle,algorithm);
        c.applyInPlace(imageIn);
        BufferedImage temp = imageIn.toBufferedImage();
        int w = width-scale;
        int h = height-scale;
        if (w < 1) {
            w = 1;
        }
        if (h < 1) {
            h = 1;
        }
        temp = Scalr.resize(temp, Mode.AUTOMATIC, w, h);

        Graphics2D buffer = img.createGraphics();
        buffer.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                           RenderingHints.VALUE_INTERPOLATION_BILINEAR);
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
        buffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 50 / 100F));
        buffer.drawImage(temp, x, y, null);
        buffer.dispose();
        angle += 2;
        scale += 2;
        if (scale>=height || scale>=width){
            scale = 1;
        }
    }
    
    @Override
    public boolean needApply(){
        return needApply=true;
    }
    @Override
    public JPanel getControl() {
        return null;
    }

    @Override
    public void resetFX() {
        // nothing here.
    }
}
