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
import java.net.URL;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JToggleButton;
import webcamstudio.exporter.vloopback.VideoDevice;
import webcamstudio.ffmpeg.FME;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.streams.SinkBroadcast;
import webcamstudio.streams.SinkFile;
import webcamstudio.streams.SinkLinuxDevice;
import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;
import webcamstudio.util.Tools.OS;

/**
 *
 * @author patrick
 */
public class OutputRecorder extends javax.swing.JPanel implements Stream.Listener {

    TreeMap<String, SinkFile> files = new TreeMap<String, SinkFile>();
    TreeMap<String, SinkBroadcast> broadcasts = new TreeMap<String, SinkBroadcast>();
    TreeMap<String, SinkLinuxDevice> devices = new TreeMap<String, SinkLinuxDevice>();
    TreeMap<String, FME> fmes = new TreeMap<String, FME>();
    TreeMap<String, ResourceMonitorLabel> labels = new TreeMap<String, ResourceMonitorLabel>();

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
                            ResourceMonitorLabel label = new ResourceMonitorLabel(0, "Rendering to " + button.getText());
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

            public synchronized void drop(DropTargetDropEvent evt) {
                boolean dropSuccess = false;
                String fileName = "";

                try {
                    evt.acceptDrop(DnDConstants.ACTION_REFERENCE);
                    if (Tools.getOS() == OS.LINUX) {
                        String files = evt.getTransferable().getTransferData(DataFlavor.stringFlavor).toString();
                        String[] lines = files.split("\n");
                        for (String line : lines) {
                            File file = new File(new URL(line.trim()).toURI());
                            fileName = file.getName();
                            if (file.exists() && file.getName().toLowerCase().endsWith("xml")) {
                                dropSuccess = true;
                                FME fme = new FME(file);
                                fmes.put(fme.getName(), fme);
                                addButtonBroadcast(fme);
                            }
                        }
                    } else if (Tools.getOS() == OS.WINDOWS) {
                        List files = (List) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        for (Object o : files) {
                            File file = (File) o;
                            if (file.exists() && file.getName().toLowerCase().endsWith("xml")) {
                                dropSuccess = true;
                                FME fme = new FME(file);
                                fmes.put(fme.getName(), fme);
                                addButtonBroadcast(fme);
                            }
                            fileName = file.getName();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                evt.dropComplete(dropSuccess);
                if (!dropSuccess) {
                    ResourceMonitor.getInstance().addMessage(new ResourceMonitorLabel(System.currentTimeMillis() + 5000, "Unsupported file: " + fileName));
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
            Logger.getLogger(OutputRecorder.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void savePrefs(Preferences prefs) {
        Preferences fmePrefs = prefs.node("fme");
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
        final OutputRecorder instance = this;
        JToggleButton button = new JToggleButton();
        button.setText(fme.getName());
        button.setActionCommand(fme.getUrl() + "/" + fme.getStream());
        button.setIcon(tglRecordToFile.getIcon());
        button.setSelectedIcon(tglRecordToFile.getSelectedIcon());
        button.setRolloverEnabled(false);
        button.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JToggleButton button = ((JToggleButton) evt.getSource());
                FME fme = fmes.get(button.getText());
                if (button.isSelected()) {
                    SinkBroadcast broadcast = new SinkBroadcast(fme);
                    broadcast.setListener(instance);
                    broadcast.read();
                    broadcasts.put(button.getText(), broadcast);
                    ResourceMonitorLabel label = new ResourceMonitorLabel(0, "Broadcasting to " + fme.getName());
                    labels.put(fme.getName(), label);
                    ResourceMonitor.getInstance().addMessage(label);
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
                SinkFile fileStream = new SinkFile(f);
                fileStream.setWidth(MasterMixer.getInstance().getWidth());
                fileStream.setHeight(MasterMixer.getInstance().getHeight());
                fileStream.setRate(MasterMixer.getInstance().getRate());
                fileStream.read();
                files.put("RECORD", fileStream);
                ResourceMonitorLabel label = new ResourceMonitorLabel(0, "Recording in " + f.getName());
                labels.put("RECORD", label);
                ResourceMonitor.getInstance().addMessage(label);
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton tglRecordToFile;
    // End of variables declaration//GEN-END:variables

    @Override
    public void sourceUpdated(Stream stream) {
        tglRecordToFile.setSelected(stream.isPlaying());
    }
}
