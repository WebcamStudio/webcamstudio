/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gstreamer.*;

import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.w3c.dom.Document;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import webcamstudio.io.ReaderListener;
import webcamstudio.io.flv.FlvReader;
import webcamstudio.rtmp.RtmpReader;
import webcamstudio.rtmp.client.ClientOptions;
import webcamstudio.rtmp.client.ClientPipelineFactory;
import webcamstudio.rtmp.client.RtmpClient;

/**
 *
 * @author pballeux
 */
public class VideoExporterFME extends VideoExporter implements ReaderListener {

    private String url = "";
    private String streamName = "";
    private long startTimeStamp = 0;
    private long nbFrame = 0;
    private RtmpReader streamReader = null;
    private String version = "";
    private int port = 48894;
    private ChannelFuture future = null;

    @Override
    public void startExport() {
        //Video
        rate = mixer.getFramerate();
        captureWidth = mixer.getImage().getWidth();
        captureHeight = mixer.getImage().getHeight();
        Element source = ElementFactory.make("fakesrc", "source");

        Element sink = null;

        sink = ElementFactory.make("tcpserversink", "sink");
        sink.set("port", port);

        Element capsSize = ElementFactory.make("capsfilter", "capsSize");
        Caps fltcaps = new Caps("video/x-raw-rgb, framerate=" + rate + "/1" + ", width=" + captureWidth + ", height=" + captureHeight + ", bpp=32, depth=32,red_mask=0x0000FF00,green_mask=0x00FF0000,blue_mask=0xFF000000");
        capsSize.setCaps(fltcaps);

        Element ffmpegColorSpace = ElementFactory.make("ffmpegcolorspace", "ffmpegcolorspace");
        Element muxer = ElementFactory.make("flvmux", "muxer");
        Element vEncoder = ElementFactory.make("ffenc_flv", "encoder");
        vEncoder.set("gop-size", 250);
        //me-cmp=8 mb-cmp=8 dct-algo=3 idct-algo=1  me-range=320 noise-reduction=64  trellis=1
//        vEncoder.set("me-cmp", 8);
//        vEncoder.set("mb-cmp", 8);
//        vEncoder.set("dct-algo", 3);
//        vEncoder.set("idct-algo", 1);
//        vEncoder.set("me-range", 16);
//        vEncoder.set("noise-reduction", 16);
//        vEncoder.set("trellis", 1);
        vEncoder.set("bitrate", vbitrate * 1000);
        Element queue = ElementFactory.make("queue", "queue");
        //Audio
        Element audioSource = ElementFactory.make("autoaudiosrc", "autoaudiosrc");
        Element audioCaps = ElementFactory.make("capsfilter", "audiocaps");
        audioCaps.setCaps(Caps.fromString("audio/x-raw-int,rate=11025,channels=1"));
        Element aEncoder = ElementFactory.make("ffenc_adpcm_swf", "audioenc");
        aEncoder.set("bitrate", abitrate * 1000);
        Element aqueue = ElementFactory.make("queue", "aqueue");

        pipe = new Pipeline();
        pipe.addMany(source, capsSize, ffmpegColorSpace, vEncoder, queue, muxer, sink);
        Element.linkMany(source, capsSize, ffmpegColorSpace, vEncoder, queue, muxer, sink);

        pipe.addMany(audioSource, audioCaps, aEncoder, aqueue);
        Element.linkMany(audioSource, audioCaps, aEncoder, aqueue, muxer);

        source.set("signal-handoffs", true);
        source.set("sizemax", captureWidth * captureHeight * 4);
        source.set("sizetype", 2);
        source.set("sync", true);
        source.set("is-live", true);
        source.set("filltype", 1); // Don't fill the buffer before handoff



        source.connect(new Element.HANDOFF() {

            @Override
            public void handoff(Element element, org.gstreamer.Buffer buffer, Pad pad) {
                nbFrame++;
                data = ((java.awt.image.DataBufferInt) mixer.getImage().getRaster().getDataBuffer()).getData();
                ByteBuffer bytes = buffer.getByteBuffer();
                IntBuffer b = bytes.asIntBuffer();
                b.put(data);
            }
        });


        pipe.getBus().connect(new Bus.ERROR() {

            @Override
            public void errorMessage(GstObject arg0, int arg1, String arg2) {
                error("FME Export Error: " + arg0 + "," + arg1 + ", " + arg2);
                System.out.println("FME Export Error: " + arg0 + "," + arg1 + ", " + arg2);
            }
        });
        pipe.play();
        while (!pipe.isPlaying() && !stopMe) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(VideoExporterFME.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        final ReaderListener readerListener = this;
        new Thread(new Runnable() {

            @Override
            public void run() {
                webcamstudio.rtmp.client.ClientOptions options = new webcamstudio.rtmp.client.ClientOptions();
                options.publishLive();
                if (version.length() > 0) {
                    options.setClientVersionToUse(version.getBytes());
                }
                options.setRtmpe(true);
                String host = url.replaceAll("rtmp://", "").split("/")[0];
                options.setHost(host);
                System.out.println(host);
                String appName = url.replaceAll("rtmp://", "").replaceAll(host + "/", "");
                System.out.println(appName);
                options.setAppName(appName);
                options.setStreamName(streamName);
                streamReader = new FlvReader(port, readerListener);
                options.setReaderToPublish(streamReader);
                options.setSaveAs(null);
                final ClientBootstrap bootstrap = getBootstrap(Executors.newCachedThreadPool(), options);
                future = bootstrap.connect(new InetSocketAddress(options.getHost(), options.getPort()));
                future.awaitUninterruptibly();
                if (!future.isSuccess()) {
                    future.getCause().printStackTrace();
                    stop();
                }
                if (future != null) {
                    future.getChannel().getCloseFuture().awaitUninterruptibly();
                }
                bootstrap.getFactory().releaseExternalResources();

            }
        }).start();


    }

    private ClientBootstrap getBootstrap(final Executor executor, final ClientOptions options) {
        final ChannelFactory factory = new NioClientSocketChannelFactory(executor, executor);
        final ClientBootstrap bootstrap = new ClientBootstrap(factory);
        bootstrap.setPipelineFactory(new ClientPipelineFactory(options));
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        return bootstrap;
    }

    public void loadFMEXML(File xml) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(xml);
        NodeList fmeEncode = doc.getElementsByTagName("encode");

        Node fstNode = fmeEncode.item(0);
        if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
            org.w3c.dom.Element fstElmnt = (org.w3c.dom.Element) fstNode;
            NodeList videoLst = fstElmnt.getElementsByTagName("video");
            org.w3c.dom.Element videoElementLst = (org.w3c.dom.Element) videoLst.item(0);
            vbitrate = new Integer(videoElementLst.getElementsByTagName("datarate").item(0).getTextContent().split(";")[0]);
            System.out.println(videoElementLst.getElementsByTagName("datarate").item(0).getTextContent());
            NodeList audioLst = fstElmnt.getElementsByTagName("audio");
            org.w3c.dom.Element audioElementLst = (org.w3c.dom.Element) audioLst.item(0);
            abitrate = new Integer(audioElementLst.getElementsByTagName("datarate").item(0).getTextContent());
            System.out.println(audioElementLst.getElementsByTagName("datarate").item(0).getTextContent());

        }


        NodeList fmeOutput = doc.getElementsByTagName("output");
        fstNode = fmeOutput.item(0);
        if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
            org.w3c.dom.Element fstElmnt = (org.w3c.dom.Element) fstNode;
            NodeList urlLst = fstElmnt.getElementsByTagName("rtmp");
            org.w3c.dom.Element urlElementLst = (org.w3c.dom.Element) urlLst.item(0);
            url = urlElementLst.getElementsByTagName("url").item(0).getTextContent();
            System.out.println(urlElementLst.getElementsByTagName("url").item(0).getTextContent());
            streamName = urlElementLst.getElementsByTagName("stream").item(0).getTextContent();
            System.out.println(urlElementLst.getElementsByTagName("stream").item(0).getTextContent());
            version = "";
            if (urlElementLst.getElementsByTagName("version").getLength() > 0) {
                version = urlElementLst.getElementsByTagName("version").item(0).getTextContent();
                System.out.println(urlElementLst.getElementsByTagName("version").item(0).getTextContent());
            }
        }
    }

    public boolean isPlaying() {
        if (pipe != null) {
            return pipe.isPlaying();
        } else {
            return false;
        }
    }
    private boolean stopMe = false;

    public void stop() {
        stopMe = true;
        System.out.println("Stopping...");
        if (future != null) {
            future.setSuccess();
            future = null;
        }

        if (pipe != null) {
            pipe.stop();
            pipe = null;
        }
    }

    public VideoExporterFME(int gstPort) {
        port = gstPort;
    }

    @Override
    public void stopped() {
        stopMe = true;
        System.out.println("Stopping...");
        if (pipe != null) {
            pipe.stop();

        }
    }
}
