/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class Exporter implements MasterMixer.SinkListener {

    private boolean cancel = false;
    private DataServer videoServer = new DataServer("video");
    private DataServer audioServer = new DataServer("audio");
    private long stamp = System.currentTimeMillis();
    private long count = 0;
    private ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
    private ArrayList<byte[]> samples = new ArrayList<byte[]>();
    final static int FRAME_LIMIT = 5;
    private Frame lastFrame = null;

    public Exporter() {
    }

    public void listen() throws IOException {
        videoServer.listen();
        audioServer.listen();
        MasterMixer.register(this);
        cancel = false;
        Thread vOutput = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!cancel) {
                    if (videoServer.canFeed()) {
                        if (!images.isEmpty()) {
                            BufferedImage img = images.remove(0);
                            int[] imgData = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
                            try {
                                videoServer.feed(imgData);

                            } catch (IOException ex) {
                                cancel = true;
                                //Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                        try {
                            Thread.sleep(1000 / MasterMixer.getRate());
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        });
        vOutput.setPriority(Thread.MIN_PRIORITY);
        vOutput.start();

        Thread aOutput = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!cancel) {
                    if (audioServer.canFeed()) {
                        if (!samples.isEmpty()) {
                            byte[] data = samples.remove(0);
                            try {
                                //System.out.println("Feeding audio");
                                audioServer.feed(data);
                            } catch (IOException ex) {
                                cancel = true;
                                Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        try {
                            Thread.sleep(1000 / MasterMixer.getRate());
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        });
        aOutput.setPriority(Thread.MIN_PRIORITY);
        aOutput.start();
    }

    public void abort() {
        MasterMixer.unregister(this);
        try {
            videoServer.shutdown();
            audioServer.shutdown();
        } catch (Exception e) {
        }
        cancel = true;
        
    }

    public int getAudioPort() {
        return audioServer.getPort();
    }

    public int getVideoPort() {
        return videoServer.getPort();
    }

    public boolean cancel() {
        cancel = true;
        return cancel;
    }
    @Override
    public void newFrame(Frame frame) {
        if (frame.getAudioData() != null) {
            samples.add(frame.getAudioData());
        }
        if (frame.getImage() != null) {
            images.add(frame.getImage());
        }
    }
}
