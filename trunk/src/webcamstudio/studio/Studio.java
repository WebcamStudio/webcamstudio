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
//import webcamstudio.util.Tools;
import webcamstudio.components.*;
import webcamstudio.channels.transitions.Transition;

/**
 *
 * @author patrick
 */
public class Studio {

/*  public static*/ private ArrayList<String> channels = MasterChannels.getInstance().getChannels();
    private ArrayList<Integer> Durations = ChannelPanel.CHTimers;
    private ArrayList<String> nextChannel = ChannelPanel.CHCurrNext;
//  public static ArrayList<String> channez = MasterChannels.getInstance().getChannels();
//  public static MasterChannels channelSC = MasterChannels.getInstance();
    ArrayList<Stream> streams = MasterChannels.getInstance().getStreams();
    Stream streamC = null;
    private static final String ELEMENT_SOURCES = "Sources";
    private static final String ELEMENT_CHANNELS = "Channels";
    private static final String ELEMENT_SOURCE = "Source";
    private static final String ELEMENT_CHANNEL = "Channel";
    private static final String ELEMENT_ROOT = "WebcamStudio";
    private static final String ELEMENT_MIXER = "Mixer";
    public static ArrayList<Stream> extstream = new ArrayList<Stream>();
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
        System.out.println("Start LoadStudio");
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
           System.out.println("Channel " + name);  
           studio.channels.add(name);
           studio.nextChannel.add(nxname);
           studio.Durations.add(duration);
           System.out.println("Duration " + duration);  
        }
        

        // Loading mixer settings
        Node nodeMixer = (Node) path.evaluate("/WebcamStudio/Mixer", doc.getDocumentElement(), XPathConstants.NODE);
        String width = nodeMixer.getAttributes().getNamedItem("width").getTextContent();
        String height = nodeMixer.getAttributes().getNamedItem("height").getTextContent();
        String rate = nodeMixer.getAttributes().getNamedItem("rate").getTextContent();
        System.out.println("Mixer: " + width + "X" + height + "@" + rate + "fps");

//      return studio;
    }

    public static void save(File file) throws IOException, XMLStreamException, TransformerConfigurationException, TransformerException, IllegalArgumentException, IllegalAccessException {
        ArrayList<String> channels = MasterChannels.getInstance().getChannels();
        ArrayList<Stream> streams = MasterChannels.getInstance().getStreams();
        ArrayList<Integer> Durations = ChannelPanel.CHTimers;
        ArrayList<String> nextChannel = ChannelPanel.CHCurrNext;
        StringWriter writer = new StringWriter();

        XMLStreamWriter xml = javax.xml.stream.XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
        xml.writeStartDocument();
        xml.writeStartElement(ELEMENT_ROOT);
        //Channels
        xml.writeStartElement(ELEMENT_CHANNELS);

        for (String c : channels) {
            int index = channels.indexOf(c);
            xml.writeStartElement(ELEMENT_CHANNEL);
            System.out.println("Channel: "+c);
            xml.writeAttribute("name", c);
            xml.writeAttribute("duration", Durations.get(index) + "");
            xml.writeAttribute("NextChannel", nextChannel.get(index) + "");
            xml.writeEndElement();
            
        }
        xml.writeEndElement();

        xml.writeStartElement(ELEMENT_SOURCES);
        for (Stream s : streams) {
            String clazzSink = s.getClass().getCanonicalName();
            if (!clazzSink.contains("SinkLinuxDevice")){
                System.out.println(streams);
                xml.writeStartElement(ELEMENT_SOURCE);
                writeObject(s, xml);
                xml.writeEndElement(); // source
            } else {
                System.out.println("SinkLinuxDevice: "+clazzSink);
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

        StringWriter formattedStringWriter = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(writer.getBuffer().toString())), new StreamResult(file));

    }
    private static void writeObject(Object o, XMLStreamWriter xml) throws IllegalArgumentException, IllegalAccessException, XMLStreamException {

        Field[] fields = o.getClass().getDeclaredFields();
        System.out.println("Fields lenght: "+fields.length);
        System.out.println("Start WriteObject");
        Field[] superFields = null;
        if (o instanceof Stream) {
            superFields = o.getClass().getSuperclass().getDeclaredFields();
            System.out.println("Is a Stream.");
        }
        String clazz = o.getClass().getCanonicalName();
        if (clazz != null) {
            System.out.println("Clazz: "+clazz);
//            Tools.sleep(50);
            xml.writeAttribute("clazz", clazz);
        }
 //       Tools.sleep(50);
        if (superFields != null) {
            for (Field f : superFields) {
                f.setAccessible(true);
                String name = f.getName();
       //         System.out.println("SuperFields: "+name);
                Object value = f.get(o);
       //         System.out.println("Value: "+value);
                if (value instanceof Integer) {
                    xml.writeAttribute(name, f.getInt(o) + "");
                } else if (value instanceof Float) {
                    xml.writeAttribute(name, f.getFloat(o) + "");
                } else if (value instanceof Boolean) {
                    xml.writeAttribute(name, f.getBoolean(o) + "");
                }

            }
        }
   //     Tools.sleep(50);
        for (Field f : fields) {
            f.setAccessible(true);
            String name = f.getName();
     //       System.out.println("Fields: "+name);
            Object value = f.get(o);
            if (value instanceof Integer) {
                xml.writeAttribute(name, f.getInt(o) + "");
            } else if (value instanceof Float) {
                xml.writeAttribute(name, f.getFloat(o) + "");
            } else if (value instanceof Boolean) {
                    xml.writeAttribute(name, f.getBoolean(o) + "");
            }
        }
   //     Tools.sleep(50);        
        if (superFields != null) {
            for (Field f : superFields) {
                f.setAccessible(true);
                String name = f.getName();
      //          System.out.println("Superfield2: "+name);
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
    //    Tools.sleep(50);
        for (Field f : fields) {
            f.setAccessible(true);
            String name = f.getName();
       //     System.out.println("Fields2: "+name);
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
    //    Tools.sleep(50);
        if (superFields != null) {
            for (Field f : superFields) {
                f.setAccessible(true);
                String name = f.getName();
      //          System.out.println("sub0Name: "+name);
                Object value = f.get(o);
       //         System.out.println("sub0ObjectValue: "+value);
                if (value instanceof List) { 
                    if ("channels".equals(name)) {
                        xml.writeStartElement(ELEMENT_CHANNELS);
                        for (Object subO : ((List) value)) {
                        if (clazz != null){
                        xml.writeStartElement(name);
         //               System.out.println("sub0: "+subO);
                        writeObject(subO, xml);
                        xml.writeEndElement(); 
                        }
                        }
                    } else {
                    xml.writeStartElement(name);
                    for (Object subO : ((List) value)) {
                        if (clazz != null){
         //               System.out.println("sub0: "+subO);
                        writeObject(subO, xml);
                        }
                        
                    }
                    }
                    xml.writeEndElement();
                }
            }
        }
   //     Tools.sleep(50);
        for (Field f : fields) {
            f.setAccessible(true);
            String name = f.getName();
   //         System.out.println("Fields3: "+name);
            Object value = f.get(o);
            if (value instanceof List) {
                xml.writeStartElement(name);
                for (Object subO : ((List) value)) {
                    writeObject(subO, xml);
                }
                xml.writeEndElement();
            }
        }
    }
   
    private static void readStreams(Document xml) throws IllegalArgumentException, IllegalAccessException, XPathExpressionException {
        XPath path = XPathFactory.newInstance().newXPath();
        NodeList sources = (NodeList) path.evaluate("/" + ELEMENT_ROOT + "/" + ELEMENT_SOURCES + "/" + ELEMENT_SOURCE, xml.getDocumentElement(), XPathConstants.NODESET);
        String videoDev = null;      
        ArrayList<String> videoDevs = new ArrayList<String>();
        ArrayList<Stream> extstreamBis = new ArrayList<Stream>();
        if (sources != null) {
            for (int i = 0; i < sources.getLength(); i++) {
                Node source = sources.item(i);            
                String clazz = source.getAttributes().getNamedItem("clazz").getTextContent();
                System.out.println(clazz);
                String file = null;
                String ObjText = null;
                String webUrl = null;
                String chNameDvb = null;
                ArrayList<String> SubChNames = new ArrayList<String>();
                ArrayList<String> SubText = new ArrayList<String>();
                ArrayList<String> SubFont = new ArrayList<String>();
                ArrayList<String> subSTrans = new ArrayList<String>();
                ArrayList<String> subETrans = new ArrayList<String>();
                ArrayList<String> sNames = new ArrayList<String>();
                String sName = null;
                String subContent = null;
                Stream stream = null;
                SourceChannel sc = null;
                ArrayList<SourceChannel> SCL = new ArrayList<SourceChannel>();
                SourceText text = null;
                for (int j = 0; j < source.getChildNodes().getLength(); j++) {
                    Node child = source.getChildNodes().item(j);
                    if (child.getNodeName().equals("file")) {                       
                        file = child.getTextContent();
                        ImgMovMus.add(file);
                        if (child.getTextContent().contains("/dev/video")){
                            videoDev = child.getTextContent();
                            videoDevs.add(videoDev);
                            System.out.println("Stream File: "+file+" Added. ****");
                        }
                    }
                    if (child.getNodeName().equals("name")) {                       
                        sName = child.getTextContent();
                        sNames.add(sName);
                        System.out.println("Stream Name: "+sName+" Added. ****");                     
                    }
                    if (child.getNodeName().equals("content")) {
                        ObjText = child.getTextContent();
                        System.out.println("Text Content: "+ObjText);
                    }
                    if (child.getNodeName().equals("webURL")) {
                        webUrl = child.getTextContent();
                        System.out.println("Web URL: "+webUrl);
                    }
                    if (child.getNodeName().equals("chNameDVB")) {
                        chNameDvb = child.getTextContent();
                        System.out.println("DVB Ch Name: "+chNameDvb);
                    }
                    if (child.getNodeName().equals("Channels")) { // Read SourceChannels
                        for (int nc = 0; nc < child.getChildNodes().getLength(); nc++) {
                            Node SuperChild = child.getChildNodes().item(nc);
                            for (int ncc = 0; ncc < SuperChild.getChildNodes().getLength(); ncc++) {
                                Node SSuperChild = SuperChild.getChildNodes().item(ncc);                        
                                if (SSuperChild.getNodeName().equals("name")) {
                                    System.out.println("SuperChild: "+SSuperChild.getTextContent());
                                    SubChNames.add(SSuperChild.getTextContent()); 
                                    sc = new SourceChannel();
                                    readObjectSC(sc, SuperChild);
                                    SCL.add(sc);
                                }  
                                if (SSuperChild.getNodeName().equals("startTransitions")) {
                                    if (SSuperChild.getAttributes().getLength()!= 0) {
                                        String sClazz = SSuperChild.getAttributes().getNamedItem("clazz").getTextContent();
                                        System.out.println("SuperChild STrans: "+sClazz);                                
                                        subSTrans.add(sClazz);
                                    } else {
                                        subSTrans.add("None");                                   
                                    }
                                }  
                                if (SSuperChild.getNodeName().equals("endTransitions")) {
                                    if (SSuperChild.getAttributes().getLength()!= 0) {
                                        String sClazz = SSuperChild.getAttributes().getNamedItem("clazz").getTextContent();
                                        System.out.println("SuperChild ETrans: "+sClazz);
                                        subETrans.add(sClazz);
                                    } else {
                                        subETrans.add("None");                                   
                                    }
                                }  
                                if (SSuperChild.getNodeName().equals("text") && SSuperChild.getTextContent() != null) {
                                    System.out.println("SuperChild Text: "+SSuperChild.getTextContent());
                                    SubText.add(SSuperChild.getTextContent());
                                }  
                                if (SSuperChild.getNodeName().equals("font") && SSuperChild.getTextContent() != null) {
                                    System.out.println("SuperChild Font: "+SSuperChild.getTextContent());
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
                    int op=0;
                    for (SourceChannel scs : SCL) {
                        scs.setName(SubChNames.get(op));
                        String sNamet=SubChNames.get(op);
                        if (subSTrans.size() != 0){
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
                        if (subETrans.size() != 0){
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
                        System.out.println("Add Channel: "+scs);
                        op+=1;
                    }
                    stream.setName(sName);
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                    System.out.println("Subs Cleared **** ");
                } else if (clazz.toLowerCase().endsWith("sourcedesktop")) {
                    stream = new SourceDesktop(); 
                    extstream.add(stream);
                    extstreamBis.add(stream);
                    ImgMovMus.add("Desktop");
                    readObject(stream, source);                    
                    int op=0;
                    for (SourceChannel scs : SCL) {
                        scs.setName(SubChNames.get(op));
                        String sNamet=SubChNames.get(op);
                        if (!subSTrans.isEmpty()){
                            if (subSTrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeIn".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeIn");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                }
                            }
                        }
                        if (subETrans.size() != 0){
                            if (subETrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                }
                            }
                        }
                        stream.addChannel(scs);                    
                        System.out.println("Add Channel: "+scs);
                        op+=1;
                    }
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                    System.out.println("Subs Cleared **** ");
                } else if (clazz.toLowerCase().endsWith("sourcetext")) {
                    text = new SourceText(ObjText);
                    LText.add(text);
                    readObject(text, source);
                    int op=0;
                    for (SourceChannel scs : SCL) {
                        scs.setName(SubChNames.get(op));
                        scs.setText(SubText.get(op));
                        scs.setFont(SubFont.get(op));
                        String sNamet=SubChNames.get(op);
                        if (subSTrans.size() != 0){
                            if (subSTrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeIn".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeIn");
                                    text.addStartTransition(t);
                                    scs.startTransitions.add(text.startTransitions.get(0));
                                }
                            }
                        }
                        if (subETrans.size() != 0){
                            if (subETrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeOut");
                                    text.addEndTransition(t);
                                    scs.endTransitions.add(text.endTransitions.get(0));
                                }
                            }
                        }
                        text.addChannel(scs);                    
                        System.out.println("Add Channel: "+scs);
                        op+=1;
                    }
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                    System.out.println("Subs Cleared **** ");
                } else if (clazz.toLowerCase().endsWith("sourceqrcode")) {
                    stream = new SourceQRCode(ObjText);
                    extstream.add(stream); 
                    extstreamBis.add(stream);
                    ImgMovMus.add("QRcode");
                    readObject(stream, source);
                    int op=0;
                    for (SourceChannel scs : SCL) {
                        scs.setName(SubChNames.get(op));
                        String sNamet=SubChNames.get(op);
                        if (!subSTrans.isEmpty()){
                            if (subSTrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeIn".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeIn");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                }
                            }
                        }
                        if (subETrans.size() != 0){
                            if (subETrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                }
                            }
                        }
                        stream.addChannel(scs);                    
                        System.out.println("Add Channel: "+scs);
                        op+=1;
                    }
//                    for (SourceChannel scs : SCL) {
//                        stream.addChannel(scs);
//                        op+=1;
//                    }
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                    System.out.println("Subs Cleared **** ");
                } else if (clazz.toLowerCase().endsWith("sourcedvb")) {
                    stream = new SourceDVB();
                    stream.setChName(chNameDvb);
                    extstream.add(stream); 
                    extstreamBis.add(stream);
                    ImgMovMus.add("DVB-T");
                    readObject(stream, source);
                    int op=0;
                    for (SourceChannel scs : SCL) {
                        scs.setName(SubChNames.get(op));
                        String sNamet=SubChNames.get(op);
                        if (!subSTrans.isEmpty()){
                            if (subSTrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeIn".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeIn");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                }
                            }
                        }
                        if (subETrans.size() != 0){
                            if (subETrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                }
                            }
                        }
                        stream.addChannel(scs);                    
                        System.out.println("Add Channel: "+scs);
                        op+=1;
                    }
  //                  for (SourceChannel scs : SCL) {
  //                      stream.addChannel(scs);
  //                      System.out.println("Add Channel: "+scs);
  //                      op+=1;
  //                  }
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                    System.out.println("Subs Cleared **** ");
                } else if (clazz.toLowerCase().endsWith("sourceurl")) {
                    stream = new SourceURL();
                    stream.setWebURL(webUrl);
                    extstream.add(stream);
                    extstreamBis.add(stream);
                    ImgMovMus.add("URL");
                    readObject(stream, source);
                    int op=0;
                    for (SourceChannel scs : SCL) {
                        scs.setName(SubChNames.get(op));
                        String sNamet=SubChNames.get(op);
                        if (!subSTrans.isEmpty()){
                            if (subSTrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeIn".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeIn");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                }
                            }
                        }
                        if (subETrans.size() != 0){
                            if (subETrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                }
                            }
                        }
                        stream.addChannel(scs);                    
                        System.out.println("Add Channel: "+scs);
                        op+=1;
                    }
//                    for (SourceChannel scs : SCL) {
//                        stream.addChannel(scs);
//                        System.out.println("Add Channel: "+scs);
//                        op+=1;
//                    }
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                    System.out.println("Subs Cleared **** ");
                } else if (clazz.toLowerCase().endsWith("sourcemicrophone")) {
                    stream = new SourceMicrophone(); 
                    extstream.add(stream);
                    extstreamBis.add(stream);
                    ImgMovMus.add("Mic");
                    readObject(stream, source);
                    int op=0;                    
                    for (SourceChannel scs : SCL) {
                        scs.setName(SubChNames.get(op));
                        String sNamet=SubChNames.get(op);
                        if (!subSTrans.isEmpty()){
                            if (subSTrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeIn".equals(subSTrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeIn");
                                    stream.addStartTransition(t);
                                    scs.startTransitions.add(stream.startTransitions.get(0));
                                }
                            }
                        }
                        if (subETrans.size() != 0){
                            if (subETrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                }
                            }
                        }
                        stream.addChannel(scs);                    
                        System.out.println("Add Channel: "+scs);
                        op+=1;
                    }
//                    for (SourceChannel scs : SCL) {
//                        stream.addChannel(scs);
//                        op+=1;
//                    }
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                    System.out.println("Subs Cleared **** ");
                } else if (clazz.toLowerCase().endsWith("sourceimagegif")) {
                    for (int an=0;an < webcamstudio.WebcamStudio.cboAnimations.getItemCount(); an++){
                        for (String aKey : sNames){
                            System.out.println("Gif Name:"+webcamstudio.WebcamStudio.cboAnimations.getItemAt(an).toString());
                            System.out.println("Current Gif Name:"+aKey);
                            if (aKey == null ? webcamstudio.WebcamStudio.cboAnimations.getItemAt(an).toString() == null : aKey.equals(webcamstudio.WebcamStudio.cboAnimations.getItemAt(an).toString())){
                                System.out.println("Loading Gif Key: "+aKey);
                                String res = webcamstudio.WebcamStudio.animations.getProperty(aKey);
                                System.out.println("Res: "+res);
                                URL url = WebcamStudio.class.getResource("/webcamstudio/resources/animations/" + res);
                                stream = new SourceImageGif(aKey, url);
                                extstream.add(stream);
                                extstreamBis.add(stream);
                                ImgMovMus.add("ImageGif");
                                readObject(stream, source);                    
                                int op=0;
                                for (SourceChannel scs : SCL) {
                                    scs.setName(SubChNames.get(op));
                                    String sNamet=SubChNames.get(op);
                                    if (!subSTrans.isEmpty()){
                                        if (subSTrans.get(op) != null) {
                                            if ("webcamstudio.channels.transitions.FadeIn".equals(subSTrans.get(op))){
                                                Transition t = Transition.getInstance(stream, "FadeIn");
                                                stream.addStartTransition(t);
                                                scs.startTransitions.add(stream.startTransitions.get(0));
                                }
                            }
                        }                                
                        if (subETrans.size() != 0){
                            if (subETrans.get(op) != null) {
                                if ("webcamstudio.channels.transitions.FadeOut".equals(subETrans.get(op))){
                                    Transition t = Transition.getInstance(stream, "FadeOut");
                                    stream.addEndTransition(t);
                                    scs.endTransitions.add(stream.endTransitions.get(0));
                                }
                            }
                        }
                        stream.addChannel(scs);                    
                        System.out.println("Add Channel: "+scs);
                        op+=1;                    
                    }
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                    System.out.println("Subs Cleared **** ");
                            }
                        }                   
                    System.out.println("Out of aGifKeys Routine:");
                    }
                } else {
                    System.err.println("Cannot handle " + clazz);
                }
            }
            for (Stream dST : extstreamBis) {
                int multi=0;
                String streamName = dST.getName();
                System.out.println(" ****** Stream Name: "+streamName);
                    for (String vDev : videoDevs){
                        if (vDev.contains(streamName)){
                                    multi += 1;
                        } 
                    }
                    if (multi>1) {
                        extstream.remove(dST);
                        ImgMovMus.remove("/dev/"+streamName);
                        System.out.println(dST+" Removed ****");
                        System.out.println(streamName+" Removed ****");
                        multi=0;
                }                 
             }          
        }   
    }

    private static void readObject(Stream stream, Node source) throws IllegalArgumentException, IllegalAccessException {
        XPath path = XPathFactory.newInstance().newXPath();

        Field[] fields = stream.getClass().getDeclaredFields();
        Field[] superFields = stream.getClass().getSuperclass().getDeclaredFields();
        // Read integer and floats
        for (Field field : superFields) {
            field.setAccessible(true);
            String name = field.getName();
            String value = null;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                if (field.get(stream) instanceof Integer) {
                    field.setInt(stream, new Integer(value));
                } else if (field.get(stream) instanceof Float) {
                    field.setFloat(stream, new Float(value));
                } else if (field.get(stream) instanceof Boolean) {
                    field.setBoolean(stream, new Boolean (value));
                } else if (field.get(stream) instanceof String) {
                    System.out.println("Field String: "+field.get(stream));
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
            System.out.println(name);
            String value = null;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                System.out.println(value);
                if (field.get(stream) instanceof Integer) {
                    field.setInt(stream, new Integer(value));
                } else if (field.get(stream) instanceof Float) {
                    field.setFloat(stream, new Float(value));
                } else if (field.get(stream) instanceof Boolean) {
                    field.setBoolean(stream, new Boolean (value));
                } else if (field.get(stream) instanceof String) {
                    System.out.println("Field String: "+field.get(stream));
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
    private static void readObjectSC(SourceChannel sc, Node source) throws IllegalArgumentException, IllegalAccessException {
        XPath path = XPathFactory.newInstance().newXPath();

        Field[] fields = sc.getClass().getDeclaredFields();
        Field[] superFields = sc.getClass().getSuperclass().getDeclaredFields();
        // Read integer and floats
        for (Field field : superFields) {
            field.setAccessible(true);
            String name = field.getName();
            String value = null;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                if (field.get(sc) instanceof Integer) {
                    field.setInt(sc, new Integer(value));
                } else if (field.get(sc) instanceof Float) {
                    field.setFloat(sc, new Float(value));
                } else if (field.get(sc) instanceof Boolean) {
                    field.setBoolean(sc, new Boolean (value));
                } else if (field.get(sc) instanceof String) {
                    System.out.println("Field String: "+field.get(sc));
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
            System.out.println(name);
            String value = null;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                System.out.println(value);
                if (field.get(sc) instanceof Integer) {
                    field.setInt(sc, new Integer(value));
                } else if (field.get(sc) instanceof Float) {
                    field.setFloat(sc, new Float(value));
                } else if (field.get(sc) instanceof Boolean) {
                    field.setBoolean(sc, new Boolean (value));
                } else if (field.get(sc) instanceof String) {
                    System.out.println("Field String: "+field.get(sc));
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
