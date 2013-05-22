/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.WSImage;
import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick
 */
public class Capturer {

//  private boolean hasaudio = false;
//  private boolean hasvideo = false;
    private int vport = 0;
    private int aport = 0;
    private int fVport =0;
    private boolean stopMe = false;
    private Stream stream;
    private ServerSocket videoServer = null;
    private ServerSocket audioServer = null;
//  private ServerSocket fakeVideoServer = null;
//  private FrameBuffer frameBuffer = new FrameBuffer();
    private WSImage image = null;
    private byte[] audio = null;
    private Frame frame = null;
    private DataInputStream videoIn = null;
    private DataInputStream audioIn = null;
    private DataInputStream fakeVideoIn = null;
    private DataInputStream fakeAudioIn = null;
    private boolean noVideoPres = true;
    private boolean noAudioPres = true;
//  private boolean firstPass = true;

    public Capturer(Stream s) {
        stream = s;
        frame = new Frame(stream.getCaptureWidth(), stream.getCaptureHeight(), stream.getRate());
        image = new WSImage(stream.getCaptureWidth(), stream.getCaptureHeight(), BufferedImage.TYPE_INT_RGB);
        audio = new byte[(44100 * 2 * 2) / stream.getRate()];
        frame.setID(stream.getID());
        System.out.println("Capturer HasVideo: "+stream.hasVideo());
        System.out.println("Capturer HasAudio: "+stream.hasAudio());
        if (stream.hasAudio()) {
            try {
                audioServer = new ServerSocket(0);
                aport = audioServer.getLocalPort();
            } catch (IOException ex) {
                Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (stream.hasVideo()) {
            try {
                videoServer = new ServerSocket(0);
                vport = videoServer.getLocalPort();
            } catch (IOException ex) {
                Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
  
        System.out.println("Port used is Video:" + vport+"/Audio:" + aport);
  
        Thread vCapture = new Thread(new Runnable() {

            @Override
            public void run() {
                
                try {
                    Socket connection = videoServer.accept();
//                  hasvideo = true;   
                    System.out.println(stream.getName() + " video accepted...");
                    if (stream.hasFakeVideo()) {
                        fakeVideoIn = new DataInputStream(new BufferedInputStream(connection.getInputStream(), 4096));
                        System.out.println("Start Fake Video Connection.");
                    }
                    do {
//                        Tools.sleep(5);
                        if (fakeAudioIn != null) {
                           Tools.sleep(20);
                            if (fakeAudioIn.available() != 0) {
                                noVideoPres=false;
                                Tools.sleep(stream.getVDelay());
                                videoIn = fakeVideoIn;//new DataInputStream(connection.getInputStream());
                                System.out.println("Start Movie/DVB Video.");
                            } //!stream.hasAudio()
                        } else if (stream.getName().contains("Desktop")) { //hasaudio ||
                            noVideoPres=false;
                            Tools.sleep(stream.getVDelay());
                            videoIn = new DataInputStream(connection.getInputStream());
                            System.out.println("Start Desktop Video.");
                        } else if (stream.getClass().getName().contains("SourceWebcam")) { //hasaudio ||
                            noVideoPres=false;
                            Tools.sleep(stream.getVDelay());
                            videoIn = new DataInputStream(connection.getInputStream());
                            System.out.println("Start Device Video.");
                        } else if (!stream.hasAudio()) {
                            noVideoPres=false;
                            Tools.sleep(stream.getVDelay());
                            videoIn = fakeVideoIn;
                            System.out.println("Start Muted Video.");
                        }/* else if (stream.getClass().getName().contains("SourceDVB")) { //hasaudio ||
                            noVideoPres=false;
                            Tools.sleep(stream.getVDelay());
                            videoIn = fakeVideoIn; //new DataInputStream(connection.getInputStream());
                            System.out.println("Start DVB Video.");
                        } */
                    } while (noVideoPres); // && !hasaudio
                    System.out.println("Out of Video Loop.");
                } catch (IOException ex) {
                    Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } 
        });
        
        vCapture.setPriority(Thread.MIN_PRIORITY);
        if (stream.hasVideo()) {  // || hasaudio       
            vCapture.start();
        }
        
        Thread aCapture = new Thread(new Runnable() {

            @Override
            public void run() {
                try {                    
                    Socket connection = audioServer.accept();
//                  hasaudio = true;
                    System.out.println(stream.getName() + " audio accepted...");
                    if (stream.hasFakeAudio()) {
                        fakeAudioIn = new DataInputStream(new BufferedInputStream(connection.getInputStream(), 4096));
                        System.out.println("Start Fake Audio Connection.");
                    }
                    do {
//                      Tools.sleep(1);
                        if (fakeVideoIn != null)  {
                            Tools.sleep(20);
                            if (fakeVideoIn.available() != 0) {
                                noAudioPres = false; 
                                Tools.sleep(stream.getADelay());
                                audioIn = fakeAudioIn;//new DataInputStream(connection.getInputStream());
                                System.out.println("Start Movie/DVB Audio.");
                            }
                      } else if (stream.getName().endsWith(".mp3") || !stream.hasVideo() ) {
                            noAudioPres = false;
                            Tools.sleep(stream.getADelay());
                            audioIn = new DataInputStream(connection.getInputStream());
                            System.out.println("Start Music Audio.");  
                      } else if (stream.getClass().getName().contains("SourceWebcam")) {
                            noAudioPres = false;
                            Tools.sleep(stream.getADelay());
                            audioIn = fakeAudioIn;//new DataInputStream(connection.getInputStream());
                            System.out.println("Start Webcam Audio.");  
                      } /*else if (stream.getClass().getName().contains("SourceDVB")) {
                            noAudioPres = false;
                            Tools.sleep(stream.getADelay());
                            audioIn = fakeAudioIn; //new DataInputStream(connection.getInputStream());
                            System.out.println("Start DVB Audio.");  
                      }*/
                    }  while (noAudioPres);// && !hasVideo
                    System.out.println("Out of Audio Loop.");
                } catch (IOException ex) {
                    Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
                }
        }}); 
        
        aCapture.setPriority(Thread.MIN_PRIORITY);
        if (stream.hasAudio()) { // || hasvideo
            aCapture.start();            
        }
    }
    
    public void abort() {
        stopMe = true;
        try {
            if (videoServer != null) {
                videoServer.close();
                videoServer = null;
            }
            if (audioServer != null) {
                audioServer.close();
                audioServer = null;
            }
        } catch (IOException ex) {
            Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getVideoPort() {
        return vport;
    }
    public int getFakeVideoPort(){
        return fVport;
    }

    public int getAudioPort() {
        return aport;
    }

    private WSImage getNextImage() throws IOException {
        if (videoIn != null) {
            image.readFully(videoIn);
            stream.applyEffects(image);
            return image;
        } else {
            return null;
        }
    }

    private byte[] getNextAudio() throws IOException {
        if (audioIn != null && audioIn.available()>0) {
            audioIn.readFully(audio);
            return audio;
        } else {
            return null;
        }
    }

    public Frame getFrame() {
        long mark = System.currentTimeMillis();
        BufferedImage nextImage = null;
        byte[] nextAudio = null;
        try {
            if (stream.hasVideo()) {      
                nextImage = getNextImage();
            }
            if (stream.hasAudio()) {
                nextAudio = getNextAudio();
            }
//            if (System.currentTimeMillis() - mark < 5000) {
            frame.setAudio(nextAudio);
            frame.setImage(nextImage);
            frame.setOutputFormat(stream.getX(), stream.getY(), stream.getWidth(), stream.getHeight(), stream.getOpacity(), stream.getVolume());
            frame.setZOrder(stream.getZOrder());
//            } else {
//                ResourceMonitor.getInstance().addMessage(new ResourceMonitorLabel(System.currentTimeMillis() + 10000, stream.getName() + " is too slow! Stopping stream..."));
//                stream.stop();
//            }
        } catch (IOException ioe) {
            stream.stop();
        }
        return frame;
    }    
}
