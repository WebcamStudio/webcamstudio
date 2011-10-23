/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.ffmpeg;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;

/**
 *
 * @author patrick
 */
public abstract class FFMPEGAbstract {

    java.io.DataInput input = null;
    boolean stopMe = true;
    BufferedImage image = null;
    int captureWidth=0;
    int captureHeight=0;
    int width = 320;
    int height = 240;
    int rate = 15;
    String sinkFormat = "rawvideo";
    String sinkOutput = "pipe:1";
    String sourceFormat = "";
    String sourceInput = "/dev/video0 ";
    String sinkAudio = "";
    String command = "ffmpeg";
    long seek = 0;
    int volume = 0;

    
    public void setCaptureWidth(int w){
        captureWidth=w;
    }
    public void setCaptureHeight(int h){
        captureHeight=h;
    }
    public void setVolume(int perc) {
        volume = perc;
    }

    public void setSeek(long sec) {
        seek = sec;
    }

    protected String[] buildParms() {
        String retValue[] = null;
        ArrayList<String> list = new ArrayList<String>();
        
        String[] commands = command.split(" ");
        for (String c : commands){
            list.add(c);
        }
        
        list.add("-re");
        if (captureWidth>0 && captureHeight>0){
            list.add("-s");
            list.add(captureWidth+"x"+captureHeight);
        }
        if (!sourceFormat.isEmpty()) {
            list.add("-f");
            list.add(sourceFormat);
        }
        if (seek > 0) {
            list.add("-ss");
            list.add(seek + "");
        }
        if (volume > 0) {
            list.add("-vol");
            list.add("" + (volume * 256 / 100));
        }
        list.add("-i");
        list.add(sourceInput);

        if (!sinkFormat.isEmpty()) {
            list.add("-f");
            list.add(sinkFormat);
            list.add("-pix_fmt");
            list.add("argb");
        }


        list.add("-s");
        list.add(width + "x" + height);

        list.add("-r");
        list.add("" + rate);
        if (!sinkOutput.isEmpty()) {
            list.add(sinkOutput);
        }


        if (!sinkAudio.isEmpty()) {
            String[] parts = sinkAudio.split(" ");
            for (String s : parts) {
                list.add(s);
            }
        }
        retValue = new String[list.size()];
        for (int i = 0; i < retValue.length; i++) {
            retValue[i] = list.get(i);
        }
        return retValue;
    }

    public void setRate(int r) {
        rate = r;
    }

    public void setWidth(int w) {
        width = w;
    }

    public void setHeight(int h) {
        height = h;
    }

    public void setInput(String input) {
        sourceInput = input;
    }

    protected void setOpaque(int[] pixels){
        //BY default, do nothing;
    }
    public void read() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                stopMe = false;
                String[] parms = buildParms();
                byte[] buffer = new byte[width * height * 4];
                try {
                    Process process = Runtime.getRuntime().exec(parms);
                    if (!sinkOutput.isEmpty()) {
                        input = new DataInputStream(process.getInputStream());
                        input.readFully(buffer);
                        while (!stopMe) {
                            if (buffer[0] != 0) {
                                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                                int[] imgData = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
                                IntBuffer data = ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
                                data.get(imgData);
                                setOpaque(imgData);
                                image = img;
                            } else {
                                image = null;
                            }
                            input.readFully(buffer);
                        }
                    } else {
                        //This is audio only...
                        while (!stopMe) {
                            try {
                                Thread.sleep(500);
                            } catch (Exception e) {
                            }
                        }
                    }
                    process.destroy();

                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println(new String(buffer).trim());
                }
                stopMe = true;
            }
        }).start();
    }

    public void stop() {
        stopMe = true;
    }

    public boolean isStopped() {
        return stopMe;
    }

    public BufferedImage getImage() {
        return image;
    }
}
