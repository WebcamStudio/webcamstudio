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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import org.bytedeco.javacpp.Loader;
import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
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
    String classifierName;
    File file;
    opencv_objdetect.CvHaarClassifierCascade classifier;
//    private final CascadeClassifier faceDetector = new CascadeClassifier(System.getProperty("user.home")+"/.webcamstudio/faces/lbpcascade_frontalface.xml"); 

    public FaceDetectorAlpha() {
        this.file = new File (System.getProperty("user.home")+"/.webcamstudio/faces/haarcascade_frontalface_alt.xml");
        this.maskImg = (System.getProperty("user.home")+"/.webcamstudio/faces/Alien.png");
        
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
        int w = img.getWidth();
        int h = img.getHeight();
        IplImage src = IplImage.createFrom(img);
        BufferedImage temp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        try {
            temp = detect(src);
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
        int scaleWidth = 80;
        int scaleHeight = 60;
        IplImage grayImage    = IplImage.create(width, height, IPL_DEPTH_8U, 1);
        IplImage scaledGrayImg    = cvCreateImage ( cvSize(scaleWidth , scaleHeight), grayImage.depth(), grayImage.nChannels() );
        File sImg = new File(maskImg);
        BufferedImage sourceMask = ImageIO.read(sImg);
        CvMemStorage storage = CvMemStorage.create();
//        CvMat randomR = CvMat.create(3, 3), randomAxis = CvMat.create(3, 1);
//        // We can easily and efficiently access the elements of CvMat objects
//        // with the set of get() and put() methods.
//        randomAxis.put((Math.random()-0.5)/4, (Math.random()-0.5)/4, (Math.random()-0.5)/4);
//        cvRodrigues2(randomAxis, randomR, null);
//        double f = (width + height)/2.0;        randomR.put(0, 2, randomR.get(0, 2)*f);
//                                                randomR.put(1, 2, randomR.get(1, 2)*f);
//        randomR.put(2, 0, randomR.get(2, 0)/f); randomR.put(2, 1, randomR.get(2, 1)/f);
//        System.out.println(randomR);
//        CvPoint hatPoints = new CvPoint(3);
        cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);
        cvResize( grayImage, scaledGrayImg );
        CvSeq faces = cvHaarDetectObjects(scaledGrayImg, classifier, storage,
                1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
        int total = faces.total();
        for (int i = 0; i < total; i++) {
            CvRect r = new CvRect(cvGetSeqElem(faces, i));
            int w = r.width()*width/scaleWidth, h = r.height()*height/scaleHeight, x = r.x()*width/scaleWidth, y = r.y()*height/scaleHeight;
//            cvRectangle(grabbedImage, cvPoint(x, y), cvPoint(x+w, y+h), CvScalar.RED, 5, CV_AA, 0);
            gImageBI = grabbedImage.getBufferedImage();
            Double ww = w*1.4;
            Double hh = h*1.9;
            Double xx = x - w*0.2;
            Double yy = y - h*0.3;
            BufferedImage sSMask = Scalr.resize(sourceMask, Scalr.Mode.AUTOMATIC, ww.intValue(), hh.intValue() );
            Graphics2D buffer = gImageBI.createGraphics();
            
            buffer.drawImage(sSMask, xx.intValue(), yy.intValue(), null);
            buffer.dispose();
// To access or pass as argument the elements of a native array, call position() before.
//            hatPoints.position(0).x(x-w/10)   .y(y-h/10);
//            hatPoints.position(1).x(x+w*11/10).y(y-h/10);
//            hatPoints.position(2).x(x+w/2)    .y(y-h/2);
//            cvFillConvexPoly(grabbedImage, hatPoints.position(0), 3, CvScalar.GREEN, CV_AA, 0);
        }
//        cvThreshold(grayImage, grayImage, 64, 255, CV_THRESH_BINARY);
//        CvSeq contour = new CvSeq(null);
//        cvFindContours(grayImage, storage, contour, Loader.sizeof(CvContour.class),
//                CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
//        while (contour != null && !contour.isNull()) {
//            if (contour.elem_size() > 0) {
//                CvSeq points = cvApproxPoly(contour, Loader.sizeof(CvContour.class),
//                        storage, CV_POLY_APPROX_DP, cvContourPerimeter(contour)*0.02, 0);
//                cvDrawContours(grabbedImage, points, CvScalar.BLUE, CvScalar.BLUE, -1, 1, CV_AA);
//            }
//            contour = contour.h_next();
//        }
//            cvWarpPerspective(grabbedImage, rotatedImage, randomR);
        
        return gImageBI;
    }
    
//    public Mat detect(Mat sourceImg){
////        return null;
//        Mat scaleImg = new Mat();  
//        Mat greyImg = new Mat();
//        Mat sourceMask = opencv_highgui.imread(maskImg, -1); // CV_LOAD_IMAGE_UNCHANGED
//        Mat alphaImg = new Mat();
//        int scaleWidth = 160;
//        int scaleHeight = 120;
//        
//        opencv_core.Rect faces = new opencv_core.Rect();    
//        opencv_imgproc.resize(sourceImg, scaleImg, new Size(scaleWidth,scaleHeight), 0, 0, 1);
//        opencv_imgproc.cvtColor( sourceImg, sourceImg, opencv_imgproc.COLOR_BGR2BGRA);
//        opencv_imgproc.cvtColor( scaleImg, greyImg, opencv_imgproc.COLOR_BGR2GRAY);
//        opencv_imgproc.cvtColor( sourceMask, sourceMask, opencv_imgproc.COLOR_BGRA2RGBA);
//        equalizeHist(greyImg, greyImg);
//        faceDetector.detectMultiScale(greyImg, faces, 1.2, 3,0, new Size(0,0), new Size(85,85));  //, 1.1, 3,0, new Size(10,10), new Size(90,70));
////        System.out.println(String.format("Detected %s faces", faces.toArray().length));  
//        Rect[] facesArray = faces.toArray();
//        for (Rect faceRect : facesArray) {
//            double h_temp =faceRect.height;    // storing original height
//            double w_temp =faceRect.width;   
//            double yy = faceRect.y - h_temp*0.2; //y is reduced by 0.2*h
//            double xx = faceRect.x - w_temp*0.1; //x is reduced by 0.1*h
//            double hh = h_temp*1.4;             // height is increases
//            double ww = w_temp*1.2; //*1.1;             // width is increases
//            double maxX = (xx + ww);
//            double maxY = (yy + hh);
////            System.out.println("maxX, maxY Value = "+maxX+","+maxY);
////            System.out.println("Mat Max Size = "+matCols+"x"+matRows);
//            if (xx > 0 && yy > 0) {
//                if (maxY < scaleHeight && maxX < scaleWidth) {
//                    double pX = (xx*w)/scaleWidth;
//                    double pY = (yy*h)/scaleHeight;
//                    double pW = (ww*w)/scaleWidth;
//                    double pH = (hh*h)/scaleHeight;
//                    Point p1 = new Point(pX, pY);
//                    Point p2 = new Point(pX + pW, pY + pH);
//                    
//                    //            Point p1 = new Point((facesArray1.x*w)/scaleWidth, (facesArray1.y*h)/scaleHeight);
//                    //            Point p2 = new Point((facesArray1.x*w)/scaleWidth + (facesArray1.width*w)/scaleWidth, (facesArray1.y*h)/scaleHeight + (facesArray1.height*h)/scaleHeight);
//                    
//                    //            // draw the rectangle
//                    //            Core.rectangle(mRgba, p1, p2, sColor, 3);
//                    
//                    Rect roi = new Rect(p1,p2);
//                    
//                    //            System.out.println("X,Y Value = "+xx+","+yy);
//                    //            System.out.println("roi Size = "+roi.size());
//                    
//                    //            if (roi.size().area() > 0) {
//                    //            if (roi.size().area() > 0) {
//                    opencv_imgproc.resize(sourceMask, sourceMask, roi.size());
//                    opencv_core.extractChannel(sourceMask,alphaImg,3);
//                    //                Core.addWeighted( mask, 0.5, mRgba.submat(roi), 0.5, 0, mRgba.submat(roi));
//                    sourceMask.copyTo(sourceImg.submat(roi),alphaImg);
//                }
//            }
//        }  
//        opencv_imgproc.cvtColor( sourceImg, sourceImg, opencv_imgproc.COLOR_BGR2RGB);
//        return sourceImg;  
//    }  

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
