/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick
 */
public class TESTTCPIP {
    public static void main(String[] args){
        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean stop = false;
                while (!stop){
                    try {
                        ServerSocket server = new ServerSocket(0);
                        System.out.println("Video Port: " + server.getLocalPort());
                        Socket connection = server.accept();
                        byte[] buffer = new byte[320*240*4];
                        DataInputStream din = new DataInputStream(connection.getInputStream());
                        while(!stop){
                            din.readFully(buffer);
                            Tools.sleep(1000/15);
                        }
                    } catch (IOException ex) {
                        stop=true;
                        Logger.getLogger(TESTTCPIP.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
            }
        }).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean stop = false;
                while (!stop){
                    try {
                        ServerSocket server = new ServerSocket(0);
                        System.out.println("Audio Port: " + server.getLocalPort());
                        Socket connection = server.accept();
                        byte[] buffer = new byte[(44100*2*2)/15];
                        DataInputStream din = new DataInputStream(connection.getInputStream());
                        while(!stop){
                            din.readFully(buffer);
                            Tools.sleep(1000/15);
                        }
                    } catch (IOException ex) {
                        stop=true;
                        Logger.getLogger(TESTTCPIP.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
            }
        }).start();
        
    }
}
