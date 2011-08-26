/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.exporter;

import com.sun.jna.Pointer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gstreamer.*;
import org.gstreamer.elements.AppSink;
import org.gstreamer.elements.AppSrc;
import webcamstudio.components.Mixer;

/**
 *
 * @author pballeux
 */
public class VideoExporterStream {

    private java.net.ServerSocket streamServer = null;
    private int port = 4888;
    private boolean stopMe = false;
    private Mixer mixer = null;
    private Pipeline pipe = null;
    private int rate = 15;
    private int captureWidth = 320;
    private int captureHeight = 240;
    private int abitrate = 128000;
    private org.gstreamer.elements.AppSink sink = null;
    private org.gstreamer.elements.AppSrc source = null;
    private int frameCount = 0;
    private boolean feedPipe = false;

    public void stop() {
        stopMe = true;
    }

    public void listen() throws IOException {
        System.out.println("NetworkStream available on port " + port);
        System.out.println("You can use FFMPEG to connect to this stream...");
        System.out.println("ffmpeg -f ogg -i tcp://127.0.0.1:" + port + " test.ogg");

        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stopMe) {
                    try {
                        if (streamServer == null) {
                            streamServer = new ServerSocket(port);
                            streamServer.setSoTimeout(1000);
                        }

                        Socket connection = streamServer.accept();
                        System.out.println("Accepting connection from " + connection.getLocalSocketAddress().toString());
                        streamServer.close();
                        streamServer = null;

                        startExport();
                        feedConnection(connection);
                        stopExport();

                    } catch (java.net.SocketTimeoutException ex) {
                        continue;
                    } catch (IOException ex) {
                        Logger.getLogger(VideoExporterStream.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }

    private void startExport() {
        rate = mixer.getFramerate();
        captureWidth = mixer.getWidth();
        captureHeight = mixer.getHeight();
        String pipeline = "";
        // FLV Stream...
        //v4l2src ! stamp sync-margin=1 sync-interval=1 ! videorate ! video/x-raw-yuv,width=320,height=240,framerate=20/1 ! ffmpegcolorspace ! x264enc bitrate=368 subme=4 b-pyramid=true weightb=true ! queue2 ! flvmux name=mux alsasrc device=hw:0,0     ! audio/x-raw-int,rate=22050,channels=2,depth=16     ! audiorate     ! queue2 max-size-buffers=600 max-size-bytes=0 max-size-time=0     ! audioconvert     ! lamemp3enc target=1 bitrate=40 mono=true encoding-engine-quality=1 target=bitrate     ! mux. mux.     ! queue2     ! filesink location=outfile.flv sync=false
        pipeline += "ffmpegcolorspace name=conv ! videorate ! video/x-raw-yuv,framerate=" + rate + "/1 ! x264enc speed-preset=slow name=enc  ! flvmux name=mux";
        pipeline += " queue ! autoaudiosrc ! audioconvert ! lame ! mux.";

        pipe = Pipeline.launch(pipeline);
        source = (AppSrc) ElementFactory.make("appsrc", "source");
        Element srcfilter = ElementFactory.make("capsfilter", "srcfilter");
        Caps fltcaps = new Caps("video/x-raw-rgb, framerate=" + rate + "/1" + ", width=" + captureWidth + ", height=" + captureHeight + ", bpp=32, depth=32,endianness=4321,red_mask=0x0000FF00,green_mask=0x00FF0000,blue_mask=0xFF000000");
        srcfilter.setCaps(fltcaps);
        source.set("emit-signals", true);
        source.setLive(true);
        sink = (AppSink) ElementFactory.make("appsink", "sink");
        pipe.addMany(source, srcfilter, sink);
        Element mux = pipe.getElementByName("mux");
        Element conv = pipe.getElementByName("conv");
        mux.link(sink);
        source.link(srcfilter);
        srcfilter.link(conv);
        frameCount = 0;
        source.connect(new AppSrc.NEED_DATA() {

            public void needData(Element elem, int size, Pointer userData) {
                int[] pixels = mixer.getRawImageDate();
                if (pixels != null) {
                    Buffer buffer = new Buffer(pixels.length * 4);
                    buffer.getByteBuffer().asIntBuffer().put(pixels);
                    //buffer.setDuration(ClockTime.fromMillis(1000 / rate));
                    //buffer.setTimestamp(ClockTime.fromMillis(System.currentTimeMillis()));
                    source.pushBuffer(buffer);
                    buffer = null;
                }
            }
        });
        source.connect(new AppSrc.ENOUGH_DATA() {

            public void enoughData(Element elem, Pointer userData) {
                //System.out.println("Frame sent " + frameCount);
            }
        });
        pipe.getBus().connect(new Bus.ERROR() {

            public void errorMessage(GstObject arg0, int arg1, String arg2) {
                System.out.println("Stream Export Error: " + arg0 + "," + arg1 + ", " + arg2 + " - Frame Count = " + frameCount);
            }
        });
        pipe.getBus().connect(new Bus.INFO() {

            @Override
            public void infoMessage(GstObject arg0, int arg1, String arg2) {
                System.out.println("Stream Export Info: " + arg0 + "," + arg1 + ", " + arg2);
            }
        });
        System.out.println("Starting pipe");
        feedPipe = false;
        pipe.play();

        frameCount = 0;
    }

    private void stopExport() {
        if (pipe != null) {
            pipe.stop();
            pipe = null;
        }
    }

    private void feedConnection(Socket conn) throws IOException {
        java.io.OutputStream out = null;
        boolean stopFeeding = false;
        byte[] buffer = new byte[4048];
        try {
            out = conn.getOutputStream();
            while (!stopFeeding) {
                org.gstreamer.Buffer b = sink.pullBuffer();
                if (b != null) {
                    buffer = new byte[b.getSize()];
                    ByteBuffer bb = b.getByteBuffer();
                    bb.get(buffer);
                    out.write(buffer);
                }
            }
        } catch (IOException ex) {
            System.out.println("Lost connection ");
            stopFeeding = true;
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
        this.mixer = mixer;
        rate = mixer.getFramerate();
        captureWidth = mixer.getWidth();
        captureHeight = mixer.getHeight();
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
