/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.mixers.AudioBuffer;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.ImageBuffer;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class Exporter implements MasterMixer.SinkListener {

    private boolean cancel = false;
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

    public Exporter(Stream s) throws SocketException {
        this.stream = s;
        imageBuffer = new ImageBuffer(MasterMixer.getInstance().getWidth(),MasterMixer.getInstance().getHeight());
        audioBuffer = new AudioBuffer(MasterMixer.getInstance().getRate());
        if (stream.hasVideo()) {
            try {
                videoServer = new ServerSocket(0);
                vport = videoServer.getLocalPort();
            } catch (IOException ex) {
                Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (stream.hasAudio()) {
            try {
                audioServer = new ServerSocket(0);
                aport = audioServer.getLocalPort();
            } catch (IOException ex) {
                Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Port used is Video:" + vport+"/Audio:" + aport);
        if (stream.hasVideo()) {
            Thread vExCapture = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        vConnection = videoServer.accept();
                        System.out.println("Video output accepted");
                        videoOutput = new BufferedOutputStream(vConnection.getOutputStream(), 4096);
                        imageBuffer.clear();
                        while (!cancel) {
                            byte[] videoData = imageBuffer.pop().getBytes();
                            if (videoData == null || videoOutput == null) {
                                Tools.sleep(30);
                            } else {
                                videoOutput.write(videoData);                            
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
                        audioOutput = new BufferedOutputStream(aConnection.getOutputStream(), 4096);
                        audioBuffer.clear();
                         while (!cancel) {
                            byte[] audioData = audioBuffer.pop();
                            if (audioData == null || audioOutput==null) {
                                Tools.sleep(30);
                            } else {
                                audioOutput.write(audioData);
                                audioOutput.flush();
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

    @Override
    public void newFrame(Frame frame) {
        if (stream.hasVideo()){
            imageBuffer.push(frame.getImage());
        }
        if (stream.hasAudio()){
            audioBuffer.push(frame.getAudioData());
        }
    }
}
