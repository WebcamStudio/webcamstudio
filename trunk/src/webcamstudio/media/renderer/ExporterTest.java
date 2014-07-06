/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
//import static webcamstudio.WebcamStudio.audioFreq;
//import webcamstudio.externals.ProcessRenderer;
import webcamstudio.mixers.AudioBuffer;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.ImageBuffer;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.mixers.WSImage;
import webcamstudio.streams.Stream;
//import webcamstudio.util.Screen;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class ExporterTest implements MasterMixer.SinkListener {
    static Listener listenerEx = null;

    public static void setListenerEx(Listener l) {
        listenerEx = l;
    }

    private boolean cancel = false;
    private ServerSocket videoServer = null;
    private ServerSocket audioServer = null;
//    private ServerSocket startVideoServer = null;
//    private ServerSocket startAudioServer = null;
    private DataOutputStream videoOutput;    
    private DataOutputStream audioOutput;
//    private DataOutputStream sVideoOutput;    
//    private DataOutputStream sAudioOutput;
    private int aport = 0;
    private int vport = 0;
//    private int sAport = 0;
//    private int sVport = 0;
    private ImageBuffer imageBuffer = null;
    private AudioBuffer audioBuffer = null;
    private WSImage imgTestBuffer = null;
    private AudioBuffer sndTestBuffer = null;
    private long vCounter = 0;
    private long aCounter = 0;
    private Stream stream = null;
    private Socket vConnection = null;
    private Socket aConnection = null;
//    private Socket sVConnection = null;
//    private Socket sAConnection = null;
//    private Process avTestStream = null;
//    private Process startingAudio = null;
    @SuppressWarnings("SocketException")
    public ExporterTest(Stream s) throws SocketException {
        this.stream = s;
        int w = MasterMixer.getInstance().getWidth();
        int h = MasterMixer.getInstance().getHeight();
        imageBuffer = new ImageBuffer(w,h);
        imgTestBuffer = new WSImage(w,h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imgTestBuffer.createGraphics();
        g.setBackground(Color.RED);
        g.clearRect(0, 0, w, h);
        g.drawImage(imgTestBuffer, 0, 0, null);
        g.dispose();
        audioBuffer = new AudioBuffer(MasterMixer.getInstance().getRate());
        sndTestBuffer = new AudioBuffer(MasterMixer.getInstance().getRate());
        if (!stream.isOnlyAudio()) { // stream.hasVideo()
            try {
                videoServer = new ServerSocket(0);
                vport = videoServer.getLocalPort();
//                startVideoServer = new ServerSocket(1);
//                sVport = startVideoServer.getLocalPort();
            } catch (IOException ex) {
                Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (stream.hasAudio()) {
            try {
                audioServer = new ServerSocket(0);
                aport = audioServer.getLocalPort();
//                startAudioServer = new ServerSocket(1);
//                sAport = startAudioServer.getLocalPort();
            } catch (IOException ex) {
                Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//        System.out.println("Port used is Video:" + vport+"/Audio:" + aport);
//        readFakeAV(stream);
        if (!stream.isOnlyAudio()) { // stream.hasVideo()
            Thread vExCapture = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
//                        sVConnection = startVideoServer.accept();
                        vConnection = videoServer.accept();
                        System.out.println("Video output accepted");
//                        sVideoOutput = new DataOutputStream(new BufferedOutputStream(sVConnection.getOutputStream()));
                        videoOutput = new DataOutputStream(new BufferedOutputStream(vConnection.getOutputStream(), 4096));
                        imageBuffer.clear();
                        while (!cancel) {
                            byte[] svideoData = imgTestBuffer.getBytes();
                            byte[] videoData = imageBuffer.pop().getBytes();
                            if (videoData == null) { //|| videoOutput == null
//                                videoOutput.write(svideoData);                            
//                                videoOutput.flush();
//                                vCounter++;
                                Tools.sleep(30);
                            } else {
//                                videoOutput.write(videoData);                            
//                                videoOutput.flush();
//                                vCounter++;
                                videoOutput.write(svideoData);                            
                                videoOutput.flush();
                                vCounter++;
                            }
                        } 
                    } catch (IOException ex) {
                        cancel = true;
                        if (imageBuffer != null){
                            imageBuffer.abort();
                        }
                        stream.stop();                   
                        stream.updateStatus();
                        listenerEx.resetFMECount();
                        Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Video output stopped");
                }
            });
            vExCapture.setPriority(Thread.MIN_PRIORITY);
            vExCapture.start();
        }
        if (stream.hasAudio()) { //stream.hasVideo()
            Thread aExCapture = new Thread(new Runnable() {
                
                @Override
                public void run() {
                    try {                    
                        aConnection = audioServer.accept();
                        System.out.println("Audio output accepted");
                        audioOutput = new DataOutputStream(new BufferedOutputStream(aConnection.getOutputStream(), 4096));
                        audioBuffer.clear();
                        while (!cancel) {
                            byte[] testAudio = imgTestBuffer.getBytes();
                            byte[] audioData = audioBuffer.pop();
                            if (audioData == null) { //|| audioOutput==null
                                audioOutput.write(testAudio);
                                if (audioOutput != null) {
                                    audioOutput.flush();
                                }
                                aCounter++;
//                                Tools.sleep(30);
                            } else {
                                audioOutput.write(audioData);
                                if (audioOutput != null) {
                                    audioOutput.flush();
                                }
                                aCounter++;
                            }                        
                        }

                    } catch (IOException ex) {
                        cancel = true;
                        if (audioBuffer != null){
                            audioBuffer.abort();
                        }
                        stream.stop();
                        stream.updateStatus();
                        Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Audio output stopped");
                }
            });
            aExCapture.setPriority(Thread.MIN_PRIORITY);
            aExCapture.start();
        }
        cancel = false;
        MasterMixer.getInstance().register(this);
    }
    
//    public void readFakeAV(final Stream s) {
//        
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                Runtime rt = Runtime.getRuntime();
//                String commandAVTest = "gst-launch-1.0 audiotestsrc ! audioconvert ! audio/x-raw, format=S16BE, channels=2, rate="+audioFreq+" ! audioconvert ! tcpclientsink port="+aport+" videotestsrc ! videoconvert ! videoscale ! videorate ! video/x-raw, format=RGB, framerate="+s.getRate()+"/1, width="+s.getWidth()+", height="+s.getHeight()+" ! videoconvert ! tcpclientsink port="+vport;
//        
//        File fileD = new File(Tools.getUserHome()+"/.webcamstudio/"+"AVTest.sh");
//        FileOutputStream fosD;
//        DataOutputStream dosD = null;
//        try {
//            fosD = new FileOutputStream(fileD);
//            dosD= new DataOutputStream(fosD);
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        try {
//            if (dosD != null) {
//            dosD.writeBytes("#!/bin/bash\n");
//            dosD.writeBytes(commandAVTest+"\n");
//            dosD.close();
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        fileD.setExecutable(true);
//        String avTestComm = Tools.getUserHome()+"/.webcamstudio/"+"AVTest.sh";
//        try {
//            avTestStream = rt.exec(avTestComm);
////                String cmdVideo = "gst-launch-1.0 videotestsrc ! decodebin ! videoconvert ! videoscale ! videorate ! video/x-raw, format=RGB, framerate="+s.getRate()+"/1, width="+s.getWidth()+", height="+s.getHeight()+" ! videoconvert ! tcpclientsink port="+vport;
////                String cmdAudio = "gst-launch-1.0 audiotestsrc ! audioconvert ! audio/x-raw, format=S16BE, channels=2, rate="+audioFreq+" ! audioconvert ! tcpclientsink port="+aport;
////                try {
////                    System.out.print("CommandVideo: "+cmdVideo+"\n");
////                    System.out.print("CommandAudio: "+cmdAudio+"\n");
////                    startingVideo = rt.exec(cmdVideo);
////                    startingAudio = rt.exec(cmdAudio);
//                } catch (IOException e) {
//                    Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, e);
//                }
//            }
//        }).start();
//    }
    
    public void abort() {
        cancel = true;
        System.out.println("Output aborted...");
        MasterMixer.getInstance().unregister(this);
        if (stream.hasVideo()) {
            imageBuffer.abort();
            imageBuffer = null;
        }
        if (stream.hasAudio()) {
            audioBuffer.abort();
            audioBuffer = null;
        }
        System.out.println("V: " +vCounter);
        System.out.println("A: " +aCounter);
        if (videoServer != null && stream.hasVideo()) {
            try {
                videoServer.close();
                videoOutput.close();
                videoOutput = null;
                videoServer = null;
            } catch (IOException ex) {
                Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
            }   
        }
        if (audioServer != null && stream.hasAudio()) {
            try {
                audioServer.close();
                if (audioOutput!=null){
                    audioOutput.close();
                    audioOutput = null;
                    audioServer = null;
                }
            } catch (IOException ex) {
                Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public int getAudioPort() {
        return aport;
    }

    public int getVideoPort() {
        return vport;
    }

//    public int getSAudioPort() {
//        return sAport;
//    }
//
//    public int getSVideoPort() {
//        return sVport;
//    }
    
    @Override
    public void newFrame(Frame frame) {
        if (stream.hasVideo()){
            imageBuffer.push(frame.getImage());
        }
        if (stream.hasAudio()){
            audioBuffer.push(frame.getAudioData());
        }
    }

    public interface Listener {

        public void resetFMECount();
    }
}
