/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.studio;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
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
import webcamstudio.channels.MasterChannels;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.streams.SourceDesktop;
import webcamstudio.streams.SourceText;
import webcamstudio.streams.Stream;

/**
 *
 * @author patrick
 */
public class Studio {

    ArrayList<String> channels = MasterChannels.getInstance().getChannels();
    ArrayList<Stream> streams = MasterChannels.getInstance().getStreams();

    protected Studio() {
    }

    public ArrayList<String> getChannels(){
        return channels;
    }
    public ArrayList<Stream> getStreams(){
        return streams;
    }
    
    public static Studio load(File file) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Studio studio = new Studio();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        XPath path = XPathFactory.newInstance().newXPath();
        
        //Loading channels
        NodeList nodeChannels = (NodeList)path.evaluate("/WebcamStudio/Channels/Channel", doc.getDocumentElement(),XPathConstants.NODESET);
        for (int i = 0;i<nodeChannels.getLength();i++){
            Node channel = nodeChannels.item(i);
            String name = channel.getAttributes().getNamedItem("name").getTextContent();
            System.out.println("Channel " + name);
            studio.channels.add(name);
        }
        
        // Loading mixer settings
        Node nodeMixer = (Node)path.evaluate("/WebcamStudio/Mixer", doc.getDocumentElement(),XPathConstants.NODE);
        String width = nodeMixer.getAttributes().getNamedItem("width").getTextContent();
        String height = nodeMixer.getAttributes().getNamedItem("height").getTextContent();
        String rate = nodeMixer.getAttributes().getNamedItem("rate").getTextContent();
        System.out.println("Mixer: " + width + "X" + height + "@" + rate + "fps");
        
        return studio;
    }

    public static void save(File file) throws IOException, XMLStreamException, TransformerConfigurationException, TransformerException {
        ArrayList<String> channels = MasterChannels.getInstance().getChannels();
        ArrayList<Stream> streams = MasterChannels.getInstance().getStreams();
        StringWriter writer = new StringWriter();

        XMLStreamWriter xml = javax.xml.stream.XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
        xml.writeStartDocument();
        xml.writeStartElement("WebcamStudio");
        //Channels
        xml.writeStartElement("Channels");
        for (String c : channels) {
            xml.writeStartElement("Channel");
            xml.writeAttribute("name", c);
            xml.writeEndElement();
        }
        xml.writeEndElement();

        xml.writeStartElement("Sources");
        for (Stream s : streams) {
            xml.writeStartElement("Source");
            xml.writeAttribute("id", s.getID());
            xml.writeAttribute("name", s.getName());
            if (s.getFile() != null) {
                xml.writeAttribute("file", s.getFile().getAbsolutePath());
            }
            if (s.getURL() != null) {
                xml.writeAttribute("url", s.getURL());
            }
            xml.writeAttribute("clazz", s.getClass().getCanonicalName());

            xml.writeStartElement("Format");
            xml.writeAttribute("x", s.getX() + "");
            xml.writeAttribute("y", s.getY() + "");
            xml.writeAttribute("captureWidth", s.getCaptureWidth() + "");
            xml.writeAttribute("captureHeight", s.getCaptureHeight() + "");
            xml.writeAttribute("width", s.getWidth() + "");
            xml.writeAttribute("height", s.getHeight() + "");
            xml.writeAttribute("zorder", s.getZOrder() + "");
            xml.writeAttribute("opacity", s.getOpacity() + "");
            xml.writeAttribute("volume", s.getVolume() + "");
            if (s instanceof SourceText) {
                SourceText t = (SourceText) s;
                xml.writeStartElement("content");
                xml.writeAttribute("font", t.getFont());
                xml.writeAttribute("color", t.getColor() + "");
                xml.writeAttribute("bgColor", t.getBackgroundColor() + "");
                xml.writeAttribute("bgOpacity", t.getBackgroundOpacity() + "");
                xml.writeCData(t.getContent());
                xml.writeEndElement();
            }
            if (s instanceof SourceDesktop) {
                SourceDesktop d = (SourceDesktop) s;
                xml.writeStartElement("desktop");
                xml.writeAttribute("x", d.getDesktopX() + "");
                xml.writeAttribute("y", d.getDesktopY() + "");
                xml.writeAttribute("w", d.getDesktopW() + "");
                xml.writeAttribute("h", d.getDesktopH() + "");
                xml.writeEndElement();
            }
            xml.writeEndElement(); //format
            xml.writeEndElement(); // source
        }
        xml.writeEndElement();  //Sources

        xml.writeStartElement("Mixer");
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
}
