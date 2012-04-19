/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

/**
 *
 * @author patrick
 */
public class DataServer {

    private ArrayList<byte[]> data = new ArrayList<byte[]>();
    private Socket connection;
    private int port = 0;
    private boolean abort = false;
    private ServerSocket server;
    private DataOutputStream output = null;
    private String source = "";
    public DataServer(String sourceName) {
        source=sourceName;
        try {
            server = new ServerSocket(0);
            port = server.getLocalPort();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return port;
    }

    public void listen() throws IOException {
        server.setSoTimeout(1000);
        abort = false;
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!abort) {
                    try {
                        connection = server.accept();
                        server.close();
                        output = new DataOutputStream(connection.getOutputStream());
                        System.out.println(source + " Connection Accepted");
                    } catch (SocketTimeoutException soe) {
                        System.out.println(source + " Waiting...");
                    } catch (IOException ioe) {
                        //ioe.printStackTrace();
                        abort = true;
                    }
                }
            }
        }).start();
    }

    public void feed(byte[] data) throws IOException {
        if (!connection.isClosed()) {
            output.write(data);
        }
    }
    public boolean canFeed(){
        return connection!=null;
    }
    public void feed(int[] data) throws IOException {
        if (!connection.isClosed()) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            intBuffer.put(data);

            byte[] array = byteBuffer.array();
            //System.out.println(source + " Data writting");
            output.write(array);
            //System.out.println(source + " Data written");
        } 
    }

    public void shutdown() throws IOException {
        abort = true;
        if (connection != null) {
            connection.close();
        }
    }
}
