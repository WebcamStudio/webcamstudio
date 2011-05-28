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
public class VideoExporterMjpeg extends VideoExporter {

    @Override
    public void startExport() {

        //Video
        Element source = ElementFactory.make("fakesrc", "source");
        Element sink = null;
        sink = ElementFactory.make("tcpserversink", "tcpserversink");
        sink.set("port", port);
        System.out.println("Image stream is available on port " + port);
        sink.set("sync-method", 1);
        sink.set("recover-policy", 1);

        Element videoRate = ElementFactory.make("videorate", "videorate");
        Element capsRate = ElementFactory.make("capsfilter", "capsrate");
        capsRate.setCaps(Caps.fromString("video/x-raw-rgb,framerate=" + rate + "/1"));

        Element capsSize = ElementFactory.make("capsfilter", "capsSize");
        Caps fltcaps = new Caps("video/x-raw-rgb, framerate=" + rate + "/1" + ", width=" + captureWidth + ", height=" + captureHeight + ", bpp=32, depth=32,red_mask=0x0000FF00,green_mask=0x00FF0000,blue_mask=0xFF000000");
        capsSize.setCaps(fltcaps);

        Element ffmpegColorSpace = ElementFactory.make("ffmpegcolorspace", "ffmpegcolorspace");
        Element vEncoder = ElementFactory.make("jpegenc", "encoder");
        vEncoder.set("quality", 100);

        pipe = new Pipeline();
        pipe.addMany(source, capsSize, videoRate, capsRate, ffmpegColorSpace, vEncoder, sink);
        Element.linkMany(source, capsSize, videoRate, capsRate, ffmpegColorSpace, vEncoder, sink);

        source.set("signal-handoffs", true);
        source.set("sizemax", captureWidth * captureHeight * 4);
        source.set("sizetype", 2);
        source.set("sync", true);
        source.set("is-live", true);
        source.set("filltype", 1); // Don't fill the buffer before handoff
        source.connect(new Element.HANDOFF() {

            @Override
            public void handoff(Element element, org.gstreamer.Buffer buffer, Pad pad) {
                if (mixer.getImage() != null) {
                    data = ((java.awt.image.DataBufferInt) mixer.getImage().getRaster().getDataBuffer()).getData();
                    ByteBuffer bytes = buffer.getByteBuffer();
                    IntBuffer b = bytes.asIntBuffer();
                    buffer.setDuration(ClockTime.fromMillis(1000 / rate));
                    b.put(data);
                }
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
                error("MJPEG Export Error: " + arg0 + "," + arg1 + ", " + arg2);
                System.out.println("MJPEG Export Error: " + arg0 + "," + arg1 + ", " + arg2);
            }
        });
        pipe.getBus().connect(new Bus.INFO() {

            @Override
            public void infoMessage(GstObject arg0, int arg1, String arg2) {
                System.out.println("MJPEG Export Info: " + arg0 + "," + arg1 + ", " + arg2);
            }
        });
        pipe.play();

    }

    public VideoExporterMjpeg(java.io.File outputFile) {
        output = outputFile;
    }

    public VideoExporterMjpeg(int port) {
        output = null;
        this.port = port;
    }

    public static void main(String[] args) {
        Gst.init();
        webcamstudio.components.Mixer mixer = new webcamstudio.components.Mixer();
        //VideoExporterMJPEG MJPEG = new VideoExporterMJPEG(new java.io.File("/home/pballeux/Bureau/test.MJPEG"));
        VideoExporterMjpeg MJPEG = new VideoExporterMjpeg(null);
        MJPEG.setMixer(mixer);
        MJPEG.startExport();
        System.out.println("Recording for 100 sec");
        try {
            Thread.sleep(100000000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        MJPEG.stopExport();
        MJPEG = null;
        System.out.println("Done!");
        System.exit(0);
    }
    private int port = 4888;
}
