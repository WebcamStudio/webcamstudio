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
 * @author patrick (modified by karl)
 */
public class Capturer {

    private int vport = 0;
    private int aport = 0;
//    private final int fVport =0;
//    private boolean stopMe = false;
    private Stream stream;
    private ServerSocket videoServer = null;
    private ServerSocket audioServer = null;
    private WSImage image = null;
    private byte[] audio = null;
    private Frame frame = null;
    private DataInputStream videoIn = null;
    private DataInputStream audioIn = null;
    private DataInputStream fakeVideoIn = null;
    private DataInputStream fakeAudioIn = null;
    private boolean noVideoPres = true;
    private boolean noAudioPres = true;

    public Capturer(Stream s) {
        stream = s;
        frame = new Frame(stream.getCaptureWidth(), stream.getCaptureHeight(), stream.getRate());
        image = new WSImage(stream.getCaptureWidth(), stream.getCaptureHeight(), BufferedImage.TYPE_INT_RGB);
        audio = new byte[(webcamstudio.WebcamStudio.audioFreq * 2 * 2) / stream.getRate()];
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
                vport = videoServer.getLocalPort();
            } catch (IOException ex) {
                Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
  
//        System.out.println("Port used is Video:" + vport+"/Audio:" + aport);
        if (stream.hasVideo()) {
            Thread vCapture = new Thread(new Runnable() {

                @Override
                public void run() {

                    try {
                        Socket connection = videoServer.accept();  
    //                    System.out.println(stream.getName() + " video accepted...");
                        if (stream.hasFakeVideo()) {
                            fakeVideoIn = new DataInputStream(new BufferedInputStream(connection.getInputStream(), 4096));
                        }
                        do {
                            Tools.sleep(20);
                            if (fakeAudioIn != null) {
                                if (fakeAudioIn.available() != 0) {
                                    noVideoPres=false;
                                    Tools.sleep(stream.getVDelay());
                                    videoIn = fakeVideoIn;
                                    System.out.println("Start Movie/DVB Video.");
                                }
                            } else if (stream.getName().contains("Desktop")) {
                                noVideoPres=false;
                                Tools.sleep(stream.getVDelay());
                                videoIn = new DataInputStream(connection.getInputStream());
                                System.out.println("Start Desktop Video.");
                            } else if (stream.getClass().getName().contains("SourceWebcam")) { //hasaudio ||
                                noVideoPres=false;
                                Tools.sleep(stream.getVDelay());
                                videoIn = new DataInputStream(new BufferedInputStream(connection.getInputStream(), 4096));
                                System.out.println("Start Webcam Video.");
                            } else if (!stream.hasAudio()) {
                                noVideoPres=false;
                                Tools.sleep(stream.getVDelay());
                                videoIn = fakeVideoIn;
                                System.out.println("Start Muted Video.");
                            }
                        } while (noVideoPres);

                    } catch (IOException ex) {
                        Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } 
            });
            vCapture.setPriority(Thread.MIN_PRIORITY);
            vCapture.start();
        }
        if (stream.hasAudio()) {
            Thread aCapture = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {                    
                        Socket connection = audioServer.accept();
    //                    System.out.println(stream.getName() + " audio accepted...");
                        if (stream.hasFakeAudio()) {
                            fakeAudioIn = new DataInputStream(new BufferedInputStream(connection.getInputStream(), 4096));
                        }
                        do {
                            Tools.sleep(20);
                            if (fakeVideoIn != null)  {                            
                                if (fakeVideoIn.available() != 0) {
                                    noAudioPres = false; 
                                    Tools.sleep(stream.getADelay());
                                    audioIn = fakeAudioIn;
                                    System.out.println("Start Movie/DVB Audio.");
                                }
                          } else if (stream.getName().endsWith(".mp3") || !stream.hasVideo() ) {
                                noAudioPres = false;
                                Tools.sleep(stream.getADelay());
                                audioIn = new DataInputStream(new BufferedInputStream(connection.getInputStream(), 4096));
                                System.out.println("Start Music/Mic Audio.");  
                          } 
                        }  while (noAudioPres);
                    } catch (IOException ex) {
                        Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }); 
            aCapture.setPriority(Thread.MIN_PRIORITY);
            aCapture.start();
        }
    }
    
    public void abort() {
//        stopMe = true;
        try {
        if (videoServer != null) {
            videoServer.close();
            videoServer = null;
            videoIn = null;
            fakeVideoIn = null;
        }
        if (audioServer != null) {
            audioServer.close();
            audioServer = null;
            audioIn = null;
            fakeAudioIn = null;
        }
        } catch (IOException ex) {
            Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
    }
    }

    public int getVideoPort() {
        return vport;
    }
//    public int getFakeVideoPort(){
//        return fVport;
//    }

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
        BufferedImage nextImage = null;
        byte[] nextAudio = null;
        try {
            if (stream.hasVideo()) {      
                nextImage = getNextImage();
            }
            if (stream.hasAudio()) {
                nextAudio = getNextAudio();
            }
            frame.setAudio(nextAudio);
            frame.setImage(nextImage);
            frame.setOutputFormat(stream.getX(), stream.getY(), stream.getWidth(), stream.getHeight(), stream.getOpacity(), stream.getVolume());
            frame.setZOrder(stream.getZOrder());
        } catch (IOException ioe) {
            stream.stop();
        }
        return frame;
    }    
}
