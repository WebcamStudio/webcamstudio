/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.components.ResourceMonitor;
import webcamstudio.components.ResourceMonitorLabel;
import webcamstudio.mixers.AudioBuffer;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.ImageBuffer;
import webcamstudio.streams.Stream;

/**
 *
 * @author patrick
 */
public class Capturer {

    private int vport = 0;
    private int aport = 0;
    private boolean stopMe = false;
    private Stream stream;
    private int audioBufferSize = 0;
    private int videoBufferSize = 0;
    private ServerSocket videoServer = null;
    private ServerSocket audioServer = null;
    // private FrameBuffer frameBuffer = new FrameBuffer();
    private ImageBuffer imageBuffer = null;
    private AudioBuffer audioBuffer = null;
    private Frame frame = null;
    public Capturer(Stream s) {
        stream = s;
        audioBufferSize = (44100 * 2 * 2) / stream.getRate();
        videoBufferSize = stream.getCaptureWidth() * stream.getCaptureHeight() * 4;
        imageBuffer = new ImageBuffer(stream.getCaptureWidth(), stream.getCaptureHeight());
        audioBuffer = new AudioBuffer(stream.getRate());
        frame = new Frame(stream.getCaptureWidth(),stream.getCaptureHeight(),stream.getRate());
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
        System.out.println("Port used is Video:" + vport + "/Audio:" + aport);
        System.out.println("Size: " + stream.getCaptureWidth() + "X" + stream.getCaptureHeight());
        Thread vCapture = new Thread(new Runnable() {

            @Override
            public void run() {
                Socket connection = null;
                try {
                    connection = videoServer.accept();
                    System.out.println(stream.getName() + " video accepted...");
                    DataInputStream din = new DataInputStream(connection.getInputStream());
                    
                    imageBuffer.clear();
                    videoBufferSize = stream.getCaptureWidth() * stream.getCaptureHeight() * 4;
                    byte[] vbuffer = new byte[videoBufferSize];
                    
                    int[] rgb = new int[videoBufferSize / 4];
//                    long mark = 0;
//                    long delta = 0;
                    while (!stopMe) {
//                        mark = System.currentTimeMillis();
                        try {
                            BufferedImage image = imageBuffer.getImageToUpdate();
                            rgb = ((DataBufferInt)(image).getRaster().getDataBuffer()).getData();
                            din.readFully(vbuffer);
//                            //Setting opacity to 100%
                            for (int i = 0;i<vbuffer.length;i+=4){
                                vbuffer[i] = (byte)0xFF;
                            }
                            IntBuffer intData = ByteBuffer.wrap(vbuffer).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
                            intData.get(rgb);
//                            //Special Effects...
                            stream.applyEffects(image);
                            imageBuffer.doneUpdate();
                        } catch (IOException ioe) {
                            stopMe = true;
                            stream.stop();
                            stream.updateStatus();
                            //ioe.printStackTrace();
                        }
//                        delta = System.currentTimeMillis()-mark;
//                        System.out.println(delta);
                    }
                    imageBuffer.clear();
                    din.close();
                } catch (IOException ex) {
                    Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        vCapture.setPriority(Thread.MIN_PRIORITY);
        if (stream.hasVideo()) {
            vCapture.start();
        }
        Thread aCapture = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Socket connection = audioServer.accept();
                    System.out.println(stream.getName() + " audio accepted...");
                    DataInputStream din = new DataInputStream(connection.getInputStream());
                    audioBuffer.clear();
                    while (!stopMe) {
                        try {
                            din.readFully(audioBuffer.getAudioToUpdate());
                            audioBuffer.doneUpdate();
                        } catch (IOException ioe) {
                            stopMe = true;
                            stream.stop();
                            stream.updateStatus();
                            //ioe.printStackTrace();
                        }
                    }
                    din.close();
                } catch (IOException ex) {
                    Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        aCapture.setPriority(Thread.MIN_PRIORITY);
        if (stream.hasAudio()) {
            aCapture.start();
        }

    }

    public void abort() {
        stopMe = true;
        audioBuffer.abort();
        imageBuffer.abort();
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

    public int getAudioPort() {
        return aport;
    }

    public Frame getFrame() {
        long mark = System.currentTimeMillis();
        BufferedImage image = null;
        if (stream.hasVideo()) {
            image = imageBuffer.pop();
        }
        byte[] audio = null;
        if (stream.hasAudio()) {
            audio = audioBuffer.pop();
        }
        if (System.currentTimeMillis() - mark < 5000) {
            frame.setAudio(audio);
            frame.setImage(image);
            frame.setOutputFormat(stream.getX(), stream.getY(), stream.getWidth(), stream.getHeight(), stream.getOpacity(), stream.getVolume());
            frame.setZOrder(stream.getZOrder());
        } else {
            ResourceMonitor.getInstance().addMessage(new ResourceMonitorLabel(System.currentTimeMillis() + 10000, stream.getName() + " is too slow! Stopping stream..."));
            stream.stop();
        }
        return frame;
    }
}
