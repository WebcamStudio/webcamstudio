/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webcamstudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import webcamstudio.sources.effects.controls.ErodeControl;

/**
 *
 * @author pballeux (modified by karl)
 */
public class Erode extends Effect{
//    private final com.jhlabs.image.HSBAdjustFilter filter = new com.jhlabs.image.HSBAdjustFilter();
    private int kSize = 1;
//    private final float  ratio = 100f;
//    private float hFactor = 0f/ratio;//gain
//    private float sFactor = 0f/ratio;//bias
//    private float bFactor = 0f/ratio;

    @Override
    public void applyEffect(BufferedImage img) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
//        int diam = 5;
//        int bType = Imgproc.BORDER_DEFAULT;
//        double sigmaColor = 100;
//        double sigmaSpace = 100;
        
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kSize,kSize));        
        Mat dst = new Mat();
//        Mat grad_x = new Mat();
//        Mat grad_y = new Mat();
//        Mat abs_grad_x = new Mat();
//        Mat abs_grad_y = new Mat();
        
        int w = img.getWidth();
        int h = img.getHeight();
        int counter = 0;
        
        int[] intData = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        byte[] byteData = new byte[intData.length * 3];
        for (int i = 0; i < byteData.length; i += 3) {
            byteData[i] = (byte) ((intData[counter] >> 16) & 0xFF);
            byteData[i + 1] = (byte) ((intData[counter] >> 8) & 0xFF);
            byteData[i + 2] = (byte) ((intData[counter]) & 0xFF);
            counter++;
        }
        
        Mat src = new Mat(h, w, CvType.CV_8UC3);
        src.put(0, 0, byteData);
        
        // Filter
        
        Imgproc.erode(src, dst, kernel);
        
//        Imgproc.bilateralFilter(src, dst, diam , sigmaColor, sigmaSpace, bType);
        
        
//        Imgproc.Sobel( mat, grad_x, ddepth, 1, 0, 3, scale, delta, 4 );
//        Core.convertScaleAbs( grad_x, abs_grad_x );
//        Imgproc.Sobel( mat, grad_y, ddepth, 0, 1, 3, scale, delta, 4 );
//        Core.convertScaleAbs( grad_y, abs_grad_y );
//        Core.addWeighted( abs_grad_x, 0.5, abs_grad_y, 0.5, 0, mat );
        
        byte[] data = new byte[dst.rows()*dst.cols()*(int)(dst.elemSize())];
        dst.get(0, 0, data);
        if (dst.channels() == 3) {
            for (int i = 0; i < data.length; i += 3) {
            byte temp = data[i];
            data[i] = data[i + 2];
            data[i + 2] = temp;
            }
        }
        BufferedImage temp = new BufferedImage(dst.cols(), dst.rows(), BufferedImage.TYPE_3BYTE_BGR);
        temp.getRaster().setDataElements(0, 0, dst.cols(), dst.rows(), data);
        
//        filter.setHFactor(hFactor);
//        filter.setSFactor(sFactor);
//        filter.setBFactor(bFactor);
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
//        BufferedImage temp = filter.filter(img, null);
//        buffer.setBackground(new java.awt.Color(0,0,0,0));
//        buffer.clearRect(0,0,img.getWidth(),img.getHeight());
        buffer.drawImage(temp, 0, 0,null);
        buffer.dispose();
    }
    @Override
    public boolean needApply(){
        return needApply=true;
    }
    @Override
    public JPanel getControl() {
        return new ErodeControl(this);
    }

    /**
     * @return the brightness
     */
    public int getErFactor() {
        return kSize;
    }

    /**
     * @param hFactor
     */
    public void setErFactor(int kSiz) {
        this.kSize = kSiz;
    }

    /**
     * @return the contrast
     */
//    public int getSFactor() {
//        return (int)(sFactor*ratio);
//    }
//
//    /**
//     * @param sFactor
//     */
//    public void setSFactor(int sFactor) {
//        this.sFactor = ((float)sFactor)/ratio;
//    }
//    
//    public int getBFactor() {
//        return (int)(bFactor*ratio);
//    }
//
//    /**
//     * @param bFactor
//     */
//    public void setBFactor(int bFactor) {
//        this.bFactor = ((float)bFactor)/ratio;
//    }

    @Override
    public void applyStudioConfig(Preferences prefs) {
//        prefs.putFloat("hFactor", hFactor);
//        prefs.putFloat("sFactor", sFactor);
//        prefs.putFloat("bFactor", bFactor);
        
    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
//        hFactor=prefs.getFloat("hFactor", hFactor);
//        sFactor=prefs.getFloat("sFactor", sFactor);
//        bFactor=prefs.getFloat("bFactor", bFactor);
    }
}
