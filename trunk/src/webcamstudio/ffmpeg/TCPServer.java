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
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.media.Image;

/**
 *
 * @author patrick
 */
public class TCPServer {

    private int vport = 0;
    private int aport = 0;
    private Image image = null;
    private Image tempImage = null;
    private int width = 320;
    private int height = 240;
    private byte[] data = null;
    ServerSocket vserver = null;
    ServerSocket aserver = null;
    private boolean stopMe = false;
    java.util.Vector<byte[]> audioData = new java.util.Vector<byte[]>();
    public TCPServer(int w, int h) {
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
        readAudio();
        readVideo();
    }

    public void shutdown() {
        System.out.println("Requesting shutdown");
        stopMe = true;
    }

    private void readAudio() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stopMe) {
                    try {
                        Socket conn = aserver.accept();
                        DataInput stream = new DataInputStream(conn.getInputStream());

                        while (!stopMe) {
                            byte[] buffer = new byte[44100];
                            stream.readFully(buffer);
                            audioData.add(buffer);
                        }
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

    private void readVideo() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stopMe) {
                    try {
                        Socket conn = vserver.accept();
                        java.io.DataInput din = new java.io.DataInputStream(conn.getInputStream());
                        while (!stopMe) {
                            data = new byte[width * height * 4];
                            din.readFully(data);
                            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                            int[] imgData = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
                            IntBuffer intData = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
                            intData.get(imgData);

                            byte[] audio = null;
                            if (audioData.size() > 0) {
                                audio = new byte[audioData.size()*44100];
                                for (int i = 0;i<audioData.size();i++){
                                    System.arraycopy(audioData.get(i), 0, audio, i*44100, 44100);
                                    audioData.clear();
                                }
                            }
                            image = new Image(img, audio, System.currentTimeMillis());
                        }
                    } catch (SocketTimeoutException timeout) {
                        System.out.println("Waiting...");
                        continue;
                    } catch (IOException ex) {
                        //We lost the connection...
                        stopMe = true;
                    }
                }
                try {
                    System.out.println("Quitting Video...");
                    vserver.close();
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

    public Image getImage() {
        return image;
    }

    public static void main(String[] args) {
        TCPServer img = new TCPServer(320, 240);
    }
}
