/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import webcamstudio.sources.effects.controls.CartoonControl;

/**
 *
 * @author pballeux
 */
public class Cartoon extends Effect {

    private final com.jhlabs.image.ContourFilter counterFilter = new com.jhlabs.image.ContourFilter();
    int contourSize = 1;
    int colorSplit = 1;
    int scale = 5;

    @Override
    public boolean needApply(){
        return needApply=true;
    }
    @Override
    public void applyEffect(BufferedImage img) {
        counterFilter.setScale((float) scale / 10F);
        counterFilter.setLevels((float) contourSize / 10F);

        int[] data = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int r, g, b, a, c;
        int delta1, delta2;
        for (int i = 0; i < data.length; i++) {
            c = data[i];
            r = (c & 0x00FF0000) >> 16;
            g = (c & 0x0000FF00) >> 8;
            b = (c & 0x000000FF);
            a = (c & 0xFF000000) >> 24;

            delta1 = r - g;
            delta2 = g - b;
            if (colorSplit == 0) {
                colorSplit = 1;
            }
            //Downscaling the red value
            r = r / colorSplit * colorSplit;
            g = r - delta1;
            if (g < 0) {
                g = 0;
            }
            b = g - delta2;
            if (b < 0) {
                b = 0;
            }
            data[i] = ((r << 16) | (g << 8) | (b) | (a << 24));
        }
        Graphics2D buffer = img.createGraphics();
        
        BufferedImage temp = counterFilter.filter(img, null);
        buffer.setBackground(new java.awt.Color(0, 0, 0, 0));
        buffer.clearRect(0, 0, img.getWidth(), img.getHeight());
        buffer.drawImage(temp, 0, 0, null);
        buffer.dispose();
    }

    public void setContourSize(int size) {
        contourSize = size;
    }

    public int getContourSize() {
        return contourSize;
    }

    public void setSplitColor(int value) {
        colorSplit = value;
    }

    public int getSplitColor() {
        return colorSplit;
    }

    public void setScale(int value) {
        scale = value;
    }

    public int getScale() {
        return scale;
    }

    @Override
    public JPanel getControl() {
        return new CartoonControl(this);
    }

    @Override
    public void applyStudioConfig(Preferences prefs) {
        prefs.putInt("scale", scale);
        prefs.putInt("colorsplit", colorSplit);
        prefs.putInt("contoursize", contourSize);
    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
        scale = prefs.getInt("scale", scale);
        colorSplit = prefs.getInt("colorsplit", colorSplit);
        contourSize = prefs.getInt("contoursize", contourSize);
    }
    }
