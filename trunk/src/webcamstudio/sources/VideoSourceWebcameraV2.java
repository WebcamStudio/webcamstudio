/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author patrick
 */
public class VideoSourceWebcameraV2 extends VideoSource {

    private String webcamURL = null;

    public VideoSourceWebcameraV2(String url) {

        stopMe = false;
        location = url.toString();
        webcamURL = url;

    }

    private void listen() {
        while (!stopMe) {
            try {
                readImage(webcamURL);
            } catch (java.net.SocketTimeoutException soe) {
            } catch (Exception e) {
                stopMe = true;
                e.printStackTrace();
            }
        }
    }

    private void readImage(String url) throws MalformedURLException, IOException, InterruptedException {
        System.out.println(url);
        java.net.URL phone = new java.net.URL(url);

        java.net.Socket connection = new java.net.Socket(phone.getHost(), phone.getPort());
        connection.setTcpNoDelay(true);
        java.io.DataInputStream din = new java.io.DataInputStream(connection.getInputStream());
        java.io.DataOutputStream dout = new java.io.DataOutputStream(connection.getOutputStream());
        
        dout.writeBytes("GET /stream/v2 HTTP/1.0\r\n");
        byte[] buffer = new byte[1024];
        int count = din.read(buffer);
        count = din.read(buffer);
        int version = 2;
        count = din.read(buffer);
        for (int i = 0; i < count; i++) {
            System.out.println("Data : " + buffer[i]);
        }

        int audio = 0;
        count = din.read(buffer);

        System.out.println("iWebCamera: Version = " + version + ", has audio = " + audio);

        switch (version) {
            case 2:
                int command = 0;
                dout.write(command);
                dout.write(command);
                dout.write(command);
                dout.write(command);
                dout.flush();
                count = din.read(buffer);
                System.out.println("Count is " + count);
                for (int i = 0; i < count; i++) {
                    System.out.println("Data : " + buffer[i]);
                }
                int size = 0;
                byte[] data = null;
                System.out.println("Blob size is " + size);
                if (size > 0) {
                    data = new byte[size];
                    din.readFully(data);
                }
                while (!stopMe) {
                    data = new byte[5000000];
                    count = din.read(data);
                    if (count != -1) {
                        System.out.println("Data received: " + count);
                        javax.swing.ImageIcon img = new javax.swing.ImageIcon(data);
                        captureWidth = 320;
                        captureHeight = 240;
                        tempimage = new BufferedImage(captureWidth,captureHeight,BufferedImage.OPAQUE);
                        tempimage.getGraphics().drawImage(img.getImage(), 0, 0, null);
                        image = tempimage;
                        System.out.println("Image received");
                    }
                }
                break;
        }

        din.close();
        dout.close();
    }

    @Override
    public void startSource() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                isPlaying = true;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(VideoSourceWebcameraV2.class.getName()).log(Level.SEVERE, null, ex);
                }
                listen();
                isPlaying = false;
            }
        }).start();
    }

    @Override
    public void stopSource() {
        stopMe = true;

    }

    @Override
    public Collection<JPanel> getControls() {
        return new Vector<JPanel>();
    }

    @Override
    public boolean canUpdateSource() {
        return false;
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void pause() {
    }

    @Override
    public void play() {
        startSource();
    }

    static public void main(String[] args) {
        VideoSourceWebcameraV2 s = new VideoSourceWebcameraV2("http://10.42.43.10:8080/stream/v2");
        s.startSource();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Logger.getLogger(VideoSourceWebcameraV2.class.getName()).log(Level.SEVERE, null, ex);
        }
        s.stopSource();
        s = null;
    }
}
