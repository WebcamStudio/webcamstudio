/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author patrick
 */
public class NetworkStream {

    private Mixer mixer = null;
    private MixerWin32 mixerWin32 = null;
    private int imagePort = 4888;
    private int audioPort = 4999;
    private java.net.ServerSocket imageServer = null;
    private java.net.ServerSocket audioServer = null;
    private java.net.Socket imageConnection = null;
    private java.net.Socket audioConnection = null;
    private boolean stopMe = false;
    private FrameAudio currentFrameAudio = null;
    private java.util.Vector<FrameAudio> streamAudio = new java.util.Vector<FrameAudio>();
    private java.util.Vector<FrameImage> streamVideo = new java.util.Vector<FrameImage>();
    private boolean stopConnection = false;

    public NetworkStream(Mixer m) {
        mixer = m;
        feedAudioBuffer();
        feedImageBuffer();

    }

    public NetworkStream(MixerWin32 m) {
        mixerWin32 = m;
        feedAudioBuffer();
        feedImageBuffer();

    }

    private void feedAudioBuffer() {
        try {

            final AudioFormat format = getFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);

            line.open(format);
            line.start();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    byte[] buffer = null;
                    byte[] data = null;
                    int bufferSize = (int) format.getSampleRate(); // * format.getFrameSize();
                    while (!stopMe) {
                        buffer = new byte[bufferSize];
                        int count = line.read(buffer, 0, buffer.length);
                        FrameImage[] images = new FrameImage[streamVideo.size()];
                        for (int i = 0; i < images.length; i++) {
                            images[i] = streamVideo.get(0);
                        }
                        streamAudio.add(new FrameAudio(buffer, count, images));
                        streamVideo.clear();
                        if (streamAudio.size() > 1) {
                            streamAudio.remove(0);
                        }
                    }

                }
            }).start();
        } catch (Exception e) {
        }
    }

    private void feedImageBuffer() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                byte[] buffer = null;
                while (!stopMe) {
                    int[] data = null;
                    if (mixer != null) {
                        data = ((java.awt.image.DataBufferInt) mixer.getImage().getRaster().getDataBuffer()).getData();
                    } else if (mixerWin32 != null) {
                        data = ((java.awt.image.DataBufferInt) mixerWin32.getImage().getRaster().getDataBuffer()).getData();
                    }
                    ByteBuffer bytes = ByteBuffer.allocate(data.length * 4);
                    bytes.asIntBuffer().put(data);
                    buffer = bytes.array();
                    streamVideo.add(new FrameImage(buffer));

                    try {
                        Thread.sleep(1000 / 30);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(NetworkStream.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        }).start();
    }

    public void start() throws IOException {
        if (imageServer != null) {
            imageServer.close();
            imageServer = null;
        }
        if (audioServer != null) {
            audioServer.close();
            audioServer = null;
        }
        imageServer = new ServerSocket(imagePort);
        imageServer.setSoTimeout(1000);
        audioServer = new ServerSocket(audioPort);
        audioServer.setSoTimeout(1000);

        System.out.println("NetworkStream available on port " + imagePort);
        System.out.println("You can use FFMPEG to connect to this stream...");
        System.out.println("ffmpeg -pix_fmt argb  -f rawvideo -s " + mixer.getWidth() + "x" + mixer.getHeight() + " -itsoffset 2 -i tcp://127.0.0.1:" + imagePort + "  -f alsa -i pulse  -ac 1 -ab 64kb -ar 22050 -r 15 test.flv");
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stopMe) {
                    try {
                        imageConnection = imageServer.accept();
                        System.out.println("Accepting Video connection from " + imageConnection.getLocalSocketAddress().toString());
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                feedConnection();
                            }
                        }).start();
                    } catch (java.net.SocketTimeoutException ex) {
                        continue;
                    } catch (IOException ex) {
                        Logger.getLogger(NetworkStream.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stopMe) {
                    try {
                        audioConnection = audioServer.accept();
                        System.out.println("Accepting Audio connection from " + audioConnection.getLocalSocketAddress().toString());
                    } catch (java.net.SocketTimeoutException ex) {
                        continue;
                    } catch (IOException ex) {
                        Logger.getLogger(NetworkStream.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }

    public void stop() {
        stopMe = true;
    }

    private void feedConnection() {
        stopConnection=false;
        while (imageConnection == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(NetworkStream.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        while (!stopConnection) {
            while (streamAudio.isEmpty()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(NetworkStream.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            final FrameAudio f = streamAudio.remove(0);
            if (f != null) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 0; i < f.images.length && !stopConnection; i++) {
                            try {
                                imageConnection.getOutputStream().write(f.images[i].data);
                                try {
                                    Thread.sleep(1000 / 30);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(NetworkStream.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } catch (IOException ex) {
                                System.out.println("Video Connection lost");
                                stopConnection = true;
                                break;
                            }
                        }
                        System.out.println("Finished sending images packet");
                    }
                }).start();

                while (audioConnection == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(NetworkStream.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    while (audioConnection.getOutputStream() == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(NetworkStream.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(NetworkStream.class.getName()).log(Level.SEVERE, null, ex);
                }
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // Sending audio
                        if (!stopConnection) {
                            try {
                                audioConnection.getOutputStream().write(f.data, 0, f.length);
                            } catch (IOException ex) {
                                System.out.println("Audio Connection lost");
                                stopConnection = true;
                            }
                        }
                    }
                }).start();
            }
        }
        try {
            imageConnection.close();
        } catch (IOException ex) {
            Logger.getLogger(NetworkStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            audioConnection.close();
        } catch (IOException ex) {
            Logger.getLogger(NetworkStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        imageConnection = null;
        audioConnection = null;
    }

    private AudioFormat getFormat() {
        float sampleRate = 22050;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}

class FrameImage {

    public FrameImage(byte[] d) {
        data = d;
    }
    byte[] data = null;
}

class FrameAudio {

    public FrameAudio(byte[] d, int l, FrameImage[] imgs) {
        data = d;
        length = l;
        images = imgs;
    }
    byte[] data = null;
    int length = 0;
    FrameImage[] images = null;
}
