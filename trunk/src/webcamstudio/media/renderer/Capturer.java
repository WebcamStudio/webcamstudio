/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.mixers.Frame;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.TimerTask;
import webcamstudio.streams.Stream;

/**
 *
 * @author patrick
 */
public class Capturer extends TimerTask {

    private int vport = 0;
    private int aport = 0;
    private boolean stopMe = false;
    private DataClient audioClient;
    private DataClient videoClient;
    private Stream stream;
    private int fps = 0;
    private long mark = System.currentTimeMillis();
    private long timeCode = 0;
    private int audioBufferSize = 0;
    private int videoBufferSize = 0;

    public Capturer(Stream s) {
        stream = s;
        audioBufferSize = (44100 * 2 * 2) / stream.getRate();
        videoBufferSize = stream.getCaptureWidth() * stream.getCaptureHeight() * 4;
        audioClient = new DataClient();
        videoClient = new DataClient();
        vport = videoClient.getPort();
        aport = audioClient.getPort();
        System.out.println("Port used is " + vport + "/" + aport);
        new Thread(audioClient).start();
        new Thread(videoClient).start();
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

    @Override
    public void run() {
        Frame frame = new Frame(stream.getID(), null, null);
        timeCode = System.currentTimeMillis() + (990 / stream.getRate());
        if (stream.canAddFrame() && audioClient.getStream()!=null && videoClient.getStream()!=null) {
            try {
                byte[] vbuffer = new byte[videoBufferSize];
                videoClient.getStream().readFully(vbuffer);
                byte[] abuffer = new byte[audioBufferSize];
                audioClient.getStream().readFully(abuffer);
                frame.setAudio(abuffer);
                
                BufferedImage img = new BufferedImage(stream.getCaptureWidth(), stream.getCaptureHeight(), BufferedImage.TYPE_INT_ARGB);
                int[] imgData = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
                IntBuffer intData = ByteBuffer.wrap(vbuffer).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
                intData.get(imgData);
                frame.setImage(img);

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            frame.setOutputFormat(stream.getX(), stream.getY(), stream.getWidth(), stream.getHeight(), stream.getOpacity(), stream.getVolume());
            frame.setZOrder(stream.getZOrder());
            stream.addFrame(frame);
            fps += 1;

        }
        long waitTime = timeCode - System.currentTimeMillis();
        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException ex) {
                Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Too long!");
        }

        float delta = System.currentTimeMillis() - mark;
        if (fps == 60) {
            System.out.println(stream.getName() + ": " + (60F / (delta / 1000F)) + " fps");
            mark = System.currentTimeMillis();
            fps = 0;
        }

    }
}
