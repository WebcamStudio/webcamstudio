/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.prefs.Preferences;
import javax.swing.JPanel;

/**
 *
 * @author pballeux
 */
public class ZoomZoom extends Effect {

    int x = 0;
    int y = 0;
    int w = 320;
    int h = 240;
    int xDir = 1;
    int yDir = 1;
    int zoom = 200;
    int zoomDir = 1;
    int counter = 0;
    Random random = new Random();

    @Override
    public void applyEffect(BufferedImage img) {
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
        BufferedImage temp = cloneImage(img);
        counter++;
        if (counter==100){
            counter = 0;
            xDir = (random.nextInt(3)-1);
            yDir = (random.nextInt(3)-1);
            zoomDir = (random.nextInt(3)-1);
        }
        x += xDir;
        y += yDir;
        if (x < 0 - img.getWidth()){
            x = 0-img.getWidth();
            xDir = 1;
        }
        if (y < 0-img.getHeight()){
            y = 0-img.getHeight();
            yDir = 1;
        }
        if (x > 0){
            x = 0;
            xDir = -1;
        }
        if (y > 0){
            y = 0;
            yDir = -1;
        }
        
        zoom += zoomDir;
        if (zoom > 400){
            zoom = 400;
            zoomDir = -1;
        }
        if (zoom < 200){
            zoom = 200;
            zoomDir = 1;
        }
        w = img.getWidth() * zoom / 100;
        h = img.getHeight() * zoom / 100;
        if ((w+x) < img.getWidth()){
            w = img.getWidth()-x;
        }
        if ((h+y) < img.getHeight()){
            h = img.getHeight()-y;
        }
        buffer.setBackground(new Color(0, 0, 0, 0));
        buffer.clearRect(0, 0, img.getWidth(), img.getHeight());
        buffer.drawImage(temp, x, y,x+ w, y+h, 0, 0, img.getWidth(), img.getHeight(), null);
        buffer.dispose();
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
    public void applyStudioConfig(Preferences prefs) {
    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
    }
}
