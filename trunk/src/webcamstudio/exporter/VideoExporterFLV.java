/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.exporter;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.gstreamer.*;

/**
 *
 * @author pballeux
 */
public class VideoExporterFLV extends VideoExporter {

    @Override
    public void startExport() {
        //Video
        Element source = ElementFactory.make("fakesrc", "source");

        Element sink = null;
        if (output!=null){
            sink=ElementFactory.make("filesink", "filesink");
            sink.set("location", output.getAbsolutePath());
            vbitrate = 10000;
        }
        else{
            sink=ElementFactory.make("tcpserversink","tcpserversink");
            sink.set("port", port);
        }

        Element videoRate = ElementFactory.make("videorate", "videorate");
        Element capsRate = ElementFactory.make("capsfilter", "capsrate");
        capsRate.setCaps(Caps.fromString("video/x-raw-yuv,framerate=" + rate + "/2"));


        Element capsSize = ElementFactory.make("capsfilter", "capsSize");
        Caps fltcaps = new Caps("video/x-raw-rgb, framerate=" + rate + "/1" + ", width=" + captureWidth + ", height=" + captureHeight + ", bpp=32, depth=32,red_mask=0x0000FF00,green_mask=0x00FF0000,blue_mask=0xFF000000");
        capsSize.setCaps(fltcaps);

        Element ffmpegColorSpace = ElementFactory.make("ffmpegcolorspace", "ffmpegcolorspace");
        Element muxer = ElementFactory.make("flvmux", "flvmux");
        Element vEncoder = ElementFactory.make("x264enc", "x264enc");
        vEncoder.set("bitrate", vbitrate);
        Element queue = ElementFactory.make("queue", "queue");
        //Audio
        Element audioSource = ElementFactory.make("autoaudiosrc", "autoaudiosrc");
        Element audioCaps = ElementFactory.make("capsfilter", "audiocaps");
        audioCaps.setCaps(Caps.fromString("audio/x-raw-int,rate=22050,channels=2"));
        Element audioConvert = ElementFactory.make("audioconvert", "audioconvert");
        Element aEncoder = ElementFactory.make("lame", "lame");
        aEncoder.set("bitrate", abitrate / 1000);
        Element aqueue = ElementFactory.make("queue", "aqueue");

        pipe = new Pipeline();
        pipe.addMany(source, capsSize, videoRate, capsRate, ffmpegColorSpace, vEncoder, queue, muxer, sink);
        Element.linkMany(source, capsSize, ffmpegColorSpace, videoRate, capsRate, vEncoder, queue, muxer, sink);

        pipe.addMany(audioSource, audioCaps, audioConvert, aEncoder, aqueue);
        Element.linkMany(audioSource, audioCaps, audioConvert, aEncoder, aqueue, muxer);
        source.set("signal-handoffs", true);
        source.set("sizemax", captureWidth * captureHeight * 4);
        source.set("sizetype", 2);
        source.set("sync", true);
        source.set("is-live", true);
        source.set("filltype", 1); // Don't fill the buffer before handoff
        source.connect(new Element.HANDOFF() {

            public void handoff(Element element, org.gstreamer.Buffer buffer, Pad pad) {
                data = ((java.awt.image.DataBufferInt) mixer.getImage().getRaster().getDataBuffer()).getData();
                ByteBuffer bytes = buffer.getByteBuffer();
                IntBuffer b = bytes.asIntBuffer();
                b.put(data);

            }
        });
        source.connect("handoff", new Closure() {

            @SuppressWarnings("unused")
            public void invoke(Element element, Buffer buffer, Pad pad) {
//                        System.out.println("Closure: Element=" + element.getNativeAddress()
//                                + " buffer=" + buffer.getNativeAddress()
//                                + " pad=" + pad.getNativeAddress());
            }
        });

        pipe.getBus().connect(new Bus.ERROR() {

            public void errorMessage(GstObject arg0, int arg1, String arg2) {
                error("FLV Export Error: " + arg0 + "," + arg1 + ", " + arg2);
                System.out.println("FLV Export Error: " + arg0 + "," + arg1 + ", " + arg2);
            }
        });
        pipe.play();



    }

    public VideoExporterFLV(java.io.File outputFile) {
        output = outputFile;
    }
    public VideoExporterFLV(int port) {
        output = null;
        this.port=port;
    }
    public static void main(String[] args) {
        Gst.init();
        webcamstudio.components.Mixer mixer = new webcamstudio.components.Mixer();
        VideoExporterFLV flv = new VideoExporterFLV(new java.io.File("/home/pballeux/Bureau/test.flv"));
        flv.setMixer(mixer);
        flv.startExport();
        System.out.println("Recording for 10 sec");
        try {
            Thread.sleep(10000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        flv.stopExport();
        flv = null;
        System.out.println("Done!");
        System.exit(0);
    }
    private int port = 4888;
}
