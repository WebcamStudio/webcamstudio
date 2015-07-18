/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import static webcamstudio.WebcamStudio.audioFreq;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.WSImage;
import webcamstudio.streams.SourceMovie;
import webcamstudio.streams.SourceMusic;
import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class Capturer {

    private int vport = 0;
    private int aport = 0;
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
    private boolean vPauseFlag = false;
    private boolean aPauseFlag = false;
    private int streamTotalEnd = 0;
    private int totalPauseTime = 0;
    private int streamEndTime = 0;
    private int currTime = 0;
    private int pauseTime = 0;

    
    public Capturer(Stream s) {
        stream = s;
        frame = new Frame(stream.getCaptureWidth(), stream.getCaptureHeight(), stream.getRate());
        image = new WSImage(stream.getCaptureWidth(), stream.getCaptureHeight(), BufferedImage.TYPE_INT_RGB);
        audio = new byte[(audioFreq * 2 * 2) / stream.getRate()];
        frame.setID(stream.getID());
        if (stream.hasAudio()) {
            try {
                audioServer = new ServerSocket(0);
                aport = audioServer.getLocalPort();
            } catch (IOException ex) {
                Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (!stream.isOnlyAudio()) {
            try {
                videoServer = new ServerSocket(0);
                vport = videoServer.getLocalPort();
            } catch (IOException ex) {
                Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (!stream.isOnlyAudio()) {
            Thread vCapture = new Thread(new Runnable() {

                @Override
                public void run() {

                    try {
                        Socket connection = videoServer.accept();
                        System.out.println(stream.getName() + " Video accepted...");
                        String duration = stream.getStreamTime();
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
                                    
                                    if (!duration.equals("N/A")) {
                                        int millisDuration = Integer.parseInt(duration.replace("s", ""))*1000;
                                        streamEndTime = (int) System.currentTimeMillis() + millisDuration;
                                        streamTotalEnd = streamEndTime;
                                    }
                                    System.out.println("Start Video ...");
                                }
                            } else if (stream.getName().contains("Desktop")) {
                                noVideoPres=false;
                                Tools.sleep(stream.getVDelay());
                                videoIn = new DataInputStream(new BufferedInputStream(connection.getInputStream(), 4096));
                                System.out.println("Start Dekstop Video ...");
                            } else if (stream.getClass().getName().contains("SourceWebcam")) {
                                noVideoPres=false;
                                Tools.sleep(stream.getVDelay());
                                videoIn = new DataInputStream(new BufferedInputStream(connection.getInputStream(), 4096));
                                System.out.println("Start Webcam Video ...");
                            } else if (!stream.hasAudio()) {
                                noVideoPres=false;
                                Tools.sleep(stream.getVDelay());
                                videoIn = fakeVideoIn;
                                if (!duration.equals("N/A")) {
                                    int millisDuration = Integer.parseInt(stream.getStreamTime().replace("s", ""))*1000;
                                    streamEndTime = (int) System.currentTimeMillis() + millisDuration;
                                    streamTotalEnd = streamEndTime;
                            }
                                System.out.println("Start NoAudio Video ...");
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
                        System.out.println(stream.getName() + " Audio accepted...");
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
                                    System.out.println("Start Audio ...");
                                }
                            } else if (stream.getName().endsWith(".mp3") || !stream.hasVideo() ) {
                                noAudioPres = false;
                                Tools.sleep(stream.getADelay());
                                audioIn = new DataInputStream(new BufferedInputStream(connection.getInputStream(), 4096));
                                System.out.println("Start Audio ...");  
                            } 
                        }   while (noAudioPres);
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

    public void vPause() {
        vPauseFlag = true;
        currTime = (int) System.currentTimeMillis();
//        System.out.println("VideoCapture Paused ...");
    }
    
    public void aPause() {
        aPauseFlag = true;
//        System.out.println("AudioCapture Paused ...");
    }
    
    public void vPlay() {
        vPauseFlag = false;
        totalPauseTime += pauseTime;
        streamTotalEnd = streamEndTime + totalPauseTime;
//        System.out.println("VideoCapture Resumed ...");
    }
    
    public void aPlay() {
        aPauseFlag = false;
//        System.out.println("AudioCapture Resumed ...");
    }
    public int getVideoPort() {
        return vport;
    }

    public int getAudioPort() {
        return aport;
    }

    private WSImage getNextImage() throws IOException {
        if (videoIn != null && !vPauseFlag) {
            if (stream instanceof SourceMovie || stream instanceof SourceMusic ) {
                if (vPauseFlag) {
                    image.readFully(videoIn);
                    return image;
                } else {
                    if ((int)System.currentTimeMillis() < streamTotalEnd) {
                        image.readFully(videoIn);
                        return image;
                    } else {
                        return null;
                    }
                }
            } else {
                image.readFully(videoIn);
                return image;
            }
        } else {
            return null;
        }
    }

    private byte[] getNextAudio() throws IOException {
        if (audioIn != null && audioIn.available() > 0 && !aPauseFlag) {
            audioIn.readFully(audio);
            return audio;
        } else {
            return null;
        }
    }
    private BufferedImage toCompatibleImage(BufferedImage image) {
	GraphicsConfiguration gfx_config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	if (image.getColorModel().equals(gfx_config.getColorModel())) {
            return image;
        }
	BufferedImage new_image = gfx_config.createCompatibleImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
	Graphics2D g2d = (Graphics2D) new_image.getGraphics();
	g2d.drawImage(image, 0, 0, null);
	g2d.dispose();
	return new_image; 
}
    public Frame getFrame() {
        BufferedImage nextImage = null;
        byte[] nextAudio = null;
        try {
            if (stream.hasVideo()) {
                BufferedImage quantumImage = getNextImage();
                if (quantumImage != null){
                    nextImage = toCompatibleImage(quantumImage);
                } else {
                    if (stream instanceof SourceMovie || stream instanceof SourceMusic ) {
                        if (vPauseFlag) {
                            pauseTime = (int) System.currentTimeMillis() - currTime;
                        } else {
                            if ((int)System.currentTimeMillis() > streamTotalEnd && streamEndTime != 0) {
                                throw new IOException("Time EOS Reached !!!");
                            }
                        }
                    }
                }
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
            stream.updateStatus();
            pauseTime = 0;
            totalPauseTime = 0;
//            Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ioe);
        }
        return frame;
    }    

    static class VideoWrapper extends PushbackInputStream {

        VideoWrapper(DataInputStream in) {
            super(in);
        }

        @Override
        public int available() throws IOException {
            int b = super.read();
            // do something specific?
            super.unread(b);
            return super.available();
        }
    }
}
