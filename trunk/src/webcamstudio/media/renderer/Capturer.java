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
    private BufferedImage previewImage = null;
    private Stream stream;
    public Capturer(Stream s) {
        stream = s;
        audioClient = new DataClient((44100 * 2 * 2) / stream.getRate(), stream.getRate());
        videoClient = new DataClient(stream.getCaptureWidth() * stream.getCaptureHeight() * 4, stream.getRate());
        vport = videoClient.getPort();
        aport = audioClient.getPort();
        System.out.println("Port used is " + vport + "/" + aport);
    }


    public BufferedImage getPreview() {
        return previewImage;
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
        Frame frame = new Frame(stream.getID(),previewImage, null);
       
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
                BufferedImage img = new BufferedImage(stream.getCaptureWidth(), stream.getCaptureHeight(), BufferedImage.TYPE_INT_ARGB);
                int[] imgData = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
                IntBuffer intData = ByteBuffer.wrap(vbuffer).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
                intData.get(imgData);
                frame.setImage(img);
                previewImage = img;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        frame.setOutputFormat(stream.getX(), stream.getY(), stream.getWidth(), stream.getHeight(), stream.getOpacity(), stream.getVolume());
        frame.setZOrder(stream.getZOrder());
        stream.addFrame(frame);
        
    }
}
