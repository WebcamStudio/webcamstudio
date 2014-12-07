/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources.effects;

//import com.googlecode.javacv.cpp.opencv_core.CvMat;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import org.bytedeco.javacpp.Loader;
import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacpp.opencv_objdetect;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.imgscalr.Scalr;
import webcamstudio.sources.effects.controls.FaceDetectorControl;


/**
 *
 * @author karl ellis
 */
public class FaceDetectorAlpha extends Effect {
    String maskImg;
    BufferedImage sourceMask;
    String classifierName;
    File file;
    int w = 320;
    int h = 240;
    double wFactor = 1.2;
    double hFactor = 2.9;
    double xFactor = 0.1;
    double yFactor = 0.2;
    opencv_objdetect.CvHaarClassifierCascade classifier;
    CascadeClassifier faceDetector = new CascadeClassifier(System.getProperty("user.home")+"/.webcamstudio/faces/lbpcascade_frontalface.xml"); 

    public FaceDetectorAlpha() {
        this.file = new File (System.getProperty("user.home")+"/.webcamstudio/faces/haarcascade_frontalface_alt2.xml");
        this.maskImg = (System.getProperty("user.home")+"/.webcamstudio/faces/Alien.png");
        File sImg = new File(maskImg);
        try {
            sourceMask = ImageIO.read(sImg);
        } catch (IOException ex) {
            Logger.getLogger(FaceDetectorAlpha.class.getName()).log(Level.SEVERE, null, ex);
        }
        classifierName = file.getAbsolutePath();
        Loader.load(opencv_objdetect.class);
        this.classifier = new opencv_objdetect.CvHaarClassifierCascade(cvLoad(classifierName));
        if (classifier.isNull()) {
            System.err.println("Error loading classifier file \"" + classifierName + "\".");
            System.exit(1);
        }
    }
     
   @Override
    public void applyEffect(BufferedImage img) {
        w = img.getWidth();
        h = img.getHeight();
        IplImage src = IplImage.createFrom(img);
        BufferedImage temp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        try {
            temp = detect2(src);
        } catch (Exception ex) {
            Logger.getLogger(FaceDetectorAlpha.class.getName()).log(Level.SEVERE, null, ex);
        }
        
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
    
    public BufferedImage detect (IplImage sourceImg) throws Exception {
        IplImage grabbedImage = sourceImg;
        int width  = grabbedImage.width();
        int height = grabbedImage.height();
        BufferedImage gImageBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int scaleWidth = 100;
        int scaleHeight = 75;
        IplImage grayImage    = IplImage.create(width, height, IPL_DEPTH_8U, 1);
        IplImage scaledGrayImg    = cvCreateImage ( cvSize(scaleWidth , scaleHeight), grayImage.depth(), grayImage.nChannels() );
        CvMemStorage storage = CvMemStorage.create();
        cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);
        cvResize( grayImage, scaledGrayImg );
        CvSeq faces = cvHaarDetectObjects(scaledGrayImg, classifier, storage,
                1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
        int total = faces.total();
        for (int i = 0; i < total; i++) {
            CvRect r = new CvRect(cvGetSeqElem(faces, i));
            int w = r.width()*width/scaleWidth, h = r.height()*height/scaleHeight, x = r.x()*width/scaleWidth, y = r.y()*height/scaleHeight;
            gImageBI = grabbedImage.getBufferedImage();
            Double ww = w*1.4;
            Double hh = h*1.9;
            Double xx = x - w*0.2;
            Double yy = y - h*0.3;
            BufferedImage sSMask = Scalr.resize(sourceMask, Scalr.Mode.AUTOMATIC, ww.intValue(), hh.intValue() );
            Graphics2D buffer = gImageBI.createGraphics();
            buffer.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
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
            buffer.drawImage(sSMask, xx.intValue(), yy.intValue(), null);
            buffer.dispose();
        }
        return gImageBI;
    }
    
    public BufferedImage detect2(IplImage sourceImg){
        int width  = sourceImg.width();
        int height = sourceImg.height();
        BufferedImage gImageBI;
        int scaleWidth = 160;
        int scaleHeight = 120;
        IplImage grayImage    = IplImage.create(width, height, IPL_DEPTH_8U, 1);
        cvCvtColor( sourceImg, grayImage, CV_BGR2GRAY );
        IplImage scaledGrayImg    = cvCreateImage ( cvSize(scaleWidth , scaleHeight), IPL_DEPTH_8U, 1 );
        cvCvtColor(sourceImg, grayImage, CV_BGR2GRAY);
        cvResize( grayImage, scaledGrayImg );
        Mat grayImageMat = new Mat(scaledGrayImg);
        equalizeHist(grayImageMat, grayImageMat);
        Rect faces = new Rect();
        faceDetector.detectMultiScale(grayImageMat, faces);  //, 1.1, 3,0, new Size(10,10), new Size(90,70));
        int w = faces.width()*width/scaleWidth, h = faces.height()*height/scaleHeight, x = faces.x()*width/scaleWidth, y = faces.y()*height/scaleHeight;
        gImageBI = sourceImg.getBufferedImage();
        Double ww = w*wFactor;
        Double hh = h*hFactor;
        Double xx = x - w*xFactor;
        Double yy = y - h*yFactor;
        BufferedImage sSMask = Scalr.resize(sourceMask, Scalr.Mode.AUTOMATIC, ww.intValue(), hh.intValue() );
        Graphics2D buffer = gImageBI.createGraphics();
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
        buffer.drawImage(sSMask, xx.intValue(), yy.intValue(), null);
        buffer.dispose();
        return gImageBI;
    }  

    @Override
    public JPanel getControl() {
        return new FaceDetectorControl(this);
    }
    @Override
    public boolean needApply(){
        return needApply=true;
    }
    public String getFace() {
        return maskImg;
    }
    public void setFace(String faceS) throws IOException{
        maskImg = (System.getProperty("user.home")+"/.webcamstudio/faces/"+faceS+".png");
        File sImg = new File(maskImg);
        sourceMask = ImageIO.read(sImg);
        sImg = null;
    }
    
    public void setHFactor (double hFact){
        hFactor = hFact;
    }
    
    public double getHFactor (){
        return hFactor;
    }
    
    public void setWFactor (double wFact){
        wFactor = wFact;
    }
    
    public double getWFactor (){
        return wFactor;
    }
    
    public void setXFactor (double xFact){
        xFactor = xFact;
    }
    
    public double getXFactor (){
        return xFactor;
    }
    
    public void setYFactor (double yFact){
        yFactor = yFact;   
    }
    
    public double getYFactor (){
        return yFactor;
    }

    @Override
    public void resetFX() {
        // nothing here.
    }
}
