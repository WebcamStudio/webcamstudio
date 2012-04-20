/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author patrick
 */
public class DataClient extends TimerTask {

    private Socket connection;
    final static int BUFFER_SIZE = 10;
    private int port = 0;
    private ServerSocket server;
    
    private DataInputStream din = null;

    public DataClient() {
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

    public boolean canFeed() {
        return connection != null;
    }
    public DataInputStream getStream(){
        return din;
    }
    public void shutdown() throws IOException {
        if (connection != null) {
            connection.close();
            din = null;
            connection = null;
        }
        if (server != null) {
            server.close();
            server = null;
        }
    }

    @Override
    public void run() {
        try {
            if (connection == null) {
                connection = server.accept();
                System.out.println("Accepted connection");
                din = new DataInputStream(connection.getInputStream());
//                server.close();
//                server = null;
            }
        } catch (IOException ex) {
            Logger.getLogger(DataClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
