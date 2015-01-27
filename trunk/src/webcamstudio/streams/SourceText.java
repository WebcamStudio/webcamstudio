/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.mixers.PreviewFrameBuilder;
import webcamstudio.sources.effects.Effect;

/**
 *
 * @author patrick (modified by karl)
 */
public class SourceText extends Stream {

    BufferedImage image = null;
    BufferedImage txImage = null;
    boolean isPlaying = false;
    boolean stop = false;
    int bgColor = 0x000000;
    Frame frame = null;
    float bgOpacity = 1f;
    private Shape shape = Shape.NONE;
    private String strShape = "";
    private final MasterMixer mixer = MasterMixer.getInstance();
    private int textCW = mixer.getWidth();
    private int textCH = mixer.getHeight();

    public SourceText(String content) {
        super();
        this.content = content;
        name = "Text";
        updateContent(content);
        color = 0xFFFFFF;
        fontName = Font.MONOSPACED;
    }

    @Override
    public void readNext() {
        if (frame != null && isPlaying) {
            frame.setImage(image);
            if (frame != null) {
                txImage = frame.getImage();
                applyEffects(txImage);
                frame.setOutputFormat(x, y, width, height, opacity, volume);
                frame.setZOrder(zorder);
                nextFrame=frame;
            }
        }
    }

    @Override
    public void play() {
        // nothing here.
    }

    public void setTextCW (int tCW) {
        textCW = tCW;
    }
    
    public int getTextCW(){
        return textCW;
    }
    
    public void setTextCH(int tCH) {
        textCH = tCH;
    }
    
    public int getTextCH(){
        return textCH;
    }
    
    @Override
    public void setX(int x){
        this.x=x;
//        System.out.println("X set ... "+x);
    }
    
    @Override
    public void setY(int y){
        this.y=y;
//        System.out.println("Y set ... "+y);
    }
    
    public void setBackgroundOpacity(float o){
        bgOpacity=o;
        if (this.isATimer || this.isACDown) {
            updateLineContent(content);
        } else {
            updateContent(content);
        }
    }
    
    public float getBackgroundOpacity(){
        return bgOpacity;
    }
    public void setBackground(Shape s) {
        shape = s;
        if (this.isATimer || this.isACDown) {
            updateLineContent(content);
        } else {
            updateContent(content);
        }
    }
    
    public Shape getBackground() {
        return shape;
    }
    
    public void setStrBackground(String strS) {
        strShape = strS;
    }
    
    @Override
    public void setIsPlaying(boolean setIsPlaying) {
        isPlaying = setIsPlaying;
    }

    public String getStrBackground() {
        return strShape;
    }
    
    @Override
    public void setWidth(int w) {
        width = w;
        if (this.isATimer || this.isACDown) {
            updateLineContent(content);
        } else {
            updateContent(content);
//            System.out.println("W set ... "+w);
        }
    }
    
    @Override
    public void setHeight(int h) {
        height = h;
        if (this.isATimer || this.isACDown) {
            updateLineContent(content);
        } else {
            updateContent(content);
//            System.out.println("H set ... "+h);
        }
    }
    
    @Override
    public void setColor(int c) {
        color = c;
        if (this.isATimer || this.isACDown) {
            updateLineContent(content);
        } else {
            updateContent(content);
        }
    }

    @Override
    public int getColor() {
        return color;
    }

    public void setBackGroundColor(int bgColor) {
        this.bgColor = bgColor;
        if (this.isATimer || this.isACDown) {
            updateLineContent(content);
        } else {
            updateContent(content);
        }
    }

    public int getBackgroundColor() {
        return bgColor;
    }

    @Override
    public void setFont(String f) {
        fontName = f;
        if (this.isATimer || this.isACDown) {
            updateLineContent(content);
        } else {
            updateContent(content);
        }
    }

    @Override
    public String getFont() {
        return fontName;
    }

    @Override
    public void setZOrder(int layer) {
        zorder = layer;
    }
    // for Clock and Timer
    @Override
    public void updateLineContent(String content) {
        Color bkgColor = new Color(bgColor);
        this.content = content;
        if (this.getIsQRCode()) {
            captureWidth = width;
            captureHeight = height;
            if (content != null && content.length() > 0) {
                frame = new Frame(captureWidth, captureHeight, rate);
                MultiFormatWriter w = new MultiFormatWriter();
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                BitMatrix b;
                try {
                    b = w.encode(content, BarcodeFormat.QR_CODE, width, height);
                    image = com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(b);
                } catch (WriterException ex) {
                    Logger.getLogger(SourceText.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (frame != null) {
                    frame.setImage(image);
                    frame.setOutputFormat(x, y, width, height, opacity, volume);
                    frame.setZOrder(zorder);
                }
            }
        } else {
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
            buffer.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                               RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            buffer.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                               RenderingHints.VALUE_COLOR_RENDER_SPEED);
            buffer.setRenderingHint(RenderingHints.KEY_DITHERING,
                               RenderingHints.VALUE_DITHER_DISABLE);
            buffer.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Font font = new Font(fontName, Font.PLAIN, textHeight);
            buffer.setFont(font);
            FontMetrics fm = buffer.getFontMetrics();
            textHeight = fm.getHeight();
            textWidth = fm.stringWidth(content);
            int fontSize = font.getSize();
            while ((textHeight > captureHeight || textWidth > captureWidth) && fontSize>1){
                font = new Font(fontName, Font.PLAIN, fontSize--);
                buffer.setFont(font);
                fm = buffer.getFontMetrics();
                textHeight = fm.getHeight();
                textWidth = fm.stringWidth(content);
            }        
            frame = new Frame(captureWidth, captureHeight, rate);
            buffer.setBackground(new Color(0,0,0,0));
            buffer.clearRect(0, 0, captureWidth, captureHeight);
            switch (shape) {
                case RECTANGLE:
                    buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, bgOpacity));
                    buffer.setColor(bkgColor);
                    buffer.fill3DRect(0, 0, captureWidth, captureHeight,true);
                    break;
                case OVAL:
                    buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, bgOpacity));
                    buffer.setColor(bkgColor);
                    buffer.fillOval(0, 0, captureWidth, captureHeight);
                    break;
                case ROUNDRECT:
                    buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, bgOpacity));
                    buffer.setColor(bkgColor);
                    buffer.fillRoundRect(0, 0, captureWidth, captureHeight,captureWidth/5,captureHeight/5);
                    break;
            }

            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 1F));
            buffer.setColor(new Color(color));
            buffer.drawString(content, (captureWidth-textWidth)/2, captureHeight/2 + textHeight/2 - fm.getDescent());
            buffer.dispose();
            if (frame != null) {
                frame.setImage(image);
                frame.setOutputFormat(x, y, width, height, opacity, volume);
                frame.setZOrder(zorder);
            }
        }
    }
    //  for Text-Area
    @Override
    public void updateContent(String content) throws NoSuchElementException {
        ArrayList<String> linee = new ArrayList<>();
        Color bkgColor = new Color(bgColor);
        this.content = content;
        
        String[] datas = content.split("\n");
        linee.clear();
        linee.addAll(Arrays.asList(datas));
        
        if (this.getIsQRCode()) {
            captureWidth = width;
            captureHeight = height;
            if (content != null && content.length() > 0) {
                frame = new Frame(captureWidth, captureHeight, rate);
                MultiFormatWriter w = new MultiFormatWriter();
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                BitMatrix b;
                try {
                    b = w.encode(content, BarcodeFormat.QR_CODE, width, height);
                    image = com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(b);
                } catch (WriterException ex) {
                    Logger.getLogger(SourceText.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (frame != null) {
                    frame.setImage(image);
                    frame.setOutputFormat(x, y, width, height, opacity, volume);
                    frame.setZOrder(zorder);
                }
            }
        } else {
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
            buffer.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                               RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            buffer.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                               RenderingHints.VALUE_COLOR_RENDER_SPEED);
            buffer.setRenderingHint(RenderingHints.KEY_DITHERING,
                               RenderingHints.VALUE_DITHER_DISABLE);
            buffer.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Font font = new Font(fontName, Font.PLAIN, textHeight);
            buffer.setFont(font);
            FontMetrics fm = buffer.getFontMetrics();
            textHeight = getHeight();
            int currentLineSize = 0;
            
            ArrayList<Integer>lineSizes = new ArrayList<>();
            
            for (String line : linee) {
                lineSizes.add(fm.stringWidth(line));
                currentLineSize = fm.stringWidth(line);
            }
            if (lineSizes.size() == 1) {
                textWidth = currentLineSize;
            } else {
                textWidth = Collections.max(lineSizes);
            }
            
            int fontSize = font.getSize();
            while ((textHeight*linee.size() > captureHeight || textWidth > captureWidth) && fontSize>1){
                font = new Font(fontName, Font.PLAIN, fontSize--);
                buffer.setFont(font);
                fm = buffer.getFontMetrics();
                textHeight = fm.getHeight();
                lineSizes = new ArrayList<>();
                for (String line : linee) {
                    lineSizes.add(fm.stringWidth(line));
                }
                textWidth = Collections.max(lineSizes);
            }
            frame = new Frame(captureWidth, captureHeight, rate);
            buffer.setBackground(new Color(0,0,0,0));
            buffer.clearRect(0, 0, captureWidth, captureHeight);
            switch (shape) {
                case RECTANGLE:
                    buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, bgOpacity));
                    buffer.setColor(bkgColor);
                    buffer.fill3DRect(0, 0, captureWidth, captureHeight,true);
                    break;
                case OVAL:
                    buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, bgOpacity));
                    buffer.setColor(bkgColor);
                    buffer.fillOval(0, 0, captureWidth, captureHeight);
                    break;
                case ROUNDRECT:
                    buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, bgOpacity));
                    buffer.setColor(bkgColor);
                    buffer.fillRoundRect(0, 0, captureWidth, captureHeight,captureWidth/5,captureHeight/5);
                    break;
            }

            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 1F));
            buffer.setColor(new Color(color));
            int tHeight = textHeight;
//            System.out.println("FontHeight: "+textHeight);
            int k = (int)(textHeight*(15.0f/100.0f));
            for (String line : linee) {
                buffer.drawString(line, (captureWidth-textWidth)/2, tHeight - k); //
                tHeight += textHeight;
            }
            tHeight = 0;
            buffer.dispose();
            if (frame != null) {
                frame.setImage(image);
                frame.setOutputFormat(x, y, width, height, opacity, volume);
                frame.setZOrder(zorder);
            }
        }
    }

    @Override
    public String getContent() {
        return content;
    }
    
    @Override
    public void read() {
        boolean isTimer = this.getIsATimer();
        boolean isACDown = this.getIsACDown();
        stop = false;
        isPlaying = true;
        if (getPreView()){
                PreviewFrameBuilder.register(this);
            } else {
                MasterFrameBuilder.register(this);
            }
        this.setBackground(shape);
        try {
            if (isTimer || isACDown) {
                updateLineContent(content);
            } else {
                updateContent(content);
            }
            frame.setID(uuid);
            frame.setImage(image);
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
            
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        // nothing here.
    }
    
    @Override
    public void stop() {
        for (int fx = 0; fx < this.getEffects().size(); fx++) {
            Effect fxT = this.getEffects().get(fx);
            if (fxT.getName().endsWith("Shapes")){
                fxT.setDoOne(true);
            } else if (fxT.getName().endsWith("Stretch") || fxT.getName().endsWith("Crop")) {
                // do nothing.
            } else {
                fxT.resetFX();
            }
        }
        stop = true;
        isPlaying = false;
        frame = null;
        if (getPreView()){
            PreviewFrameBuilder.unregister(this);
        } else {
            MasterFrameBuilder.unregister(this);
        }
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
        return isPlaying;
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

    public enum Shape {
        NONE, RECTANGLE, ROUNDRECT, OVAL
    }
}
