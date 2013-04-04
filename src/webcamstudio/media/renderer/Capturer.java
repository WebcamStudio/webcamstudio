/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.awt.image.BufferedImage;
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

    private boolean hasaudio = false;
    private boolean hasvideo = false;
    private int vport = 0;
    private int aport = 0;
    private int fVport =0;
    private boolean stopMe = false;
    private Stream stream;
    private ServerSocket videoServer = null;
    private ServerSocket audioServer = null;
    private ServerSocket fakeVideoServer = null;
 // private FrameBuffer frameBuffer = new FrameBuffer();
    private WSImage image = null;
    private byte[] audio = null;
    private Frame frame = null;
    private DataInputStream videoIn = null;
    private DataInputStream audioIn = null;
    private DataInputStream fakeVideoIn = null;
    private boolean videoPres = false;
    private boolean firstPass = true;

    public Capturer(Stream s) {
        stream = s;
        frame = new Frame(stream.getCaptureWidth(), stream.getCaptureHeight(), stream.getRate());
        image = new WSImage(stream.getCaptureWidth(), stream.getCaptureHeight(), BufferedImage.TYPE_INT_RGB);
        audio = new byte[(44100 * 2 * 2) / stream.getRate()];
        frame.setID(stream.getID());
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
                fakeVideoServer = new ServerSocket(0);
                vport = videoServer.getLocalPort();
                fVport = fakeVideoServer.getLocalPort();
            } catch (IOException ex) {
                Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        System.out.println("Port used is Video:" + vport+ "fake: "+fVport + "/Audio:" + aport);
  /*      Thread Vsync = new Thread(new Runnable() {

            @Override
            public void run() {
                do {
                    if (videoIn != null) {
                    try {
                        if (videoIn.available() != 0) {
                            vPres = true;
                    } 
                    } catch (IOException ex) {
                        Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
                    }
            } } while (!vPres);
            }
        });
        Thread Async = new Thread(new Runnable() {

            @Override
            public void run() {
                do {
                if (audioIn != null ) {
                    try { 
                       if (audioIn.available() != 0) {
                          aPres = true;
                    }
                    } catch (IOException ex) {
                        Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
                    }
            } } while (!aPres);
            } 
        }); */
//        Async.start();
//        Vsync.start();
/*        Thread fVCapture = new Thread(new Runnable() {

            @Override
            public void run() {
                
                try {
                    Socket fakeConnection = fakeVideoServer.accept();
                    hasvideo = true;                    
                    System.out.println(stream.getName() + " video accepted...");
                    do {
                        Tools.sleep(10);
                        if (hasaudio || stream.getName().contains("Desktop")) {
  //                      if (hasaudio) {
                            fakeVideoIn = new DataInputStream(fakeConnection.getInputStream());
                        }
                    } while (!hasaudio || !hasvideo);         
                } catch (IOException ex) {
                    Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } 
        }); */
        
        Thread vCapture = new Thread(new Runnable() {

            @Override
            public void run() {
                
                try {
                    Socket connection = videoServer.accept();
                    Socket fakeConnection = fakeVideoServer.accept();
                    hasvideo = true;                    
                    System.out.println(stream.getName() + " video accepted...");
                    do {
                        Tools.sleep(10);
                        if (hasaudio || stream.getName().contains("Desktop")) {
  //                      if (hasaudio) {
                            System.out.println("Start Fake Video.");
                            fakeVideoIn = new DataInputStream(fakeConnection.getInputStream());
                            if (stream.getSeek() != 0) {
                            Tools.sleep(stream.getVDelay()*10);
                            } else {
                            Tools.sleep(stream.getVDelay());    
                            }
                            videoIn = new DataInputStream(connection.getInputStream());
                            System.out.println("Start Video.");
                             
                        }
                    } while (!hasaudio || !hasvideo); //  (aPres);           
                } catch (IOException ex) {
                    Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } 
        });
 //       fVCapture.setPriority(Thread.MIN_PRIORITY);
        vCapture.setPriority(Thread.MIN_PRIORITY);
        if (stream.hasVideo() || hasaudio) {         
 //           fVCapture.start();
 //           Tools.sleep(stream.getVDelay());
            vCapture.start();
        }
 //       vCapture.setPriority(Thread.MIN_PRIORITY);
 //       if (stream.hasVideo() || hasaudio) {         
  //          vCapture.start();
  //      }
        Thread aCapture = new Thread(new Runnable() {

            @Override
            public void run() {
                try {                    
                    Socket connection = audioServer.accept();
                    hasaudio = true;
                    System.out.println(stream.getName() + " audio accepted...");
   //                 for (videoIn.available() != 0)
                    do {
                      Tools.sleep(10);
 //                     System.out.println("videoIn:" + videoIn.available());
                      if (fakeVideoIn != null) {
                        if (fakeVideoIn.available() != 0) {
                                videoPres = true; 
                            
                            
  //                          if (hasvideo || stream.getName().contains("mp3")) {
 //                       if (vPres) {
                                Tools.sleep(stream.getADelay());
                                
                                audioIn = new DataInputStream(connection.getInputStream());
                                fakeVideoServer.close();
    //                            fakeVideoServer = null;
                                fakeVideoIn.close();
   //                             fakeVideoIn = null;
                                stream.fakeStop();
                                System.out.println("Start Audio.");
                               
  //                          }    
                        }
                      } 
                                                    
                      
                      }  while (!videoPres);//(!hasvideo || !hasaudio);
                            //(videoIn.available() == 0);
                       
                      // while (!hasvideo || !hasaudio);
                     
                } catch (IOException ex) {
                    Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
                }
             
        }});       
       
       aCapture.setPriority(Thread.MIN_PRIORITY);
       if (stream.hasAudio()|| hasvideo) { // 
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
