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
import webcamstudio.mixers.MasterMixer;
import webcamstudio.sources.effects.controls.RevealLeftNFadeControl;

/**
 *
 * @author pballeux (modified by karl)
 */
public class RevealLeftNFade extends Effect {

    private final com.jhlabs.image.CropFilter filter = new com.jhlabs.image.CropFilter();
    private int widthCount = MasterMixer.getInstance().getWidth();
    private final int minWidth = 1;
    private int vel = 5;
    private int opacity = 100;
    private boolean loop = false;
    Graphics2D buffer = null;
    BufferedImage temp = null;
    
    @Override
    public void applyEffect(BufferedImage img) {
        
        int w = img.getWidth();
        int h = img.getHeight();
        int mixerW = MasterMixer.getInstance().getWidth();
        int wDiff = mixerW-w;
        filter.setX(widthCount-wDiff);
        filter.setY(0);
        filter.setWidth(w);
        filter.setHeight(h);
        if (widthCount > minWidth+wDiff) {
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
            temp = filter.filter(img, null);
            buffer.setBackground(new Color(0, 0, 0, 0));
            buffer.clearRect(0, 0, w, h);
            buffer.drawImage(temp, 0, 0, w, h, 0, 0, temp.getWidth(), temp.getHeight(), null);
            buffer.dispose();
            widthCount = widthCount - vel;
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
                    widthCount = MasterMixer.getInstance().getWidth();
                    opacity = 100;
                } else {
                    opacity = 1;
                }
            }        
        }
    }

    @Override
    public JPanel getControl() {
        return new RevealLeftNFadeControl(this);
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
        widthCount = MasterMixer.getInstance().getWidth();
        opacity = 100;
    }
}
