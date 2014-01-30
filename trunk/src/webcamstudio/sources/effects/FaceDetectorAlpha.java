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
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.equalizeHist;
import org.opencv.objdetect.CascadeClassifier;
import webcamstudio.sources.effects.controls.FaceDetectorControl;


/**
 *
 * @author karl ellis
 */
public class FaceDetectorAlpha extends Effect {
    private final CascadeClassifier faceDetector = new CascadeClassifier(System.getProperty("user.home")+"/.webcamstudio/faces/lbpcascade_frontalface.xml"); //(getClass().getResource(/webcamstudio/resources/lbpcascade_frontalface.xml").getPath())
    private final Scalar sColor = new Scalar( 255, 0, 255 );
    private int w = 320;
    private int h = 240;
    String maskImg = (System.getProperty("user.home")+"/.webcamstudio/faces/Alien.png");

   @Override
    public void applyEffect(BufferedImage img) {
        
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat dst = new Mat();
        w = img.getWidth();
        h = img.getHeight();
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
        
        dst = detect(src);

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
    public Mat detect(Mat sourceImg){
        Mat scaleImg = new Mat();  
        Mat greyImg = new Mat();
        Mat sourceMask = Highgui.imread(maskImg, -1); // CV_LOAD_IMAGE_UNCHANGED
        Mat alphaImg = new Mat();
        int scaleWidth = 160;
        int scaleHeight = 120;
        
        MatOfRect faces = new MatOfRect();    
        Imgproc.resize(sourceImg, scaleImg, new Size(scaleWidth,scaleHeight), 0, 0, 1);
        Imgproc.cvtColor( sourceImg, sourceImg, Imgproc.COLOR_BGR2BGRA);
        Imgproc.cvtColor( scaleImg, greyImg, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor( sourceMask, sourceMask, Imgproc.COLOR_BGRA2RGBA);
        equalizeHist(greyImg, greyImg);
        faceDetector.detectMultiScale(greyImg, faces, 1.2, 3,0, new Size(0,0), new Size(85,85));  //, 1.1, 3,0, new Size(10,10), new Size(90,70));
//        System.out.println(String.format("Detected %s faces", faces.toArray().length));  
        Rect[] facesArray = faces.toArray();
        for (Rect faceRect : facesArray) {
            
            // Increase Rect Size
//            int xx = faceRect.x;
            
            double h_temp =faceRect.height;    // storing original height
            double w_temp =faceRect.width;   
            double yy = faceRect.y - h_temp*0.2; //y is reduced by 0.2*h
            double xx = faceRect.x - w_temp*0.1; //x is reduced by 0.1*h
            double hh = h_temp*1.4;             // height is increases
            double ww = w_temp*1.2; //*1.1;             // width is increases
            
//            int matRows = sourceImg.rows();
//            int matCols = sourceImg.cols();
            double maxX = (xx + ww);
            double maxY = (yy + hh);
//            System.out.println("maxX, maxY Value = "+maxX+","+maxY);
//            System.out.println("Mat Max Size = "+matCols+"x"+matRows);
            if (xx > 0 && yy > 0) {
                if (maxY < scaleHeight && maxX < scaleWidth) {
                    double pX = (xx*w)/scaleWidth;
                    double pY = (yy*h)/scaleHeight;
                    double pW = (ww*w)/scaleWidth;
                    double pH = (hh*h)/scaleHeight;
                    Point p1 = new Point(pX, pY);
                    Point p2 = new Point(pX + pW, pY + pH);
                    
                    //            Point p1 = new Point((facesArray1.x*w)/scaleWidth, (facesArray1.y*h)/scaleHeight);
                    //            Point p2 = new Point((facesArray1.x*w)/scaleWidth + (facesArray1.width*w)/scaleWidth, (facesArray1.y*h)/scaleHeight + (facesArray1.height*h)/scaleHeight);
                    
                    //            // draw the rectangle
                    //            Core.rectangle(mRgba, p1, p2, sColor, 3);
                    
                    Rect roi = new Rect(p1,p2);
                    
                    //            System.out.println("X,Y Value = "+xx+","+yy);
                    //            System.out.println("roi Size = "+roi.size());
                    
                    //            if (roi.size().area() > 0) {
                    //            if (roi.size().area() > 0) {
                    Imgproc.resize(sourceMask, sourceMask, roi.size());
                    Core.extractChannel(sourceMask,alphaImg,3);
                    //                Core.addWeighted( mask, 0.5, mRgba.submat(roi), 0.5, 0, mRgba.submat(roi));
                    sourceMask.copyTo(sourceImg.submat(roi),alphaImg);
                }
            }
        }  
        Imgproc.cvtColor( sourceImg, sourceImg, Imgproc.COLOR_BGR2RGB);
        return sourceImg;  
    }  

    @Override
    public JPanel getControl() {
        return new FaceDetectorControl(this);
    }
    @Override
    public boolean needApply(){
        return needApply=false;
    }
    public String getFace() {
        return maskImg;
    }
    public void setFace(String faceS){
        maskImg = (System.getProperty("user.home")+"/.webcamstudio/faces/"+faceS+".png");
    }
    @Override
    public void applyStudioConfig(Preferences prefs) {

    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
        
    }
}
