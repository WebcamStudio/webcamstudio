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
public class Exporter {

    private boolean cancel = false;
    private DataServer videoServer = new DataServer();
    private DataServer audioServer = new DataServer();
    private long stamp = System.currentTimeMillis();
    private long count = 0;
    private ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
    private ArrayList<byte[]> samples = new ArrayList<byte[]>();
    final static int FRAME_LIMIT = 2;
    private Frame lastFrame = null;

    public Exporter() {
    }

    public void listen() throws IOException {
        videoServer.listen();
        audioServer.listen();
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
                                Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                        }                         
                        try {
                            Thread.sleep(1000/MasterMixer.getRate());
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
        //vOutput.start();

        Thread aOutput = new Thread(new Runnable() {

            @Override
            public void run() {

                while (!cancel) {
                    if (audioServer.canFeed()) {

                        if (!samples.isEmpty()) {
                            byte[] data = samples.remove(0);
                            try {
                                audioServer.feed(data);
                            } catch (IOException ex) {
                                cancel = true;
                                Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        try {
                            Thread.sleep(1000/MasterMixer.getRate());
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
        //aOutput.start();

        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!cancel) {
                    
                    try {
                        try {
                            fetch();
                        } catch (IOException ex) {
                            Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        Thread.sleep(800/MasterMixer.getRate());
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        }).start();
    }

    public void abort() {
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

    public void fetch() throws IOException {
        Frame frame = MasterMixer.getCurrentFrame();
        if (frame != null && !cancel && frame != lastFrame) {
            lastFrame=frame;
            
            if (frame.getAudioData() != null && audioServer.canFeed()) {
                //samples.add(frame.getAudioData());
                audioServer.feed(frame.getAudioData());
            }
            if (frame.getImage() != null && videoServer.canFeed()) {
                int[] imgData = ((java.awt.image.DataBufferInt) frame.getImage().getRaster().getDataBuffer()).getData();
                videoServer.feed(imgData);
            }
        }
    }
}
