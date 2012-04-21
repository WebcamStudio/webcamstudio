/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick
 */
public class Exporter implements MasterMixer.SinkListener {

    private boolean cancel = false;
    final static int FRAME_LIMIT = 5;
    ServerSocket videoServer = null;
    ServerSocket audioServer = null;
    OutputStream videoOutput = null;
    OutputStream audioOutput = null;
    int aport = 0;
    int vport = 0;
    BufferedImage lastImage = null;
    byte[] audioData = null;

    public Exporter() {
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
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Socket connection = videoServer.accept();
                    System.out.println("Video output accepted");
                    videoOutput = connection.getOutputStream();
                    long mark = 0;
                    while (!cancel) {
                        BufferedImage image = lastImage;
                        mark = System.currentTimeMillis();
                        if (image != null) {
                            byte[] data = new byte[image.getWidth() * image.getHeight() * 4];
                            ByteBuffer buffer = ByteBuffer.wrap(data);
                            IntBuffer iBuffer = buffer.asIntBuffer();
                            int[] imgData = new int[data.length / 4];
                            image.getRGB(0, 0, image.getWidth(), image.getHeight(), imgData, 0, image.getWidth());
                            iBuffer.put(imgData);
                            videoOutput.write(data);
                        }
                        Tools.wait(1000 / MasterMixer.getRate(), mark);

                    }
                } catch (IOException ex) {
                    //Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Socket connection = audioServer.accept();
                    //System.out.println("Audio output accepted");
                    audioOutput = connection.getOutputStream();
                    long mark = 0;
                    while (!cancel) {
                        mark = System.currentTimeMillis();
                        if (audioData != null) {
                            audioOutput.write(audioData);
                            audioData = null;
                        }
                        Tools.wait(1000 / MasterMixer.getRate(), mark);
                    }

                } catch (IOException ex) {
                    //Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
        cancel = false;
        MasterMixer.register(this);
    }

    public void abort() {
        MasterMixer.unregister(this);
        cancel = true;
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
                audioOutput.close();
                audioOutput = null;
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

    public boolean cancel() {
        cancel = true;
        return cancel;
    }

    @Override
    public void newFrame(Frame frame) {

        if (frame.getImage() != null) {
            lastImage = frame.getImage();

        }
        if (frame.getAudioData() != null) {
            audioData = frame.getAudioData();
        }
    }
}
