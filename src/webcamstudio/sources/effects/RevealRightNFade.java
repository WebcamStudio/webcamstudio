/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.imgscalr.Scalr;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.sources.effects.controls.RevealRightNFadeControl;

/**
 *
 * @author pballeux (modified by karl)
 */
public class RevealRightNFade extends Effect {

    private final com.jhlabs.image.CropFilter filter = new com.jhlabs.image.CropFilter();
    private int widthCount = 1;
    private final int maxWidth = MasterMixer.getInstance().getWidth();
    private int vel = 5;
    private int opacity = 100;
    private boolean loop = false;
    Graphics2D buffer = null;
    BufferedImage temp = null;
    
    @Override
    public void applyEffect(BufferedImage img) {
        
        int w = img.getWidth();
        int h = img.getHeight();
        filter.setX(0);
        filter.setY(0);
        filter.setWidth(widthCount);
        filter.setHeight(h);
        if (widthCount < w) {
            buffer = img.createGraphics();
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
            BufferedImage temp = Scalr.crop(img,0,0,widthCount,h);
            buffer.setBackground(new Color(0, 0, 0, 0));
            buffer.clearRect(0, 0, w, h);
            buffer.drawImage(temp, 0, 0, w, h, 0, 0, temp.getWidth(), temp.getHeight(), null);
            buffer.dispose();
            widthCount += vel;
        } else {
            buffer = img.createGraphics();
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
            temp = cloneImage(img);
            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, opacity / 100F));
            buffer.setBackground(new Color(0, 0, 0, 0));
            buffer.clearRect(0, 0, w, h);
            buffer.drawImage(temp, 0, 0, w, h, 0, 0, temp.getWidth(), temp.getHeight(), null);
            buffer.dispose();
            opacity --;
            if (opacity <= 0) {
                if (loop) {
                    widthCount = 1;
                    opacity = 100;
                } else {
                    opacity = 1;
                }
            }        
        }
    }

    @Override
    public JPanel getControl() {
        return new RevealRightNFadeControl(this);
    }

    @Override
    public boolean needApply(){
        return needApply= true;
    }

    public int getVel() {
        return vel;
    }
    
    public void setVel(int v) {
        this.vel = v;
    }
    
    public void setLoop(boolean lo) {
        this.loop = lo;
    }
    
    public boolean getLoop() {
        return this.loop;
    }
    
    @Override
    public void resetFX() {
        widthCount = 1;
        opacity = 100;
    }
}
