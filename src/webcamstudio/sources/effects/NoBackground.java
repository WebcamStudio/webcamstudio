/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import webcamstudio.sources.effects.controls.NoBackgroundControl;

/**
 *
 * @author pballeux
 */
public class NoBackground extends Effect {

    private final com.jhlabs.image.KeyFilter filter = new com.jhlabs.image.KeyFilter();
    private BufferedImage background = null;
    private BufferedImage lastImage = null;
    private int rThreshold = 50;
    private int gThreshold = 50;
    private int bThreshold = 50;

    @Override
    public void applyEffect(BufferedImage img) {
        lastImage = img;
        if (background != null) {
            int[] data = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
            int[] dataBG = ((java.awt.image.DataBufferInt) background.getRaster().getDataBuffer()).getData();
            int r, g, b, c, a, cb;
            for (int i = 0; i < data.length; i++) {
                c = data[i];
                cb = dataBG[i];
                a = (c & 0xFF000000) >> 24;
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
                if (r < rThreshold && b < bThreshold && g < gThreshold) {
                    data[i] = c & 0x00FFFFFF;
                }
            }
        }
    }
    @Override
    public boolean needApply(){
        return needApply=true;
    }
    public void setRThreshold(int t) {
        rThreshold = t;
    }

    public void setGThreshold(int t) {
        gThreshold = t;
    }

    public void setBThreshold(int t) {
        bThreshold = t;
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
        return new NoBackgroundControl(this);
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
