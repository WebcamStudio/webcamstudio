/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;
import webcamstudio.sources.effects.Effect;

/**
 *
 * @author patrick
 */
public class SourceText extends Stream {

    BufferedImage image = null;
//    String content = "";
    boolean playing = false;
    boolean stop = false;
//    String fontName = Font.MONOSPACED;
//    int color = 0xFFFFFF; 
    int bgColor = 0x000000;
    Frame frame = null;
    float bgOpacity = 1f;
    private Shape shape = Shape.NONE;
    private String strShape = "";

    @Override
    public void readNext() {
        frame.setImage(image);
        if (frame != null) {
            for (int fx = 0; fx < this.getEffects().size(); fx++) {
                Effect fxT = this.getEffects().get(fx);
                if (fxT.needApply()){
                    BufferedImage txImage = frame.getImage(); 
                    fxT.applyEffect(txImage);
                }
            }
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
            nextFrame=frame;
        }
//        if (frame != null) {
//            for (Effect fxT : this.getEffects()) {
//                if (fxT.needApply()){   
//                    fxT.applyEffect(frame.getImage());
//                }
//            }
//            frame.setOutputFormat(x, y, width, height, opacity, volume);
//            frame.setZOrder(zorder);
//            nextFrame=frame;
//        }
//        BufferedImage txImage = frame.getImage(); 
//        applyEffects(txImage);
//        frame.setOutputFormat(x, y, width, height, opacity, volume);
//        frame.setZOrder(zorder);
//        nextFrame=frame;
    }

    public enum Shape {

        NONE,
        RECTANGLE,
        ROUNDRECT,
        OVAL
    }
    

    public SourceText(String content) {
        super();
        this.content = content;
        name = "Text";
        updateContent(content);
        color = 0xFFFFFF;
        fontName = Font.MONOSPACED;      
    }

    @Override
    public void setX(int x){
        this.x=x;
//        updateContent(content);
        }
    @Override
    public void setY(int y){
        this.y=y;
//        updateContent(content);
        }
    public void setBackgroundOpacity(float o){
        bgOpacity=o;
        updateContent(content);
    }
    public float getBackgroundOpacity(){
        return bgOpacity;
    }
    public void setBackground(Shape s) {
        shape = s;
        updateContent(content);
    }

    public Shape getBackground() {
        return shape;
    }
    public void setStrBackground(String strS) {
        strShape = strS;
    }
    @Override
    public void setIsPlaying(boolean setIsPlaying) {
        playing = setIsPlaying;
    }

    public String getStrBackground() {
        return strShape;
    }
    
    @Override
    public void setWidth(int w) {
        width = w;
        updateContent(content);
    }
    
    @Override
    public void setHeight(int h) {
        height = h;
        updateContent(content);
    }
    
    @Override
    public void setColor(int c) {
        color = c;
        updateContent(content);
    }

    @Override
    public int getColor() {
        return color;
    }

    public void setBackGroundColor(int bgColor) {
        this.bgColor = bgColor;
        updateContent(content);
    }

    public int getBackgroundColor() {
        return bgColor;
    }

    @Override
    public void setFont(String f) {
        fontName = f;
        updateContent(content);
    }

    @Override
    public String getFont() {
        return fontName;
    }

    @Override
    public void setZOrder(int layer) {
        zorder = layer;
//        updateContent(content);
    }

    @Override
    public void updateContent(String content) {
        this.content = content;
        captureWidth = width;
        captureHeight = height;
        int textHeight = captureHeight;
        int textWidth; // = captureWidth;
        image = new BufferedImage(captureWidth, captureHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D buffer = image.createGraphics();
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
        Font font = new java.awt.Font(fontName, java.awt.Font.PLAIN, textHeight);
        buffer.setFont(font);
        FontMetrics fm = buffer.getFontMetrics();
        textHeight = fm.getHeight();
        textWidth = fm.stringWidth(content);
        int fontSize = font.getSize();
        while ((textHeight > captureHeight || textWidth > captureWidth) && fontSize>1){
            font = new java.awt.Font(fontName, java.awt.Font.PLAIN, fontSize--);
            buffer.setFont(font);
            fm = buffer.getFontMetrics();
            textHeight = fm.getHeight();
            textWidth = fm.stringWidth(content);
            
        }        
        buffer.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        buffer.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        buffer.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        buffer.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        buffer.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        frame = new Frame(captureWidth, captureHeight, rate);
        buffer.setBackground(new Color(0,0,0,0));
        buffer.clearRect(0, 0, captureWidth, captureHeight);
        switch (shape) {
            case RECTANGLE:
                buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, bgOpacity));
                buffer.setColor(new Color(bgColor));
                buffer.fill3DRect(0, 0, captureWidth, captureHeight,true);
                break;
            case OVAL:
                buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, bgOpacity));
                buffer.setColor(new Color(bgColor));
                buffer.fillOval(0, 0, captureWidth, captureHeight);
                break;
            case ROUNDRECT:
                buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, bgOpacity));
                buffer.setColor(new Color(bgColor));
                buffer.fillRoundRect(0, 0, captureWidth, captureHeight,captureWidth/5,captureHeight/5);
                
                break;
        }

        buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 1F));
        buffer.setColor(new Color(color));
        buffer.drawString(content, (captureWidth-textWidth)/2, (captureHeight/2)+(textHeight/2)-fm.getDescent());
        buffer.dispose();
        frame.setImage(image);
        if (frame != null) {
            frame.setImage(image);
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
        }
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public void read() {
        stop = false;
        playing = true;
        try {
            updateContent(content);
            frame.setID(uuid);
            frame.setImage(image);
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
            MasterFrameBuilder.register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        stop = true;
        playing = false;
        frame = null;
        MasterFrameBuilder.unregister(this);
    }
    @Override
    public boolean needSeek() {
            return needSeekCTRL=false;
    }
    @Override
    public Frame getFrame() {
        
        return nextFrame;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public BufferedImage getPreview() {
        return image;
    }

    @Override
    public boolean hasAudio() {
        return false;
    }

    @Override
    public boolean hasVideo() {
        return true;
    }
}
