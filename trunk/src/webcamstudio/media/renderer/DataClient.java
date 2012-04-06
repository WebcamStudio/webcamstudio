/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author patrick
 */
public class DataClient implements Runnable {

    private Socket connection;
    
    private byte[] datablock = null;
    private int port = 0;
    private boolean abort = false;
    private ServerSocket server;
    private int dataSize = 0;
    private int rate = 15;
    private DataInputStream din = null;
    private boolean doneReading = false;

    public DataClient(int dataSize, int rate) {
        this.dataSize = dataSize;
        this.rate = rate;
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

    public byte[] getData() throws IOException {
        return datablock;
    }

    public boolean canFeed() {
        return connection != null;
    }

    public void feed() throws IOException {
        if (din != null) {
            if (!abort && !connection.isClosed()) {
                datablock = new byte[dataSize];
                din.readFully(datablock);
            }
        }
    }

    public boolean done(){
        return doneReading;
    }
    public void shutdown() throws IOException {
        abort = true;
        if (connection != null) {
            connection.close();
            din=null;
            connection = null;
        }
        if (server != null) {
            server.close();
            server = null;
        }
    }

    @Override
    public void run() {
        doneReading=false;
        try {
            if (connection == null) {
                abort=false;
                connection = server.accept();
                din = new DataInputStream(connection.getInputStream());
                server.close();
                server = null;
            }
            feed();
        } catch (IOException ex) {
            abort=true;
            Logger.getLogger(DataClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        doneReading=true;
    }
}
