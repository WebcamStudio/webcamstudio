/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.swing.JPanel;

/**
 *
 * @author karl
 */
public class Convolve extends Effect {
    private final Color color = new Color(-16711936);

    @Override
    public void applyEffect(BufferedImage img) {
        BufferedImage dstImage = null;
        float[] sharpen = new float[] {
        0.0f, -1.0f, 0.0f,
        -1.0f, 5.0f, -1.0f,
        0.0f, -1.0f, 0.0f
        };
        Kernel kernel = new Kernel(3, 3, sharpen);
        ConvolveOp op = new ConvolveOp(kernel);
        dstImage = op.filter(img, null);
        img = dstImage;
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
