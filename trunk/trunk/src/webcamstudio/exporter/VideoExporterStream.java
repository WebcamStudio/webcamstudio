/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.exporter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gstreamer.*;
import webcamstudio.components.Mixer;

/**
 *
 * @author pballeux
 */
public class VideoExporterStream extends VideoExporter {

    private java.net.ServerSocket streamServer = null;
    private int port = 4888;
    public java.util.Vector<byte[]> dataStream = new java.util.Vector<byte[]>();

    public void stop() {
        stopMe = true;
    }

    public void listen() throws IOException {
        if (streamServer != null) {
            streamServer.close();
            streamServer = null;
        }
        streamServer = new ServerSocket(port);
        streamServer.setSoTimeout(1000);

        System.out.println("NetworkStream available on port " + port);
        System.out.println("You can use FFMPEG to connect to this stream...");
        System.out.println("ffmpeg -f ogg -i tcp://127.0.0.1:" + port + " test.ogg");
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stopMe) {
                    try {
                        final Socket connection = streamServer.accept();
                        System.out.println("Accepting connection from " + connection.getLocalSocketAddress().toString());
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                feedConnection(connection);
                            }
                        }).start();
                    } catch (java.net.SocketTimeoutException ex) {
                        continue;
                    } catch (IOException ex) {
                        //Logger.getLogger(VideoExporterStream.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }

    @Override
    public void startExport() {
        dataStream = new java.util.Vector<byte[]>();
        stopMe = false;
        //Video
        Element source = ElementFactory.make("fakesrc", "source");
        Element sink = ElementFactory.make("fakesink", "sink");

        Element videoRate = ElementFactory.make("videorate", "videorate");
        Element capsRate = ElementFactory.make("capsfilter", "capsrate");
        capsRate.setCaps(Caps.fromString("video/x-raw-rgb,framerate=" + rate + "/1"));

        Element capsSize = ElementFactory.make("capsfilter", "capsSize");
        Caps fltcaps = new Caps("video/x-raw-rgb, framerate=" + rate + "/1" + ", width=" + captureWidth + ", height=" + captureHeight + ", bpp=32, depth=32,red_mask=0x0000FF00,green_mask=0x00FF0000,blue_mask=0xFF000000");
        capsSize.setCaps(fltcaps);

        Element ffmpegColorSpace = ElementFactory.make("ffmpegcolorspace", "ffmpegcolorspace");
        Element muxer = ElementFactory.make("oggmux", "oggmux");
        Element vEncoder = ElementFactory.make("theoraenc", "encoder");
        vEncoder.set("quality", 63);
        Element queue = ElementFactory.make("queue", "queue");
        //Audio
        Element audioSource = ElementFactory.make("autoaudiosrc", "autoaudiosrc");
        Element audioCaps = ElementFactory.make("capsfilter", "audiocaps");
        audioCaps.setCaps(Caps.fromString("audio/x-raw-int,rate=44100,channels=2"));
        Element audioConvert = ElementFactory.make("audioconvert", "audioconvert");
        Element aEncoder = ElementFactory.make("vorbisenc", "vorbisenc");
        aEncoder.set("bitrate", abitrate);
        Element aqueue = ElementFactory.make("queue", "aqueue");

        pipe = new Pipeline();
        pipe.addMany(source, capsSize, videoRate, capsRate, ffmpegColorSpace, vEncoder, queue, muxer, sink);
        Element.linkMany(source, capsSize, videoRate, capsRate, ffmpegColorSpace, vEncoder, queue, muxer, sink);

        pipe.addMany(audioSource, audioCaps, audioConvert, aEncoder, aqueue);
        Element.linkMany(audioSource, audioCaps, audioConvert, aEncoder, aqueue, muxer);
        source.set("signal-handoffs", true);
        source.set("sizemax", captureWidth * captureHeight * 4);
        source.set("sizetype", 2);
        source.set("sync", true);
        source.set("is-live", true);
        source.set("filltype", 1); // Don't fill the buffer before handoff
        source.connect(new Element.HANDOFF() {

            @Override
            public void handoff(Element element, org.gstreamer.Buffer buffer, Pad pad) {
                data = ((java.awt.image.DataBufferInt) mixer.getImage().getRaster().getDataBuffer()).getData();
                ByteBuffer bytes = buffer.getByteBuffer();
                IntBuffer b = bytes.asIntBuffer();
                buffer.setDuration(ClockTime.fromMillis(1000 / rate));
                b.put(data);
            }
        });
        source.set("signal-handoffs", true);
        source.set("sizemax", captureWidth * captureHeight * 4);
        source.set("sizetype", 2);
        source.set("sync", true);
        source.set("is-live", true);
        source.set("filltype", 1); // Don't fill the buffer before handoff
        source.connect(new Element.HANDOFF() {

            @Override
            public void handoff(Element element, org.gstreamer.Buffer buffer, Pad pad) {
                data = ((java.awt.image.DataBufferInt) mixer.getImage().getRaster().getDataBuffer()).getData();
                ByteBuffer bytes = buffer.getByteBuffer();
                IntBuffer b = bytes.asIntBuffer();
                buffer.setDuration(ClockTime.fromMillis(1000 / rate));
                b.put(data);
            }
        });
        sink.connect("handoff", new Closure() {

            @SuppressWarnings("unused")
            public void invoke(Element element, Buffer buffer, Pad pad) {
//                        System.out.println("Closure: Element=" + element.getNativeAddress()
//                                + " buffer=" + buffer.getNativeAddress()
//                                + " pad=" + pad.getNativeAddress());
            }
        });
        sink.set("signal-handoffs", true);
        sink.connect(new Element.HANDOFF() {

            @Override
            public void handoff(Element element, org.gstreamer.Buffer buffer, Pad pad) {
                byte[] data = new byte[buffer.getSize()];
                buffer.getByteBuffer().get(data);
                if (dataStream != null) {
                    dataStream.add(data);
                    while (dataStream.size() > 50) {
                        dataStream.remove(0);
                    }
                }
            }
        });
        pipe.getBus().connect(new Bus.ERROR() {

            public void errorMessage(GstObject arg0, int arg1, String arg2) {
                error("OGG Export Error: " + arg0 + "," + arg1 + ", " + arg2);
                System.out.println("Stream Export Error: " + arg0 + "," + arg1 + ", " + arg2);
            }
        });
        pipe.getBus().connect(new Bus.INFO() {

            @Override
            public void infoMessage(GstObject arg0, int arg1, String arg2) {
                System.out.println("Stream Export Info: " + arg0 + "," + arg1 + ", " + arg2);
            }
        });
        pipe.play();
        try {
            listen();
        } catch (IOException ex) {
            Logger.getLogger(VideoExporterStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stopExport() {
        stopMe = true;
        super.stopExport();
        try {
            if (streamServer != null && streamServer.isBound()) {
                streamServer.close();
            }
            streamServer = null;
        } catch (IOException ex) {
            Logger.getLogger(VideoExporterStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void feedConnection(Socket conn) {
        java.io.BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(conn.getOutputStream());
            while (!stopMe) {
                if (dataStream.size() > 0) {
                    byte[] data = dataStream.remove(0);
                    out.write(data);
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(VideoExporterStream.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("Lost connection");
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(VideoExporterStream.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public VideoExporterStream(int port, Mixer mixer) {
        this.port = port;
        setMixer(mixer);
    }

    public static void main(String[] args) {
        Gst.init();
        webcamstudio.components.Mixer mixer = new webcamstudio.components.Mixer();
        VideoExporterStream stream = new VideoExporterStream(4888, mixer);
        stream.startExport();
        System.out.println("Recording for 100 sec");
        try {
            Thread.sleep(100000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        stream.stopExport();
        stream = null;
        System.out.println("Done!");
        System.exit(0);
    }
}
