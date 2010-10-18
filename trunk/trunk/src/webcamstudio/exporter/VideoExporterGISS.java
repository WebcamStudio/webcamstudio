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
public class VideoExporterGISS extends VideoExporter {

    //shout2send streamname="$title" description="$descrp" genre="probando..." ip=giss.tv port=8000 password=$pwd mount=$mountpoint
    private String name = "WebcamStudio";
    private String description = "Default Description";
    private String genre = "Live broadcast";
    private String ip = "giss.tv";
    private String port = "8000";
    private String password = "password";
    private String mount = "offical-ws4gl.ogg";


    @Override
    public void startExport() {

        //Video
        Element source = ElementFactory.make("fakesrc", "source");
        Element sink = null;
        sink = ElementFactory.make("shout2send", "shout2send");
        sink.set("streamname",name);
        sink.set("description",getDescription());
        sink.set("genre",getGenre());
        sink.set("ip",getIp());
        sink.set("port",getPort());
        sink.set("password",getPassword());
        sink.set("mount",getMount());
        
        Element videoRate = ElementFactory.make("videorate", "videorate");
        Element capsRate = ElementFactory.make("capsfilter", "capsrate");
        capsRate.setCaps(Caps.fromString("video/x-raw-rgb,framerate=" + rate + "/1"));

        Element capsSize = ElementFactory.make("capsfilter", "capsSize");
        Caps fltcaps = new Caps("video/x-raw-rgb, framerate=" + rate + "/1" + ", width=" + captureWidth + ", height=" + captureHeight + ", bpp=32, depth=32,red_mask=0x0000FF00,green_mask=0x00FF0000,blue_mask=0xFF000000");
        capsSize.setCaps(fltcaps);

        Element ffmpegColorSpace = ElementFactory.make("ffmpegcolorspace", "ffmpegcolorspace");
        Element muxer = ElementFactory.make("oggmux", "oggmux");
        Element vEncoder = ElementFactory.make("theoraenc", "encoder");
        vEncoder.set("bitrate", vbitrate);
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
                error("OGG Export Error: " + arg0 + "," + arg1 + ", " + arg2);
                System.out.println("OGG Export Error: " + arg0 + "," + arg1 + ", " + arg2);
            }
        });
        pipe.getBus().connect(new Bus.INFO() {

            @Override
            public void infoMessage(GstObject arg0, int arg1, String arg2) {
                System.out.println("OGG Export Info: " + arg0 + "," + arg1 + ", " + arg2);
            }
        });
        pipe.play();

    }

    public VideoExporterGISS() {
    }

    public static void main(String[] args) {
        Gst.init();
        webcamstudio.components.Mixer mixer = new webcamstudio.components.Mixer();
        //VideoExporterOGG ogg = new VideoExporterOGG(new java.io.File("/home/pballeux/Bureau/test.ogg"));
        VideoExporterGISS ogg = new VideoExporterGISS();
        ogg.setMixer(mixer);
        ogg.startExport();
        System.out.println("Recording for 100 sec");
        try {
            Thread.sleep(100000000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ogg.stopExport();
        ogg = null;
        System.out.println("Done!");
        System.exit(0);
    }

    public void setName(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the genre
     */
    public String getGenre() {
        return genre;
    }

    /**
     * @param genre the genre to set
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the mount
     */
    public String getMount() {
        return mount;
    }

    /**
     * @param mount the mount to set
     */
    public void setMount(String mount) {
        this.mount = mount;
    }
}
