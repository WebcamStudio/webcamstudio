/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
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

    public DataServer() {
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

    public void addData(byte[] d) {
        data.add(d);
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
        OutputStream output = connection.getOutputStream();
        System.out.println("Feeding...");
        while (!abort && !connection.isClosed()) {
            if (data.size() > 0) {
                byte[] raw = data.remove(0);
                if (raw!=null){
                    output.write(raw);
                    output.flush();
                }
            } else {
                Thread.sleep(10);
            }
        }
    }

    public void shutdown() {
        abort = true;
    }
}
