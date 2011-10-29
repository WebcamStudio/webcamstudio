/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.ffmpeg;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.components.Mixer;

/**
 *
 * @author patrick
 */
public class FFMPEGEncoder extends FFMPEGAbstract {

    private BufferedOutputStream output = null;
    private Timer timer = new Timer("Encoder",true);
    public FFMPEGEncoder() {
        sourceFormat = "rawvideo";
        sourcePixelFormat = "argb";
        sinkFormat = "";
        sinkQuality = "-qmin 1 -qmax 10";
        sourceInput = "-";
        command = "ffmpeg  -y -f alsa -i pulse";
    }

    public void pushData(int[] data) throws IOException {
        if (output != null) {
            ByteBuffer buffer = ByteBuffer.allocate(data.length * 4);
            IntBuffer iBuffer = buffer.asIntBuffer();
            iBuffer.put(data);
            byte[] bBuffer = buffer.array();
            output.write(bBuffer);
        }
    }

    @Override
    public void read() {
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
        timer = new Timer("Encoder",true);
        timer.scheduleAtFixedRate(new PushData(this), 0, 1000/Mixer.getFPS());

        new Thread(new Runnable() {

            @Override
            public void run() {
                
                stopped = false;
                String[] parms = buildParms();
                for (String p : parms) {
                    System.out.print(p + " ");
                }
                System.out.println();
                try {
                    boolean wait = true;
                    Process process = Runtime.getRuntime().exec(parms);

                    output = new BufferedOutputStream(process.getOutputStream());
                    while (!stopMe) {
                       Thread.sleep(100);
                    }
                    output.write(3);
                    System.out.println("Process exited");
                    process.destroy();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                timer.cancel();
                timer=null;
                stopped = true;
            }
        }).start();

    }

    public static void main(String[] args) {

        String file = "/home/patrick/tmp/test.ogg";
        final FFMPEGEncoder encoder = new FFMPEGEncoder();
        encoder.setCaptureHeight(240);
        encoder.setCaptureWidth(320);
        encoder.setOutput(file);
        final int[] buffer = new int[320 * 240];
        encoder.read();
        new Thread(new Runnable() {

            @Override
            public void run() {
                Mixer m= new Mixer();
                m.setFramerate(15);
                m.setSize(320, 240);
                try {
                    for (int i = 0; i < 100; i++) {
                        try {
                            encoder.pushData(Mixer.getData());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Thread.sleep(100);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(FFMPEGEncoder.class.getName()).log(Level.SEVERE, null, ex);
                }
                encoder.stop();
            }
        }).start();
    }
}
class PushData extends TimerTask{
    FFMPEGEncoder encoder = null;
    public PushData (FFMPEGEncoder encoder){
        this.encoder = encoder;
    }

    @Override
    public void run() {
        try {
            encoder.pushData(Mixer.getData());
        } catch (IOException ex) {
        }
    }
}
