/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.io.BufferedOutputStream;
//import java.io.DataOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.mixers.*;
import webcamstudio.streams.Stream;
//import webcamstudio.util.Tools;
//import java.nio.ByteBuffer;

/**
 *
 * @author patrick
 */
public class Exporter implements MasterMixer.SinkListener {

    private boolean cancel = false;
    //final static int FRAME_LIMIT = 5;
    private ServerSocket videoServer = null;
    private ServerSocket audioServer = null;
    private BufferedOutputStream videoOutput;    
    private BufferedOutputStream audioOutput;
    private int aport = 0;
    private int vport = 0;
    private ImageBuffer imageBuffer = null;
    private AudioBuffer audioBuffer = null;
    private long vCounter = 0;
    private long aCounter = 0;
    private Stream stream = null;
    private Socket vConnection = null;
    private Socket aConnection = null;

    public Exporter(Stream s) {
        this.stream = s;
        imageBuffer = new ImageBuffer(MasterMixer.getInstance().getWidth(),MasterMixer.getInstance().getHeight());
        audioBuffer = new AudioBuffer(MasterMixer.getInstance().getRate());
        try {
            videoServer = new ServerSocket(0);
            vport = videoServer.getLocalPort();
        } catch (IOException ex) {
            Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            audioServer = new ServerSocket(0);
            aport = audioServer.getLocalPort();
        } catch (IOException ex) {
            Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Port used is Video:" + vport+"/Audio:" + aport);
        Thread vExCapture = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    vConnection = videoServer.accept();
                    System.out.println("Video output accepted");
                    videoOutput = new BufferedOutputStream(vConnection.getOutputStream(), 4096);
                    imageBuffer.clear();
 //                 int counter = 0;
                    while (!cancel) {
                        byte[] videoData = imageBuffer.pop().getBytes();
                        if (videoData == null || videoOutput == null) {                            
                        } else {
                            videoOutput.write(videoData);                            
                            videoOutput.flush();
                            vCounter++;
                        }
                    } 
                } catch (IOException ex) {
                    cancel = true;
                    imageBuffer.abort();
                    stream.stop();
                    stream.updateStatus();
                    Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Video output stopped");
            }
        });
        vExCapture.setPriority(Thread.MIN_PRIORITY);
        if (stream.hasVideo()) {  // || hasaudio       
            vExCapture.start();
        }
        Thread aExCapture = new Thread(new Runnable() {

            @Override
            public void run() {
                try {                    
                    aConnection = audioServer.accept();
                    System.out.println("Audio output accepted");
                    audioOutput = new BufferedOutputStream(aConnection.getOutputStream(), 4096);
                    audioBuffer.clear();
                     while (!cancel) {
                        byte[] audioData = audioBuffer.pop();
                        if (audioData == null || audioOutput==null) {
                        } else {
                            audioOutput.write(audioData);
                            audioOutput.flush();
                            aCounter++;
                        }                        
                    }

                } catch (IOException ex) {
                    cancel = true;
                    audioBuffer.abort();
                    stream.stop();
                    stream.updateStatus();
                    Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Audio output stopped");
            }
        });
        aExCapture.setPriority(Thread.MIN_PRIORITY);
        if (stream.hasVideo()) {  // || hasaudio       
            aExCapture.start();
        }
        
        cancel = false;
        MasterMixer.getInstance().register(this);

    }

    public void abort() {
        cancel = true;
        System.out.println("Output aborted...");
        MasterMixer.getInstance().unregister(this);
        imageBuffer.abort();
        audioBuffer.abort();
        System.out.println("V: " +vCounter);
        System.out.println("A: " +aCounter);
        if (videoServer != null) {
            try {
                videoServer.close();
                videoOutput.close();
                videoOutput = null;
            } catch (IOException ex) {
                Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            videoServer = null;
        }
        if (audioServer != null) {
            try {
                audioServer.close();
                if (audioOutput!=null){
                    audioOutput.close();
                    audioOutput = null;
                }
            } catch (IOException ex) {
                Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            audioServer = null;
        }
    }

    public int getAudioPort() {
        return aport;
    }

    public int getVideoPort() {
        return vport;
    }

    @Override
    public void newFrame(Frame frame) {
        imageBuffer.push(frame.getImage());
        audioBuffer.push(frame.getAudioData());
    }
}
