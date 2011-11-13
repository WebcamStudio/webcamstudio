/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.ffmpeg;

import java.awt.image.BufferedImage;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import webcamstudio.media.Image;
import webcamstudio.media.Sample;
import webcamstudio.mixers.AudioListener;
import webcamstudio.mixers.AudioMixer;
import webcamstudio.mixers.VideoListener;

/**
 *
 * @author patrick
 */
public class TCPServer extends TimerTask {

    private int vport = 0;
    private int aport = 0;
    private int width = 320;
    private int height = 240;
    private byte[] data = null;
    ServerSocket vserver = null;
    ServerSocket aserver = null;
    private boolean stopMe = false;
    private long startingTimeStamp;
    private int frameRate = 15;
    private AudioListener audioListener = null;
    private VideoListener videoListener = null;
    private long audioTimecode = 0;
    private long videoTimecode = 0;
    private DataInput astream = null;
    private DataInput vstream = null;

    public TCPServer(int w, int h, int fps) {
        frameRate = fps;
        try {
            width = w;
            height = h;
            vserver = new ServerSocket(0);
            vserver.setSoTimeout(1000);
            vport = vserver.getLocalPort();
            aserver = new ServerSocket(0);
            aserver.setSoTimeout(1000);
            aport = aserver.getLocalPort();
            System.out.println("Port used is " + vport + "/" + aport);

        } catch (IOException ex) {
            Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        read();
    }

    public void setAudioListener(AudioListener l) {
        audioListener = l;
    }

    public void setVideoListener(VideoListener l) {
        videoListener = l;
    }

    public void shutdown() {
        System.out.println("Requesting shutdown");
        stopMe = true;
    }

    private void read() {
        final TCPServer instance = this;
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stopMe) {
                    try {
                        Socket aconn = aserver.accept();
                        Socket vconn = vserver.accept();
                        astream = new DataInputStream(aconn.getInputStream());
                        vstream = new DataInputStream(vconn.getInputStream());
                        startingTimeStamp = AudioMixer.getTimeCode() + (44100 * 2 * 2 * 3);
                        audioTimecode = startingTimeStamp;
                        Timer timer = new Timer("TCPServer", false);
                        timer.scheduleAtFixedRate(instance, 0, 1000 / frameRate);
                        while (!stopMe) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        timer.cancel();
                        timer = null;
                    } catch (SocketTimeoutException timeout) {
                        continue;
                    } catch (IOException ex) {
                        //We lost the connection...
                        stopMe = true;
                    }
                }
                try {
                    System.out.println("Quitting Audio...");
                    aserver.close();
                } catch (IOException ex) {
                    Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }).start();
    }

    public int getVideoPort() {
        return vport;
    }

    public int getAudioPort() {
        return aport;
    }

    @Override
    public void run() {
        try {
            videoTimecode = audioTimecode;
            byte[] data = new byte[width * height * 4];
            vstream.readFully(data);
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            int[] imgData = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
            IntBuffer intData = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
            intData.get(imgData);
            Image image = new Image(img, videoTimecode, 100, 0);
            if (videoListener != null) {
                videoListener.newImage(image);
            }
            videoTimecode += ((44100 * 2 * 2) / frameRate);
            byte[] abuffer = new byte[(44100 * 2 * 2) / frameRate];
            astream.readFully(abuffer);
            Sample sample = new Sample(abuffer, audioTimecode, new AudioFormat(44100, 16, 2, true, true));
            if (audioListener != null) {
                audioListener.newSample(sample);
            }
            audioTimecode += ((44100 * 2 * 2) / frameRate);
        } catch (IOException e) {
            stopMe = true;
        }

    }
}
