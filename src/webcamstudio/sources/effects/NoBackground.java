/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import javax.swing.JPanel;
import webcamstudio.sources.effects.controls.NoBackgroundControl;

/**
 *
 * @author pballeux (modified by karl)
 */
public class NoBackground extends Effect {

    private BufferedImage background = null;
    private BufferedImage lastImage = null;
    private int rThreshold = 0;
    private int gThreshold = 0;
    private int bThreshold = 0;

//    @Override
    public void applyEffectTest(BufferedImage img) {
        lastImage = deepCopy(img);
        if (background != null) {
            int[] data = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
            int[] dataBG = ((java.awt.image.DataBufferInt) background.getRaster().getDataBuffer()).getData();
            int r, g, b, c, cb;
            for (int i = 0; i < data.length; i++) {
                c = data[i];
                cb = dataBG[i];
                r = ((((c & 0x00FF0000) >> 16))) - (((cb & 0x00FF0000) >> 16));
                g = (((c & 0x0000FF00) >> 8)) - (((cb & 0x0000FF00) >> 8));
                b = (((c & 0x000000FF))) - ((cb & 0x000000FF));
                if (r < 0) {
                    r *= -1;
                }
                if (g < 0) {
                    g *= -1;
                }
                if (b < 0) {
                    b *= -1;
                }
                int rRatio = Math.abs(r) * 100 / 255;
                int gRatio = Math.abs(g) * 100 / 255;
                int bRatio = Math.abs(b) * 100 / 255;
                
                if (rRatio < rThreshold && bRatio < bThreshold && gRatio < gThreshold) {
                    data[i] = c & 0x00FFFFFF;
                }
            }
        }
    }
    
    @Override
    public void applyEffect(BufferedImage img) {
        lastImage = deepCopy(img);
        if (background != null) {
            int[] data = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
            int[] dataBG = ((java.awt.image.DataBufferInt) background.getRaster().getDataBuffer()).getData();
            int r, g, b, c, cb;
            int r1, g1, b1;
            for (int i = 0; i < data.length; i++) {
                c = data[i];
                cb = dataBG[i];
                r = (c >> 16) & 0xff;
                g = (c >> 8) & 0xff;
                b = c & 0xff;
                r1 = (cb >> 16) & 0xff;
                g1 = (cb >> 8) & 0xff;
                b1 = cb & 0xff;
                int rRatio = Math.abs(r1 - r) * 100 / 255;
                int gRatio = Math.abs(g1 - g) * 100 / 255;
                int bRatio = Math.abs(b1 - b) * 100 / 255;
                if (rThreshold > rRatio && gThreshold > gRatio && bThreshold > bRatio) {
                    data[i] &= 0x00FFFFFF;
                }
            }
        }
    }
    
    BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        BufferedImage temp = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        return temp;
    }
    
    public void setRThreshold(int t) {
        rThreshold = t/2;
    }

    public void setGThreshold(int t) {
        gThreshold = t/2;
    }

    public void setBThreshold(int t) {
        bThreshold = t/2;
    }

    public int getRThreshold() {
        return rThreshold*2;
    }

    public int getGThreshold() {
        return gThreshold*2;
    }

    public int getBThreshold() {
        return bThreshold*2;
    }

    public BufferedImage getLastImage() {
        return lastImage;
    }
    
    @Override
    public boolean needApply(){
        return needApply=true;
    }
    
    @Override
    public JPanel getControl() {
        return new NoBackgroundControl(this);
    }

    public void setBackgroundImage(BufferedImage img) {
        if (img != null) {
            background = img;
        }
    }

    @Override
    public void resetFX() {
        // nothing here.
    }
}
