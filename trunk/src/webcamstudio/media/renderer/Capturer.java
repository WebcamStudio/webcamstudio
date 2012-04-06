/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.mixers.Frame;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.TimerTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import webcamstudio.mixers.MasterFrameBuilder;

/**
 *
 * @author patrick
 */
public class Capturer extends TimerTask {

    private int vport = 0;
    private int aport = 0;
    private int width = 320;
    private int height = 240;
    private int captureWidth = 320;
    private int captureHeight = 240;
    private int x = 0;
    private int y = 0;
    private int opacity = 100;
    private float volume = 1f;
    private boolean stopMe = false;
    private int frameRate = 15;
    private long timeCode = 0;
    private String uuid = "";
    private DataClient audioClient;
    private DataClient videoClient;
    private BufferedImage previewImage = null;
    private int zOrder = 0;
    private ExecutorService executor = java.util.concurrent.Executors.newCachedThreadPool();

    public Capturer(String uuid, int x, int y, int w, int h, int fps, int opacity, float volume) {
        audioClient = new DataClient((44100 * 2 * 2) / frameRate, frameRate);
        videoClient = new DataClient(w * h * 4, frameRate);
        frameRate = fps;
        this.uuid = uuid;
        captureWidth = w;
        captureHeight = h;
        width = w;
        height = h;
        this.x = x;
        this.y = y;
        this.opacity = opacity;
        this.volume = volume;
        vport = videoClient.getPort();
        aport = audioClient.getPort();
        System.out.println("Port used is " + vport + "/" + aport);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZOrder(int z) {
        zOrder = z;
    }

    public BufferedImage getPreview() {
        return previewImage;
    }

    public void setFormat(int x, int y, int w, int h, int opacity, float volume) {
        width = w;
        height = h;
        this.x = x;
        this.y = y;
        this.opacity = opacity;
        this.volume = volume;
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
        Frame frame = new Frame(uuid,previewImage, null, timeCode, null);
       
        new Thread(audioClient).start();
        new Thread(videoClient).start();
        while(!(audioClient.done() && videoClient.done())){
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            byte[] abuffer = audioClient.getData();
            if (abuffer != null) {
                frame.setAudio(abuffer);
            } 
            byte[] vbuffer = videoClient.getData();
            if (vbuffer != null) {
                BufferedImage img = new BufferedImage(captureWidth, captureHeight, BufferedImage.TYPE_INT_ARGB);
                int[] imgData = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
                IntBuffer intData = ByteBuffer.wrap(vbuffer).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
                intData.get(imgData);
                frame.setImage(img);
                previewImage = img;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        timeCode += ((44100 * 2 * 2) / frameRate);
        frame.setTimeCode(timeCode);
        frame.setOutputFormat(x, y, width, height, opacity, volume);
        frame.setZOrder(zOrder);
        MasterFrameBuilder.addFrame(uuid, frame);

    }
}
