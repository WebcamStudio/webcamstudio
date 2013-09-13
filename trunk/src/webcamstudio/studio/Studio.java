/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.studio;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import webcamstudio.WebcamStudio;
import webcamstudio.channels.MasterChannels;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.streams.*;
import webcamstudio.components.*;
import webcamstudio.channels.transitions.Transition;
import webcamstudio.externals.ProcessRenderer;
import webcamstudio.sources.effects.*;
import static webcamstudio.streams.SourceText.Shape.NONE;
import static webcamstudio.streams.SourceText.Shape.OVAL;
import static webcamstudio.streams.SourceText.Shape.RECTANGLE;
import static webcamstudio.streams.SourceText.Shape.ROUNDRECT;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class Studio {

    private ArrayList<String> channels = MasterChannels.getInstance().getChannels();
    private ArrayList<Integer> Durations = ChannelPanel.CHTimers;
    private ArrayList<String> nextChannel = ChannelPanel.CHCurrNext;
    ArrayList<Stream> streams = MasterChannels.getInstance().getStreams();
    Stream streamC = null;
    private static final String ELEMENT_SOURCES = "Sources";
    private static final String ELEMENT_CHANNELS = "Channels";
    private static final String ELEMENT_EFFECTS = "Effects";
    private static final String ELEMENT_SOURCE = "Source";
    private static final String ELEMENT_CHANNEL = "Channel";
    private static final String ELEMENT_ROOT = "WebcamStudio";
    private static final String ELEMENT_MIXER = "Mixer";   
    public static ArrayList<Stream> extstream = new ArrayList<Stream>();
    public static ArrayList<SourceChannel> chanLoad = new ArrayList<SourceChannel>();
    public static File filename;
    public static ArrayList<SourceText> LText = new ArrayList<SourceText>();
    public static ArrayList<String> ImgMovMus = new ArrayList<String>();
    public static ArrayList<String> aGifKeys = new ArrayList<String>();
    static boolean FirstChannel=false;
    
    protected Studio() {
    }
    // Studio removed, put void
    public static void load(File file) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Studio studio = new Studio();
        System.out.println("Loading Studio ...");
        filename = file; 
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        XPath path = XPathFactory.newInstance().newXPath();

        //Loading channels
        NodeList nodeChannels = (NodeList) path.evaluate("/WebcamStudio/Channels/Channel", doc.getDocumentElement(), XPathConstants.NODESET);
        for (int i = 0; i < nodeChannels.getLength(); i++) {
           Node channel = nodeChannels.item(i);
           String name = channel.getAttributes().getNamedItem("name").getTextContent();
           String nxname = channel.getAttributes().getNamedItem("NextChannel").getTextContent();
           String Sduration = channel.getAttributes().getNamedItem("duration").getTextContent();
           int duration = Integer.parseInt(Sduration);
           studio.channels.add(name);
           studio.nextChannel.add(nxname);
           studio.Durations.add(duration);
           System.out.println("Channel: " + name + " - Duration: " + duration);  
        }
        

        // Loading mixer settings
        Node nodeMixer = (Node) path.evaluate("/WebcamStudio/Mixer", doc.getDocumentElement(), XPathConstants.NODE);
        String width = nodeMixer.getAttributes().getNamedItem("width").getTextContent();
        int widthInt = Integer.parseInt(width);
        MasterMixer.getInstance().setWidth(widthInt);
        String height = nodeMixer.getAttributes().getNamedItem("height").getTextContent();
        int heightInt = Integer.parseInt(height);
        MasterMixer.getInstance().setHeight(heightInt);
        String rate = nodeMixer.getAttributes().getNamedItem("rate").getTextContent();
        int rateInt = Integer.parseInt(rate);
        MasterMixer.getInstance().setRate(rateInt);
        System.out.println("Setting Mixer to: " + width + "X" + height + "@" + rate + "fps");
//      return studio;
    }

    public static void save(File file) throws IOException, XMLStreamException, TransformerConfigurationException, TransformerException, IllegalArgumentException, IllegalAccessException {
        ArrayList<String> channels = MasterChannels.getInstance().getChannels();
        ArrayList<Stream> streams = MasterChannels.getInstance().getStreams();
        ArrayList<Integer> Durations = ChannelPanel.CHTimers;
        ArrayList<String> nextChannel = ChannelPanel.CHCurrNext;
        StringWriter writer = new StringWriter();
        System.out.println("Saving Studio ...");

        XMLStreamWriter xml = javax.xml.stream.XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
        xml.writeStartDocument();
        xml.writeStartElement(ELEMENT_ROOT);
        //Channels
        xml.writeStartElement(ELEMENT_CHANNELS);

        for (String c : channels) {
            int index = channels.indexOf(c);
            xml.writeStartElement(ELEMENT_CHANNEL);
            System.out.println("Saving Channel: "+c);
            xml.writeAttribute("name", c);
            xml.writeAttribute("duration", Durations.get(index) + "");
            xml.writeAttribute("NextChannel", nextChannel.get(index) + "");
            xml.writeEndElement();
            
        }
        xml.writeEndElement();
        xml.writeStartElement(ELEMENT_SOURCES);
        for (Stream s : streams) {
            String clazzSink = s.getClass().getCanonicalName();
            if (clazzSink.contains("Sink")){
                System.out.println("Skipping Sink: "+clazzSink);
            } else {
                System.out.println("Saving Stream: "+s.getName());
                xml.writeStartElement(ELEMENT_SOURCE);
                writeObject(s, xml);
                xml.writeEndElement(); // source
            }
        }
        xml.writeEndElement();  //Sources

        xml.writeStartElement(ELEMENT_MIXER);
        xml.writeAttribute("width", MasterMixer.getInstance().getWidth() + "");
        xml.writeAttribute("height", MasterMixer.getInstance().getHeight() + "");
        xml.writeAttribute("rate", MasterMixer.getInstance().getRate() + "");
        xml.writeEndElement(); //Mixer

        xml.writeEndElement(); //WebcamStudio
        xml.writeEndDocument();
        xml.flush();
        xml.close();
        TransformerFactory factory = TransformerFactory.newInstance();

        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
//        StringWriter formattedStringWriter = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(writer.getBuffer().toString())), new StreamResult(file));

    }
    private static void writeObject(Object o, XMLStreamWriter xml) throws IllegalArgumentException, IllegalAccessException, XMLStreamException {

        Field[] fields = o.getClass().getDeclaredFields();
        Field[] superFields = null;
        if (o instanceof Stream) {
            superFields = o.getClass().getSuperclass().getDeclaredFields();
        }
        String clazz = o.getClass().getCanonicalName();
        if (clazz != null) {
            xml.writeAttribute("clazz", clazz);
        }
        if (superFields != null) {
            for (Field f : superFields) {
                f.setAccessible(true);
                String name = f.getName();
                Object value = f.get(o);
                if (value instanceof Integer) {
                    xml.writeAttribute(name, f.getInt(o) + "");
                } else if (value instanceof Float) {
                    xml.writeAttribute(name, f.getFloat(o) + "");
                } else if (value instanceof Boolean) {
                    xml.writeAttribute(name, f.getBoolean(o) + "");
                } 

            }
        }
        for (Field f : fields) {
            f.setAccessible(true);
            String name = f.getName();
            Object value = f.get(o);
            if (value instanceof Integer) {
                xml.writeAttribute(name, f.getInt(o) + "");
            } else if (value instanceof Float) {
                xml.writeAttribute(name, f.getFloat(o) + "");
            } else if (value instanceof Boolean) {
                    xml.writeAttribute(name, f.getBoolean(o) + "");
            } 
        }    
        if (superFields != null) {
            for (Field f : superFields) {
                f.setAccessible(true);
                String name = f.getName();
                Object value = f.get(o);
                if (value instanceof String) {
                    xml.writeStartElement(name);
                    xml.writeCData(value.toString());
                    Tools.sleep(5);
                    xml.writeEndElement();
                } else if (value instanceof File) {
                    xml.writeStartElement(name);
                    xml.writeCData(((File) value).getAbsolutePath());
                    xml.writeEndElement();
                }
            }
        }
        for (Field f : fields) {
            f.setAccessible(true);
            String name = f.getName();
            Object value = f.get(o);
            if (value instanceof String) {
                xml.writeStartElement(name);
                xml.writeCData(value.toString());
                Tools.sleep(5);
                xml.writeEndElement();
            } else if (value instanceof File) {
                xml.writeStartElement(name);
                xml.writeCData(((File) value).getAbsolutePath());
                xml.writeEndElement();
            }
        }
        if (superFields != null) {
            for (Field f : superFields) {
                f.setAccessible(true);
                String name = f.getName();
                Object value = f.get(o);
                if (value instanceof List) { 
                    if ("channels".equals(name)) {
                        xml.writeStartElement(ELEMENT_CHANNELS);
                        for (Object subO : ((List) value)) {
                        if (clazz != null){
                        xml.writeStartElement(name);
                        writeObject(subO, xml);
                        xml.writeEndElement(); 
                        }
                        }
                    } else if ("effects".equals(name)) {
                        xml.writeStartElement(ELEMENT_EFFECTS);
                        for (Object subO : ((List) value)) {
                        if (clazz != null){
                        xml.writeStartElement(name);
                        writeObject(subO, xml);
                        xml.writeEndElement(); 
                        }
                        }
                    } else {
                        xml.writeStartElement(name);
                        for (Object subO : ((List) value)) {
                            if (clazz != null){
                            writeObject(subO, xml);
                            }
                        
                        }
                    }
                    xml.writeEndElement();
                }
            }
        }
        for (Field f : fields) {
            f.setAccessible(true);
            String name = f.getName();
            Object value = f.get(o);
            if (value instanceof List) {
                if ("effects".equals(name)) {
                    xml.writeStartElement(ELEMENT_EFFECTS);
                    for (Object subO : ((List) value)) {
                        if (clazz != null){
                            xml.writeStartElement(name);
                            writeObject(subO, xml);
                            xml.writeEndElement(); 
                        }
                    }
                } else {
                    xml.writeStartElement(name);
                    for (Object subO : ((List) value)) {
                        writeObject(subO, xml);
                    }
                }
                xml.writeEndElement();
            }
        }
    }
   
    private static void readStreams(Document xml) throws IllegalArgumentException, IllegalAccessException, XPathExpressionException {
        XPath path = XPathFactory.newInstance().newXPath();
        NodeList sources = (NodeList) path.evaluate("/" + ELEMENT_ROOT + "/" + ELEMENT_SOURCES + "/" + ELEMENT_SOURCE, xml.getDocumentElement(), XPathConstants.NODESET);
        String videoDev;      
        ArrayList<String> videoDevs = new ArrayList<String>();
        ArrayList<Stream> extstreamBis = new ArrayList<Stream>();
        if (sources != null) {
            for (int i = 0; i < sources.getLength(); i++) {
                Node source = sources.item(i);            
                String clazz = source.getAttributes().getNamedItem("clazz").getTextContent();
                String file = null;
                String ObjText = null;
                String webUrl = null;
                String comm = null;
                String streamTime = null;
                String strShapez = null;
                String chNameDvb = null;
                ArrayList<String> SubChNames = new ArrayList<String>();
                ArrayList<String> SubText = new ArrayList<String>();
                ArrayList<String> SubFont = new ArrayList<String>();
                ArrayList<String> subSTrans = new ArrayList<String>();
                ArrayList<String> subETrans = new ArrayList<String>();
                ArrayList<String> sNames = new ArrayList<String>();
                String sName = null;
//                String subContent = null;
                Stream stream = null;
                SourceChannel sc = null;
                Effect effeX = null;
                ArrayList<SourceChannel> SCL = new ArrayList<SourceChannel>();
                ArrayList<Effect> fXL = new ArrayList<Effect>();
                SourceText text;
                for (int j = 0; j < source.getChildNodes().getLength(); j++) {
                    Node child = source.getChildNodes().item(j);
                    if (child.getNodeName().equals("file")) {                       
                        file = child.getTextContent();
                        ImgMovMus.add(file);
                        if (child.getTextContent().contains("/dev/video")){
                            videoDev = child.getTextContent();
                            videoDevs.add(videoDev);
                        }
                    }
                    if (child.getNodeName().equals("name")) {                       
                        sName = child.getTextContent();
                        sNames.add(sName);
                    }
                    if (child.getNodeName().equals("content")) {
                        ObjText = child.getTextContent();
                    }
                    if (child.getNodeName().equals("strShape")) {
                        strShapez = child.getTextContent();
                    }
                    if (child.getNodeName().equals("webURL")) {
                        webUrl = child.getTextContent();
                    }
                    if (child.getNodeName().equals("chNameDVB")) {
                        chNameDvb = child.getTextContent();
                    }
                    if (child.getNodeName().equals("comm")) {                       
                        comm = child.getTextContent();
                    }
                    if (child.getNodeName().equals("streamTime")) {                       
                        streamTime = child.getTextContent();
                    }
                    if (child.getNodeName().equals("Effects")) { // Read Effects
//                        System.out.println("childnodename: "+child.getNodeName());
                        for (int nc = 0; nc < child.getChildNodes().getLength(); nc++) {
                            Node SuperChild = child.getChildNodes().item(nc);
//                            System.out.println("SuperChildnodename: "+SuperChild.getNodeName());
                            if (SuperChild.getNodeName().equals("effects")) {     
                                String sClazz = SuperChild.getAttributes().getNamedItem("clazz").getTextContent();
//                                System.out.println("effect clazz: "+ sClazz);        
                                if (sClazz.endsWith("ChromaKey")) {
                                    effeX = new ChromaKey();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Blink")) {
                                    effeX = new Blink();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Block")) {
                                    effeX = new Block();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Cartoon")) {
                                    effeX = new Cartoon();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Contrast")) {
                                    effeX = new Contrast();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Edge")) {
                                    effeX = new Edge();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("FlipHorizontal")) {
                                    effeX = new FlipHorizontal();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("FlipVertical")) {
                                    effeX = new FlipVertical();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Gain")) {
                                    effeX = new Gain();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Gray")) {
                                    effeX = new Gray();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("HSB")) {
                                    effeX = new HSB();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Light")) {
                                    effeX = new Light();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("MegaMind")) {
                                    effeX = new MegaMind();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Mirror1")) {
                                    effeX = new Mirror1();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Mirror2")) {
                                    effeX = new Mirror2();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Mirror3")) {
                                    effeX = new Mirror3();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Mirror4")) {
                                    effeX = new Mirror4();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Mosaic")) {
                                    effeX = new Mosaic();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("NoBackground")) {
                                    effeX = new NoBackground();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Opacity")) {
                                    effeX = new Opacity();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Perspective")) {
                                    effeX = new Perspective();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("RGB")) {
                                    effeX = new RGB();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Radar")) {
                                    effeX = new Radar();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Rotation")) {
                                    effeX = new Rotation();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Sphere")) {
                                    effeX = new Sphere();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("SwapRedBlue")) {
                                    effeX = new SwapRedBlue();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("Twirl")) {
                                    effeX = new Twirl();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                } else
                                if (sClazz.endsWith("ZoomZoom")) {
                                    effeX = new ZoomZoom();
                                    readObjectFx(effeX, SuperChild);
                                    fXL.add(effeX);
                                }
                            }
                    }
                    }
                    if (child.getNodeName().equals("Channels")) { // Read SourceChannels
                        for (int nc = 0; nc < child.getChildNodes().getLength(); nc++) {
                            Node SuperChild = child.getChildNodes().item(nc);
                            for (int ncc = 0; ncc < SuperChild.getChildNodes().getLength(); ncc++) {
                                Node SSuperChild = SuperChild.getChildNodes().item(ncc);                        
                                if (SSuperChild.getNodeName().equals("name")) {
                                    SubChNames.add(SSuperChild.getTextContent()); 
                                    sc = new SourceChannel();                                    
                                    readObjectSC(sc, SuperChild);
                                    SCL.add(sc);                                    
                                    chanLoad.add(sc);
                                }  
                                if (SSuperChild.getNodeName().equals("Effects")) {
                                    for (int ncs = 0; ncs < SSuperChild.getChildNodes().getLength(); ncs++) {
                                        Node SSSuperChild = SSuperChild.getChildNodes().item(ncs);            
                                        if (SSSuperChild.getNodeName().equals("effects")) {     
                                            String sClazz = SSSuperChild.getAttributes().getNamedItem("clazz").getTextContent();
//                                            System.out.println("channel effect clazz: "+ sClazz);     
                                            if (sClazz.endsWith("ChromaKey")) {
                                                effeX = new ChromaKey();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Blink")) {
                                                effeX = new Blink();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Block")) {
                                                effeX = new Block();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Cartoon")) {
                                                effeX = new Cartoon();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Contrast")) {
                                                effeX = new Contrast();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Edge")) {
                                                effeX = new Edge();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("FlipHorizontal")) {
                                                effeX = new FlipHorizontal();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("FlipVertical")) {
                                                effeX = new FlipVertical();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Gain")) {
                                                effeX = new Gain();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Gray")) {
                                                effeX = new Gray();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("HSB")) {
                                                effeX = new HSB();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Light")) {
                                                effeX = new Light();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("MegaMind")) {
                                                effeX = new MegaMind();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Mirror1")) {
                                                effeX = new Mirror1();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Mirror2")) {
                                                effeX = new Mirror2();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Mirror3")) {
                                                effeX = new Mirror3();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Mirror4")) {
                                                effeX = new Mirror4();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Mosaic")) {
                                                effeX = new Mosaic();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("NoBackground")) {
                                                effeX = new NoBackground();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Opacity")) {
                                                effeX = new Opacity();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Perspective")) {
                                                effeX = new Perspective();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("RGB")) {
                                                effeX = new RGB();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Radar")) {
                                                effeX = new Radar();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Rotation")) {
                                                effeX = new Rotation();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Sphere")) {
                                                effeX = new Sphere();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("SwapRedBlue")) {
                                                effeX = new SwapRedBlue();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("Twirl")) {
                                                effeX = new Twirl();
                                                readObjectFx(effeX, SSSuperChild);
                                            } else
                                            if (sClazz.endsWith("ZoomZoom")) {
                                                effeX = new ZoomZoom();
                                                readObjectFx(effeX, SSSuperChild);
                                            }
                                            sc.addEffects(effeX);         
                                        }     
                                    }
                                }
                                if (SSuperChild.getNodeName().equals("startTransitions")) {
                                    if (SSuperChild.getAttributes().getLength()!= 0) {
                                        String sClazz = SSuperChild.getAttributes().getNamedItem("clazz").getTextContent();
                                        subSTrans.add(sClazz);
                                    } else {
                                        subSTrans.add("None");                                   
                                    }
                                }  
                                if (SSuperChild.getNodeName().equals("endTransitions")) {
                                    if (SSuperChild.getAttributes().getLength()!= 0) {
                                        String sClazz = SSuperChild.getAttributes().getNamedItem("clazz").getTextContent();
                                        subETrans.add(sClazz);
                                    } else {
                                        subETrans.add("None");                                   
                                    }
                                }  
                                if (SSuperChild.getNodeName().equals("text") && SSuperChild.getTextContent() != null) {
                                    SubText.add(SSuperChild.getTextContent());
                                }  
                                if (SSuperChild.getNodeName().equals("font") && SSuperChild.getTextContent() != null) {
                                    SubFont.add(SSuperChild.getTextContent());
                                }
                            }
                        }
                    }
                }              
                if (file != null) {
                    stream = Stream.getInstance(new File(file));
                    extstream.add(stream); 
                    extstreamBis.add(stream);
                    readObject(stream, source);
                    stream.setComm(comm);
                    for (Effect fx : fXL) {
                        stream.addEffect(fx);
                            }
                    if (streamTime != null){
                        stream.setStreamTime(streamTime);
                    } else {
                        if (stream instanceof SourceMovie || stream instanceof SourceMusic) {
                        Runtime rt = Runtime.getRuntime();
                        String commandDuration = "avconv -i " + "\"" + file + "\"";
                        File fileD=new File(System.getProperty("user.home")+"/.webcamstudio/"+"DurationCalc.sh");
                        FileOutputStream fosD;
                        DataOutputStream dosD = null;
                        try {
                        fosD = new FileOutputStream(fileD);
                        dosD= new DataOutputStream(fosD);
                        } catch (FileNotFoundException ex) {
                        Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                         try {
                         dosD.writeBytes("#!/bin/bash\n");
                         dosD.writeBytes(commandDuration+"\n");
                         } catch (IOException ex) {
                         Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                         }
                         String batchDurationComm = "sh "+System.getProperty("user.home")+"/.webcamstudio/"+"DurationCalc.sh";
                         System.out.println(batchDurationComm);
                         try {
                         Process duration = rt.exec(batchDurationComm);
                         Tools.sleep(10);
                         duration.waitFor(); //Author spoonybard896
                         InputStream lsOut = lsOut = duration.getErrorStream();
                         InputStreamReader isr = new InputStreamReader(lsOut);
                         BufferedReader in = new BufferedReader(isr);
                         String line = "";
                         while ((line = in.readLine()) != null) {
                         if(line.contains("Duration:"))
                         {
                         line = line.replaceFirst("Duration: ", "");
                         line = line.trim();
                         String resu = line.substring(0, 8);
                         String[] temp;
                         temp = resu.split(":");
                         int hours = Integer.parseInt(temp[0]);
                         int minutes = Integer.parseInt(temp[1]);
                         int seconds = Integer.parseInt(temp[2]);
                         int totalTime = (hours*3600)+(minutes*60)+seconds;
                         String strDuration = Integer.toString(totalTime);
                         System.out.println("Duration in Sec: "+strDuration);
                         stream.setStreamTime(strDuration+"s");
                         //                                    break;
                         }
                         } //Author spoonybard896
                         } catch (Exception e) {
                         e.printStackTrace();
                         }
                        }
                    }
                    stream.setLoaded(true);
                    int op=0;
                    for (SourceChannel scs : SCL) {
                        scs.setName(SubChNames.get(op));
//                        String sNamet=SubChNames.get(op);
                        if (!subSTrans.isEmpty()){
                            if (subSTrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeIn".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeIn");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.Translate".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "Translate");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.Resize".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "Resize");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.RevealLeft".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "RevealLeft");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                }
                            }
                        }
                        if (!subETrans.isEmpty()){
                            if (subETrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.ShrinkOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "ShrinkOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                }
                            }
                        }
                        stream.addChannel(scs);
                        op+=1;
                    }
                    stream.setName(sName);
                    SCL.clear();
                    fXL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                } else if (clazz.toLowerCase().endsWith("sourcedesktop")) {
                    stream = new SourceDesktop(); 
                    extstream.add(stream);
                    extstreamBis.add(stream);
                    ImgMovMus.add("Desktop");
                    readObject(stream, source);  
                    for (Effect fx : fXL) {
                        stream.addEffect(fx);
                    }
                    int op=0;
                    for (SourceChannel scs : SCL) {
                        scs.setName(SubChNames.get(op));
//                        String sNamet=SubChNames.get(op);
                        if (!subSTrans.isEmpty()){
                            if (subSTrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeIn".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeIn");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.Translate".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "Translate");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.Resize".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "Resize");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.RevealLeft".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "RevealLeft");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                }
                            }
                        }
                        if (!subETrans.isEmpty()){
                            if (subETrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.ShrinkOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "ShrinkOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                }
                            }
                        }
                        stream.addChannel(scs);                    
                        op+=1;
                    }
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                } else if (clazz.toLowerCase().endsWith("sourcetext")) {
                    text = new SourceText(ObjText);
                    LText.add(text);
                    readObject(text, source);
                    for (Effect fx : fXL) {
                        text.addEffect(fx);
                    }
                    if (strShapez != null) {
                        if (strShapez.equals("none")){
                                text.setBackground(NONE);
                        } else if (strShapez.equals("rectangle")) {
                                text.setBackground(RECTANGLE);
                        } else if (strShapez.equals("oval")) {
                                text.setBackground(OVAL);
                        } else if (strShapez.equals("roundrect")) {
                                text.setBackground(ROUNDRECT);
                        }
                    }
                    int op=0;
                    for (SourceChannel scs : SCL) {
                        scs.setName(SubChNames.get(op));
                        scs.setText(SubText.get(op));
                        scs.setFont(SubFont.get(op));
//                        String sNamet=SubChNames.get(op);
                        if (!subSTrans.isEmpty()){
                            if (subSTrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeIn".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(text, "FadeIn");
                                    text.addStartTransition(t);
                                    scs.startTransitions.add(text.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.Translate".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(text, "Translate");
                                    text.addStartTransition(t);
                                    scs.startTransitions.add(text.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.Resize".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(text, "Resize");
                                    text.addStartTransition(t);
                                    scs.startTransitions.add(text.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.RevealLeft".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(text, "RevealLeft");
                                    text.addStartTransition(t);
                                    scs.startTransitions.add(text.startTransitions.get(0));
                                }
                            }
                        }
                        if (!subETrans.isEmpty()){
                            if (subETrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(text, "FadeOut");
                                    text.addEndTransition(t);
                                    scs.endTransitions.add(text.endTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.ShrinkOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(text, "ShrinkOut");
                                    text.addEndTransition(t);
                                    scs.endTransitions.add(text.endTransitions.get(0));
                                }
                            }
                        }
                        text.addChannel(scs);                    
                        op+=1;
                    }
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                } else if (clazz.toLowerCase().endsWith("sourceqrcode")) {
                    stream = new SourceQRCode(ObjText);
                    extstream.add(stream); 
                    extstreamBis.add(stream);
                    ImgMovMus.add("QRcode");
                    readObject(stream, source);
                    for (Effect fx : fXL) {
                        stream.addEffect(fx);
                    }
                    int op=0;
                    for (SourceChannel scs : SCL) {
                        scs.setName(SubChNames.get(op));
//                        String sNamet=SubChNames.get(op);
                        if (!subSTrans.isEmpty()){
                            if (subSTrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeIn".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeIn");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.Translate".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "Translate");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.Resize".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "Resize");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.RevealLeft".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "RevealLeft");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                }
                            }
                        }
                        if (!subETrans.isEmpty()){
                            if (subETrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.ShrinkOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "ShrinkOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                }
                            }
                        }
                        stream.addChannel(scs);                    
                        op+=1;
                    }
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                } else if (clazz.toLowerCase().endsWith("sourcedvb")) {
                    stream = new SourceDVB();
                    stream.setChName(chNameDvb);
                    extstream.add(stream); 
                    extstreamBis.add(stream);
                    ImgMovMus.add("DVB-T");
                    readObject(stream, source);
                    for (Effect fx : fXL) {
                        stream.addEffect(fx);
                    }
                    int op=0;
                    for (SourceChannel scs : SCL) {
                        scs.setName(SubChNames.get(op));
//                        String sNamet=SubChNames.get(op);
                        if (!subSTrans.isEmpty()){
                            if (subSTrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeIn".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeIn");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.Translate".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "Translate");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.Resize".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "Resize");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.RevealLeft".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "RevealLeft");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                }
                            }
                        }
                        if (!subETrans.isEmpty()){
                            if (subETrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.ShrinkOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "ShrinkOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                }
                            }
                        }
                        stream.addChannel(scs);                    
                        op+=1;
                    }
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                } else if (clazz.toLowerCase().endsWith("sourceurl")) {
                    stream = new SourceURL();
                    stream.setWebURL(webUrl);
                    extstream.add(stream);
                    extstreamBis.add(stream);
                    ImgMovMus.add("URL");
                    readObject(stream, source);
                    stream.setComm(comm);
                    stream.setLoaded(true);
                    for (Effect fx : fXL) {
                        stream.addEffect(fx);
                    }
                    int op=0;
                    for (SourceChannel scs : SCL) {
                        scs.setName(SubChNames.get(op));
//                        String sNamet=SubChNames.get(op);
                        if (!subSTrans.isEmpty()){
                            if (subSTrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeIn".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeIn");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.Translate".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "Translate");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.Resize".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "Resize");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.RevealLeft".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "RevealLeft");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                }
                            }
                        }
                        if (!subETrans.isEmpty()){
                            if (subETrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.ShrinkOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "ShrinkOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                }
                            }
                        }
                        stream.addChannel(scs);                    
                        op+=1;
                    }
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                } else if (clazz.toLowerCase().endsWith("sourcemicrophone")) {
                    stream = new SourceMicrophone(); 
                    extstream.add(stream);
                    extstreamBis.add(stream);
                    ImgMovMus.add("Mic");
                    readObject(stream, source);
                    int op=0;                    
                    for (SourceChannel scs : SCL) {
                        scs.setName(SubChNames.get(op));
//                        String sNamet=SubChNames.get(op);
                        if (!subSTrans.isEmpty()){
                            if (subSTrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeIn".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeIn");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.Translate".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "Translate");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.Resize".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "Resize");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.RevealLeft".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "RevealLeft");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                }
                            }
                        }
                        if (!subETrans.isEmpty()){
                            if (subETrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                } if ("webcamstudio.channels.transitions.ShrinkOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "ShrinkOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                }
                            }
                        }
                        stream.addChannel(scs);                    
                        op+=1;
                    }
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                } else if (clazz.toLowerCase().endsWith("sourceimagegif")) {
                    for (int an=0;an < webcamstudio.WebcamStudio.cboAnimations.getItemCount(); an++){
                        for (String aKey : sNames){
                            if (aKey == null ? webcamstudio.WebcamStudio.cboAnimations.getItemAt(an).toString() == null : aKey.equals(webcamstudio.WebcamStudio.cboAnimations.getItemAt(an).toString())){
                                String res = webcamstudio.WebcamStudio.animations.getProperty(aKey);
                                URL url = WebcamStudio.class.getResource("/webcamstudio/resources/animations/" + res);
                                stream = new SourceImageGif(aKey, url);
                                extstream.add(stream);
                                extstreamBis.add(stream);
                                ImgMovMus.add("ImageGif");
                                readObject(stream, source);  
                                for (Effect fx : fXL) {
                                    stream.addEffect(fx);
                                }
                                int op=0;
                                for (SourceChannel scs : SCL) {
                                    scs.setName(SubChNames.get(op));
//                                    String sNamet=SubChNames.get(op);
                                    if (!subSTrans.isEmpty()){
                                        if (subSTrans.get(op) != null) {
                                            if ("webcamstudio.channels.transitions.FadeIn".equals(subSTrans.get(op))){
                                                Transition t = Transition.getInstance(stream, "FadeIn");
                                                stream.addStartTransition(t);
                                                scs.startTransitions.add(stream.startTransitions.get(0));
                                                }
                                            } if ("webcamstudio.channels.transitions.Translate".equals(subSTrans.get(op))){
                                                Transition t = Transition.getInstance(stream, "Translate");
                                                stream.addStartTransition(t);
                                                scs.startTransitions.add(stream.startTransitions.get(0));
                                            } if ("webcamstudio.channels.transitions.Resize".equals(subSTrans.get(op))){
                                                Transition t = Transition.getInstance(stream, "Resize");
                                                stream.addStartTransition(t);
                                                scs.startTransitions.add(stream.startTransitions.get(0));
                                            } if ("webcamstudio.channels.transitions.RevealLeft".equals(subSTrans.get(op))){
                                                Transition t = Transition.getInstance(stream, "RevealLeft");
                                                stream.addStartTransition(t);
                                                scs.startTransitions.add(stream.startTransitions.get(0));
                                            }
                                    }                                
                                    if (!subETrans.isEmpty()){
                                        if (subETrans.get(op) != null) {
                                            if ("webcamstudio.channels.transitions.FadeOut".equals(subETrans.get(op))){
                                                Transition t = Transition.getInstance(stream, "FadeOut");
                                                stream.addEndTransition(t);
                                                scs.endTransitions.add(stream.endTransitions.get(0));
                                            } if ("webcamstudio.channels.transitions.ShrinkOut".equals(subETrans.get(op))){
                                                Transition t = Transition.getInstance(stream, "ShrinkOut");
                                                stream.addEndTransition(t);
                                                scs.endTransitions.add(stream.endTransitions.get(0));
                                            }
                                        }
                                    }
                        stream.addChannel(scs);                    
                        op+=1;                    
                    }
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                            }
                        }                   
                    }
                } else {
                    System.err.println("Cannot handle " + clazz);
                }
            }
            for (Stream dST : extstreamBis) {
                int multi=0;
                String streamName = dST.getName();
                System.out.println("Found Stream Name: "+streamName);
                    for (String vDev : videoDevs){
                        if (vDev.contains(streamName)){
                                    multi += 1;
                        } 
                    }
                    if (multi>1) {
                        extstream.remove(dST);
                        ImgMovMus.remove("/dev/"+streamName);
                        System.out.println(dST+" Removed ...");
                        System.out.println(streamName+" Removed ...");
                        multi=0;
                }                 
             }          
        }   
    }

    private static void readObject(Stream stream, Node source) throws IllegalArgumentException, IllegalAccessException {
//        XPath path = XPathFactory.newInstance().newXPath();

        Field[] fields = stream.getClass().getDeclaredFields();
        Field[] superFields = stream.getClass().getSuperclass().getDeclaredFields();
        // Read integer and floats
        for (Field field : superFields) {
            field.setAccessible(true);
            String name = field.getName();
            String value;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                if (field.get(stream) instanceof Integer) {
                    field.setInt(stream, new Integer(value));
                } else if (field.get(stream) instanceof Float) {
                    field.setFloat(stream, new Float(value));
                } else if (field.get(stream) instanceof Boolean) {
                    field.setBoolean(stream, Boolean.valueOf(value));
                } else if (field.get(stream) instanceof String) {
                    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
                        Node node = source.getChildNodes().item(i);
                        if (node.getNodeName().equals(name)) {
                            field.set(stream, node.getTextContent());
                        }
                    }
                }
            }
        }


        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            String value;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                if (field.get(stream) instanceof Integer) {
                    field.setInt(stream, new Integer(value));
                } else if (field.get(stream) instanceof Float) {
                    field.setFloat(stream, new Float(value));
                } else if (field.get(stream) instanceof Boolean) {
                    field.setBoolean(stream, Boolean.valueOf(value));
                } else if (field.get(stream) instanceof String) {
                    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
                        Node node = source.getChildNodes().item(i);
                        if (node.getNodeName().equals(name)) {
                            field.set(stream, node.getTextContent());
                        }
                    }
                }

            }
        }
        // Read List
        
    }
    private static void readObjectFx (Effect fx, Node source) throws IllegalArgumentException, IllegalAccessException {
//        XPath path = XPathFactory.newInstance().newXPath();

        Field[] fields = fx.getClass().getDeclaredFields();
        Field[] superFields = fx.getClass().getSuperclass().getDeclaredFields();
        // Read integer and floats
        for (Field field : superFields) {
            field.setAccessible(true);
            String name = field.getName();
            String value;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                if (field.get(fx) instanceof Integer) {
                    field.setInt(fx, new Integer(value));
                } else if (field.get(fx) instanceof Float) {
                    field.setFloat(fx, new Float(value));
                } else if (field.get(fx) instanceof Boolean) {
                    field.setBoolean(fx, Boolean.valueOf(value));
                } else if (field.get(fx) instanceof String) {
                    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
                        Node node = source.getChildNodes().item(i);
                        if (node.getNodeName().equals(name)) {
                            field.set(fx, node.getTextContent());
                        }
                    }
                }
            }
        }
        
        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            String value;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                if (field.get(fx) instanceof Integer) {
                    field.setInt(fx, new Integer(value));
                } else if (field.get(fx) instanceof Float) {
                    field.setFloat(fx, new Float(value));
                } else if (field.get(fx) instanceof Boolean) {
                    field.setBoolean(fx, Boolean.valueOf(value));
                } else if (field.get(fx) instanceof String) {
                    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
                        Node node = source.getChildNodes().item(i);
                        if (node.getNodeName().equals(name)) {
                            field.set(fx, node.getTextContent());
                        }
                    }
                }

            }
        }
        // Read List
        
    }
    private static void readObjectSC(SourceChannel sc, Node source) throws IllegalArgumentException, IllegalAccessException {
//        XPath path = XPathFactory.newInstance().newXPath();

        Field[] fields = sc.getClass().getDeclaredFields();
        Field[] superFields = sc.getClass().getSuperclass().getDeclaredFields();
        // Read integer and floats
        for (Field field : superFields) {
            field.setAccessible(true);
            String name = field.getName();
            String value;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                if (field.get(sc) instanceof Integer) {
                    field.setInt(sc, new Integer(value));
                } else if (field.get(sc) instanceof Float) {
                    field.setFloat(sc, new Float(value));
                } else if (field.get(sc) instanceof Boolean) {
                    field.setBoolean(sc, Boolean.valueOf(value));
                } else if (field.get(sc) instanceof String) {
                    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
                        Node node = source.getChildNodes().item(i);
                        if (node.getNodeName().equals(name)) {
                            field.set(sc, node.getTextContent());
                        }
                    }                       
                    
                } 

            }
        }


        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            String value;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                if (field.get(sc) instanceof Integer) {
                    field.setInt(sc, new Integer(value));
                } else if (field.get(sc) instanceof Float) {
                    field.setFloat(sc, new Float(value));
                } else if (field.get(sc) instanceof Boolean) {
                    field.setBoolean(sc, Boolean.valueOf(value));
                } else if (field.get(sc) instanceof String) {
                    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
                        Node node = source.getChildNodes().item(i);
                        if (node.getNodeName().equals(name)) {
                            field.set(sc, node.getTextContent());
                        }
                    }
                }

            }
        }
        // Read List 
        
    }
    public static void main() { // removed (String[] args)
        try {
            try {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Studio.filename);
                readStreams(doc);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathExpressionException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
