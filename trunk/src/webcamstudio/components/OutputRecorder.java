/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OutputRecorder.java
 *
 * Created on 15-Apr-2012, 1:28:32 AM
 */
package webcamstudio.components;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.TreeMap;
import javax.swing.JFileChooser;
import javax.swing.JToggleButton;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import webcamstudio.exporter.vloopback.VideoDevice;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.streams.SinkBroadcast;
import webcamstudio.streams.SinkFile;
import webcamstudio.streams.SinkLinuxDevice;
import webcamstudio.util.Tools;
import webcamstudio.util.Tools.OS;

/**
 *
 * @author patrick
 */
public class OutputRecorder extends javax.swing.JPanel {

    SinkFile fileStream = null;
    TreeMap<String, SinkBroadcast> broadcasts = new TreeMap<String, SinkBroadcast>();
    TreeMap<String, SinkLinuxDevice> devices = new TreeMap<String, SinkLinuxDevice>();

    /** Creates new form OutputRecorder */
    public OutputRecorder() {
        initComponents();


        if (Tools.getOS() == OS.LINUX) {
            for (VideoDevice d : VideoDevice.getInputDevices()) {
                JToggleButton button = new JToggleButton();
                button.setText(d.getName());
                button.setActionCommand(d.getFile().getAbsolutePath());
                button.setIcon(tglRecordToFile.getIcon());
                button.setSelectedIcon(tglRecordToFile.getSelectedIcon());
                button.setRolloverEnabled(false);
                button.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        String device = evt.getActionCommand();
                        JToggleButton button = ((JToggleButton) evt.getSource());
                        if (button.isSelected()) {
                            SinkLinuxDevice stream = new SinkLinuxDevice(new File(device), button.getText());
                            stream.setRate(MasterMixer.getInstance().getRate());
                            stream.setWidth(MasterMixer.getInstance().getWidth());
                            stream.setHeight(MasterMixer.getInstance().getHeight());
                            stream.read();
                            devices.put(button.getText(), stream);

                        } else {
                            SinkLinuxDevice stream = devices.get(button.getText());
                            if (stream != null) {
                                stream.stop();
                                devices.remove(button.getText());
                            }
                        }
                    }
                });
                this.add(button);
                this.revalidate();
            }
        }

        this.setDropTarget(new DropTarget() {

            public synchronized void drop(DropTargetDropEvent evt) {
                boolean dropSuccess = false;
                try {
                    evt.acceptDrop(DnDConstants.ACTION_REFERENCE);
                    if (Tools.getOS() == OS.LINUX) {
                        String files = evt.getTransferable().getTransferData(DataFlavor.stringFlavor).toString();
                        String[] lines = files.split("\n");
                        for (String line : lines) {
                            File file = new File(new URL(line.trim()).toURI());
                            if (file.exists() && file.getName().toLowerCase().endsWith("xml")) {
                                dropSuccess = true;
                                addButtonBroadcast(file);
                            }
                        }
                    } else if (Tools.getOS()==OS.WINDOWS){
                        List files = (List)evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        for (Object o : files){
                            File file = (File)o;
                            if (file.exists() && file.getName().toLowerCase().endsWith("xml")) {
                                dropSuccess = true;
                                addButtonBroadcast(file);
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                evt.dropComplete(dropSuccess);
                if (!dropSuccess){
                    ResourceMonitor.setMessage("Unsupported file");
                }
            }
        });
    }

    private void addButtonBroadcast(File xml) throws ParserConfigurationException, SAXException, IOException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
        NodeList nodesURL = doc.getDocumentElement().getElementsByTagName("url");
        NodeList nodeStreams = doc.getDocumentElement().getElementsByTagName("stream");
        NodeList nodeNames = doc.getDocumentElement().getElementsByTagName("name");
        JToggleButton button = new JToggleButton();

        String url = nodesURL.item(0).getTextContent();
        String stream = nodeStreams.item(0).getTextContent();
        String name = nodeNames.item(0).getTextContent();
        button.setText(name);
        button.setActionCommand(url + "/" + stream);
        button.setIcon(tglRecordToFile.getIcon());
        button.setSelectedIcon(tglRecordToFile.getSelectedIcon());
        button.setRolloverEnabled(false);
        button.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String url = evt.getActionCommand();
                JToggleButton button = ((JToggleButton) evt.getSource());
                if (button.isSelected()) {
                    SinkBroadcast broadcast = new SinkBroadcast(url, button.getText());
                    broadcast.read();
                    broadcasts.put(button.getText(), broadcast);
                } else {
                    SinkBroadcast broadcast = broadcasts.get(button.getText());
                    if (broadcast != null) {
                        broadcast.stop();
                        broadcasts.remove(button.getText());
                    }
                }
            }
        });
        this.add(button);
        this.revalidate();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tglRecordToFile = new javax.swing.JToggleButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("OUTPUT"))); // NOI18N
        setToolTipText(bundle.getString("DROP_OUTPUT")); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        tglRecordToFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-record.png"))); // NOI18N
        tglRecordToFile.setText(bundle.getString("RECORD")); // NOI18N
        tglRecordToFile.setName("tglRecordToFile"); // NOI18N
        tglRecordToFile.setRolloverEnabled(false);
        tglRecordToFile.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglRecordToFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglRecordToFileActionPerformed(evt);
            }
        });
        add(tglRecordToFile);
    }// </editor-fold>//GEN-END:initComponents

    private void tglRecordToFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglRecordToFileActionPerformed
        if (tglRecordToFile.isSelected()) {
            File f = null;
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("WebcamStudio");
            chooser.showSaveDialog(this);
            f = chooser.getSelectedFile();
            if (f != null) {
                fileStream = new SinkFile(f);
                fileStream.setWidth(MasterMixer.getInstance().getWidth());
                fileStream.setHeight(MasterMixer.getInstance().getHeight());
                fileStream.setRate(MasterMixer.getInstance().getRate());
                fileStream.read();
            }
        } else {
            if (fileStream != null) {
                fileStream.stop();
                fileStream = null;
            }
        }
    }//GEN-LAST:event_tglRecordToFileActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton tglRecordToFile;
    // End of variables declaration//GEN-END:variables
}
