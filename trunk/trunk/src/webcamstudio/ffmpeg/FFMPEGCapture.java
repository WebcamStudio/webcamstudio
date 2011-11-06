/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.ffmpeg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import webcamstudio.media.Image;

/**
 *
 * @author patrick
 */
public class FFMPEGCapture {

    java.io.DataInput input = null;
    boolean stopMe = false;
    boolean stopped = true;
    Image image = null;
    int captureWidth = 320;
    int captureHeight = 240;
    int width = 320;
    int height = 240;
    int rate = 15;
    long seek = 0;
    int volume = 0;
    private String file = "/dev/video0";
    private static Properties plugins = null;
    String plugin = "";
    int videoPort = 0;
    int audioPort = 0;
    int frequency = 44100;
    int channels = 2;
    int bitSize = 16;

    public FFMPEGCapture(String plugin) {
        if (plugins == null) {
            plugins = new Properties();
            try {
                plugins.load(getResource().openStream());
            } catch (IOException ex) {
                Logger.getLogger(FFMPEGCapture.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.plugin = plugin;
    }

    private static URL getResource() throws MalformedURLException {
        File userSettings = new File(new File(System.getProperty("user.home")+"/.webcamstudio"), "ffmpeg-capture.properties");
        URL res = null;
        System.out.println(userSettings.getAbsolutePath());
        if (userSettings.exists()) {
            res = userSettings.toURI().toURL();
        } else {
            String OS = System.getProperty("os.name").toLowerCase();
            String path = "/webcamstudio/ffmpeg/ffmpeg-capture_" + OS + ".properties";
            res = FFMPEGCapture.class.getResource(path);
        }
        System.out.println("Resource Used: " + res.toString());
        return res;
    }

    public static String[] getPlugins() {
        if (plugins == null) {
            plugins = new Properties();
            try {
                plugins.load(getResource().openStream());
            } catch (IOException ex) {
                Logger.getLogger(FFMPEGCapture.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String[] keys = new String[plugins.keySet().size()];
        int index = 0;
        for (Object key : plugins.keySet()) {
            keys[index++] = key.toString();
        }
        return keys;
    }

    public void setCaptureWidth(int w) {
        captureWidth = w;
    }

    public void setCaptureHeight(int h) {
        captureHeight = h;
    }

    public void setVolume(int perc) {
        volume = perc;
    }

    public void setSeek(long sec) {
        seek = sec;
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

    public void setFile(File f) {
        file = f.getAbsolutePath();
    }

    protected void setOpaque(int[] pixels) {
        //BY default, do nothing;
    }

    private String setParameters(String cmd) {
        String command = cmd;
        for (FFMPEGTags tag : FFMPEGTags.values()) {
            switch (tag) {
                case APORT:
                    command = command.replaceAll(FFMPEGTags.APORT.toString(), "" + audioPort);
                    break;
                case CHEIGHT:
                    command = command.replaceAll(FFMPEGTags.CHEIGHT.toString(), "" + captureHeight);
                    break;
                case CWIDTH:
                    command = command.replaceAll(FFMPEGTags.CWIDTH.toString(), "" + captureWidth);
                    break;
                case FILE:
                    command = command.replaceAll(FFMPEGTags.FILE.toString(), "" + file);
                    break;
                case OHEIGHT:
                    command = command.replaceAll(FFMPEGTags.OHEIGHT.toString(), "" + height);
                    break;
                case OWIDTH:
                    command = command.replaceAll(FFMPEGTags.OWIDTH.toString(), "" + width);
                    break;
                case RATE:
                    command = command.replaceAll(FFMPEGTags.RATE.toString(), "" + rate);
                    break;
                case SEEK:
                    command = command.replaceAll(FFMPEGTags.SEEK.toString(), "" + seek);
                    break;
                case VOLUME:
                    command = command.replaceAll(FFMPEGTags.VOLUME.toString(), "" + volume);
                    break;
                case VPORT:
                    command = command.replaceAll(FFMPEGTags.VPORT.toString(), "" + videoPort);
                    break;
                case FREQ:
                    command = command.replaceAll(FFMPEGTags.FREQ.toString(), "" + frequency);
                    break;
                case BITSIZE:
                    command = command.replaceAll(FFMPEGTags.BITSIZE.toString(), "" + bitSize);
                    break;
                case CHANNELS:
                    command = command.replaceAll(FFMPEGTags.CHANNELS.toString(), "" + channels);
                    break;
            }
        }
        return command;
    }

    public void read() {
        stopped = false;
        stopMe=false;
        new Thread(new Runnable() {

            @Override
            public void run() {
                final TCPServer imgListen = new TCPServer(width, height);
                videoPort = imgListen.getVideoPort();
                audioPort = imgListen.getAudioPort();
                String command = plugins.getProperty(plugin).replaceAll("  ", " "); //Making sure there is no double spaces
                command = setParameters(command);
                System.out.println(command);
                final String[] parms = command.split(" ");
                try {
                    Process process = Runtime.getRuntime().exec(parms);
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            while (!stopMe) {
                                image = imgListen.getImage();
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(FFMPEGCapture.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            imgListen.shutdown();
                            stopped = true;
                        }
                    }).start();
                    process.waitFor();
                    byte[] output = new byte[64000];
                    process.getErrorStream().read(output);
                    System.out.println(new String(output).trim());
                    stopMe = true;
                    System.out.println("Process ended");
                    process.destroy();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    public void stop() {
        stopMe = true;
        while (!stopped) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(FFMPEGCapture.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        stopMe = false;
    }

    public boolean isStopped() {
        return stopped;
    }

    public Image getImage() {
        return image;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getBitSize() {
        return bitSize;
    }

    public int getChannels() {
        return channels;
    }

    public static void main(String[] args) {
        FFMPEGCapture f = new FFMPEGCapture("movie");
        f.setFile(new File("/home/patrick/Desktop/Howfast.ogg"));
        f.read();
        java.util.Vector<Image> list = new java.util.Vector<Image>();
        AudioFormat format = new AudioFormat(f.getFrequency(), f.getBitSize(), f.getChannels(), true, true);
        System.out.println("Frame Size: " + format.getFrameSize());
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        try {
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open();
            speaker.start();
            
            while (!f.isStopped()) {
                if (f.getImage() != null) {
                    byte[] data = f.getImage().getSound();
                    if (data != null && data.length > 0) {
                        speaker.write(data, 0,data.length);
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FFMPEGCapture.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            speaker.close();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(TCPAudioListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f.stop();
    }
}
