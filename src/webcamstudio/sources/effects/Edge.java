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


/**
 *
 * @author pballeux
 */
public class Edge extends Effect {

    @Override
    public void applyEffect(BufferedImage img) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        int ddepth = 3;
        int scale = 1;
        int delta = 0;
//        Mat mat = new Mat();
        Mat grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_x = new Mat();
        Mat abs_grad_y = new Mat();
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
        Mat mat = new Mat(h, w, CvType.CV_8UC3);
        mat.put(0, 0, byteData);
        
        Imgproc.blur(mat, mat, new Size(4, 4));
        Imgproc.Sobel( mat, grad_x, ddepth, 1, 0, 3, scale, delta, 4 );
        Core.convertScaleAbs( grad_x, abs_grad_x );
        Imgproc.Sobel( mat, grad_y, ddepth, 0, 1, 3, scale, delta, 4 );
        Core.convertScaleAbs( grad_y, abs_grad_y );
        Core.addWeighted( abs_grad_x, 0.5, abs_grad_y, 0.5, 0, mat );
        
        byte[] data = new byte[mat.rows()*mat.cols()*(int)(mat.elemSize())];
        mat.get(0, 0, data);
        if (mat.channels() == 3) {
            for (int i = 0; i < data.length; i += 3) {
            byte temp = data[i];
            data[i] = data[i + 2];
            data[i + 2] = temp;
            }
        }
        BufferedImage temp = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_3BYTE_BGR);
        temp.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);

        Graphics2D buffer = img.createGraphics();
        buffer.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                           java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
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
        buffer.drawImage(temp, 0, 0, null);
        buffer.dispose();
    }

    @Override
    public boolean needApply(){
        return needApply=true;
    }
    @Override
    public JPanel getControl() {
        return null;
    }

    @Override
    public void applyStudioConfig(Preferences prefs) {

    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
        
    }
}
