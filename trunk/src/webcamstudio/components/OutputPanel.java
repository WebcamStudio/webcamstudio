/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OutputPanel.java
 *
 * Created on 15-Apr-2012, 1:28:32 AM
 */
package webcamstudio.components;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JToggleButton;
import webcamstudio.channels.MasterChannels;
import webcamstudio.exporter.vloopback.VideoDevice;
import webcamstudio.externals.FME;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.streams.SinkBroadcast;
import webcamstudio.streams.SinkUDP;
import webcamstudio.streams.SinkFile;
import webcamstudio.streams.SinkLinuxDevice;
import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;
import webcamstudio.util.Tools.OS;

/**
 *
 * @author patrick (modified by karl)
 */
public class OutputPanel extends javax.swing.JPanel implements Stream.Listener {

    TreeMap<String, SinkFile> files = new TreeMap<String, SinkFile>();
    TreeMap<String, SinkBroadcast> broadcasts = new TreeMap<String, SinkBroadcast>();
    TreeMap<String, SinkLinuxDevice> devices = new TreeMap<String, SinkLinuxDevice>();
    TreeMap<String, SinkUDP> udpOut = new TreeMap<String, SinkUDP>();
    TreeMap<String, FME> fmes = new TreeMap<String, FME>();
    TreeMap<String, ResourceMonitorLabel> labels = new TreeMap<String, ResourceMonitorLabel>();

    /** Creates new form OutputPanel */
    public OutputPanel() {
        initComponents();
        final OutputPanel instanceSink = this;
        if (Tools.getOS() == OS.LINUX) {
            for (VideoDevice d : VideoDevice.getInputDevices()) {
                JToggleButton button = new JToggleButton();
                button.setText(d.getName());
                button.setActionCommand(d.getFile().getAbsolutePath());
                button.setIcon(tglRecordToFile.getIcon());
                button.setSelectedIcon(tglRecordToFile.getSelectedIcon());
                button.setRolloverEnabled(false);
                button.addActionListener(new java.awt.event.ActionListener() {                    
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        String device = evt.getActionCommand();
                        JToggleButton button = ((JToggleButton) evt.getSource());
                        if (button.isSelected()) {
                            SinkLinuxDevice stream = new SinkLinuxDevice(new File(device), button.getText());
                            stream.setRate(MasterMixer.getInstance().getRate());
                            stream.setWidth(MasterMixer.getInstance().getWidth());
                            stream.setHeight(MasterMixer.getInstance().getHeight());
                            stream.setListener(instanceSink);
                            stream.read();
                            devices.put(button.getText(), stream);
                            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Rendering to " + button.getText());
                            labels.put(button.getText(), label);
                            ResourceMonitor.getInstance().addMessage(label);

                        } else {
                            SinkLinuxDevice stream = devices.get(button.getText());
                            if (stream != null) {
                                stream.stop();
                                devices.remove(button.getText());
                                ResourceMonitorLabel label = labels.remove(button.getText());
                                ResourceMonitor.getInstance().removeMessage(label);
                            }
                        }
                    }
                });
                this.add(button);
                this.revalidate();
            }
        }
        
        this.setDropTarget(new DropTarget() {

        @Override
        public synchronized void drop(DropTargetDropEvent evt) {
            try {
                    String fileName = "";
                    evt.acceptDrop(DnDConstants.ACTION_REFERENCE);
                    boolean success = false;
                    DataFlavor dataFlavor = null;
                    if (evt.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dataFlavor = DataFlavor.javaFileListFlavor;
                    } else if (evt.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        dataFlavor = DataFlavor.stringFlavor;
                    } else {
                        for (DataFlavor d : evt.getTransferable().getTransferDataFlavors()) {
                            if (evt.getTransferable().isDataFlavorSupported(d)) {
                                System.out.println("Supported: " + d.getDefaultRepresentationClassAsString());
                                dataFlavor = d;
                                break;
                            }
                        }
                    }
                    Object data = evt.getTransferable().getTransferData(dataFlavor);
                    String files = "";
                    System.out.println(data.getClass().getCanonicalName());
                    if (data instanceof Reader) {
                        Reader reader = (Reader) data;
                        char[] text = new char[65536];
                        int count = reader.read(text);
                        files = new String(text).trim();
                    } else if (data instanceof InputStream) {
                        InputStream list = (InputStream) data;
                        java.io.InputStreamReader reader = new java.io.InputStreamReader(list);
                        char[] text = new char[65536];
                        int count = reader.read(text);
                        files = new String(text).trim();
                    } else if (data instanceof String) {
                        files = data.toString().trim();
                    } else {
                        List list = (List) data;
                        for (Object o : list) {
                            files += new File(o.toString()).toURI().toURL().toString() + "\n";
                        }
                    }
                    if (files.length() > 0) {
                        String[] lines = files.split("\n");
                        for (String line : lines) {
                            File file = new File(new URL(line.trim()).toURI());
                            fileName = file.getName();
                            if (file.exists() && file.getName().toLowerCase().endsWith("xml")) {
                                success = true;
                                FME fme = new FME(file);
                                fmes.put(fme.getName(), fme);
                                addButtonBroadcast(fme);
                            }
                        }
                    }
                    evt.dropComplete(success);
                    if (!success) {
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 5000, "Unsupported file: " + fileName);
                        ResourceMonitor.getInstance().addMessage(label);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void loadPrefs(Preferences prefs) {
        Preferences fmePrefs = prefs.node("fme");
        try {
            String[] services = fmePrefs.childrenNames();
            for (String s : services) {
                Preferences service = fmePrefs.node(s);
                String url = service.get("url", "");
                String name = service.get("name", "");
                String abitrate = service.get("abitrate", "");
                String vbitrate = service.get("vbitrate", "");
                String vcodec = service.get("vcodec", "");
                String acodec = service.get("acodec", "");
                String width = service.get("width", "");
                String height = service.get("height", "");
                String stream = service.get("stream", "");
                FME fme = new FME(url, stream, name, abitrate, vbitrate, vcodec, acodec, width, height);
                fmes.put(fme.getName(), fme);
                addButtonBroadcast(fme);
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void savePrefs(Preferences prefs) {
        Preferences fmePrefs = prefs.node("fme");
        try {
            fmePrefs.removeNode();
            fmePrefs.flush();
            fmePrefs = prefs.node("fme");
        } catch (BackingStoreException ex) {
            Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (FME fme : fmes.values()) {
            Preferences service = fmePrefs.node(fme.getName());
            service.put("url", fme.getUrl());
            service.put("name", fme.getName());
            service.put("abitrate", fme.getAbitrate());
            service.put("vbitrate", fme.getVbitrate());
            service.put("vcodec", fme.getVcodec());
            service.put("acodec", fme.getAcodec());
            service.put("width", fme.getWidth());
            service.put("height", fme.getHeight());
            service.put("stream", fme.getStream());
        }
    }

    private void addButtonBroadcast(FME fme) {
        final OutputPanel instanceSink = this;
        JToggleButton button = new JToggleButton();
        button.setText(fme.getName());
        button.setActionCommand(fme.getUrl()+"/"+fme.getStream());
        button.setIcon(tglRecordToFile.getIcon());
        button.setSelectedIcon(tglRecordToFile.getSelectedIcon());
        button.setRolloverEnabled(false);
        button.setToolTipText("Drag to the right to remove...");
        button.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JToggleButton button = ((JToggleButton) evt.getSource());
                FME fme = fmes.get(button.getText());
                if (button.isSelected()) {
                    if (fme != null){
                    SinkBroadcast broadcast = new SinkBroadcast(fme);
                    broadcast.setRate(MasterMixer.getInstance().getRate());
                    broadcast.setWidth(MasterMixer.getInstance().getWidth());
                    broadcast.setHeight(MasterMixer.getInstance().getHeight());
                    broadcast.setListener(instanceSink);
                    broadcast.read();
                    broadcasts.put(button.getText(), broadcast);
                    ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Broadcasting to " + fme.getName());
                    labels.put(fme.getName(), label);
                    ResourceMonitor.getInstance().addMessage(label);
                    } else {
                        button.setSelected(false);
                    }
                } else {
                    SinkBroadcast broadcast = broadcasts.get(button.getText());
                    if (broadcast != null) {
                        broadcast.stop();
                        broadcasts.remove(fme.getName());
                        ResourceMonitorLabel label = labels.get(fme.getName());
                        labels.remove(fme.getName());
                        ResourceMonitor.getInstance().removeMessage(label);
                    }
                }
            }
        });
        button.addMouseMotionListener(new java.awt.event.MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (e.getX() > getWidth()) {
                    JToggleButton button = ((JToggleButton) e.getSource());
                    if (!button.isSelected()) {
                        if (e.getX() > getWidth()) {
                            System.out.println(button.getText());
                            SinkBroadcast broadcast = broadcasts.remove(button.getText());
                            if (broadcast != null) {
                                MasterChannels.getInstance().unregister(broadcast);
                            }
                            FME fme = fmes.remove(button.getText());
                            ResourceMonitorLabel label = labels.remove(fme.getName());
                            remove(button);
                            revalidate();
                        }
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
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
        tglUDP = new javax.swing.JToggleButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("OUTPUT"))); // NOI18N
        setToolTipText(bundle.getString("DROP_OUTPUT")); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        tglRecordToFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-record.png"))); // NOI18N
        tglRecordToFile.setText(bundle.getString("RECORD")); // NOI18N
        tglRecordToFile.setToolTipText("Save to File. Remeber to add video extension (.avi or .mp4).");
        tglRecordToFile.setName("tglRecordToFile"); // NOI18N
        tglRecordToFile.setRolloverEnabled(false);
        tglRecordToFile.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglRecordToFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglRecordToFileActionPerformed(evt);
            }
        });
        add(tglRecordToFile);

        tglUDP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-record.png"))); // NOI18N
        tglUDP.setText(bundle.getString("UDP_MPEG_OUT")); // NOI18N
        tglUDP.setToolTipText("Stream to udp://@127.0.0.1:7000");
        tglUDP.setName("tglUDP"); // NOI18N
        tglUDP.setRolloverEnabled(false);
        tglUDP.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglUDP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglUDPActionPerformed(evt);
            }
        });
        add(tglUDP);
    }// </editor-fold>//GEN-END:initComponents

    private void tglRecordToFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglRecordToFileActionPerformed
        if (tglRecordToFile.isSelected()) {
            File f = null;
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("Choose Destination File ... >> Add .avi or .mp4 Extension !!! <<");
            chooser.showSaveDialog(this);
            f = chooser.getSelectedFile();
            if (f != null) {
                SinkFile fileStream = new SinkFile(f);
                fileStream.setWidth(MasterMixer.getInstance().getWidth());
                fileStream.setHeight(MasterMixer.getInstance().getHeight());
                fileStream.setRate(MasterMixer.getInstance().getRate());
                fileStream.setListener(instanceSink);
                fileStream.read();
                files.put("RECORD", fileStream);
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Recording to " + f.getName());
                labels.put("RECORD", label);
                ResourceMonitor.getInstance().addMessage(label);
            } else {
                tglRecordToFile.setSelected(false);
            }
        } else {
            SinkFile fileStream = files.get("RECORD");
            if (fileStream != null) {
                fileStream.stop();
                fileStream = null;
                files.remove("RECORD");
                ResourceMonitorLabel label = labels.get("RECORD");
                ResourceMonitor.getInstance().removeMessage(label);
            }
        }
        
    }//GEN-LAST:event_tglRecordToFileActionPerformed

    private void tglUDPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglUDPActionPerformed
        if (tglUDP.isSelected()) {       
                SinkUDP udpStream = new SinkUDP();
                udpStream.setWidth(MasterMixer.getInstance().getWidth());
                udpStream.setHeight(MasterMixer.getInstance().getHeight());
                udpStream.setRate(MasterMixer.getInstance().getRate());
                udpStream.setListener(instanceSink);
                udpStream.read();
                udpOut.put("UDPOut", udpStream);
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Unicast mpeg2 to udp://127.0.0.1:7000");
                labels.put("UDPOut", label);
                ResourceMonitor.getInstance().addMessage(label);
        } else {
            SinkUDP udpStream = udpOut.get("UDPOut");
            if (udpStream != null) {
                udpStream.stop();
                udpStream = null;
                udpOut.remove("UDPOut");
                ResourceMonitorLabel label = labels.get("UDPOut");
                ResourceMonitor.getInstance().removeMessage(label);
            }
        }
        
    }//GEN-LAST:event_tglUDPActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton tglRecordToFile;
    final OutputPanel instanceSink = this;
    private javax.swing.JToggleButton tglUDP;
    // End of variables declaration//GEN-END:variables

    
    @Override
    public void sourceUpdated(Stream stream) {
        if (stream instanceof SinkFile) {
            tglRecordToFile.setSelected(stream.isPlaying());
        } else if (stream instanceof SinkUDP) {
            tglUDP.setSelected(stream.isPlaying());
        } else if (stream instanceof SinkBroadcast) {
            String name = stream.getName();
            for (Component c : this.getComponents()) {
                if (c instanceof JToggleButton) {
                    JToggleButton b = (JToggleButton) c;
                    if (b.getText().equals(name)) {
                        b.setSelected(stream.isPlaying());
                    }
                }
            }
                    }  else if (stream instanceof SinkLinuxDevice) {
            String name = stream.getName();
            for (Component c : this.getComponents()) {
                if (c instanceof JToggleButton) {
                    JToggleButton b = (JToggleButton) c;
                    if (b.getText().equals(name)) {
                        b.setSelected(stream.isPlaying());
                    }
                }
            }
            
                    } 
                }

    @Override
    public void updatePreview(BufferedImage image) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
