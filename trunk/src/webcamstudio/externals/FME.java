/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.externals;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author patrick
 */
public class FME {

    private String url = "";
    private String name = "";
    private String abitrate = "";
    private String vbitrate = "";
    private String vcodec = "";
    private String acodec = "";
    private String width = "";
    private String height = "";
    private String stream = "";
    
    public FME(File xml) {
        try{
            parse(xml);
        }catch (Exception e ){
            e.printStackTrace();
        }
    }
    
    public FME(String url,String stream, String name, String abitrate,String vbitrate, String vcodec, String acodec, String width, String height){
        this.name = name;
        this.width = width;
        this.height = height;
        this.vcodec = vcodec;
        this.vbitrate = vbitrate;
        this.acodec = acodec;
        this.abitrate = abitrate;
        this.url = url;
        this.stream = stream;
    }
    private void parse(File xml) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        XPath path = XPathFactory.newInstance().newXPath();
        String root = "/flashmediaencoder_profile";
        
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
        name = (String)path.evaluate(root + "/preset/name", doc,XPathConstants.STRING);
        if (name==null || name.length()==0){
            root = "/flashmedialiveencoder_profile";
            name = (String)path.evaluate(root + "/preset/name", doc,XPathConstants.STRING);
        }
        width = (String)path.evaluate(root + "/capture/video/size/width", doc,XPathConstants.STRING);
        height = (String)path.evaluate(root + "/capture/video/size/height", doc,XPathConstants.STRING);
        vcodec = (String)path.evaluate(root + "/encode/video/format", doc,XPathConstants.STRING);
        vbitrate = (String)path.evaluate(root + "/encode/video/datarate", doc,XPathConstants.STRING);
        if (vbitrate.indexOf(";")!=-1){
            vbitrate = vbitrate.substring(0, vbitrate.indexOf(";"));
        }
        acodec = (String)path.evaluate(root + "/encode/audio/format", doc,XPathConstants.STRING);
        abitrate = (String)path.evaluate(root + "/encode/audio/datarate", doc,XPathConstants.STRING);
        url = (String)path.evaluate(root + "/output/rtmp/url", doc,XPathConstants.STRING);
        stream = (String)path.evaluate(root + "/output/rtmp/stream", doc,XPathConstants.STRING);

//        System.out.println(getName());
//        System.out.println(getWidth()+"X"+getHeight());
//        System.out.println(getVcodec() + "/" + getAcodec());
//        System.out.println(getVbitrate() + "/" + getAbitrate());
//        System.out.println(getUrl()+"/"+getStream());
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the abitrate
     */
    public String getAbitrate() {
        return abitrate;
    }

    /**
     * @return the vbitrate
     */
    public String getVbitrate() {
        return vbitrate;
    }

    /**
     * @return the vcodec
     */
    public String getVcodec() {
        return vcodec;
    }

    /**
     * @return the acodec
     */
    public String getAcodec() {
        return acodec;
    }

    /**
     * @return the width
     */
    public String getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public String getHeight() {
        return height;
    }

    /**
     * @return the stream
     */
    public String getStream() {
        return stream;
    }
}
