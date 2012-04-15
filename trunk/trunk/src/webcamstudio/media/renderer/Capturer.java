/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.mixers.Frame;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import webcamstudio.streams.Stream;

/**
 *
 * @author patrick
 */
public class Capturer {

    private int vport = 0;
    private int aport = 0;
    private boolean stopMe = false;
    private DataClient audioClient;
    private DataClient videoClient;
    private Stream stream;
    private int audioBufferSize = 0;
    private int videoBufferSize = 0;
    private ArrayList<BufferedImage> videoBuffer = new ArrayList<BufferedImage>();
    private ArrayList<byte[]> audioBuffer = new ArrayList<byte[]>();
    private final static int BUFFER_LIMIT = 2;
    BufferedImage workingImage = null;
    BufferedImage renderedImage = null;
    int[] workingImageBuffer = null;
    public Capturer(Stream s) {
        stream = s;
        audioBufferSize = (44100 * 2 * 2) / stream.getRate();
        videoBufferSize = stream.getCaptureWidth() * stream.getCaptureHeight() * 4;
        audioClient = new DataClient();
        videoClient = new DataClient();
        vport = videoClient.getPort();
        aport = audioClient.getPort();
        workingImage = new BufferedImage(s.getCaptureWidth(),s.getCaptureHeight(),BufferedImage.TYPE_INT_ARGB);
        renderedImage = new BufferedImage(s.getCaptureWidth(),s.getCaptureHeight(),BufferedImage.TYPE_INT_ARGB);
        workingImageBuffer = ((java.awt.image.DataBufferInt) workingImage.getRaster().getDataBuffer()).getData();
        System.out.println("Port used is " + vport + "/" + aport);
        new Thread(audioClient).start();
        new Thread(videoClient).start();
        Thread vCapture = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stopMe) {
                    if (videoClient.getStream() != null) {
                        if (videoBuffer.size()<BUFFER_LIMIT) {
                            try {
                                byte[] vbuffer = new byte[videoBufferSize];
                                videoClient.getStream().readFully(vbuffer);
                                IntBuffer intData = ByteBuffer.wrap(vbuffer).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
                                intData.get(workingImageBuffer);
                                //Special Effects...
                                
                                renderedImage.setRGB(0, 0, workingImage.getWidth(), workingImage.getHeight(), workingImageBuffer, 0, workingImage.getWidth());
                                videoBuffer.add(renderedImage);
                            } catch (IOException ioe) {
                                stopMe = true;
                                ioe.printStackTrace();
                            }
                        } else {
                            try {
                                Thread.sleep(30);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        Thread.yield();
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

            }
        });
        vCapture.setPriority(Thread.MIN_PRIORITY);
        vCapture.start();
        Thread aCapture = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stopMe) {
                    if (audioBuffer.size()<BUFFER_LIMIT) {
                        if (audioClient.getStream() != null) {
                            try {
                                byte[] abuffer = new byte[audioBufferSize];
                                audioClient.getStream().readFully(abuffer);
                                audioBuffer.add(abuffer);
                            } catch (IOException ioe) {
                                stopMe = true;
                                ioe.printStackTrace();
                            }
                            Thread.yield();
                        } else {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } else {
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        });
        aCapture.setPriority(Thread.MIN_PRIORITY);
        aCapture.start();

    }

    private boolean isBuffersFull() {
        return videoBuffer.size() >= BUFFER_LIMIT || audioBuffer.size() >= BUFFER_LIMIT;
    }

    public void abort() {
        try {
            videoClient.shutdown();
            audioClient.shutdown();
        } catch (IOException ex) {
            Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getVideoPort() {
        return vport;
    }

    public int getAudioPort() {
        return aport;
    }

    public Frame getFrame() {
        Frame frame = null;
        if (!videoBuffer.isEmpty() && !audioBuffer.isEmpty()) {
            frame = new Frame(stream.getID(), null, null);
            frame.setOutputFormat(stream.getX(), stream.getY(), stream.getWidth(), stream.getHeight(), stream.getOpacity(), stream.getVolume());
            frame.setZOrder(stream.getZOrder());
            frame.setImage(videoBuffer.remove(0));
            frame.setAudio(audioBuffer.remove(0));
        }
        //System.out.println(videoBuffer.size() + ", " + audioBuffer.size());
        return frame;
    }
}
