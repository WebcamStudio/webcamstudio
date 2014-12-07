/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.imgscalr.Scalr;

/**
 *
 * @author karl
 */
public class MotionAlpha extends Effect {

    private ArrayList<BufferedImage> previusImagesIn = new ArrayList<>();
    private int imageCount = 0;
    BufferedImage previusImageIn;
    boolean motion = false;
    int keepMotion = 0;

    @Override
    public void applyEffect(BufferedImage img) {
        BufferedImage currImg = img;
        currImg = Scalr.resize(currImg, Scalr.Mode.AUTOMATIC, 320, 240);
        previusImagesIn.add(currImg);
        if (imageCount > 1){
            previusImageIn = previusImagesIn.get(1);
            previusImagesIn.remove(0);
        } else {
            previusImageIn = currImg;
        }
        
        Graphics2D buffer = img.createGraphics();
        buffer.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                           RenderingHints.VALUE_INTERPOLATION_BILINEAR);
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
        
//        System.out.println("NumPixelChanged: "+bufferedImagesEqual(currImg, previusImageIn));

        if (bufferedImagesEqual(currImg, previusImageIn) > 10) {
            motion = true;
            keepMotion = imageCount + 50; 
        } else if (keepMotion <= imageCount) {
            motion = false;
        }
        if (motion) {
            buffer.drawImage(img, 0, 0, null);
            buffer.dispose();
//            System.out.println("Motion");
            imageCount ++ ;
        } else {
            buffer.setBackground(new Color(0, 0, 0, 0));
            buffer.clearRect(0, 0, img.getWidth(), img.getHeight());
            buffer.dispose();
//            System.out.println("No Motion");
            imageCount ++ ;
        }
        
    }

    private int bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
        int numPixelChanged = 0;   
               
            for (int i=0; i<((BufferedImage)img1).getWidth(); i++) {   
                for (int j=0; j<((BufferedImage)img1).getHeight(); j++) {   
                    int curr = ((BufferedImage)img1).getRGB(i, j);   
                    //int currA = (curr >>> 24) & 0xFF;   
                    int currR = (curr >>> 16) & 0xFF;   
                    int currG = (curr >>> 8) & 0xFF;   
                    int currB = (curr >>> 0) & 0xFF;   
                       
                    int prev = ((BufferedImage)img2).getRGB(i, j);
                    //int prevA = (prev >>> 24) & 0xFF;   
                    int prevR = (prev >>> 16) & 0xFF;   
                    int prevG = (prev >>> 8) & 0xFF;   
                    int prevB = (prev >>> 0) & 0xFF;   
                       
                    int diff = Math.abs(currR-prevR)   
                    + Math.abs(currG-prevG)   
                    + Math.abs(currB-prevB);   
                    if (diff > 300)   
                        numPixelChanged ++;
                }   
            }   
            
            if (numPixelChanged > 10000) {
                return 0;
            } else {
                return numPixelChanged;
            }        
    }
    
    @Override
    public boolean needApply(){
        return needApply=true;
    }
    
    @Override
    public JPanel getControl() {
        JPanel dummy = new JPanel();
        return dummy;
    }

    @Override
    public void resetFX() {
        // nothing here.
    }
}
