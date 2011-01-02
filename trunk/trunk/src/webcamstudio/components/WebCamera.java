/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.io.IOException;
import java.net.SocketException;
import webcamstudio.sources.VideoSourcePipeline;

/**
 *
 * @author patrick
 */
public class WebCamera {

    private int listeningPort = 10310;
    private String webcamURL = null;
    private boolean stopMe = false;
    private java.net.DatagramSocket socket = null;
    private java.net.DatagramPacket packet = null;
    private String name = "";
    private VideoSourcePipeline pipeline = null;
    private VideoSourcePipeline oldPipeLine = null;

    public WebCamera() {
        System.out.println("Starting iWebcamera Detection...");
        
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    socket = new java.net.DatagramSocket(listeningPort);
                    while (!stopMe) {
                        try {
                            if (socket.isClosed()) {
                                socket = new java.net.DatagramSocket(listeningPort);
                                socket.setSoTimeout(5000);
                            }
                            listen();
                        } catch (Exception e) {
                            stopMe = true;
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void listen() throws SocketException, IOException {

        try {
            packet = new java.net.DatagramPacket(new byte[1024], 1024);
            socket.receive(packet);
            if (packet.getLength() > 0) {
                if (new String(packet.getData()).indexOf("de.drahtwerk.iwebcam:") != -1) {
                    name = new String(packet.getData()).trim().replaceFirst("de.drahtwerk.iwebcam:", "");
                    if (oldPipeLine == null || !oldPipeLine.getName().equals(name.split(":")[0].trim())) {
                        webcamURL = "souphttpsrc location=http://" + packet.getAddress().getHostAddress().toString().trim() + ":" + name.split(":")[1].trim() + "/strm ! jpegdec ! ffmpegcolorspace name=tosink";
                        pipeline = new VideoSourcePipeline(webcamURL);
                        pipeline.setName(name.split(":")[0].trim());
                        socket.close();
                        oldPipeLine = pipeline;
                        System.out.println("Adding Webcamera...");
                    }
                }
            }
        } catch (java.net.SocketTimeoutException soe) {
            pipeline = null;
            if (oldPipeLine != null) {
                oldPipeLine = null;
                System.out.println("Removing Webcamera...");
            }
        }

    }

    public void stop() {
        stopMe = true;
    }

    public VideoSourcePipeline getSource() {
        return pipeline;
    }
}
