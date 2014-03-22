/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterFrameBuilder;

/**
 *
 * @author patrick
 */
public class SourceQRCode extends Stream {

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
//        applyEffects(frame.getImage());
        frame.setOutputFormat(x, y, width, height, opacity, volume);
        frame.setZOrder(zorder);
        nextFrame=frame;
    }

    @Override
    public void play() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public enum Shape {

        NONE,
        RECTANGLE,
        ROUNDRECT,
        OVAL
    }
    

    public SourceQRCode(String content) {
        super();
        this.content = content;
        name = "QRCode";
        updateContent(content);
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
//        updateContent(content);
    }
    public float getBackgroundOpacity(){
        return bgOpacity;
    }
    public void setBackground(Shape s) {
        shape = s;
//        updateContent(content);
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
//            updateContent(content);
        }
    
    @Override
    public void setHeight(int h) {
        height = h;
//            updateContent(content);
        }
    
    @Override
    public void setColor(int c) {
        color = c;
//        updateContent(content);
    }

    @Override
    public int getColor() {
        return color;
    }

    public void setBackGroundColor(int bgColor) {
        this.bgColor = bgColor;
//        updateContent(content);
    }

    public int getBackgroundColor() {
        return bgColor;
    }

    @Override
    public void setFont(String f) {
        fontName = f;
//        updateContent(content);
    }

    @Override
    public String getFont() {
        return fontName;
    }

    @Override
    public void setZOrder(int layer) {
        zorder = layer;
//            updateContent(content);
        }
    
    @Override
    public void updateContent(String content) {
        this.content = content;
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
                Logger.getLogger(SourceQRCode.class.getName()).log(Level.SEVERE, null, ex);
            }

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
    public void pause() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
