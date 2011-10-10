/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author pballeux
 */
public abstract class Effect {

    public abstract void applyEffect(BufferedImage img);
    public abstract void applyStudioConfig(java.util.prefs.Preferences prefs);
    public abstract void loadFromStudioConfig(java.util.prefs.Preferences prefs);
    public abstract javax.swing.JPanel getControl();
    public static java.util.TreeMap<String, Effect> getEffects() {
        java.util.TreeMap<String, Effect> retValue = new java.util.TreeMap<String, Effect>();
        retValue.put(FlipHorizontal.class.getSimpleName(), new FlipHorizontal());
        retValue.put(FlipVertical.class.getSimpleName(), new FlipVertical());
        retValue.put(Mirror1.class.getSimpleName(), new Mirror1());
        retValue.put(Mirror2.class.getSimpleName(), new Mirror2());
        retValue.put(Mirror3.class.getSimpleName(), new Mirror3());
        retValue.put(Mirror4.class.getSimpleName(), new Mirror4());
        retValue.put(Mosaic.class.getSimpleName(), new Mosaic());
        retValue.put(Cartoon.class.getSimpleName(), new Cartoon());
        retValue.put(Gray.class.getSimpleName(), new Gray());
        retValue.put(Block.class.getSimpleName(), new Block());
        retValue.put(Sphere.class.getSimpleName(), new Sphere());
        retValue.put(Light.class.getSimpleName(), new Light());
        retValue.put(Rotation.class.getSimpleName(), new Rotation());
        retValue.put(ChromaKey.class.getSimpleName(), new ChromaKey());
        retValue.put(Contrast.class.getSimpleName(), new Contrast());
        retValue.put(Glow.class.getSimpleName(), new Glow());
        retValue.put(SwapRedBlue.class.getSimpleName(), new SwapRedBlue());
        retValue.put(Perspective.class.getSimpleName(), new Perspective());
        retValue.put(Twirl.class.getSimpleName(), new Twirl());
        retValue.put(Opacity.class.getSimpleName(), new Opacity());
        retValue.put(NoBackground.class.getSimpleName(), new NoBackground());
        retValue.put(RGB.class.getSimpleName(), new RGB());
        retValue.put(ZoomZoom.class.getSimpleName(), new ZoomZoom());
        retValue.put(MegaMind.class.getSimpleName(), new MegaMind());
        retValue.put(Glass.class.getSimpleName(), new Glass());
        retValue.put(Edge.class.getSimpleName(), new Edge());
        retValue.put(Radar.class.getSimpleName(), new Radar());
        //retValue.put(TEST.class.getSimpleName(), new TEST());

        return retValue;
    }
    public BufferedImage cloneImage(BufferedImage src){
        BufferedImage tempimage = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(src.getWidth(), src.getHeight(), java.awt.image.BufferedImage.TRANSLUCENT);
        Graphics2D tempbuffer = tempimage.createGraphics();
        tempbuffer.drawImage(src, 0, 0, null);
        tempbuffer.dispose();
        return tempimage;
    }
    public String getName(){
        return getClass().getSimpleName();
    }
    public String toString(){
        return getClass().getSimpleName();
    }
}
