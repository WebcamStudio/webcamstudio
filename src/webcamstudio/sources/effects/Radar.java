/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;

/**
 *
 * @author pballeux
 */
public class Radar extends Effect {

    private int lastColumn = 0;

    @Override
    public void applyEffect(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int frame = img.getWidth()/2;
        BufferedImage original = cloneImage(img);
        int[] pixels = original.getRGB(0, 0, w, h, null, 0, w);
        int[] pixelsOut = new int[pixels.length];
        for (int i = 0;i<pixelsOut.length;i++){
            pixelsOut[i] = 0xFF000000;
        }
        for (int i = 0; i < frame && (i + lastColumn) < w; i+=4) {
            int opacity = (i * 255 / frame);
            for (int y = 0; y < h; y++) {
                if ((i+lastColumn) >= 0) {
                    int green = (0x0000FF00 & pixels[(y * w) + (i + lastColumn)]) >> 8;
                    green = (green / 100) * 100;
                    if (i >= (frame-4)){
                        pixelsOut[(y * w) + (i + lastColumn)] = 0xFF00FF00;
                    } else if (green > 0) {
                        green = green *opacity / 255;
                        pixelsOut[(y * w) + (i + lastColumn)] = (green << 8) + 0xFF000000;
                    } 
                }
            }
        }
        img.setRGB(0, 0, w, h, pixelsOut, 0, w);
        lastColumn += 4;
        if (lastColumn > w) {
            lastColumn = 0 - frame;
        }

    }

    @Override
    public JPanel getControl() {
        return null;
    }

    @Override
    public void applyStudioConfig(Preferences prefs) {
    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
    }
    @Override
    public boolean needApply(){
        return needApply=true;
}
}
