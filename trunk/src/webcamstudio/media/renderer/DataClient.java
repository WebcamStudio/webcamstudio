/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 *
 * @author patrick
 */
public class DataClient {

    private Socket connection;
    private ArrayList<byte[]> data = new ArrayList<byte[]>();
    private int port = 0;
    private boolean abort = false;
    private ServerSocket server;
    private int dataSize = 0;
    private int rate = 15;
    public DataClient(int dataSize,int rate) {
        this.dataSize = dataSize;
        this.rate=rate;
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

    public byte[] getData() {
        if (data.isEmpty()){
            return null;
        } else {
            return data.remove(0);
        }
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
                        feed();
                        connection.close();
                    } catch (SocketTimeoutException soe) {
                        System.out.println("Waiting...");
                    } catch (IOException ioe) {
                        //ioe.printStackTrace();
                        abort = true;
                    } catch (InterruptedException iee) {
                        iee.printStackTrace();
                        abort = true;
                    }
                }
            }
        }).start();
    }

    private void feed() throws InterruptedException, IOException {
        DataInputStream din = new DataInputStream(connection.getInputStream());
        System.out.println("Reading...");
        long start = 0;
        while (!abort && !connection.isClosed()) {
            start = System.currentTimeMillis();
            byte[] datablock = new byte[dataSize];
            din.readFully(datablock);
            data.add(datablock);
            if (data.size()> 30){
                System.out.println("Warning, feeding faster than expected " + data.size());
            }
        }
    }

    public void shutdown() {
        abort = true;
    }
}
