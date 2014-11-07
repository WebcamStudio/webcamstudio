/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import webcamstudio.sources.effects.controls.NoBackgroundControl;

/**
 *
 * @author pballeux (modified by karl)
 */
public class NoBackground_Old extends Effect {

    private final com.jhlabs.image.KeyFilter filter = new com.jhlabs.image.KeyFilter();
    private BufferedImage background = null;
    private BufferedImage lastImage = null;
    private float rThreshold = 0;
    private float gThreshold = 0;
    private float bThreshold = 0;

    @Override
    public void applyEffect(BufferedImage img) {
        lastImage = img;
        if (background != null) {
            filter.setCleanImage(background);
            filter.setDestination(img);
            filter.setHTolerance(rThreshold);
            filter.setSTolerance(gThreshold);
            filter.setBTolerance(bThreshold);
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
            BufferedImage temp = filter.filter(img, null);
            buffer.setBackground(new Color(0, 0, 0, 0));
            buffer.clearRect(0, 0, img.getWidth(), img.getHeight());
            buffer.drawImage(temp, 0, 0, null);
            buffer.dispose();
        }
    }
    @Override
    public boolean needApply(){
        return needApply=true;
    }
    public void setRThreshold(float t) {
        rThreshold = t/100;
//        System.out.println("RThreshold: "+ rThreshold);
    }

    public void setGThreshold(float t) {
        gThreshold = t/100;
//        System.out.println("GThreshold: "+ gThreshold);
    }

    public void setBThreshold(float t) {
        bThreshold = t/100;
//        System.out.println("BThreshold: "+ bThreshold);
    }

    public int getRThreshold() {
        return (int)rThreshold*100;
    }

    public int getGThreshold() {
        return (int) gThreshold*100;
    }

    public int getBThreshold() {
        return (int) bThreshold*100;
    }

    public BufferedImage getLastImage() {
        return lastImage;
    }

    @Override
    public JPanel getControl() {
//        return new NoBackgroundControl(this);
        return null;
    }

    @Override
    public void applyStudioConfig(Preferences prefs) {
        prefs.putFloat("rThreshold", rThreshold);
        prefs.putFloat("gThreshold", gThreshold);
        prefs.putFloat("bThreshold", bThreshold);
    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
        rThreshold = prefs.getFloat("rThreshold", rThreshold);
        gThreshold = prefs.getFloat("gThreshold", gThreshold);
        bThreshold = prefs.getFloat("bThreshold", bThreshold);
    }

    public void setBackgroundImage(BufferedImage img) {
        background = img;
    }
}
