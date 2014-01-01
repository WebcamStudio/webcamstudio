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

import java.awt.Color;
import java.awt.Component;
import webcamstudio.media.renderer.ProcessExecutor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import webcamstudio.WebcamStudio;
import webcamstudio.channels.MasterChannels;
import webcamstudio.exporter.vloopback.VideoDevice;
import webcamstudio.externals.FME;
import webcamstudio.externals.ProcessRenderer;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.streams.SinkAudio;
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
public class OutputPanel extends javax.swing.JPanel implements Stream.Listener, WebcamStudio.Listener, ChannelPanel.Listener {

    TreeMap<String, SinkFile> files = new TreeMap<>();
    TreeMap<String, SinkBroadcast> broadcasts = new TreeMap<>();
    TreeMap<String, SinkLinuxDevice> devices = new TreeMap<>();
    TreeMap<String, SinkUDP> udpOut = new TreeMap<>();
    TreeMap<String, SinkAudio> audioOut = new TreeMap<>();
    TreeMap<String, FME> fmes = new TreeMap<>();
    ProcessExecutor processSkyVideo;
    boolean skyCamMode = false;
    boolean iSkyCamFree = true;
    boolean iSkyCam = true;
    boolean flip = false;
    String skyRunComm = null;
    int camCount = 0;
    int fmeCount = 0;
    String virtualDevice = "webcamstudio";
    TreeMap<String, ResourceMonitorLabel> labels = new TreeMap<>();
    JFrame wDFrame;
    /** Creates new form OutputPanel
     * @param aFrame */
    public OutputPanel(JFrame aFrame) {
        initComponents();
        wDFrame = aFrame;
        final OutputPanel instanceSinkOP = this;
        WebcamStudio.setListenerOP(instanceSinkOP);
        ChannelPanel.setListenerCP(instanceSinkOP);
        if (Tools.getOS() == OS.LINUX) {
            paintWSCamButtons ();
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
                    if (data instanceof Reader) {
                        char[] text = new char[65536];
                        files = new String(text).trim();
                    } else if (data instanceof InputStream) {
                        char[] text = new char[65536];
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
                } catch (UnsupportedFlavorException | IOException | URISyntaxException ex) {
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
                String mount = service.get("mount", "");
                String password = service.get("password", "");
                String port = service.get("port", "");
                FME fme = new FME(url, stream, name, abitrate, vbitrate, vcodec, acodec, width, height, mount, password, port);
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
            service.put("mount", fme.getMount());
            service.put("password", fme.getPassword());
            service.put("port", fme.getPort());
        }
    }

    private void addButtonBroadcast(FME fme) {
        final OutputPanel instanceSinkFME = this;
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
                        fmeCount ++;
                        SinkBroadcast broadcast = new SinkBroadcast(fme);
                        UIManager.put("OptionPane.noButtonText", "HQ");
                        UIManager.put("OptionPane.yesButtonText", "Standard");
                        int resultHQ = JOptionPane.showConfirmDialog(instanceSinkFME,"HQ or Standard mode?","Choose",JOptionPane.YES_NO_OPTION);
                        switch(resultHQ){
                            case JOptionPane.YES_OPTION:
                                broadcast.setStandard("STD");
                                break;
                            case JOptionPane.NO_OPTION:
                                broadcast.setStandard("HQ");
                                break;
                            case JOptionPane.CLOSED_OPTION:
                                broadcast.setStandard("STD");
                                break;
                        }
                        UIManager.put("OptionPane.noButtonText", "No");
                        UIManager.put("OptionPane.yesButtonText", "Yes");     
                        broadcast.setRate(MasterMixer.getInstance().getRate());
                        broadcast.setWidth(MasterMixer.getInstance().getWidth());
                        broadcast.setHeight(MasterMixer.getInstance().getHeight());
                        broadcast.setListener(instanceSinkFME);
                        broadcast.read();
                        broadcasts.put(button.getText(), broadcast);
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Broadcasting to " + fme.getName());
                        labels.put(fme.getName(), label);
                        tglSkyCam.setEnabled(false);
                        ResourceMonitor.getInstance().addMessage(label);
                    } else {
                        fmeCount --;
                        button.setSelected(false);
                        if (fmeCount == 0 && camCount == 0) {
                            tglSkyCam.setEnabled(true);
                        }                        
                    }
                    System.out.println("FMECount = "+fmeCount);
                } else {
                    SinkBroadcast broadcast = broadcasts.get(button.getText());
                    if (broadcast != null) {
                        fmeCount --;
                        broadcast.stop();
                        broadcasts.remove(fme.getName());
                        ResourceMonitorLabel label = labels.get(fme.getName());
                        labels.remove(fme.getName());
                        System.out.println("FMECount = "+fmeCount);
                        if (fmeCount == 0 && camCount == 0) {
                            tglSkyCam.setEnabled(true);
                        }                        
                        ResourceMonitor.getInstance().removeMessage(label);
                    }
                    if (fmeCount == 0 && camCount == 0) {
                        tglSkyCam.setEnabled(true);
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

        tglSkyCam = new javax.swing.JToggleButton();
        jcbV4l2loopback = new javax.swing.JCheckBox();
        btnSkyFlip = new javax.swing.JToggleButton();
        tglAudioOut = new javax.swing.JToggleButton();
        tglRecordToFile = new javax.swing.JToggleButton();
        tglUDP = new javax.swing.JToggleButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("OUTPUT"))); // NOI18N
        setToolTipText(bundle.getString("DROP_OUTPUT")); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        tglSkyCam.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/camera-video.png"))); // NOI18N
        tglSkyCam.setText(bundle.getString("SKYCAM")); // NOI18N
        tglSkyCam.setToolTipText("Activate Skype Cam Compatibility");
        tglSkyCam.setName("tglSkyCam"); // NOI18N
        tglSkyCam.setRolloverEnabled(false);
        tglSkyCam.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/camera-video-on.png")));
        tglSkyCam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglSkyCamActionPerformed(evt);
            }
        });
        add(tglSkyCam);

        jcbV4l2loopback.setText("V4l2loopback");
        jcbV4l2loopback.setToolTipText("If selected SkyCam will use v4l2loopback module");
        jcbV4l2loopback.setName("jcbV4l2loopback"); // NOI18N
        jcbV4l2loopback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbV4l2loopbackActionPerformed(evt);
            }
        });
        add(jcbV4l2loopback);

        btnSkyFlip.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/view-refresh.png"))); // NOI18N
        btnSkyFlip.setText("FlipSkyCam");
        btnSkyFlip.setToolTipText("Flips SkyCam Horizontally");
        btnSkyFlip.setEnabled(false);
        btnSkyFlip.setName("btnSkyFlip"); // NOI18N
        btnSkyFlip.setRolloverEnabled(false);
        btnSkyFlip.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/view-refresh-on.png"))); // NOI18N
        btnSkyFlip.setPreferredSize(new java.awt.Dimension(113, 21));
        btnSkyFlip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSkyFlipActionPerformed(evt);
            }
        });
        add(btnSkyFlip);

        tglAudioOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/audio-card.png"))); // NOI18N
        tglAudioOut.setText("Audio Output");
        tglAudioOut.setToolTipText("WebcamStudio Master Audio Output");
        tglAudioOut.setName("tglAudioOut"); // NOI18N
        tglAudioOut.setPreferredSize(new java.awt.Dimension(32, 32));
        tglAudioOut.setRolloverEnabled(false);
        tglAudioOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglAudioOutActionPerformed(evt);
            }
        });
        add(tglAudioOut);

        tglRecordToFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-record.png"))); // NOI18N
        tglRecordToFile.setText(bundle.getString("RECORD")); // NOI18N
        tglRecordToFile.setToolTipText("Save to FIle.");
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
            boolean overWrite = true;
            File f;
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter aviFilter = new FileNameExtensionFilter("AVI files (*.avi)", "avi");
            FileNameExtensionFilter mp4Filter = new FileNameExtensionFilter("MP4 files (*.mp4)", "mp4");
            chooser.setFileFilter(aviFilter);
            chooser.setFileFilter(mp4Filter);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("Choose Destination File ...");
            int retval = chooser.showSaveDialog(this);
            f = chooser.getSelectedFile();
            if (f != null) {
                if (chooser.getFileFilter().equals(aviFilter)) {
                    if(!chooser.getSelectedFile().getAbsolutePath().endsWith(".avi")){
                        f =  new File(chooser.getSelectedFile() + ".avi");
                    }
                } else if (chooser.getFileFilter().equals(mp4Filter)) {
                    if(!chooser.getSelectedFile().getAbsolutePath().endsWith(".mp4")){
                        f =  new File(chooser.getSelectedFile() + ".mp4");
                    }
                }
                if(f.exists()){
                    int result = JOptionPane.showConfirmDialog(this,"File exists, overwrite?","Attention",JOptionPane.YES_NO_CANCEL_OPTION);
                    switch(result){
                        case JOptionPane.YES_OPTION:
                            overWrite = true;
                            break;
                        case JOptionPane.NO_OPTION:
                            overWrite = false;
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            overWrite = false;
                            break;
                        case JOptionPane.CLOSED_OPTION:
                            overWrite = false;
                            break;
                    }
                }
            }
            if (retval == JFileChooser.APPROVE_OPTION && overWrite) {
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
                ResourceMonitorLabel label3 = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Record Cancelled!");
                ResourceMonitor.getInstance().addMessage(label3);
            }
        } else {
            SinkFile fileStream = files.get("RECORD");
            if (fileStream != null) {
                fileStream.stop();
                fileStream = null;
                files.remove("RECORD");
                ResourceMonitorLabel label = labels.get("RECORD");
                ResourceMonitor.getInstance().removeMessage(label);

                ResourceMonitorLabel label2 = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Stop Recording!");
                ResourceMonitor.getInstance().addMessage(label2);
            }
        }
        
    }//GEN-LAST:event_tglRecordToFileActionPerformed

    private void tglUDPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglUDPActionPerformed
        if (tglUDP.isSelected()) {
            SinkUDP udpStream = new SinkUDP();
            UIManager.put("OptionPane.noButtonText", "HQ");
            UIManager.put("OptionPane.yesButtonText", "Standard");
            int resultHQ = JOptionPane.showConfirmDialog(this,"HQ or Standard mode?","Choose",JOptionPane.YES_NO_OPTION);
            switch(resultHQ){
                case JOptionPane.YES_OPTION:
                    udpStream.setStandard("STD");
                    break;
                case JOptionPane.NO_OPTION:
                    udpStream.setStandard("HQ");
                    break;
                case JOptionPane.CLOSED_OPTION:
                    udpStream.setStandard("STD");
                    break;
            }
            UIManager.put("OptionPane.noButtonText", "No");
            UIManager.put("OptionPane.yesButtonText", "Yes");     
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
    private void repaintFMEButtons(){
        for (FME fme : fmes.values()) {
            addButtonBroadcast(fme);
        }
    }
    private void repaintOuputButtons() {
        instanceSink.removeAll();
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages");
       
        tglSkyCam.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/camera-video.png"))); // NOI18N
        tglSkyCam.setText(bundle.getString("SKYCAM")); // NOI18N
        tglSkyCam.setToolTipText("Activate Skype Cam Compatibility");
        tglSkyCam.setName("tglSkyCam"); // NOI18N
        tglSkyCam.setRolloverEnabled(false);
        tglSkyCam.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/camera-video-on.png"))); // NOI18N
        add(tglSkyCam);
        
        jcbV4l2loopback.setText("V4l2loopback");
        jcbV4l2loopback.setName("jcbV4l2loopback"); // NOI18N
        add(jcbV4l2loopback);
        
        btnSkyFlip.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/view-refresh.png"))); // NOI18N
        btnSkyFlip.setText("FlipSkyCam");
        btnSkyFlip.setToolTipText("Flips SkyCam Horizontally");
        btnSkyFlip.setName("btnSkyFlip"); // NOI18N
        btnSkyFlip.setEnabled(true);
        btnSkyFlip.setRolloverEnabled(false);
        btnSkyFlip.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/view-refresh-on.png"))); // NOI18N
        btnSkyFlip.setPreferredSize(new java.awt.Dimension(113, 21));
        add(btnSkyFlip);

        tglAudioOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/audio-card.png"))); // NOI18N
        tglAudioOut.setText("Audio Output");
        tglAudioOut.setToolTipText("Audio to Speakers");
        tglAudioOut.setName("tglAudioOut"); // NOI18N
        tglSkyCam.setRolloverEnabled(false);
        tglAudioOut.setPreferredSize(new java.awt.Dimension(32, 32));
        add(tglAudioOut);
        
        tglRecordToFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-record.png"))); // NOI18N
        tglRecordToFile.setText(bundle.getString("RECORD")); // NOI18N
        tglRecordToFile.setToolTipText("Save to FIle.");
        tglRecordToFile.setName("tglRecordToFile"); // NOI18N
        tglRecordToFile.setRolloverEnabled(false);
        tglRecordToFile.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        add(tglRecordToFile);

        tglUDP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-record.png"))); // NOI18N
        tglUDP.setText(bundle.getString("UDP_MPEG_OUT")); // NOI18N
        tglUDP.setToolTipText("Stream to udp://@127.0.0.1:7000");
        tglUDP.setName("tglUDP"); // NOI18N
        tglUDP.setRolloverEnabled(false);
        tglUDP.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        add(tglUDP);
    }
    private void paintWSCamButtons () {
        for (VideoDevice d : VideoDevice.getInputDevices()) {
            String vdName = d.getFile().getName();
            if (vdName.endsWith("video21")) {
            } else {
                JToggleButton wsCamButton = new JToggleButton();
                wsCamButton.setText(d.getName());
                wsCamButton.setActionCommand(d.getFile().getAbsolutePath());
                wsCamButton.setIcon(tglRecordToFile.getIcon());
                wsCamButton.setSelectedIcon(tglRecordToFile.getSelectedIcon());
                wsCamButton.setRolloverEnabled(false);
                wsCamButton.addActionListener(new java.awt.event.ActionListener() {                    
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        String device = evt.getActionCommand();
                        JToggleButton button = ((JToggleButton) evt.getSource());
                        
                        if (button.isSelected()) {
                            camCount ++;
                            SinkLinuxDevice stream = new SinkLinuxDevice(new File(device), button.getText());
                            stream.setRate(MasterMixer.getInstance().getRate());
                            stream.setWidth(MasterMixer.getInstance().getWidth());
                            stream.setHeight(MasterMixer.getInstance().getHeight());
                            stream.setListener(instanceSink);
                            stream.read();
                            devices.put(button.getText(), stream);
                            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Rendering to " + button.getText() + " (SkyCam Disengaged)");
                            labels.put(button.getText(), label);
                            ResourceMonitor.getInstance().addMessage(label);
                            tglSkyCam.setEnabled(false);
                            System.out.println("CamCount = "+camCount);
                        } else {
                            SinkLinuxDevice stream = devices.get(button.getText());
                            if (stream != null) {
                                camCount --;
                                stream.stop();
                                devices.remove(button.getText());
                                System.out.println("WS Camera Stopped ...");
                                ResourceMonitorLabel label = labels.remove(button.getText());
                                ResourceMonitor.getInstance().removeMessage(label);
                                System.out.println("CamCount = "+camCount);
                                if (camCount == 0 && fmeCount == 0) {
                                    tglSkyCam.setEnabled(true);
                                }
                            } 
                            if (camCount == 0 && fmeCount == 0) {
                                tglSkyCam.setEnabled(true);
                            }
                        }
                    }
                });
                this.add(wsCamButton);
                this.revalidate();
            }
        }
    } 
    private void repaintSkyCamButtons (){
        for (VideoDevice d : VideoDevice.getInputDevices()) {
            String vdName = d.getFile().getName();
            if (vdName.endsWith("video21")) {
            } else {
                JToggleButton skyCamButton = new JToggleButton();
                skyCamButton.setText(d.getName());
                skyCamButton.setActionCommand(d.getFile().getAbsolutePath());
                skyCamButton.setIcon(tglRecordToFile.getIcon());
                skyCamButton.setSelectedIcon(tglRecordToFile.getSelectedIcon());
                skyCamButton.setRolloverEnabled(false);
                skyCamButton.addActionListener(new java.awt.event.ActionListener() {                    
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        String device = evt.getActionCommand();
                        JToggleButton button = ((JToggleButton) evt.getSource());
                        if (button.isSelected()) {
                            camCount ++;
                            if (iSkyCamFree) {
                                SinkLinuxDevice stream = new SinkLinuxDevice(new File(device), button.getText());
                                stream.setRate(MasterMixer.getInstance().getRate());
                                stream.setWidth(MasterMixer.getInstance().getWidth());
                                stream.setHeight(MasterMixer.getInstance().getHeight());
                                stream.setListener(instanceSink);
                                stream.read();
                                devices.put(button.getText(), stream);
                                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Rendering to " + button.getText() + " (SkyCam Engaged)");
                                labels.put(button.getText(), label);
                                ResourceMonitor.getInstance().addMessage(label);
                                processSkyVideo = new ProcessExecutor(stream.getName());
                                File fileD=new File(System.getProperty("user.home")+"/.webcamstudio/"+"SkyCamC.sh");
                                if (flip){
                                    skyRunComm = "gst-launch-0.10 v4l2src device="+device+" ! videoflip method=horizontal-flip ! v4l2sink device=/dev/video21"; // videoflip method=horizontal-flip ! 
                                } else {
                                    skyRunComm = "gst-launch-0.10 v4l2src device="+device+" ! v4l2sink device=/dev/video21";
                                }
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
                                    dosD.writeBytes(skyRunComm +"\n");
                                    dosD.writeBytes("wait"+"\n");
                                } catch (IOException ex) {
                                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                String batchSkyCommC = "sh "+System.getProperty("user.home")+"/.webcamstudio/"+"SkyCamC.sh";
                                try {
                                    Tools.sleep(20);
                                    processSkyVideo.executeString(batchSkyCommC);
                                    Tools.sleep(20);
                                } catch (                        IOException | InterruptedException ex) {
                                    Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                tglSkyCam.setEnabled(false);
                                btnSkyFlip.setEnabled(false);
                                iSkyCamFree = false;
                                iSkyCam = true;
                                System.out.println("Skype Camera on /dev/video21 ...");
                            } else {
                                if (processSkyVideo != null) {
                                    iSkyCam = false;
                                }
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
                            }
                            System.out.println("CamCount = "+camCount);
                        } else {
                            SinkLinuxDevice stream = devices.get(button.getText());
                            if (stream != null) {
                                camCount --;
                                stream.stop();
                                devices.remove(button.getText());                                    
                                if (iSkyCam) {
                                    if (processSkyVideo != null){
                                        processSkyVideo.destroy();
                                        processSkyVideo = null;
                                        System.out.println("WS Skype Camera Stopped iSkyCam ...");
                                        System.out.println("CamCount = "+camCount);
                                        if (camCount == 0 && fmeCount == 0) {
                                            tglSkyCam.setEnabled(true);
                                        }
                                        btnSkyFlip.setEnabled(true);
                                        iSkyCamFree = true;    
                                    }
                                }
                                if (!iSkyCamFree) {
                                    iSkyCam = true;
                                }
                                devices.put(button.getText(), stream);
                                ResourceMonitorLabel label = null;
                                if (iSkyCamFree) {
                                    label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "WS Skype Camera Stopped");
                                } else {
                                    label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "WS Camera Stopped");
                                }
                                labels.put(button.getText(), label);
                                ResourceMonitor.getInstance().addMessage(label);
                            }
                        }
                    }
                });
                this.add(skyCamButton);
                this.revalidate();
            }
        }
    }
    private static class WaitingDialogOP extends JDialog {
        private final JLabel workingLabelOP = new JLabel();
        public WaitingDialogOP(JFrame owner) {
            workingLabelOP.setBorder(BorderFactory.createLineBorder(Color.black));
            workingLabelOP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/view-fullscreen.png"))); // NOI18N        
            workingLabelOP.setText(" Working... ");
            setUndecorated(true);
            add(workingLabelOP);
            pack();
            pack();
            // move window to center of owner
            int x = owner.getX()
                + (owner.getWidth() - getPreferredSize().width) / 2;
            int y = owner.getY()
                + (owner.getHeight() - getPreferredSize().height) / 2;
            setLocation(x, y);
            repaint();
        }
    } 
    private void tglSkyCamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglSkyCamActionPerformed
        final WaitingDialogOP waitingD = new WaitingDialogOP(wDFrame);
            waitingD.setModal(true);
            SwingWorker<?,?> worker = new SwingWorker<Void,Integer>(){  
                @Override
                protected Void doInBackground() throws InterruptedException{
        Runtime rt = Runtime.getRuntime();
        String unregisterWSDevice = "modprobe -r "+virtualDevice;
        String registerWSDevice = "modprobe "+virtualDevice;
        String register2WSDevices = "modprobe "+virtualDevice+" devices=2 video_nr=21";
        if (tglSkyCam.isSelected()) {          
            jcbV4l2loopback.setEnabled(false);
            camCount = 0;
            fmeCount = 0;
            skyCamMode = true;
            File fileD=new File(System.getProperty("user.home")+"/.webcamstudio/"+"SkyCam.sh");
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
            dosD.writeBytes(unregisterWSDevice+"\n");
            dosD.writeBytes("wait"+"\n");
            dosD.writeBytes(register2WSDevices+"\n");
            dosD.writeBytes("wait"+"\n");
            } catch (IOException ex) {
            Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
            String batchSkyComm = "gksudo sh "+System.getProperty("user.home")+"/.webcamstudio/"+"SkyCam.sh";
            try {
                Process urDevice = rt.exec(batchSkyComm); 
                Tools.sleep(50);
                urDevice.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            
            repaintOuputButtons();
            Tools.sleep(30);
            repaintSkyCamButtons ();
            Tools.sleep(30);
            repaintFMEButtons();
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "SkyCam Engaged");
            ResourceMonitor.getInstance().addMessage(label);
            instanceSink.repaint();
        } else {
            skyCamMode = false;
            jcbV4l2loopback.setEnabled(true);
            File fileD=new File(System.getProperty("user.home")+"/.webcamstudio/"+"SkyCamR.sh");
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
            dosD.writeBytes(unregisterWSDevice +"\n");
            dosD.writeBytes("wait"+"\n");
            dosD.writeBytes(registerWSDevice +"\n");
            dosD.writeBytes("wait"+"\n");
            } catch (IOException ex) {
            Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
            String batchSkyCommR = "gksudo sh "+System.getProperty("user.home")+"/.webcamstudio/"+"SkyCamR.sh";
            try {
                Process rDevice = rt.exec(batchSkyCommR); 
                Tools.sleep(50);
                rDevice.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            
            repaintOuputButtons();
            Tools.sleep(30);
            paintWSCamButtons ();
            Tools.sleep(30);
            repaintFMEButtons();
            btnSkyFlip.setSelected(false);
            btnSkyFlip.setEnabled(false);
            flip = false;
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "SkyCam Disengaged");
            ResourceMonitor.getInstance().addMessage(label);
            instanceSink.repaint();
        }
        return null; 
    }
                @Override
                protected void done(){
                    Tools.sleep(10);
                    waitingD.dispose();
                }  
    };
        worker.execute();
        waitingD.toFront();
        waitingD.setVisible(true);
    }//GEN-LAST:event_tglSkyCamActionPerformed

    private void btnSkyFlipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSkyFlipActionPerformed
        if (btnSkyFlip.isSelected()) {
            flip = true;
        } else {
            flip = false;
        }
    }//GEN-LAST:event_btnSkyFlipActionPerformed

    private void jcbV4l2loopbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbV4l2loopbackActionPerformed
        if (jcbV4l2loopback.isSelected()) {
            virtualDevice = "v4l2loopback";
        } else {
            virtualDevice = "webcamstudio";
        }
    }//GEN-LAST:event_jcbV4l2loopbackActionPerformed

    private void tglAudioOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglAudioOutActionPerformed
        if (tglAudioOut.isSelected()) {
            SinkAudio audioStream = new SinkAudio();
            audioStream.setWidth(MasterMixer.getInstance().getWidth());
            audioStream.setHeight(MasterMixer.getInstance().getHeight());
            audioStream.setRate(MasterMixer.getInstance().getRate());
            audioStream.setListener(instanceSink);
            audioStream.read();
            audioOut.put("AudioOut", audioStream);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "WS Audio to Speakers");
            labels.put("AudioOut", label);
            ResourceMonitor.getInstance().addMessage(label);
        } else {
            SinkAudio audioStream = audioOut.get("AudioOut");
            if (audioStream != null) {
                audioStream.stop();
                audioStream = null;
                audioOut.remove("AudioOut");
                ResourceMonitorLabel label = labels.get("AudioOut");
                ResourceMonitor.getInstance().removeMessage(label);
            }
        }
//            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "WS Audio-Out Active");
//            ResourceMonitor.getInstance().addMessage(label);
//        } else {
//            if (audioStream != null) {
//                audioStream.stop();
//                audioStream = null;
//            }
//            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "WS Audio-Out Stopped");
//            ResourceMonitor.getInstance().addMessage(label);
//        }
    }//GEN-LAST:event_tglAudioOutActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnSkyFlip;
    private javax.swing.JCheckBox jcbV4l2loopback;
    private javax.swing.JToggleButton tglAudioOut;
    private javax.swing.JToggleButton tglRecordToFile;
    final OutputPanel instanceSink = this;
    private javax.swing.JToggleButton tglSkyCam;
    // final OutputPanel instanceSink = this;
    private javax.swing.JToggleButton tglUDP;
    // End of variables declaration//GEN-END:variables

    
    @Override
    public void sourceUpdated(Stream stream) {
        if (stream instanceof SinkFile) {
            tglRecordToFile.setSelected(stream.isPlaying());
        } else if (stream instanceof SinkUDP) {
            tglUDP.setSelected(stream.isPlaying());
        } else if (stream instanceof SinkAudio) {
            tglAudioOut.setSelected(stream.isPlaying());
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
        
    }

    @Override
    public void stopChTime(ActionEvent evt) {
        
    }

    @Override
    public void resetBtnStates(ActionEvent evt) {
        tglSkyCam.setEnabled(true);
        camCount = 0;
        fmeCount = 0;
        iSkyCamFree = true;
        if (processSkyVideo != null){
            processSkyVideo.destroy();
            System.out.println("WS Skype Camera Stopped ...");   
        }
                         
    }

    @Override
    public void addLoadingChannel(String name) {
     
    }

    @Override
    public void removeChannels(String removeSc, int a) {
        
    }
}
