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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.mixers.Frame;
import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;

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
    private Frame frame = null;
    private BufferedImage lastImage = null;

    public Capturer(Stream s) {
        stream = s;
        audioBufferSize = (44100 * 2 * 2) / stream.getRate();
        videoBufferSize = stream.getCaptureWidth() * stream.getCaptureHeight() * 4;
        frame = new Frame(stream.getID(), null, null);
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
                    long mark = 0;
//                    long count = 0;
                    while (!stopMe) {
                        try {
//                            count++;
                            mark = System.currentTimeMillis();
                            videoBufferSize = stream.getCaptureWidth() * stream.getCaptureHeight() * 4;
                            byte[] vbuffer = new byte[videoBufferSize];
                            int[] rgb = new int[videoBufferSize / 4];
                            BufferedImage image = new BufferedImage(stream.getCaptureWidth(), stream.getCaptureHeight(), BufferedImage.TYPE_INT_ARGB);
                            din.readFully(vbuffer);
                            IntBuffer intData = ByteBuffer.wrap(vbuffer).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
                            intData.get(rgb);
                            //Special Effects...
                            image.setRGB(0, 0, stream.getCaptureWidth(), stream.getCaptureHeight(), rgb, 0, stream.getCaptureWidth());
                            stream.applyEffects(image);
                            lastImage = image;
                            Tools.wait(1000/stream.getRate(), mark);
//                            if (count == stream.getRate()){
//                                System.out.println("Video Frame " + (System.currentTimeMillis()-mark)/1000 + " secs");
//                                count=0;
//                            }
                        } catch (IOException ioe) {
                            stopMe = true;
                            //ioe.printStackTrace();
                        }

                    }
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
                    long mark = 0;
                    while (!stopMe) {
                        try {
                            mark=System.currentTimeMillis();
                            audioBufferSize = (44100 * 2 * 2) / stream.getRate();
                            byte[] abuffer = new byte[audioBufferSize];
                            din.readFully(abuffer);
                            frame = new Frame(stream.getID(), lastImage, abuffer);
                            frame.setOutputFormat(stream.getX(), stream.getY(), stream.getWidth(), stream.getHeight(), stream.getOpacity(), stream.getVolume());
                            frame.setZOrder(stream.getZOrder());
                            Tools.wait(1000/stream.getRate(), mark);
                        } catch (IOException ioe) {
                            stopMe = true;
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
        try {
            if (videoServer != null) {
                videoServer.close();
                videoServer=null;
            }
            if (audioServer != null) {
                audioServer.close();
                audioServer=null;
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
        return frame;
    }
}
