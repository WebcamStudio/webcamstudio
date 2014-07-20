/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.studio;

import java.io.File;
import java.io.IOException;
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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
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
import webcamstudio.channels.transitions.Transition;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.sources.effects.Blink;
import webcamstudio.sources.effects.Block;
import webcamstudio.sources.effects.Cartoon;
import webcamstudio.sources.effects.ChromaKey;
import webcamstudio.sources.effects.ComboGhost;
import webcamstudio.sources.effects.Contrast;
import webcamstudio.sources.effects.Edge;
import webcamstudio.sources.effects.Effect;
import webcamstudio.sources.effects.Emboss;
import webcamstudio.sources.effects.FlipHorizontal;
import webcamstudio.sources.effects.FlipVertical;
import webcamstudio.sources.effects.Gain;
import webcamstudio.sources.effects.Ghosting;
import webcamstudio.sources.effects.Gray;
import webcamstudio.sources.effects.Green;
import webcamstudio.sources.effects.HSB;
import webcamstudio.sources.effects.Marble;
import webcamstudio.sources.effects.Mirror1;
import webcamstudio.sources.effects.Mirror2;
import webcamstudio.sources.effects.Mirror3;
import webcamstudio.sources.effects.Mirror4;
import webcamstudio.sources.effects.Mosaic;
import webcamstudio.sources.effects.NoBackground;
import webcamstudio.sources.effects.Opacity;
import webcamstudio.sources.effects.Perspective;
import webcamstudio.sources.effects.RGB;
import webcamstudio.sources.effects.Radar;
import webcamstudio.sources.effects.Rotation;
import webcamstudio.sources.effects.SaltNPepper;
import webcamstudio.sources.effects.Shapes;
import webcamstudio.sources.effects.Sharpen;
import webcamstudio.sources.effects.SwapRedBlue;
import webcamstudio.sources.effects.Twirl;
import webcamstudio.sources.effects.WaterFx;
import webcamstudio.sources.effects.Weave;
import webcamstudio.sources.effects.ZoomZoom;
import webcamstudio.streams.SourceAudioSource;
import webcamstudio.streams.SourceChannel;
import webcamstudio.streams.SourceDV;
import webcamstudio.streams.SourceDVB;
import webcamstudio.streams.SourceDesktop;
import webcamstudio.streams.SourceIPCam;
import webcamstudio.streams.SourceImageGif;
import webcamstudio.streams.SourceMovie;
import webcamstudio.streams.SourceMusic;
import webcamstudio.streams.SourceText;
import static webcamstudio.streams.SourceText.Shape.NONE;
import static webcamstudio.streams.SourceText.Shape.OVAL;
import static webcamstudio.streams.SourceText.Shape.RECTANGLE;
import static webcamstudio.streams.SourceText.Shape.ROUNDRECT;
import webcamstudio.streams.SourceURL;
import webcamstudio.streams.Stream;

/**
 *
 * @author patrick (modified by karl)
 */
public class Studio {

    private static final String ELEMENT_SOURCES = "Sources";
    private static final String ELEMENT_CHANNELS = "Channels";
    private static final String ELEMENT_EFFECTS = "Effects";
    private static final String ELEMENT_SOURCE = "Source";
    private static final String ELEMENT_CHANNEL = "Channel";
    private static final String ELEMENT_ROOT = "WebcamStudio";
    private static final String ELEMENT_MIXER = "Mixer";
    public static ArrayList<Stream> extstream = new ArrayList<>();
    public static ArrayList<SourceChannel> chanLoad = new ArrayList<>();
    public static File filename;
    public static String shapeImg = null;
    public static ArrayList<SourceText> LText = new ArrayList<>();
    public static ArrayList<String> ImgMovMus = new ArrayList<>();
    public static ArrayList<String> aGifKeys = new ArrayList<>();
    static boolean FirstChannel=false;
    static Listener listener = null;
    public static void setListener(Studio.Listener l) {
        listener = l;
    }
    // Studio removed, put void
    public static void load(File file, String loadType) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
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
        if (loadType.equals("load")) {
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
        }
//      return studio;
    }
    public static void save(File file) throws IOException, XMLStreamException, TransformerConfigurationException, TransformerException, IllegalArgumentException, IllegalAccessException {
        ArrayList<String> channels = MasterChannels.getInstance().getChannels();
        ArrayList<Stream> streams = MasterChannels.getInstance().getStreams();
        ArrayList<Integer> Durations = listener.getCHTimers();
        ArrayList<String> nextChannel = listener.getCHCurrNext();
        StringWriter writer = new StringWriter();
        System.out.println("Saving Studio ...");

        XMLStreamWriter xml = javax.xml.stream.XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
        xml.writeStartDocument();
        xml.writeStartElement(ELEMENT_ROOT);
        // Save Channels
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
//                System.out.println("Skipping Sink: "+clazzSink);
            } else {
                System.out.println("Saving Stream: "+s.getName());
                xml.writeStartElement(ELEMENT_SOURCE);
                writeObject(s, xml);
                xml.writeEndElement(); // Save Source
            }
        }
        xml.writeEndElement();  //Save Sources

        xml.writeStartElement(ELEMENT_MIXER);
        xml.writeAttribute("width", MasterMixer.getInstance().getWidth() + "");
        xml.writeAttribute("height", MasterMixer.getInstance().getHeight() + "");
        xml.writeAttribute("rate", MasterMixer.getInstance().getRate() + "");
        xml.writeEndElement(); //Save Mixer

        xml.writeEndElement(); //Save WebcamStudio
        xml.writeEndDocument();
        xml.flush();
        xml.close();
        TransformerFactory factory = TransformerFactory.newInstance();

        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
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
                    switch (name) {
                        case "channels":
                            xml.writeStartElement(ELEMENT_CHANNELS);
                            for (Object subO : ((Iterable<? extends Object>) value)) {
                                if (clazz != null){
                                    xml.writeStartElement(name);
                                    writeObject(subO, xml);
                                    xml.writeEndElement(); 
                                }
                            }
                            break;
                        case "effects":
                            xml.writeStartElement(ELEMENT_EFFECTS);
                            for (Object subO : ((Iterable<? extends Object>) value)) {
                                if (clazz != null){
                                    xml.writeStartElement(name);
                                    writeObject(subO, xml);
                                    xml.writeEndElement(); 
                                }
                            }
                            break;
                        default:
                            xml.writeStartElement(name);
                            for (Object subO : ((Iterable<? extends Object>) value)) {
                                if (clazz != null){
                                    writeObject(subO, xml);
                                }
                            
                            }
                            break;
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
                    for (Object subO : ((Iterable<? extends Object>) value)) {
                        if (clazz != null){
                            xml.writeStartElement(name);
                            writeObject(subO, xml);
                            xml.writeEndElement(); 
                        }
                    }
                } else {
                    xml.writeStartElement(name);
                    for (Object subO : ((Iterable<? extends Object>) value)) {
                        writeObject(subO, xml);
                    }
                }
                xml.writeEndElement();
            }
        }
    }
    private static void loadTransitions(ArrayList<SourceChannel> SCL, Stream stream, ArrayList<String> subSTrans, ArrayList<String> subETrans, ArrayList<String> SubChNames, ArrayList<String> SubText, ArrayList<String> SubFont) {
        int op=0;
        for (SourceChannel scs : SCL) {
            scs.setName(SubChNames.get(op));
            if (SubText != null) {
                scs.setText(SubText.get(op));
                scs.setFont(SubFont.get(op));
            }
            if (!subSTrans.isEmpty() && subSTrans.get(op) != null){
                if (subSTrans.get(op).endsWith("FadeIn")){
                    Transition t = Transition.getInstance(stream, "FadeIn");
                    scs.startTransitions.add(t);
                } if (subSTrans.get(op).endsWith("AudioFadeIn")){
                    Transition t = Transition.getInstance(stream, "AudioFadeIn");
                    scs.startTransitions.add(t);
                } if (subSTrans.get(op).endsWith("TranslateIn")){
                    Transition t = Transition.getInstance(stream, "TranslateIn");
                    scs.startTransitions.add(t);
                } if (subSTrans.get(op).endsWith("ResizeIn")){
                    Transition t = Transition.getInstance(stream, "ResizeIn");
                    scs.startTransitions.add(t);
                } if (subSTrans.get(op).endsWith("RevealLeft")){
                    Transition t = Transition.getInstance(stream, "RevealLeft");
                    scs.startTransitions.add(t);
                } if (subSTrans.get(op).endsWith("CornerResize")){
                    Transition t = Transition.getInstance(stream, "CornerResize");
                    scs.startTransitions.add(t);
                } if (subSTrans.get(op).endsWith("RevealRight")){
                    Transition t = Transition.getInstance(stream, "RevealRight");
                    scs.startTransitions.add(t);
                }
            }
            if (!subETrans.isEmpty() && subETrans.get(op) != null){
                if (subETrans.get(op).endsWith("FadeOut")){
                    Transition t = Transition.getInstance(stream, "FadeOut");
                    scs.endTransitions.add(t);
                } if (subETrans.get(op).endsWith("TranslateOut")){
                    Transition t = Transition.getInstance(stream, "TranslateOut");
                    scs.endTransitions.add(t);
                } if (subETrans.get(op).endsWith("AudioFadeOut")){
                    Transition t = Transition.getInstance(stream, "AudioFadeOut");
                    scs.endTransitions.add(t);
                } if (subETrans.get(op).endsWith("ShrinkOut")){
                    Transition t = Transition.getInstance(stream, "ShrinkOut");
                    scs.endTransitions.add(t);
                } if (subETrans.get(op).endsWith("HideLeft")){
                    Transition t = Transition.getInstance(stream, "HideLeft");
                    scs.endTransitions.add(t);
                } if (subETrans.get(op).endsWith("HideRight")){
                    Transition t = Transition.getInstance(stream, "HideRight");
                    scs.endTransitions.add(t);
                } if (subETrans.get(op).endsWith("CornerShrink")){
                    Transition t = Transition.getInstance(stream, "CornerShrink");
                    scs.endTransitions.add(t);
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
    
    private static Effect loadEffects(String sClazz, Node SuperChild){
        Effect effeX = null;
        try {
            if (sClazz.endsWith("ChromaKey")) {
                effeX = new ChromaKey();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("ComboGhost")) {
                effeX = new ComboGhost();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Ghosting")) {
                effeX = new Ghosting();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Blink")) {
                effeX = new Blink();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Block")) {
                effeX = new Block();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Cartoon")) {
                effeX = new Cartoon();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Contrast")) {
                effeX = new Contrast();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Edge")) {
                effeX = new Edge();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("FlipHorizontal")) {
                effeX = new FlipHorizontal();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("FlipVertical")) {
                effeX = new FlipVertical();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Gain")) {
                effeX = new Gain();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Gray")) {
                effeX = new Gray();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("HSB")) {
                effeX = new HSB();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Sharpen")) {
                effeX = new Sharpen();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("SaltNPepper")) {
                effeX = new SaltNPepper();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Mirror1")) {
                effeX = new Mirror1();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Mirror2")) {
                effeX = new Mirror2();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Mirror3")) {
                effeX = new Mirror3();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Mirror4")) {
                effeX = new Mirror4();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Mosaic")) {
                effeX = new Mosaic();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("NoBackground")) {
                effeX = new NoBackground();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Opacity")) {
                effeX = new Opacity();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Perspective")) {
                effeX = new Perspective();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("RGB")) {
                effeX = new RGB();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Radar")) {
                effeX = new Radar();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Rotation")) {
                effeX = new Rotation();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Emboss")) {
                effeX = new Emboss();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("SwapRedBlue")) {
                effeX = new SwapRedBlue();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Twirl")) {
                effeX = new Twirl();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("ZoomZoom")) {
                effeX = new ZoomZoom();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Green")) {
                effeX = new Green();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Shapes")) {
                effeX = new Shapes();
                readObjectFx(effeX, SuperChild);
                effeX.setShape(shapeImg);
                
            } else if (sClazz.endsWith("Marble")) {
                effeX = new Marble();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Weave")) {
                effeX = new Weave();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("WaterFx")) {
                effeX = new WaterFx();
                readObjectFx(effeX, SuperChild);
                
            }
        } catch (IllegalArgumentException | IllegalAccessException illegalArgumentException) {
        }
        return effeX;
    }

    private static void readStreams (Document xml) throws IllegalArgumentException, IllegalAccessException, XPathExpressionException{
        XPath path = XPathFactory.newInstance().newXPath();
        NodeList sources = (NodeList) path.evaluate("/" + ELEMENT_ROOT + "/" + ELEMENT_SOURCES + "/" + ELEMENT_SOURCE, xml.getDocumentElement(), XPathConstants.NODESET);
        String videoDev;      
        ArrayList<String> videoDevs = new ArrayList<>();
        ArrayList<Stream> extstreamBis = new ArrayList<>();
        if (sources != null) {
            for (int i = 0; i < sources.getLength(); i++) {
                Node source = sources.item(i);            
                String clazz = source.getAttributes().getNamedItem("clazz").getTextContent();
                String file = null;
                String ObjText = null;
                String fontName = null;
                String webUrl = null;
                String ipcPWD = null;
                String ipcUser = null;
                String pBrand = null;
                String comm = null;
                String desktopXid = null;
                String elementXid = null;
                String streamTime = null;
                String streamAudioSrc = null;
                String strShapez = null;
                String guid = null;
                
                String chNameDvb = null;
                ArrayList<String> SubChNames = new ArrayList<>();
                ArrayList<String> SubText = new ArrayList<>();
                ArrayList<String> SubFont = new ArrayList<>();
                ArrayList<String> subSTrans = new ArrayList<>();
                ArrayList<String> subETrans = new ArrayList<>();
                ArrayList<String> sNames = new ArrayList<>();
                String sName = null;
                Stream stream = null;
                SourceChannel sc = null;
                Effect effeX = null;
                ArrayList<SourceChannel> SCL = new ArrayList<>();
                ArrayList<Effect> fXL = new ArrayList<>();
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
                    if (child.getNodeName().equals("fontName")) {
                        fontName = child.getTextContent();
                    }
                    if (child.getNodeName().equals("strShape")) {
                        strShapez = child.getTextContent();
                    }
                    if (child.getNodeName().equals("webURL")) {
                        webUrl = child.getTextContent();
                    }
                    if (child.getNodeName().equals("guid")) {
                        guid = child.getTextContent();
                    }
                    if (child.getNodeName().equals("ptzBrand")) {
                        pBrand = child.getTextContent();
                    }
                    if (child.getNodeName().equals("ipcUser")) {
                        ipcUser = child.getTextContent();
                    }
                    if (child.getNodeName().equals("ipcPWD")) {
                        ipcPWD = child.getTextContent();
                    }
                    if (child.getNodeName().equals("chNameDVB")) {
                        chNameDvb = child.getTextContent();
                    }
                    if (child.getNodeName().equals("comm")) {                       
                        comm = child.getTextContent();
                    }
                    if (child.getNodeName().equals("desktopXid")) {                       
                        desktopXid = child.getTextContent();
                    }
                    if (child.getNodeName().equals("elementXid")) {                       
                        elementXid = child.getTextContent();
                    }
                    if (child.getNodeName().equals("streamTime")) {                       
                        streamTime = child.getTextContent();
                    }
                    if (child.getNodeName().equals("audioSource")) {                       
                        streamAudioSrc = child.getTextContent();
                    }
                    
                    if (child.getNodeName().equals("Effects")) { // Read Effects
//                        System.out.println("childnodename: "+child.getNodeName());
                        for (int nc = 0; nc < child.getChildNodes().getLength(); nc++) {
                            Node SuperChild = child.getChildNodes().item(nc);
//                            System.out.println("SuperChildnodename: "+SuperChild.getNodeName());
                            if (SuperChild.getNodeName().equals("effects")) {
                                for (int ncc = 0; ncc < SuperChild.getChildNodes().getLength(); ncc++) {
                                    Node SSuperChild = SuperChild.getChildNodes().item(ncc);
                                    if (SSuperChild.getNodeName().equals("shapeS")){
                                        shapeImg = SSuperChild.getTextContent();
//                                        System.out.println("Ass ShapeImg: "+ shapeImg);
                                    }
                                }
                                String sClazz = SuperChild.getAttributes().getNamedItem("clazz").getTextContent();
                                effeX = loadEffects (sClazz, SuperChild);
                                fXL.add(effeX);
//                                System.out.println("effect clazz: "+ sClazz);
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
                                            for (int nccC = 0; nccC < SSSuperChild.getChildNodes().getLength(); nccC++) {
                                                Node SSSSuperChildC = SSSuperChild.getChildNodes().item(nccC);
                                                if (SSSSuperChildC.getNodeName().equals("shapeS")){
                                                    shapeImg = SSSSuperChildC.getTextContent();
//                                                System.out.println("Ass ShapeImg Chan: "+ shapeImg);
                                                }
                                            }
                                            String sClazz = SSSuperChild.getAttributes().getNamedItem("clazz").getTextContent();
                                            effeX = loadEffects (sClazz, SSSuperChild);
                                            sc.addEffects(effeX);
//                                            System.out.println("channel effect clazz: "+ sClazz);
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
                    File fileL = new File(file);
                    stream = Stream.getInstance(fileL);
                    extstream.add(stream); 
                    extstreamBis.add(stream);
                    readObject(stream, source);
                    stream.setComm(comm);
                    for (Effect fx : fXL) {
                        if (fx.getName().endsWith("Shapes")){
                            fx.setDoOne(true);
                        }
                        stream.addEffect(fx);
                    }
                    if (streamTime != null){
                        stream.setStreamTime(streamTime);
                    } else {
                        if (stream instanceof SourceMovie || stream instanceof SourceMusic) {
                            WebcamStudio.durationCalc(stream, fileL);
                        }
                    }
                    stream.setLoaded(true);
                    loadTransitions(SCL, stream, subSTrans, subETrans, SubChNames, null, null);
                    stream.setName(sName);
                    fXL.clear();
                } else if (clazz.toLowerCase().endsWith("sourcedesktop")) {
                    stream = new SourceDesktop(); 
                    extstream.add(stream);
                    extstreamBis.add(stream);
                    ImgMovMus.add("Desktop");
                    readObject(stream, source);
                    stream.setComm(comm);
                    if (!"".equals(desktopXid)){
                        stream.setSingleWindow(true);
                    }
                    for (Effect fx : fXL) {
                        if (fx.getName().endsWith("Shapes")){
                            fx.setDoOne(true);
                        }
                        stream.addEffect(fx);
                    }
                    stream.setLoaded(true);
                    loadTransitions(SCL, stream, subSTrans, subETrans, SubChNames, null, null);
                } else if (clazz.toLowerCase().endsWith("sourcetext")) {
                    text = new SourceText(ObjText);
                    LText.add(text);
                    readObject(text, source);
                    for (Effect fx : fXL) {
                        if (fx.getName().endsWith("Shapes")){
                            fx.setDoOne(true);
                        }
                        text.addEffect(fx);
                    }
                    if (strShapez != null) {
                        switch (strShapez) {
                            case "none":
                                text.setBackground(NONE); 
                                text.setStrBackground("none");
                                break;
                            case "rectangle":
                                text.setBackground(RECTANGLE);
                                text.setStrBackground("rectangle");
                                break;
                            case "oval":
                                text.setBackground(OVAL);
                                text.setStrBackground("oval");
                                break;
                            case "roundrect":
                                text.setBackground(ROUNDRECT);
                                text.setStrBackground("roundrect");
                                break;
                        }
                    }
                    text.setFont(fontName);
                    text.setLoaded(true);
                    loadTransitions(SCL, text, subSTrans, subETrans, SubChNames, SubText, SubFont);
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
                        if (fx.getName().endsWith("Shapes")){
                            fx.setDoOne(true);
                        }
                        stream.addEffect(fx);
                    }
                    loadTransitions(SCL, stream, subSTrans, subETrans, SubChNames, null, null);
                } else if (clazz.toLowerCase().endsWith("sourcedv")) {
                    stream = new SourceDV();
                    extstream.add(stream); 
                    extstreamBis.add(stream);
                    ImgMovMus.add("DV");
                    readObject(stream, source);
                    stream.setGuid(guid);
                    for (Effect fx : fXL) {
                        if (fx.getName().endsWith("Shapes")){
                            fx.setDoOne(true);
                        }
                        stream.addEffect(fx);
                    }
                    stream.setLoaded(true);
                    loadTransitions(SCL, stream, subSTrans, subETrans, SubChNames, null, null);
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
                        if (fx.getName().endsWith("Shapes")){
                            fx.setDoOne(true);
                        }
                        stream.addEffect(fx);
                    }
                    loadTransitions(SCL, stream, subSTrans, subETrans, SubChNames, null, null);
                } else if (clazz.toLowerCase().endsWith("sourceipcam")) {
                    stream = new SourceIPCam();
                    stream.setWebURL(webUrl);
                    extstream.add(stream);
                    extstreamBis.add(stream);
                    ImgMovMus.add("URL");
                    readObject(stream, source);
                    if (stream.getProtected()){
                        stream.setIPUser(ipcUser);
                        stream.setIPPwd(ipcPWD);
                    }
                    stream.setPtzBrand(pBrand);
                    stream.setComm(comm);
                    stream.setLoaded(true);
                    for (Effect fx : fXL) {
                        if (fx.getName().endsWith("Shapes")){
                            fx.setDoOne(true);
                        }
                        stream.addEffect(fx);
                    }
                    loadTransitions(SCL, stream, subSTrans, subETrans, SubChNames, null, null);
                } else if (clazz.toLowerCase().endsWith("sourceaudiosource")) {
                    stream = new SourceAudioSource();
                    extstream.add(stream);
                    extstreamBis.add(stream);
                    ImgMovMus.add("Mic");
                    readObject(stream, source);
                    stream.setComm(comm);
                    stream.setAudioSource(streamAudioSrc);
                    stream.setLoaded(true);
                    loadTransitions(SCL, stream, subSTrans, subETrans, SubChNames, null, null);
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
                                loadTransitions(SCL, stream, subSTrans, subETrans, SubChNames, null, null);
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
//                System.out.println("Found Stream Name: "+streamName);
                for (String vDev : videoDevs){
                    if (vDev.contains(streamName)){
                        multi += 1; 
                    }
                }
                if (multi>1) {
                    extstream.remove(dST);
                    ImgMovMus.remove("/dev/"+streamName);
//                        System.out.println(dST+" Removed ...");
//                        System.out.println(streamName+" Removed ...");
                    multi=0;
                }
            }
        }   
    }
    private static void readObject(Stream stream, Node source) throws IllegalArgumentException, IllegalAccessException {
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

    private static void readObjectFx(Effect fx, Node source) throws IllegalArgumentException, IllegalAccessException {
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
    private static void readObjectSC (SourceChannel sc, Node source) throws IllegalArgumentException, IllegalAccessException {
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
            } catch (    IllegalArgumentException | IllegalAccessException | XPathExpressionException | SAXException | IOException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    private final ArrayList<String> channels = MasterChannels.getInstance().getChannels();
    private final ArrayList<Integer> Durations = listener.getCHTimers();
    private final ArrayList<String> nextChannel = listener.getCHCurrNext();
    ArrayList<Stream> streams = MasterChannels.getInstance().getStreams();
    Stream streamC = null;
    protected Studio() {
    }

    public interface Listener {

        public ArrayList<String> getCHCurrNext ();

        public ArrayList<Integer> getCHTimers ();
    }
}
