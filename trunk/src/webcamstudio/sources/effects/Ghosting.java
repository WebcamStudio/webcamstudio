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
import javax.swing.JPanel;

/**
 *
 * @author karl
 */
public class Ghosting extends Effect {

    private ArrayList<FastBitmap> previusImagesIn = new ArrayList<>();
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
        
        buffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 30 / 100F));
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
