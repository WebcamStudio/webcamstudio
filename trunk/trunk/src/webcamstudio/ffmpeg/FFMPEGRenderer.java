/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.ffmpeg;

import java.awt.image.BufferedImage;
import webcamstudio.media.renderer.Capturer;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.media.renderer.Exporter;

/**
 *
 * @author patrick
 */
public class FFMPEGRenderer {

    java.io.DataInput input = null;
    boolean stopMe = false;
    boolean stopped = true;
    int captureWidth = 320;
    int captureHeight = 240;
    int width = 320;
    int height = 240;
    int rate = 15;
    long seek = 0;
    float volume = 1f;
    int opacity = 100;
    int x = 0;
    int y = 0;
    private String file = "/dev/video0";
    private static Properties plugins = null;
    String plugin = "";
    int videoPort = 0;
    int audioPort = 0;
    int frequency = 44100;
    int channels = 2;
    int bitSize = 16;
    int zOrder = 0;
    static String OS = "";
    BufferedImage previewImage = null;
    String uuid = "";

    public FFMPEGRenderer(String uuid, String plugin) {
        this.uuid = uuid;
        if (plugins == null) {
            plugins = new Properties();
            try {
                plugins.load(getResource().openStream());
            } catch (IOException ex) {
                Logger.getLogger(FFMPEGRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.plugin = plugin;
    }

    public BufferedImage getPreview() {
        return previewImage;
    }

    public void setZOrder(int z) {
        zOrder = z;
    }

    public void updateFormat(int x, int y, int width, int height, int opacity, float volume) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.opacity = opacity;
        this.volume = volume;
    }

    public void setOpacity(int o) {
        opacity = o;
    }

    private static URL getResource() throws MalformedURLException {
        File userSettings = new File(new File(System.getProperty("user.home") + "/.webcamstudio"), "ffmpeg-capture.properties");
        URL res = null;
        System.out.println(userSettings.getAbsolutePath());
        if (userSettings.exists()) {
            res = userSettings.toURI().toURL();
        } else {
            OS = System.getProperty("os.name").toLowerCase();
            String path = "/webcamstudio/ffmpeg/ffmpeg-capture_" + OS + ".properties";
            res = FFMPEGRenderer.class.getResource(path);
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
                Logger.getLogger(FFMPEGRenderer.class.getName()).log(Level.SEVERE, null, ex);
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

    public void setVolume(float value) {
        volume = value;
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
                    if (OS.equals("windows")) {
                        command = command.replaceAll(FFMPEGTags.FILE.toString(), "\"" + file + "\"");
                    } else {
                        command = command.replaceAll(FFMPEGTags.FILE.toString(), "" + file.replaceAll(" ", "\\ ") + "");
                    }
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
        stopMe = false;
        new Thread(new Runnable() {

            @Override
            public void run() {
                final Capturer capture = new Capturer(uuid, x, y, captureWidth, captureHeight, rate, opacity, volume);
                videoPort = capture.getVideoPort();
                audioPort = capture.getAudioPort();
                String command = plugins.getProperty(plugin).replaceAll("  ", " "); //Making sure there is no double spaces
                command = command.replaceAll(" ", "=");
                command = setParameters(command);
                final String[] parms = command.split("=");
                try {
                    for (String p : parms) {
                        System.out.print(p + " ");
                    }
                    System.out.println();
                    final Process process = Runtime.getRuntime().exec(parms);
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            while (!stopMe) {
                                capture.setFormat(x, y, width, height, opacity, volume);
                                capture.setZOrder(zOrder);
                                capture.run();
                                previewImage = capture.getPreview();
                                try {
                                    Thread.sleep(1000 / rate);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(FFMPEGRenderer.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            capture.abort();
                            stopped = true;
                            try {
                                byte[] output = new byte[64000];
                                process.getErrorStream().read(output);
                                System.out.println(new String(output).trim());
                                process.destroy();
                                stopMe = true;
                                System.out.println("Process ended");
                            } catch (Exception ex) {
                                Logger.getLogger(FFMPEGRenderer.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    }).start();


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    public void write() {
        stopped = false;
        stopMe = false;
        new Thread(new Runnable() {

            @Override
            public void run() {

                final Exporter renderer = new Exporter();
                videoPort = renderer.getVideoPort();
                audioPort = renderer.getAudioPort();
                String command = plugins.getProperty(plugin).replaceAll("  ", " "); //Making sure there is no double spaces
                command = setParameters(command);
                System.out.println(command);

                final String[] parms = command.split(" ");
                try {
                    final Process process = Runtime.getRuntime().exec(parms);
                    renderer.listen();
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            while (!stopMe) {
                                try {
                                    renderer.run();
                                    Thread.sleep(1);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(FFMPEGRenderer.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            stopped = true;
                            try {
                                byte[] output = new byte[64000];
                                process.getErrorStream().read(output);
                                System.out.println(new String(output).trim());
                                process.destroy();
                                stopMe = true;
                                System.out.println("Process ended");
                            } catch (Exception ex) {
                                Logger.getLogger(FFMPEGRenderer.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    }).start();
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
                Logger.getLogger(FFMPEGRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        stopMe = false;
    }

    public boolean isStopped() {
        return stopped;
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
}
