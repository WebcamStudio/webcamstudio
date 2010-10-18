/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import webcamstudio.sources.effects.controls.ChromaKeyControl;

/**
 *
 * @author pballeux
 */
public class ChromaKey extends Effect {

    private int rTolerance = 0;
    private int gTolerance = 0;
    private int bTolerance = 0;
    private Color color = new Color(0x00ff00);

    @Override
    public void applyEffect(BufferedImage img) {
        
        int[] data = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int r, g, b, c;
        for (int i = 0; i < data.length; i++) {
            c = data[i];
            r = (c & 0x00FF0000) >> 16;
            g = (c & 0x0000FF00) >> 8;
            b = (c & 0x000000FF) >> 0;
            int rRatio = Math.abs(color.getRed() - r) * 100 / 255;
            int gRatio = Math.abs(color.getGreen() - g) * 100 / 255;
            int bRatio = Math.abs(color.getBlue() - b) * 100 / 255;
            if (rTolerance >= rRatio && gTolerance >= gRatio && bTolerance >= bRatio) {
                data[i] = data[i] & 0x00FFFFFF;
            }
        }
    }

    public int getColor() {
        return color.getRGB();
    }

    public void setColor(int c) {
        color = new Color(c);
    }

    @Override
    public JPanel getControl() {
        return new ChromaKeyControl(this);
    }

    /**
     * @return the hTolerance
     */
    public int getrTolerance() {
        return rTolerance;
    }

    /**
     * @param hTolerance the hTolerance to set
     */
    public void setrTolerance(int rTolerance) {
        this.rTolerance = rTolerance;
    }

    /**
     * @return the sTolerance
     */
    public int getgTolerance() {
        return gTolerance;
    }

    /**
     * @param sTolerance the sTolerance to set
     */
    public void setgTolerance(int gTolerance) {
        this.gTolerance = gTolerance;
    }

    /**
     * @return the bTolerance
     */
    public int getbTolerance() {
        return bTolerance;
    }

    /**
     * @param bTolerance the bTolerance to set
     */
    public void setbTolerance(int bTolerance) {
        this.bTolerance = bTolerance;
    }

    @Override
    public void applyStudioConfig(Preferences prefs) {
        prefs.putInt("rtolerance", rTolerance);
        prefs.putInt("gtolerance", gTolerance);
        prefs.putInt("btolerance", bTolerance);
        prefs.putInt("color", color.getRGB());
    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
        rTolerance = prefs.getInt("rtolerance", rTolerance);
        gTolerance = prefs.getInt("gtolerance", gTolerance);
        bTolerance = prefs.getInt("btolerance", bTolerance);
        color = new Color(prefs.getInt("color", color.getRGB()));
    }
}
