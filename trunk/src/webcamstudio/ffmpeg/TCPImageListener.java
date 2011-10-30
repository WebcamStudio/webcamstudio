/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.ffmpeg;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author patrick
 */
public class TCPImageListener {

    private int port = 0;
    private BufferedImage image = null;
    private int width = 320;
    private int height = 240;
    private byte[] data = null;
    ServerSocket server = null;
    private boolean stopMe = false;

    public TCPImageListener(int w, int h) {
        try {
            width = w;
            height = h;
            server = new ServerSocket(0);
            server.setSoTimeout(1000);
            port = server.getLocalPort();
            System.out.println("Port used is " + port);

        } catch (IOException ex) {
            Logger.getLogger(TCPImageListener.class.getName()).log(Level.SEVERE, null, ex);
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                read();
            }
        }).start();
    }

    public void shutdown() {
        stopMe = true;
    }

    private void read() {
        while (!stopMe) {
            try {
                Socket conn = server.accept();
                java.io.DataInput din = new java.io.DataInputStream(conn.getInputStream());
                while (!stopMe) {
                    data = new byte[width * height * 4];
                    din.readFully(data);
                    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    int[] imgData = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
                    IntBuffer intData = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
                    intData.get(imgData);
                    image=img;
                }
            } catch (SocketTimeoutException timeout) {
                System.out.println("Waiting...");
                continue;
            } catch (IOException ex) {
                //We lost the connection...
                stopMe=true;
            }
        }
        try {
            System.out.println("Quitting...");
            server.close();
        } catch (IOException ex) {
            Logger.getLogger(TCPImageListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public int getPort(){
        return port;
    }
    public BufferedImage getImage(){
        return image;
    }
    public static void main(String[] args) {
        TCPImageListener img = new TCPImageListener(320, 240);
    }
}
