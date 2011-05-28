/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author patrick
 */
public class NetworkStreamWin32 {

    private MixerWin32 mixer = null;
    private int port = 4888;
    private java.net.ServerSocket server = null;
    private java.net.Socket connection = null;
    private boolean stopMe = false;

    public NetworkStreamWin32(MixerWin32 m) {
        mixer = m;
    }

    public void start() throws IOException {
        if (server != null) {
            server.close();
            server = null;
        }
        server = new ServerSocket(port);
        server.setSoTimeout(1000);
        System.out.println("NetworkStream available on port " + port);
        System.out.println("You can use FFMPEG to connect to this stream...");
        System.out.println("ffmpeg -pix_fmt argb  -f rawvideo -s " + mixer.getWidth() + "x" + mixer.getHeight() + " -itsoffset 2 -i tcp://127.0.0.1:"+ port +"  -f alsa -i pulse  -ac 1 -ab 64kb -ar 22050 -r 15 test.flv");
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
                                    feedConnection(new BufferedOutputStream(threadConnection.getOutputStream()));
                                } catch (IOException ex) {
                                    Logger.getLogger(NetworkStreamWin32.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }).start();
                    } catch (java.net.SocketTimeoutException ex) {
                        continue;
                    } catch (IOException ex) {
                        Logger.getLogger(NetworkStreamWin32.class.getName()).log(Level.SEVERE, null, ex);
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
            if (mixer.getImage() != null) {
                int[] data = ((java.awt.image.DataBufferInt) mixer.getImage().getRaster().getDataBuffer()).getData();
                ByteBuffer bytes = ByteBuffer.allocate(data.length * 4);
                bytes.asIntBuffer().put(data);
                buffer = bytes.array();
                try {
                    output.write(buffer);
                } catch (IOException ex) {
                    System.out.println("Connection lost");
                    stopConnection = true;
                }
            }
            try {
                Thread.sleep(1000 / 30);
            } catch (InterruptedException ex) {
                Logger.getLogger(NetworkStreamWin32.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        output = null;

    }
}
