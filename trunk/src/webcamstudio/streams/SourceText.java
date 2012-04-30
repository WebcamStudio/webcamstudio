/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;

/**
 *
 * @author patrick
 */
public class SourceText extends Stream {

    BufferedImage image = null;
    String content = "";
    boolean playing = false;
    boolean stop = false;
    String fontName = Font.MONOSPACED;
    int color = 0xFFFFFF;
    Frame frame = null;

    public SourceText(String content) {
        super();
        this.content = content;
        name = "Text";
        updateContent(content);
    }
    @Override
    public void setWidth(int w){
        width=w;
        updateContent(content);
    }
    public void setHeight(int h){
        height=h;
        updateContent(content);
    }
    public void setColor(int c){
        color=c;
        updateContent(content);
    }
    public int getColor(){
        return color;
    }
    public void setFont(String f){
        fontName=f;
        updateContent(content);
    }
    public String getFont(){
        return fontName;
    }
    @Override
    public void setZOrder(int layer){
        zorder=layer;
        updateContent(content);
    }
    public void updateContent(String content) {
        this.content = content;
        captureWidth=width;
        captureHeight=height;
        frame = new Frame(captureWidth,captureHeight,rate);
        image = new BufferedImage(captureWidth, captureHeight+(captureHeight/5), BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D buffer = image.createGraphics();
        buffer.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        buffer.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        buffer.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        buffer.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        buffer.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        buffer.setFont(new java.awt.Font(fontName, java.awt.Font.PLAIN, captureHeight));

        buffer.setBackground(Color.BLACK);
        buffer.clearRect(0, 0, captureWidth, captureHeight+(captureHeight/5));
        buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 0F));
        buffer.setColor(new Color(color));
        buffer.fillRect(0, 0, captureWidth, captureHeight+(captureHeight/5));
        buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 1F));
        buffer.drawString(content, 0, captureHeight);
        buffer.dispose();
        frame.setImage(image);
        applyEffects(image);
        if (frame != null) {
            frame.setImage(image);
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
        }
    }

    public String getContent(){
        return content;
    }
    @Override
    public void read() {
        stop = false;
        playing=true;
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
        playing=false;
        frame = null;
        MasterFrameBuilder.unregister(this);
    }

    @Override
    public Frame getFrame() {
        return frame;
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
