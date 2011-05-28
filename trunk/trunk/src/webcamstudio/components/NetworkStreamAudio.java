/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author patrick
 */
public class NetworkStreamAudio {

    private int port = 4999;
    private java.net.ServerSocket server = null;
    private java.net.Socket connection = null;
    private boolean stopMe = false;
    private boolean running;
    private ByteArrayOutputStream out;
    public static long currentTimeStamp = 0;
    private java.util.TreeMap<Long, byte[]> frameBuffer = new java.util.TreeMap<Long, byte[]>();

    public NetworkStreamAudio() {
        feedBuffer();
    }

    private void feedBuffer() {
        try {
            System.out.println("Starting buffer");

            boolean stopConnection = false;
            final AudioFormat format = getFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);

            line.open(format);
            line.start();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    byte[] buffer = null;
                    byte[] data = null;
                    int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
                    buffer = new byte[bufferSize];
                    while (!stopMe) {
                        int count = line.read(buffer, 0, buffer.length);
                        data = new byte[count];
                        System.arraycopy(buffer, 0, data, 0, count);
                        frameBuffer.put(new Long(System.currentTimeMillis()), data);
                        while (frameBuffer.size() > 15) {
                            frameBuffer.remove(frameBuffer.firstKey());
                        }
                    }
                }
            }).start();
        } catch (Exception e) {
        }
    }

    public void start() throws IOException {
        if (server != null) {
            server.close();
            server = null;
        }
        server = new ServerSocket(port);
        server.setSoTimeout(1000);
        System.out.println("NetworkStreamAudio available on port " + port);
        System.out.println("You can use FFMPEG to connect to this stream...");
        System.out.println("ffmpeg -f alaw  -acodec pcm_s16le -ar 22050 -ac 1 -i tcp://127.0.0.1:4999 test.mp3");
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stopMe) {
                    try {
                        connection = server.accept();
                        System.out.println("Accepting connection from " + connection.getLocalSocketAddress().toString());
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                Socket threadConnection = connection;
                                try {
                                    BufferedOutputStream bout = new BufferedOutputStream(threadConnection.getOutputStream(), 44100*2);
                                    feedConnection(bout);
                                } catch (IOException ex) {
                                    Logger.getLogger(NetworkStreamAudio.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }).start();
                    } catch (java.net.SocketTimeoutException ex) {
                        continue;
                    } catch (IOException ex) {
                        Logger.getLogger(NetworkStreamAudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }

    public void stop() {
        stopMe = true;
    }

    private void feedConnection(java.io.BufferedOutputStream output) {
        byte[] buffer = null;
        boolean stopConnection = false;
        while (!stopConnection) {
            if (frameBuffer.size() > 0) {
                Long key = frameBuffer.lowerKey(currentTimeStamp);
                if (key != null) {
                    buffer = frameBuffer.get(key);
                    while (key != null) {
                        frameBuffer.remove(key);
                    }
                    try {
                        output.write(buffer);
                    } catch (IOException ex) {
                        stopConnection = true;
                        System.out.println("Audio connection lost");
                    }
                }
            } else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(NetworkStreamAudio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }


        }
        output = null;
    }

    private AudioFormat getFormat() {
        float sampleRate = 22050;


        int sampleSizeInBits = 16;


        int channels = 1;


        boolean signed = true;


        boolean bigEndian = false;


        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);


    }

    public static void main(String[] args) {
        NetworkStreamAudio audio = new NetworkStreamAudio();


        try {
            audio.start();


            try {
                Thread.sleep(200000);



            } catch (InterruptedException ex) {
                Logger.getLogger(NetworkStreamAudio.class.getName()).log(Level.SEVERE, null, ex);
            }
            audio.stop();



        } catch (IOException ex) {
            Logger.getLogger(NetworkStreamAudio.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
}
