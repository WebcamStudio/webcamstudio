/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import webcamstudio.sources.effects.controls.NoBackgroundControl;

/**
 *
 * @author pballeux
 */
public class NoBackground_test2 extends Effect {

    private final com.jhlabs.image.KeyFilter filter = new com.jhlabs.image.KeyFilter();
    private BufferedImage background = null;
    private BufferedImage lastImage = null;
    private int rThreshold = 0;
    private int gThreshold = 0;
    private int bThreshold = 0;

    @Override
    public void applyEffect(BufferedImage img) {
//        lastImage = img;
        lastImage = deepCopy(img);
        if (background != null) {
            int[] data = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
            int[] dataBG = ((java.awt.image.DataBufferInt) background.getRaster().getDataBuffer()).getData();
            int r, g, b, c, a, cb;
            for (int i = 0; i < data.length; i++) {
                c = data[i];
                cb = dataBG[i];
//                r = (c & 0x00FF0000) >> 16 - (cb & 0x00FF0000) >> 16;
//                g = (c & 0x0000FF00) >> 8 - (cb & 0x0000FF00) >> 8;
//                b = c & 0x000000FF - cb & 0x000000FF;
                int rRatio = Math.abs((c & 0x00FF0000) >> 16 - (cb & 0x00FF0000) >> 16);
                int gRatio = Math.abs((c & 0x0000FF00) >> 8 - (cb & 0x0000FF00) >> 8);
                int bRatio = Math.abs(c & 0x000000FF - cb & 0x000000FF);
                if (rRatio < 0) {
                    rRatio *= -1;
                }
                if (gRatio < 0) {
                    gRatio *= -1;
                }
                if (bRatio < 0) {
                    bRatio *= -1;
                }
                if (rRatio < rThreshold && bRatio < bThreshold && gRatio < gThreshold) {
                    data[i] = c & 0x00FFFFFF;
                }
            }
        }
        lastImage = deepCopy(img);
    }
    
    public void applyEffectTest(BufferedImage img) {
        lastImage = deepCopy(img);
//        lastImage = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
//        Raster imgData = img.getRaster();
//        lastImage.setData(imgData);
//        System.out.println("processing NoBkGr");
        if (background != null) {
            
//            for(int x = 0; x < img.getWidth(); x++)
//                for(int y = 0; y < img.getHeight(); y++) {
//                    int argb0 = img.getRGB(x, y);
//                    int argb1 = background.getRGB(x, y);
//
//                    int a0 = (argb0 >> 24) & 0xFF;
//                    int r0 = (argb0 >> 16) & 0xFF;
//                    int g0 = (argb0 >>  8) & 0xFF;
//                    int b0 = (argb0      ) & 0xFF;
//
//                    int a1 = (argb1 >> 24) & 0xFF;
//                    int r1 = (argb1 >> 16) & 0xFF;
//                    int g1 = (argb1 >>  8) & 0xFF;
//                    int b1 = (argb1      ) & 0xFF;
//
//                    int aDiff = Math.abs(a1 - a0);
//                    int rDiff = Math.abs(r1 - r0);
//                    int gDiff = Math.abs(g1 - g0);
//                    int bDiff = Math.abs(b1 - b0);
//
//                    int diff = 
//                        (aDiff << 24) | (rDiff << 16) | (gDiff << 8) | bDiff;
//                    img.setRGB(x, y, diff);
            
            int[] data = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
            int[] dataBG = ((java.awt.image.DataBufferInt) background.getRaster().getDataBuffer()).getData();
//            System.out.println("LastImage: "+data.hashCode()+"  BackGround:"+dataBG.hashCode());
            int r, g, b, c, a, cb;
            int r1, g1, b1, a1;
            for (int i = 0; i < data.length; i++) {
                c = data[i];
                cb = dataBG[i];
//                a = (c >> 24) & 0xff;
                r = (c >> 16) & 0xff;
                g = (c >> 8) & 0xff;
                b = c & 0xff;
//                a1 = (cb >> 24) & 0xff;
                r1 = (cb >> 16) & 0xff;
                g1 = (cb >> 8) & 0xff;
                b1 = cb & 0xff;
//                int aDiff = Math.abs(a - a1);
                int rRatio = Math.abs(r1 - r) * 100 / 255;
                int gRatio = Math.abs(g1 - g) * 100 / 255;
                int bRatio = Math.abs(b1 - b) * 100 / 255;
//                int rDiff = Math.abs(r - r1);
//                int gDiff = Math.abs(g - g1);
//                int bDiff = Math.abs(b - b1);
                if (r < 0) {
                    r *= -1;
                }
                if (g < 0) {
                    g *= -1;
                }
                if (b < 0) {
                    b *= -1;
                }
                if (rThreshold > rRatio && gThreshold > gRatio && bThreshold > bRatio) {
                    data[i] &= 0x00FFFFFF;
                }
//                if (rDiff < rThreshold && bDiff < bThreshold && gDiff < gThreshold) {
//                    data[i] &= 0x00FFFFFF;
//                }
                
                
            }
        }
    }
    
    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    
    @Override
    public boolean needApply(){
        return needApply=true;
    }
    public void setRThreshold(int t) {
        rThreshold = t/5;
    }

    public void setGThreshold(int t) {
        gThreshold = t/5;
    }

    public void setBThreshold(int t) {
        bThreshold = t/5;
    }

    public int getRThreshold() {
        return rThreshold;
    }

    public int getGThreshold() {
        return gThreshold;
    }

    public int getBThreshold() {
        return bThreshold;
    }

    public BufferedImage getLastImage() {
        return lastImage;
    }

    @Override
    public JPanel getControl() {
        return null;
//        return new NoBackgroundControl(this);
    }

    @Override
    public void applyStudioConfig(Preferences prefs) {
        prefs.putInt("rThreshold", rThreshold);
        prefs.putInt("gThreshold", gThreshold);
        prefs.putInt("bThreshold", bThreshold);
    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
        rThreshold = prefs.getInt("rThreshold", rThreshold);
        gThreshold = prefs.getInt("gThreshold", gThreshold);
        bThreshold = prefs.getInt("bThreshold", bThreshold);
    }

    public void setBackgroundImage(BufferedImage img) {
        background = img;
    }
}
