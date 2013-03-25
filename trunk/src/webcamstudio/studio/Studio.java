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
import webcamstudio.channels.MasterChannels;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.streams.*;
import webcamstudio.util.Tools;
import webcamstudio.components.*;

/**
 *
 * @author patrick
 */
public class Studio {

    private ArrayList<String> channels = MasterChannels.getInstance().getChannels();
    private ArrayList<Integer> Durations = ChannelPanel.CHTimers;
    private ArrayList<String> nextChannel = ChannelPanel.CHCurrNext;
    public static ArrayList<String> channez = MasterChannels.getInstance().getChannels();
    public static MasterChannels channelSC = MasterChannels.getInstance();
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
    static boolean FirstChannel=false;
    protected Studio() {
    }
    public ArrayList<String> getStrings() {
        return ImgMovMus;
    }
    public ArrayList<String> getChannels() {
        return channels;
    }
    
    public ArrayList<Stream> getStreams() {
        return streams;
    }
// Void removed, reput Studio
    public static Studio load(File file) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
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

        return studio;
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
            System.out.println(streams);
            xml.writeStartElement(ELEMENT_SOURCE);
            writeObject(s, xml);
            xml.writeEndElement(); // source
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
            Tools.sleep(50);
            xml.writeAttribute("clazz", clazz);
        }
        Tools.sleep(50);
        if (superFields != null) {
            for (Field f : superFields) {
                f.setAccessible(true);
                String name = f.getName();
                System.out.println("SuperFields: "+name);
                Object value = f.get(o);
                System.out.println("Value: "+value);
                if (value instanceof Integer) {
                    xml.writeAttribute(name, f.getInt(o) + "");
                } else if (value instanceof Float) {
                    xml.writeAttribute(name, f.getFloat(o) + "");
                } else if (value instanceof Boolean) {
                    xml.writeAttribute(name, f.getBoolean(o) + "");
                }

            }
        }
        Tools.sleep(50);
        for (Field f : fields) {
            f.setAccessible(true);
            String name = f.getName();
            System.out.println("Fields: "+name);
            Object value = f.get(o);
            if (value instanceof Integer) {
                xml.writeAttribute(name, f.getInt(o) + "");
            } else if (value instanceof Float) {
                xml.writeAttribute(name, f.getFloat(o) + "");
            } else if (value instanceof Boolean) {
                    xml.writeAttribute(name, f.getBoolean(o) + "");
            }
        }
        Tools.sleep(50);        
        if (superFields != null) {
            for (Field f : superFields) {
                f.setAccessible(true);
                String name = f.getName();
                System.out.println("Superfield2: "+name);
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
        Tools.sleep(50);
        for (Field f : fields) {
            f.setAccessible(true);
            String name = f.getName();
            System.out.println("Fields2: "+name);
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
        Tools.sleep(50);
        if (superFields != null) {
            for (Field f : superFields) {
                f.setAccessible(true);
                String name = f.getName();
                System.out.println("sub0Name: "+name);
                Object value = f.get(o);
                System.out.println("sub0ObjectValue: "+value);
                if (value instanceof List) { 
                    if ("channels".equals(name)) {
                        xml.writeStartElement(ELEMENT_CHANNELS);
                        for (Object subO : ((List) value)) {
                        if (clazz != null){
                        xml.writeStartElement(name);
                        System.out.println("sub0: "+subO);
                        writeObject(subO, xml);
                        xml.writeEndElement(); 
                        }
                        }
                    } else {
                    xml.writeStartElement(name);
                    for (Object subO : ((List) value)) {
                        if (clazz != null){
                        System.out.println("sub0: "+subO);
                        writeObject(subO, xml);
                        }
                        
                    }
                    }
                    xml.writeEndElement();
                }
            }
        }
        Tools.sleep(50);
        for (Field f : fields) {
            f.setAccessible(true);
            String name = f.getName();
            System.out.println("Fields3: "+name);
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
        if (sources != null) {
            for (int i = 0; i < sources.getLength(); i++) {
                Node source = sources.item(i);
                
                String clazz = source.getAttributes().getNamedItem("clazz").getTextContent();
                System.out.println(clazz);
                String file = null;
                String ObjText = null;
                ArrayList<String> SubChNames = new ArrayList<String>();
                ArrayList<String> SubText = new ArrayList<String>();
                ArrayList<String> SubFont = new ArrayList<String>();
                Stream stream = null;
                SourceChannel sc = null; 
                ArrayList<SourceChannel> SCL = new ArrayList<SourceChannel>();
                SourceText text = null;
                for (int j = 0; j < source.getChildNodes().getLength(); j++) {
                    Node child = source.getChildNodes().item(j);
                    if (child.getNodeName().equals("file")) {                       
                        file = child.getTextContent();
                        System.out.println(file);
                        ImgMovMus.add(file);
                    }
                    if (child.getNodeName().equals("content")) {
                        ObjText = child.getTextContent();
                        System.out.println(ObjText);
                    }
                    if (child.getNodeName().equals("Channels")) {
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
                            }  if (SSuperChild.getNodeName().equals("text") && SSuperChild.getTextContent() != null) {
                                System.out.println("SuperChild Text: "+SSuperChild.getTextContent());
                                SubText.add(SSuperChild.getTextContent());
                            }  if (SSuperChild.getNodeName().equals("font") && SSuperChild.getTextContent() != null) {
                                System.out.println("SuperChild Font: "+SSuperChild.getTextContent());
                                SubFont.add(SSuperChild.getTextContent());
                            }
                                
                            
                        } 
                    }
                  }
                }
                if (file != null) {
                    stream = Stream.getInstance(new File(file));
                    extstream.add(stream);  // extstream = stream;
                    readObject(stream, source);
                    int op=0;
                    for (SourceChannel scs : SCL) {
                    scs.setName(SubChNames.get(op));
                    stream.addChannel(scs);                    
                    System.out.println("Add Channel: "+scs);
                    op+=1;
                    }
                    SCL.clear();
                } else if (clazz.toLowerCase().endsWith("sourcedesktop")) {
                    stream = new SourceDesktop();                   
                    readObject(stream, source);
                    for (SourceChannel scs : SCL) {
                    stream.addChannel(scs);
                    }
                    SCL.clear();
                } else if (clazz.toLowerCase().endsWith("sourcetext")) {
                    text = new SourceText(ObjText);
                    Studio.LText.add(text);
                    readObject(text, source);
                    int op=0;
                    for (SourceChannel scs : SCL) {
                    scs.setName(SubChNames.get(op));
                    scs.setText(SubText.get(op));
                    scs.setFont(SubFont.get(op));
                    text.addChannel(scs);                    
                    System.out.println("Add Channel: "+scs);
                    op+=1;
                    }
                    SCL.clear();
                } else if (clazz.toLowerCase().endsWith("sourceqrcode")) {
                    stream = new SourceQRCode("");
                                      
                    readObject(stream, source);
                    for (SourceChannel scs : SCL) {
                    stream.addChannel(scs);
                    }
                    SCL.clear();
                } else if (clazz.toLowerCase().endsWith("sourcemicrophone")) {
                    stream = new SourceMicrophone();                    
                    readObject(stream, source);
                    for (SourceChannel scs : SCL) {
                    stream.addChannel(scs);
                    }
                    SCL.clear();
                } else {
                    System.err.println("Cannot handle " + clazz);
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
    public static void main() { // removed (String[] args) from main
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
