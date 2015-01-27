/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import Catalano.Imaging.FastBitmap;
import static java.awt.AlphaComposite.SRC_OVER;
import static java.awt.AlphaComposite.getInstance;
import java.awt.Graphics2D;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_COLOR_RENDERING;
import static java.awt.RenderingHints.KEY_DITHERING;
import static java.awt.RenderingHints.KEY_FRACTIONALMETRICS;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;
import static java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED;
import static java.awt.RenderingHints.VALUE_DITHER_DISABLE;
import static java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.RenderingHints.VALUE_RENDER_SPEED;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author karl
 */
public class Ghosting extends Effect {

    private final ArrayList<FastBitmap> previusImagesIn = new ArrayList<>();
    private int imageCount = 0;
    FastBitmap previusImageIn;
    @Override
    public void applyEffect(BufferedImage img) {
        
        FastBitmap imageIn = new FastBitmap(img);
        imageIn.toRGB();
        previusImagesIn.add(imageIn);
        if (imageCount > 8){
            previusImageIn = previusImagesIn.get(1);
            previusImagesIn.remove(0);
        } else {
            previusImageIn = imageIn;
        }
        BufferedImage temp = previusImageIn.toBufferedImage();

        Graphics2D buffer = img.createGraphics();
        buffer.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
        buffer.setRenderingHint(KEY_RENDERING, VALUE_RENDER_SPEED);
        buffer.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);
        buffer.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_OFF);
        buffer.setRenderingHint(KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_OFF);
        buffer.setRenderingHint(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_SPEED);
        buffer.setRenderingHint(KEY_DITHERING, VALUE_DITHER_DISABLE);
        
        buffer.setComposite(getInstance(SRC_OVER, 30 / 100F));
//        buffer.drawImage(temp, 0, 0,img.getWidth(),img.getHeight(),0,0,temp.getWidth(),temp.getHeight(),null);
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
    }

    @Override
    public void resetFX() {
        // nothing here.
    }
}
