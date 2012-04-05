/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.TimerTask;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick
 */
public class Exporter extends TimerTask {
    private boolean cancel = false;
    private DataServer videoServer = new DataServer();
    private DataServer audioServer = new DataServer();
    private long stamp = System.currentTimeMillis();
    private long count = 0;
    private Frame lastFrame = null;
    public Exporter() {
    }

    public void listen() throws IOException{
        videoServer.listen();
        audioServer.listen();
    }
    public void abort() {
        videoServer.shutdown();
        audioServer.shutdown();
        cancel = true;
    }

    public int getAudioPort() {
        return audioServer.getPort();
    }

    public int getVideoPort() {
        return videoServer.getPort();
    }

    public boolean cancel() {
        boolean retValue = super.cancel();
        cancel = true;
        return retValue;
    }

    @Override
    public void run() {
        
        if (System.currentTimeMillis()-stamp > 1000){
            System.out.println("FPS: " + count);
            count = 0;
            stamp=System.currentTimeMillis();
        }
        Frame frame = MasterMixer.getCurrentFrame();
        if (frame != null && !cancel && frame!=lastFrame) {
            BufferedImage image = frame.getImage();
            if (image != null) {
                int[] imgData = ((java.awt.image.DataBufferInt) image.getRaster().getDataBuffer()).getData();
                byte[] data = new byte[imgData.length * 4];
                int index = 0;
                for (int i = 0; i < imgData.length; i++) {
                    data[index++] = (byte) (imgData[i] >> 24 & 0xFF);
                    data[index++] = (byte) (imgData[i] >> 16 & 0xFF);
                    data[index++] = (byte) (imgData[i] >> 8 & 0xFF);
                    data[index++] = (byte) (imgData[i] >> 0 & 0xFF);
                }
                videoServer.addData(data);
            }
            byte[] audio = frame.getAudioData();
            if (audio != null) {
                audioServer.addData(audio);
            }
            lastFrame=frame;
            count++;
        }
    }
}
    
