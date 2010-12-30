/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author pballeux
 */
public class VideoSourceWidget extends VideoSource {

    private URL data = null;
    private String author = "";
    private String description = "";
    private TreeMap<String, String> treeValues = new TreeMap<String, String>();

    public VideoSourceWidget(URL url) {
        location = url.toString();
        frameRate = 1;
        try {
            loadXML(location, false);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(VideoSourceWidget.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(VideoSourceWidget.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(VideoSourceWidget.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    public VideoSourceWidget() {
    }

    private void loadData(String url) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(url);
        treeValues.clear();
        buildTree(doc.getChildNodes(), "");

    }

    private void buildTree(NodeList nodes, String path) {
        for (int i = 0; i < nodes.getLength(); i++) {
            String key = "";
            String attrKey = "";
            String value = "";
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                key = getNextKey(path, nodes.item(i).getNodeName());
                value = nodes.item(i).getTextContent().trim();
                treeValues.put(key, value);
                for (int j = 0; j < nodes.item(i).getAttributes().getLength(); j++) {
                    attrKey = getNextKey(key, nodes.item(i).getAttributes().item(j).getNodeName());
                    value = nodes.item(i).getAttributes().item(j).getTextContent();
                    treeValues.put(attrKey, value);
                }
                buildTree(nodes.item(i).getChildNodes(), key);
            }

        }
    }

    private String getNextKey(String path, String key) {
        String retValue = path + "." + key;
        int index = 1;
        while (treeValues.containsKey(retValue)) {
            retValue = path + "." + key + "." + index++;
        }
        if (retValue.startsWith(".")) {
            retValue = retValue.substring(1);
        }
        //System.out.println(retValue);
        return retValue;
    }

    private void loadXML(String url, boolean render) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(url);
        NodeList root = doc.getElementsByTagName("ws4gl");
        Node rootNode = root.item(0);
        Color bgColor = null;
        int color = 0;
        int alpha = 0;
        //Initialize parameters

        for (int i = 0; i < rootNode.getAttributes().getLength(); i++) {
            Node attr = rootNode.getAttributes().item(i);
            if (attr.getNodeName().equalsIgnoreCase("width")) {
                captureWidth = new Integer(attr.getTextContent());
            } else if (attr.getNodeName().equalsIgnoreCase("height")) {
                captureHeight = new Integer(attr.getTextContent());
            } else if (attr.getNodeName().equalsIgnoreCase("updatefrequency")) {
                updateTimeLaspe = new Integer(attr.getTextContent()) * 1000 * 60;  //Value is in minutes
            } else if (render && attr.getNodeName().equalsIgnoreCase("xmldataurl")) {
                data = new URL(attr.getNodeValue());
                loadData(data.toString());
            } else if (attr.getNodeName().equalsIgnoreCase("bgcolor")) {
                color = Integer.decode(attr.getTextContent());
            } else if (attr.getNodeName().equalsIgnoreCase("bgalpha")) {
                alpha = Integer.decode(attr.getTextContent());
            }
        }
        bgColor = new Color(color, true);
        bgColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), alpha);
        // Init image
        tempimage = graphicConfiguration.createCompatibleImage(captureWidth, captureHeight, java.awt.image.BufferedImage.TRANSLUCENT);

        //Go over all the items in the list
        NodeList elements = rootNode.getChildNodes();
        for (int i = 0; i < elements.getLength(); i++) {
            Node item = elements.item(i);
            if (item.getNodeName().equalsIgnoreCase("name")) {
                name = item.getTextContent();
            } else if (item.getNodeName().equalsIgnoreCase("description")) {
                description = item.getTextContent();
            } else if (item.getNodeName().equalsIgnoreCase("author")) {
                author = item.getTextContent();
            } else if (render && item.getNodeName().equalsIgnoreCase("image")) {
                drawImage(item);

            } else if (render && item.getNodeName().equalsIgnoreCase("text")) {
                drawText(item);
            }
        }
        applyEffects(tempimage);
        applyShape(tempimage);
        image = tempimage;
    }

    private void drawImage(Node img) throws MalformedURLException, IOException {
        Graphics2D g = tempimage.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        int x = 0;
        int y = 0;
        int w = 0;
        int h = 0;
        URL imgURL = null;
        for (int i = 0; i < img.getAttributes().getLength(); i++) {
            Node item = img.getAttributes().item(i);
            if (item.getNodeName().equalsIgnoreCase("x")) {
                x = new Integer(item.getTextContent());
            } else if (item.getNodeName().equalsIgnoreCase("y")) {
                y = new Integer(item.getTextContent());
            } else if (item.getNodeName().equalsIgnoreCase("width")) {
                w = new Integer(item.getTextContent());
            } else if (item.getNodeName().equalsIgnoreCase("height")) {
                h = new Integer(item.getTextContent());
            } else if (item.getNodeName().equalsIgnoreCase("url")) {
                imgURL = new URL(decodeValues(item.getTextContent()));
            }
        }
        if (imgURL != null) {
            BufferedImage tmp = javax.imageio.ImageIO.read(imgURL);
            g.drawImage(tmp, x, y, x + w, y + h, 0, 0, tmp.getWidth(), tmp.getHeight(), null);
        } else {
            System.out.println("Image URL = NULL");
        }

    }

    private void drawText(Node text) {
        Graphics2D g = tempimage.createGraphics();
        fontSize = 12;
        int style = Font.PLAIN;
        String value = text.getTextContent();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        int x = 0;
        int y = 0;
        for (int i = 0; i < text.getAttributes().getLength(); i++) {
            Node item = text.getAttributes().item(i);
            if (item.getNodeName().equalsIgnoreCase("x")) {
                x = new Integer(item.getTextContent());
            } else if (item.getNodeName().equalsIgnoreCase("y")) {
                y = new Integer(item.getTextContent());
            } else if (item.getNodeName().equalsIgnoreCase("color")) {
                g.setColor(new Color(Integer.decode(item.getTextContent())));
            } else if (item.getNodeName().equalsIgnoreCase("fontname")) {
                fontName = item.getTextContent();
            } else if (item.getNodeName().equalsIgnoreCase("fontsize")) {
                fontSize = new Integer(item.getTextContent());
            } else if (item.getNodeName().equalsIgnoreCase("bold")) {
                if (item.getTextContent().equalsIgnoreCase("true")) {
                    style += Font.BOLD;
                }
            } else if (item.getNodeName().equalsIgnoreCase("italic")) {
                if (item.getTextContent().equalsIgnoreCase("true")) {
                    style += Font.ITALIC;
                }
            }
        }
        Font font = new Font(fontName, style, fontSize);
        g.setFont(font);
        g.drawString(decodeValues(value), x, y);
    }

    private String decodeValues(String value) {
        String retValue = value + "";
        int startIndex = 0;
        int endIndex = 0;
        String key = "";
        startIndex = retValue.indexOf('[');
        while (startIndex != -1) {
            endIndex = retValue.indexOf(']', startIndex);
            key = retValue.substring(startIndex + 1, endIndex);
            //System.out.println("KEY: " + key);
            if (treeValues.containsKey(key)) {
                retValue = retValue.substring(0, startIndex) + treeValues.get(key) + retValue.substring(endIndex + 1);
            }

            startIndex = retValue.indexOf('[', startIndex + 1);
        }
        return retValue;
    }

    public static void main(String[] args) {
        try {
            VideoSourceWidget source = new VideoSourceWidget(new URL("file:///home/pballeux/Desktop/widget.xml"));

            System.out.println("toString: " + source.toString());
            System.out.println("Name: " + source.getName());
            System.out.println("Description: " + source.description);
            System.out.println("Author: " + source.author);
            System.out.println("Data URL: " + source.data.toString());
            System.out.println("Width: " + source.getCaptureWidth());
            System.out.println("Height: " + source.getCaptureHeight());
            System.out.println("Frequency: " + source.getUpdateTimeLapse());
        } catch (MalformedURLException ex) {
            Logger.getLogger(VideoSourceWidget.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void startSource() {
        isPlaying = true;
        stopMe = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    loadXML(location, true);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(VideoSourceWidget.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(VideoSourceWidget.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(VideoSourceWidget.class.getName()).log(Level.SEVERE, null, ex);
                }
                long lastTimeStamp = System.currentTimeMillis();
                while (!stopMe) {
                    try {
                        Thread.sleep(1000);
                        if (updateTimeLaspe != 0 && (System.currentTimeMillis() - lastTimeStamp) > updateTimeLaspe) {
                            loadXML(location, true);
                            lastTimeStamp = System.currentTimeMillis();
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(VideoSourceWidget.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ParserConfigurationException ex) {
                        Logger.getLogger(VideoSourceWidget.class.getName()).log(Level.SEVERE, null, ex);
                        stopMe = true;
                    } catch (SAXException ex) {
                        Logger.getLogger(VideoSourceWidget.class.getName()).log(Level.SEVERE, null, ex);
                        stopMe = true;
                    } catch (IOException ex) {
                        Logger.getLogger(VideoSourceWidget.class.getName()).log(Level.SEVERE, null, ex);
                        stopMe = true;
                    }
                }
                isPlaying = false;
            }
        }).start();
    }

    @Override
    public void stopSource() {
        stopMe = true;
        isPlaying = false;
    }

    @Override
    public boolean canUpdateSource() {
        return true;
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void pause() {
    }

    @Override
    public void play() {
    }

    public String toString() {
        return "Widget: " + name;
    }

    @Override
    public java.util.Collection<JPanel> getControls() {
        java.util.Vector<JPanel> list = new java.util.Vector<JPanel>();
        list.add(new webcamstudio.controls.ControlPosition(this));
        list.add(new webcamstudio.controls.ControlEffects(this));
        list.add(new webcamstudio.controls.ControlShapes(this));
        list.add(new webcamstudio.controls.ControlLayout(this));
        list.add(new webcamstudio.controls.ControlReload(this));
        return list;
    }

}
