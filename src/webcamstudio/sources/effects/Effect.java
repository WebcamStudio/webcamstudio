/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author pballeux (modified by karl)
 */
public abstract class Effect {

    public static java.util.TreeMap<String, Effect> getEffects() {
        java.util.TreeMap<String, Effect> retValue = new java.util.TreeMap<>();
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
        retValue.put(Emboss.class.getSimpleName(), new Emboss());
        retValue.put(Sharpen.class.getSimpleName(), new Sharpen());
        retValue.put(Rotation.class.getSimpleName(), new Rotation());
        retValue.put(ChromaKey.class.getSimpleName(), new ChromaKey());
        retValue.put(Contrast.class.getSimpleName(), new Contrast());
        retValue.put(SwapRedBlue.class.getSimpleName(), new SwapRedBlue());
        retValue.put(Perspective.class.getSimpleName(), new Perspective());
        retValue.put(Twirl.class.getSimpleName(), new Twirl());
        retValue.put(Opacity.class.getSimpleName(), new Opacity());
        retValue.put(NoBackground.class.getSimpleName(), new NoBackground());
        retValue.put(RGB.class.getSimpleName(), new RGB());
        retValue.put(ZoomZoom.class.getSimpleName(), new ZoomZoom());
        retValue.put(SaltNPepper.class.getSimpleName(), new SaltNPepper());
        retValue.put(Edge.class.getSimpleName(), new Edge());
        retValue.put(Radar.class.getSimpleName(), new Radar());
        retValue.put(Blink.class.getSimpleName(), new Blink());
        retValue.put(Gain.class.getSimpleName(), new Gain());
        retValue.put(HSB.class.getSimpleName(), new HSB());
        retValue.put(Weave.class.getSimpleName(), new Weave());
        retValue.put(Shapes.class.getSimpleName(), new Shapes());
        retValue.put(Marble.class.getSimpleName(), new Marble());
        retValue.put(Green.class.getSimpleName(), new Green());
//        retValue.put(MergeTest.class.getSimpleName(), new MergeTest());
        retValue.put(ComboGhost.class.getSimpleName(), new ComboGhost());
        retValue.put(WaterFx.class.getSimpleName(), new WaterFx());
        retValue.put(FaceDetectorAlpha.class.getSimpleName(), new FaceDetectorAlpha());
        retValue.put(MotionAlpha.class.getSimpleName(), new MotionAlpha());
        retValue.put(Ghosting.class.getSimpleName(), new Ghosting());
        return retValue;
    }
    protected boolean needApply=true;

    public boolean needApply(){
        return needApply;
    }
    public abstract void applyEffect(BufferedImage img);
    public abstract void applyStudioConfig(java.util.prefs.Preferences prefs);
    public abstract void loadFromStudioConfig(java.util.prefs.Preferences prefs);
    public abstract javax.swing.JPanel getControl();
    public BufferedImage cloneImage(BufferedImage src) {
        BufferedImage tempimage = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(src.getWidth(), src.getHeight(), BufferedImage.TRANSLUCENT);
        Graphics2D tempbuffer = tempimage.createGraphics();
        tempbuffer.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_SPEED);
        tempbuffer.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
        tempbuffer.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        tempbuffer.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS,
                java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        tempbuffer.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING,
                java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED);
        tempbuffer.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING,
                java.awt.RenderingHints.VALUE_DITHER_DISABLE);
        tempbuffer.drawImage(src, 0, 0, null);
        tempbuffer.dispose();
        return tempimage;
    }
    
    public String getName(){
        return getClass().getSimpleName();
    }
    
    @Override
    public String toString(){
        return getClass().getSimpleName();
    }
    
    public void setShape(String shapeImg){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setDoOne(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void clearEffect(Effect e) {
        e = null;
    }

    }
