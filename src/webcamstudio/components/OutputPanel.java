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
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import webcamstudio.WebcamStudio;
import static webcamstudio.WebcamStudio.theme;
import webcamstudio.channels.MasterChannels;
import static webcamstudio.components.ChannelPanel.lblOnAir;
import webcamstudio.exporter.vloopback.VideoDevice;
import webcamstudio.externals.FME;
import webcamstudio.media.renderer.Exporter;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.streams.SinkAudio;
import webcamstudio.streams.SinkBroadcast;
import webcamstudio.streams.SinkFile;
import webcamstudio.streams.SinkLinuxDevice;
import webcamstudio.streams.SinkUDP;
import webcamstudio.streams.Stream;
import webcamstudio.util.GridLayout2;
import webcamstudio.util.Tools;
import webcamstudio.util.Tools.OS;

/**
 *
 * @author patrick (modified by karl)
 */
public class OutputPanel extends javax.swing.JPanel implements Stream.Listener, WebcamStudio.Listener, ChannelPanel.Listener, Exporter.Listener {

    TreeMap<String, SinkFile> files = new TreeMap<>();
    TreeMap<String, SinkBroadcast> broadcasts = new TreeMap<>();
    ArrayList<String> broadcastsOut = new ArrayList<>();
    TreeMap<String, SinkLinuxDevice> devices = new TreeMap<>();
    ArrayList<String> devicesOut = new ArrayList<>();
    TreeMap<String, SinkUDP> udpOut = new TreeMap<>();
    TreeMap<String, SinkAudio> audioOut = new TreeMap<>();
    TreeMap<String, FME> fmes = new TreeMap<>();
    private final static String userHomeDir = Tools.getUserHome();
    boolean skyCamMode = false;
    int camCount = 0;
    int fmeCount = 0;
    String virtualDevice = "webcamstudio";
    TreeMap<String, ResourceMonitorLabel> labels = new TreeMap<>();
    JPanel wDFrame;
    FME currFME;
    JPopupMenu fmePopup = new JPopupMenu();
    JPopupMenu sinkFilePopup = new JPopupMenu();
    JPopupMenu sinkUDPPopup = new JPopupMenu();
    File f;
    SinkFile fileStream;
    SinkUDP udpStream;
    SinkAudio audioStream;
    private boolean audioOutState = false;
    private boolean udpOutState = false;
    private boolean audioOutSwitch = false;
    private boolean udpOutSwitch = false;
    private boolean fmeOutState = false;
    private boolean fmeOutSwitch = false;
    private boolean camOutState = false;
    private boolean camOutSwitch = false;
    private final ArrayList<String> idWSOuts = new ArrayList<>();
    private String idWSAD;
    private String idDef;
    private String idWSOut;
    private final String[] v4l2PixelFormats = { "RGB24", "UYUV", "BGR24" };


    @SuppressWarnings("unchecked")
    /** Creates new form OutputPanel
     * @param aFrame */
    public OutputPanel(JPanel aFrame) {
        initComponents();
        GridLayout2 columLayout = new GridLayout2(0,1);
        this.setLayout(columLayout);
        panelPixelFormat.setLayout(columLayout);
        panelDefaultOutputs.setLayout(columLayout);
        panelVirtualAudio.setLayout(columLayout);
        panelVirtualC.setLayout(columLayout);
        panelVirtualA.setLayout(columLayout); 
        panelDefault.setLayout(columLayout);
        panelFME.setLayout(columLayout);
        comboPixelFormat.setModel(new javax.swing.DefaultComboBoxModel(v4l2PixelFormats));
        f = new File(userHomeDir + "/.webcamstudio/Record To File");
        udpStream = new SinkUDP();
        fileStream = new SinkFile(f);
        audioStream = new SinkAudio();
//        System.out.println("SinkAudio"+audioStream);

        tglRecordToFile.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                JToggleButton button = ((JToggleButton) evt.getSource());
                if (!button.isSelected()) {
                    sinkFileRightMousePressed(evt);
                }
            }
        });

        tglUDP.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                JToggleButton button = ((JToggleButton) evt.getSource());
                if (!button.isSelected()) {
                    sinkUDPRightMousePressed(evt);
                }
            }
        });

        wDFrame = aFrame;
        fmeInitPopUp();
        sinkFileInitPopUp();
        sinkUDPInitPopUp();
        final OutputPanel instanceSinkOP = this;
        WebcamStudio.setListenerOP(instanceSinkOP);
        ChannelPanel.setListenerCPOPanel(instanceSinkOP);
        Exporter.setListenerEx(instanceSinkOP);
        if (Tools.getOS() == OS.LINUX) {
            paintWSCamButtons ();
        }

        fileStream.setWidth(MasterMixer.getInstance().getWidth());
        fileStream.setHeight(MasterMixer.getInstance().getHeight());
        fileStream.setRate(MasterMixer.getInstance().getRate());

        udpStream.setWidth(MasterMixer.getInstance().getWidth());
        udpStream.setHeight(MasterMixer.getInstance().getHeight());
        udpStream.setRate(MasterMixer.getInstance().getRate());

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
        Preferences filePrefs = prefs.node("filerec");
        Preferences udpPrefs = prefs.node("udp");
        try {
            String[] services = fmePrefs.childrenNames();
            String[] servicesF = filePrefs.childrenNames();
            String[] servicesU = udpPrefs.childrenNames();

            for (String s : servicesF){
                Preferences serviceF = filePrefs.node(s);
                fileStream.setVbitrate(serviceF.get("vbitrate", "1200"));
                fileStream.setAbitrate(serviceF.get("abitrate", "128"));
            }

            for (String s : servicesU){
                Preferences serviceU = udpPrefs.node(s);
                udpStream.setVbitrate(serviceU.get("vbitrate", "1200"));
                udpStream.setAbitrate(serviceU.get("abitrate", "128"));
                udpStream.setStandard(serviceU.get("standard", "STD"));
            }

            for (String s : services) {
                Preferences service = fmePrefs.node(s);
                String url = service.get("url", "");
                String name = service.get("name", "");
                String abitrate = service.get("abitrate", "512000");
                String vbitrate = service.get("vbitrate", "96000");
                String vcodec = service.get("vcodec", "");
                String acodec = service.get("acodec", "");
                String width = service.get("width", "");
                String height = service.get("height", "");
                String stream = service.get("stream", "");
                String mount = service.get("mount", "");
                String password = service.get("password", "");
                String port = service.get("port", "");
                String keyInt = service.get("keyint", "");
                String standard = service.get("standard", "STD");
                // for compatibility before KeyInt
                if ("".equals(keyInt)) {
                    keyInt = "125";
                }

//                System.out.println("Loaded KeyInt: "+keyInt+"###");
                FME fme = new FME(url, stream, name, abitrate, vbitrate, vcodec, acodec, width, height, mount, password, port, keyInt);
                fme.setStandard(standard);
                fmes.put(fme.getName(), fme);
                addButtonBroadcast(fme);
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void savePrefs(Preferences prefs) {
        Preferences fmePrefs = prefs.node("fme");
        Preferences filePrefs = prefs.node("filerec");
        Preferences udpPrefs = prefs.node("udp");
        try {
            fmePrefs.removeNode();
            fmePrefs.flush();
            fmePrefs = prefs.node("fme");
            filePrefs.removeNode();
            filePrefs.flush();
            filePrefs = prefs.node("filerec");
            udpPrefs.removeNode();
            udpPrefs.flush();
            udpPrefs = prefs.node("udp");
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
            service.put("keyint", fme.getKeyInt());
            service.put("standard", fme.getStandard());
        }
        Preferences serviceF = filePrefs.node("frecordset");
        serviceF.put("abitrate", fileStream.getAbitrate());
        serviceF.put("vbitrate", fileStream.getVbitrate());
        Preferences serviceU = udpPrefs.node("uoutset");
        serviceU.put("abitrate", udpStream.getAbitrate());
        serviceU.put("vbitrate", udpStream.getVbitrate());
        serviceU.put("standard", udpStream.getStandard());
    }

    private String checkDoubleBroad(String s) {
        String res = s;
        for (String broName : broadcastsOut) {
            if (s.equals(broName)){
                res = "";
            }
        }
        return res;
    }

    private String checkDoubleCam(String s) {
        String res = s;
        for (String camName : devicesOut) {
            if (s.equals(camName)){
                res = "";
            }
        }
        return res;
    }

    private void addButtonBroadcast(final FME fme) {
        final OutputPanel instanceSinkFME = this;
        JToggleButton button = new JToggleButton();
//        Dimension d = new Dimension(139,22);
//        button.setPreferredSize(d);
        button.setText(fme.getName());
        button.setActionCommand(fme.getUrl()+"/"+fme.getStream());
        button.setIcon(tglRecordToFile.getIcon());
        button.setSelectedIcon(tglRecordToFile.getSelectedIcon());
        button.setRolloverEnabled(false);
        button.setToolTipText("Drag to the right to Remove... - Right Click for Settings");
        button.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                JToggleButton button = ((JToggleButton) evt.getSource());
                FME fme = fmes.get(button.getText());
                if (button.isSelected()) {
                    if (fme != null){
                        fmeOutState = true;
                        String cleanBroad = checkDoubleBroad(button.getText());
                        if (!"".equals(cleanBroad)) {
                            broadcastsOut.add(cleanBroad);
//                            System.out.println("broadcastsOut: "+broadcastsOut);
                        }
                        fmeCount ++;
                        SinkBroadcast broadcast = new SinkBroadcast(fme);
                        broadcast.setStandard(fme.getStandard());
                        broadcast.setRate(MasterMixer.getInstance().getRate());
                        broadcast.setWidth(MasterMixer.getInstance().getWidth());
                        fme.setWidth(Integer.toString(MasterMixer.getInstance().getWidth()));
                        broadcast.setHeight(MasterMixer.getInstance().getHeight());
                        fme.setHeight(Integer.toString(MasterMixer.getInstance().getHeight()));
                        broadcast.setListener(instanceSinkFME);
                        broadcast.read();
                        broadcasts.put(button.getText(), broadcast);
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Broadcasting to " + fme.getName());
                        labels.put(fme.getName(), label);
                        ResourceMonitor.getInstance().addMessage(label);
                        lblOnAir.setForeground(Color.RED);
                    } else {
                        fmeCount --;
                        button.setSelected(false);
                    }
                } else {
                    fmeOutState = fmeCount > 0;
                    broadcastsOut.remove(button.getText());
                    SinkBroadcast broadcast = broadcasts.get(button.getText());
                    if (broadcast != null) {
                        fmeCount --;
                        broadcast.stop();
                        broadcast.destroy();
                        broadcasts.remove(fme.getName());
                        ResourceMonitorLabel label = labels.get(fme.getName());
                        labels.remove(fme.getName());
                        ResourceMonitor.getInstance().removeMessage(label);
                        if (fmeCount == 0 && !udpOutState) {
                            if (theme.equals("Dark")) {
                                lblOnAir.setForeground(Color.WHITE);
                            } else {
                                lblOnAir.setForeground(Color.BLACK);
                            }
                        }
                    }
                }
            }
        });
        button.addMouseMotionListener(new java.awt.event.MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (e.getX() > getWidth()) {
                    JToggleButton button = ((JToggleButton) e.getSource());
                    if (!button.isSelected() && e.getX() > getWidth()) {
                        System.out.println(button.getText()+" removed ...");
                        SinkBroadcast broadcast = broadcasts.remove(button.getText());
                        if (broadcast != null) {
                            MasterChannels.getInstance().unregister(broadcast);
                        }
                        fmes.remove(button.getText());
                        labels.remove(fme.getName());
                        remove(button);
                        revalidate();
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                JToggleButton button = ((JToggleButton) evt.getSource());
                if (!button.isSelected()) {
                    fmeRightMousePressed(evt, fme);
                }
            }
        });
        this.add(button);
        this.revalidate();
    }

    private void fmeRightMousePressed(java.awt.event.MouseEvent evt, FME fme) {
        if (evt.isPopupTrigger()) {
            fmePopup.show(evt.getComponent(), evt.getX(), evt.getY());
            currFME = fme;
        }
    }

    private void sinkFileRightMousePressed(java.awt.event.MouseEvent evt) { // , SinkFile sinkfile
        if (evt.isPopupTrigger()) {
            sinkFilePopup.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    private void sinkUDPRightMousePressed(java.awt.event.MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            sinkUDPPopup.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    private void fmeInitPopUp(){
        JMenuItem fmeSettings = new JMenuItem (new AbstractAction("FME Settings") {
            @Override
            public void actionPerformed(ActionEvent e) {
                FMESettings fmeSet = new FMESettings(currFME);
                fmeSet.setLocationRelativeTo(WebcamStudio.cboAnimations);
                fmeSet.setAlwaysOnTop(true);
                fmeSet.setVisible(true);
            }
        });
        fmeSettings.setIcon(new ImageIcon(getClass().getResource("/webcamstudio/resources/tango/working-4.png"))); // NOI18N
        fmePopup.add(fmeSettings);
    }

    private void sinkFileInitPopUp(){
        JMenuItem sinkSettings = new JMenuItem (new AbstractAction("Record Settings") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SinkSettings sinkSet = new SinkSettings(fileStream, null);
                sinkSet.setLocationRelativeTo(WebcamStudio.cboAnimations);
                sinkSet.setAlwaysOnTop(true);
                sinkSet.setVisible(true);
            }
        });
        sinkSettings.setIcon(new ImageIcon(getClass().getResource("/webcamstudio/resources/tango/working-4.png"))); // NOI18N
        sinkFilePopup.add(sinkSettings);
    }

    private void sinkUDPInitPopUp(){
        JMenuItem sinkSettings = new JMenuItem (new AbstractAction("UDP Settings") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SinkSettings sinkSet = new SinkSettings(null, udpStream);
                sinkSet.setLocationRelativeTo(WebcamStudio.cboAnimations);
                sinkSet.setAlwaysOnTop(true);
                sinkSet.setVisible(true);
            }
        });
        sinkSettings.setIcon(new ImageIcon(getClass().getResource("/webcamstudio/resources/tango/working-4.png"))); // NOI18N
        sinkUDPPopup.add(sinkSettings);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        outputTabs = new javax.swing.JTabbedPane();
        panelVirtualC = new javax.swing.JPanel();
        panelPixelFormat = new javax.swing.JPanel();
        labelPixelFormat = new javax.swing.JLabel();
        comboPixelFormat = new javax.swing.JComboBox();
        panelVirtualA = new javax.swing.JPanel();
        panelVirtualAudio = new javax.swing.JPanel();
        tglWSAudioDev = new javax.swing.JCheckBox();
        tglAudioOut = new javax.swing.JToggleButton();
        panelDefault = new javax.swing.JPanel();
        panelDefaultOutputs = new javax.swing.JPanel();
        tglRecordToFile = new javax.swing.JToggleButton();
        tglUDP = new javax.swing.JToggleButton();
        panelFME = new javax.swing.JPanel();
        panelSetFME = new javax.swing.JPanel();
        btnAddFME = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("OUTPUT"))); // NOI18N
        setToolTipText(bundle.getString("DROP_OUTPUT")); // NOI18N
        setPreferredSize(new java.awt.Dimension(237, 393));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        outputTabs.setFont(new java.awt.Font("Noto Sans", 0, 11)); // NOI18N
        outputTabs.setMaximumSize(new java.awt.Dimension(370, 32767));
        outputTabs.setMinimumSize(new java.awt.Dimension(150, 98));
        outputTabs.setName("outputTabs"); // NOI18N
        // outputTabs.setFont(new java.awt.Font("Noto Sans", 0, 10));

        panelVirtualC.setName("panelVirtualC"); // NOI18N

        panelPixelFormat.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "WebcamStudio Virtual Cam", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Noto Sans", 0, 10))); // NOI18N
        panelPixelFormat.setName("panelPixelFormat"); // NOI18N

        labelPixelFormat.setFont(new java.awt.Font("Noto Sans", 0, 10)); // NOI18N
        labelPixelFormat.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelPixelFormat.setText("Pixel Format");
        labelPixelFormat.setName("labelPixelFormat"); // NOI18N

        comboPixelFormat.setName("comboPixelFormat"); // NOI18N

        javax.swing.GroupLayout panelPixelFormatLayout = new javax.swing.GroupLayout(panelPixelFormat);
        panelPixelFormat.setLayout(panelPixelFormatLayout);
        panelPixelFormatLayout.setHorizontalGroup(
            panelPixelFormatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPixelFormatLayout.createSequentialGroup()
                .addGroup(panelPixelFormatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelPixelFormat)
                    .addComponent(comboPixelFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(84, Short.MAX_VALUE))
        );
        panelPixelFormatLayout.setVerticalGroup(
            panelPixelFormatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPixelFormatLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(labelPixelFormat)
                .addGap(0, 0, 0)
                .addComponent(comboPixelFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout panelVirtualCLayout = new javax.swing.GroupLayout(panelVirtualC);
        panelVirtualC.setLayout(panelVirtualCLayout);
        panelVirtualCLayout.setHorizontalGroup(
            panelVirtualCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 178, Short.MAX_VALUE)
            .addGroup(panelVirtualCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelVirtualCLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelPixelFormat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        panelVirtualCLayout.setVerticalGroup(
            panelVirtualCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 124, Short.MAX_VALUE)
            .addGroup(panelVirtualCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelVirtualCLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(panelPixelFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        outputTabs.addTab("Webcam", panelVirtualC);

        panelVirtualA.setName("panelVirtualA"); // NOI18N

        panelVirtualAudio.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "WebcamStudio Virtual Audio", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Noto Sans", 0, 10))); // NOI18N
        panelVirtualAudio.setName("panelVirtualAudio"); // NOI18N

        tglWSAudioDev.setText("WSAudioDevice");
        tglWSAudioDev.setToolTipText("WebcamStudio Master Audio Sink");
        tglWSAudioDev.setName("tglWSAudioDev"); // NOI18N
        tglWSAudioDev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglWSAudioDevActionPerformed(evt);
            }
        });

        tglAudioOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/audio-card.png"))); // NOI18N
        tglAudioOut.setText("Audio Out");
        tglAudioOut.setToolTipText("WebcamStudio Master Audio Output");
        tglAudioOut.setName("tglAudioOut"); // NOI18N
        tglAudioOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglAudioOutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelVirtualAudioLayout = new javax.swing.GroupLayout(panelVirtualAudio);
        panelVirtualAudio.setLayout(panelVirtualAudioLayout);
        panelVirtualAudioLayout.setHorizontalGroup(
            panelVirtualAudioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVirtualAudioLayout.createSequentialGroup()
                .addGroup(panelVirtualAudioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tglWSAudioDev, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tglAudioOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        panelVirtualAudioLayout.setVerticalGroup(
            panelVirtualAudioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVirtualAudioLayout.createSequentialGroup()
                .addComponent(tglAudioOut)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addComponent(tglWSAudioDev))
        );

        javax.swing.GroupLayout panelVirtualALayout = new javax.swing.GroupLayout(panelVirtualA);
        panelVirtualA.setLayout(panelVirtualALayout);
        panelVirtualALayout.setHorizontalGroup(
            panelVirtualALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVirtualALayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelVirtualAudio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelVirtualALayout.setVerticalGroup(
            panelVirtualALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVirtualALayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelVirtualAudio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        outputTabs.addTab("Audio", panelVirtualA);

        panelDefault.setName("panelDefault"); // NOI18N

        panelDefaultOutputs.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "WebcamStudio Default Outputs", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Noto Sans", 0, 10))); // NOI18N
        panelDefaultOutputs.setName("panelDefaultOutputs"); // NOI18N

        tglRecordToFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-record.png"))); // NOI18N
        tglRecordToFile.setText(bundle.getString("RECORD")); // NOI18N
        tglRecordToFile.setToolTipText("Save to FIle - Right Click for Settings");
        tglRecordToFile.setMaximumSize(new java.awt.Dimension(32767, 32));
        tglRecordToFile.setMinimumSize(new java.awt.Dimension(87, 21));
        tglRecordToFile.setName("tglRecordToFile"); // NOI18N
        tglRecordToFile.setPreferredSize(new java.awt.Dimension(300, 22));
        tglRecordToFile.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-record.png"))); // NOI18N
        tglRecordToFile.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglRecordToFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglRecordToFileActionPerformed(evt);
            }
        });

        tglUDP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-record.png"))); // NOI18N
        tglUDP.setText(bundle.getString("UDP_MPEG_OUT")); // NOI18N
        tglUDP.setToolTipText("Stream to udp://@127.0.0.1:7000 - Right Click for Settings");
        tglUDP.setMaximumSize(new java.awt.Dimension(32767, 32));
        tglUDP.setMinimumSize(new java.awt.Dimension(237, 21));
        tglUDP.setName("tglUDP"); // NOI18N
        tglUDP.setPreferredSize(new java.awt.Dimension(300, 22));
        tglUDP.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-record.png"))); // NOI18N
        tglUDP.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglUDP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglUDPActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelDefaultOutputsLayout = new javax.swing.GroupLayout(panelDefaultOutputs);
        panelDefaultOutputs.setLayout(panelDefaultOutputsLayout);
        panelDefaultOutputsLayout.setHorizontalGroup(
            panelDefaultOutputsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDefaultOutputsLayout.createSequentialGroup()
                .addGroup(panelDefaultOutputsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tglUDP, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tglRecordToFile, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        panelDefaultOutputsLayout.setVerticalGroup(
            panelDefaultOutputsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDefaultOutputsLayout.createSequentialGroup()
                .addComponent(tglRecordToFile, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tglUDP, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout panelDefaultLayout = new javax.swing.GroupLayout(panelDefault);
        panelDefault.setLayout(panelDefaultLayout);
        panelDefaultLayout.setHorizontalGroup(
            panelDefaultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 178, Short.MAX_VALUE)
            .addGroup(panelDefaultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelDefaultLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelDefaultOutputs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        panelDefaultLayout.setVerticalGroup(
            panelDefaultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 124, Short.MAX_VALUE)
            .addGroup(panelDefaultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelDefaultLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(panelDefaultOutputs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        outputTabs.addTab("Default", panelDefault);

        panelFME.setName("panelFME"); // NOI18N

        panelSetFME.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "WebcamStudio FME", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Noto Sans", 0, 10))); // NOI18N
        panelSetFME.setName("panelSetFME"); // NOI18N

        btnAddFME.setFont(new java.awt.Font("Noto Sans", 3, 12)); // NOI18N
        btnAddFME.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/list-add.png"))); // NOI18N
        btnAddFME.setText("Add FME");
        btnAddFME.setToolTipText("Add FME");
        btnAddFME.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnAddFME.setMinimumSize(new java.awt.Dimension(22, 22));
        btnAddFME.setName("btnAddFME"); // NOI18N
        btnAddFME.setPreferredSize(new java.awt.Dimension(300, 22));
        btnAddFME.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFMEActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelSetFMELayout = new javax.swing.GroupLayout(panelSetFME);
        panelSetFME.setLayout(panelSetFMELayout);
        panelSetFMELayout.setHorizontalGroup(
            panelSetFMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 144, Short.MAX_VALUE)
            .addGroup(panelSetFMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelSetFMELayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(btnAddFME, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        panelSetFMELayout.setVerticalGroup(
            panelSetFMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 79, Short.MAX_VALUE)
            .addGroup(panelSetFMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelSetFMELayout.createSequentialGroup()
                    .addGap(15, 15, 15)
                    .addComponent(btnAddFME, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(29, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout panelFMELayout = new javax.swing.GroupLayout(panelFME);
        panelFME.setLayout(panelFMELayout);
        panelFMELayout.setHorizontalGroup(
            panelFMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 178, Short.MAX_VALUE)
            .addGroup(panelFMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelFMELayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelSetFME, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        panelFMELayout.setVerticalGroup(
            panelFMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 124, Short.MAX_VALUE)
            .addGroup(panelFMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelFMELayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelSetFME, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        outputTabs.addTab("FME", panelFME);

        add(outputTabs, new org.netbeans.lib.awtextra.AbsoluteConstraints(17, 31, 190, -1));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("FMEs");
        jLabel1.setName("jLabel1"); // NOI18N
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(17, 203, 203, -1));
    }// </editor-fold>//GEN-END:initComponents


    private void tglRecordToFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglRecordToFileActionPerformed
        if (tglRecordToFile.isSelected()) {
            boolean overWrite = true;
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter aviFilter = new FileNameExtensionFilter("AVI files (*.avi)", "avi");
            FileNameExtensionFilter mp4Filter = new FileNameExtensionFilter("MP4 files (*.mp4)", "mp4");
            FileNameExtensionFilter flvFilter = new FileNameExtensionFilter("FLV files (*.flv)", "flv");

            chooser.setFileFilter(aviFilter);
            chooser.setFileFilter(mp4Filter);
            chooser.setFileFilter(flvFilter);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("Choose Destination File ...");
            int retval = chooser.showSaveDialog(this);
            f = chooser.getSelectedFile();
            if (retval == JFileChooser.APPROVE_OPTION && f != null) {
                if (chooser.getFileFilter().equals(aviFilter)) {
                    if(!chooser.getSelectedFile().getAbsolutePath().endsWith(".avi")){
                        f =  new File(chooser.getSelectedFile() + ".avi");
                    }
                } else if (chooser.getFileFilter().equals(mp4Filter) && !chooser.getSelectedFile().getAbsolutePath().endsWith(".mp4")) {
                    f =  new File(chooser.getSelectedFile() + ".mp4");
                } else if (chooser.getFileFilter().equals(flvFilter) && !chooser.getSelectedFile().getAbsolutePath().endsWith(".flv")) {
                    f =  new File(chooser.getSelectedFile() + ".flv");
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
                fileStream.setFile(f);
                fileStream.setListener(instanceSink);
                // Fix lost prefs
                if ("".equals(fileStream.getVbitrate())) {
                    fileStream.setVbitrate("1200");
                }
                if ("".equals(fileStream.getAbitrate())) {
                    fileStream.setAbitrate("128");
                }

                fileStream.read();
//                System.out.println("VBitRate: "+fileStream.getVbitrate());
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

                ResourceMonitorLabel label2 = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "File is saved!");
                ResourceMonitor.getInstance().addMessage(label2);
            }
        }

    }//GEN-LAST:event_tglRecordToFileActionPerformed


    private void tglUDPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglUDPActionPerformed
        if (tglUDP.isSelected()) {
            udpOutState = true;
            udpStream.setListener(instanceSink);
            // Fix lost prefs
            if ("".equals(udpStream.getVbitrate())) {
                udpStream.setVbitrate("1200");
            }
            if ("".equals(udpStream.getAbitrate())) {
                udpStream.setAbitrate("128");
            }
            if ("".equals(udpStream.getStandard())) {
                udpStream.setStandard("STD");
            }

            udpStream.read();
            udpOut.put("UDPOut", udpStream);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Unicast mpeg2 to udp://127.0.0.1:7000");
            labels.put("UDPOut", label);
            ResourceMonitor.getInstance().addMessage(label);
            lblOnAir.setForeground(Color.RED);
        } else {
            udpOutState = false;
            SinkUDP udpStream = udpOut.get("UDPOut");
            if (udpStream != null) {
                udpStream.stop();
                udpStream = null;
                udpOut.remove("UDPOut");
                ResourceMonitorLabel label = labels.get("UDPOut");
                ResourceMonitor.getInstance().removeMessage(label);
            }
            if (fmeCount == 0) {
                if (theme.equals("Dark")) {
                    lblOnAir.setForeground(Color.WHITE);
                } else {
                    lblOnAir.setForeground(Color.BLACK);
                }
            }
        }
    }//GEN-LAST:event_tglUDPActionPerformed

    private void paintWSCamButtons() {
        for (final VideoDevice d : VideoDevice.getInputDevices()) {
            String vdName = d.getFile().getName();
            if (!vdName.endsWith("video21")) {
                JToggleButton wsCamButton = new JToggleButton();
                Dimension dim = new Dimension(139,22);
                wsCamButton.setPreferredSize(dim);
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
                            camOutState = true;
                            String cleanCam = checkDoubleCam(button.getText());
                            if (!"".equals(cleanCam)) {
                                devicesOut.add(d.getName());
                            }
                            camCount ++;
                            SinkLinuxDevice stream = new SinkLinuxDevice(new File(device), button.getText(), (comboPixelFormat.getSelectedIndex() + 1));
                            stream.setRate(MasterMixer.getInstance().getRate());
                            stream.setWidth(MasterMixer.getInstance().getWidth());
                            stream.setHeight(MasterMixer.getInstance().getHeight());
                            stream.setListener(instanceSink);
                            stream.read();
                            devices.put(button.getText(), stream);
                            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Rendering to " + button.getText() + " ("+(comboPixelFormat.getSelectedIndex() + 1)+")");
                            labels.put(button.getText(), label);
                            ResourceMonitor.getInstance().addMessage(label);
                            comboPixelFormat.setEnabled(false);
                        } else {
                            camOutState = camCount > 0;
                            devicesOut.remove(d.getName());
                            SinkLinuxDevice stream = devices.get(button.getText());
                            if (stream != null) {
                                camCount --;
                                stream.stop();
                                devices.remove(button.getText());
//                                System.out.println("WS Camera Stopped ...");
                                ResourceMonitorLabel label = labels.remove(button.getText());
                                ResourceMonitor.getInstance().removeMessage(label);
                                if (camCount == 0) {
                                    comboPixelFormat.setEnabled(true);
                                }
                            }
                            if (camCount == 0) {
                                comboPixelFormat.setEnabled(true);
                            }
                        }
                    }
                });

                panelPixelFormat.add(wsCamButton);
                panelPixelFormat.revalidate();
                this.revalidate();
            }
        }
    }

    @Override
    public void resetFMECount() {
        fmeCount=0;
    }

    @Override
    public void resetSinks(ActionEvent evt) {
        fileStream.destroy();
        udpStream.destroy();
        audioStream.destroy();
        f = new File(userHomeDir + "/.webcamstudio/Record To File");
        fileStream = new SinkFile(f);
        udpStream = new SinkUDP();
        audioStream = new SinkAudio();
        Preferences filePrefs = WebcamStudio.prefs.node("filerec");
        Preferences udpPrefs = WebcamStudio.prefs.node("udp");
        try {
            String[] servicesF = filePrefs.childrenNames();
            String[] servicesU = udpPrefs.childrenNames();

            for (String s : servicesF){
                Preferences serviceF = filePrefs.node(s);
                fileStream.setVbitrate(serviceF.get("vbitrate", ""));
                fileStream.setAbitrate(serviceF.get("abitrate", ""));
            }

            for (String s : servicesU){
                Preferences serviceU = udpPrefs.node(s);
                udpStream.setVbitrate(serviceU.get("vbitrate", ""));
                udpStream.setAbitrate(serviceU.get("abitrate", ""));
                udpStream.setStandard(serviceU.get("standard", ""));
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void requestReset() {
        audioOutSwitch = audioOutState;
        udpOutSwitch = udpOutState;
        if (fmeOutState) {
            fmeOutSwitch = true;
            fmeCount = 0;
        } else {
            fmeOutSwitch = false;
        }
        if (camOutState) {
            camOutSwitch = true;
            camCount = 0;
        } else {
            camOutSwitch = false;
        }

    }

    @Override
    public void requestStart() {
        String[] currentBroadcasts = new String[broadcastsOut.size()];
        currentBroadcasts = broadcastsOut.toArray(currentBroadcasts);
        String[] currentDevices = new String[devicesOut.size()];
        currentDevices = devicesOut.toArray(currentDevices);
        if (audioOutSwitch){
            tglAudioOut.doClick();
        }
        if (udpOutSwitch){
            tglUDP.doClick();
        }
        if (fmeOutSwitch){
            for (String bro : currentBroadcasts) {
                for (Component c : this.getComponents()) {
                    if (c instanceof JToggleButton) {
                        JToggleButton b = (JToggleButton) c;
                        if (b.getText().equals(bro)) {
                            b.doClick();
                        }
                    }
                }
            }
        }
        if (camOutSwitch){
            for (String cam : currentDevices) {
                for (Component c : this.getComponents()) {
                    if (c instanceof JToggleButton) {
                        JToggleButton b = (JToggleButton) c;
                        if (b.getText().equals(cam)) {
                            b.doClick();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void requestStop() {
        audioOutSwitch = false;
        udpOutSwitch = false;
        fmeOutSwitch = false;
        camOutSwitch = false;
    }

    @Override
    public void resetAutoPLBtnState(ActionEvent evt) {
        // Nothing Here
    }

    @Override
    public void resetBtnStates(ActionEvent evt) {
        comboPixelFormat.setEnabled(true);
        tglWSAudioDev.setEnabled(true);
        camCount = 0;
        fmeCount = 0;
        broadcastsOut.clear();
        devicesOut.clear();
    }

    @Override
    public void setRemoteOn() {
        // Nothing Here
    }

    private void tglAudioOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglAudioOutActionPerformed

        if (tglAudioOut.isSelected()) {
            audioOutState = true;
            tglWSAudioDev.setEnabled(false);
            audioStream.setListener(instanceSink);
            audioStream.read();
            audioOut.put("AudioOut", audioStream);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Master Audio to Speakers");
            labels.put("AudioOut", label);
            ResourceMonitor.getInstance().addMessage(label);
            if (tglWSAudioDev.isSelected()){
                try {
                    Process p = Runtime.getRuntime().exec("pactl list short sinks");
                    InputStreamReader isr;
                    BufferedReader reader;
                    try (InputStream in = p.getInputStream()) {
                        isr = new InputStreamReader(in);
                        reader = new BufferedReader(isr);
                        String line = reader.readLine();
                        while (line != null) {
                            if (line.contains("WSAudioDevice")) {
                                String [] id = line.split("\t");
                                System.out.println("Found WSAD: <"+id[0]+">");
                                idWSAD = id[0];
                            }
                            line = reader.readLine();
                        }
                    }
                    isr.close();
                    reader.close();
                    p.destroy();
                } catch (IOException ex) {
                    Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                Tools.sleep(250);
                try {
                    Process p = Runtime.getRuntime().exec("pactl list short sink-inputs");
                    InputStreamReader isr;
                    BufferedReader reader;
                    try (InputStream in = p.getInputStream()) {
                        isr = new InputStreamReader(in);
                        reader = new BufferedReader(isr);
                        String line = reader.readLine();
                        while (line != null) {
                            String [] id = line.split("\t");
                            System.out.println("Found WSOut: <"+id[0]+">"+"<"+id[1]+">");
                            idWSOuts.add(id[0]);
                            idDef = id[1];
                            line = reader.readLine();
                        }
                    }
                    isr.close();
                    reader.close();
                    p.destroy();
                } catch (IOException ex) {
                    Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                idWSOut = idWSOuts.get(idWSOuts.size()-1);
                try {
                    execPACTL("pactl move-sink-input "+idWSOut+" "+idWSAD);
                } catch (IOException ex) {
                    Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            audioOutState = false;
            SinkAudio audioStream = audioOut.get("AudioOut");
//            System.out.println("Killed SinkAudio"+audioStream);
            tglWSAudioDev.setEnabled(true);
            if (audioStream != null) {
                audioStream.stop();
                audioStream = null;
                audioOut.remove("AudioOut");
                ResourceMonitorLabel label = labels.get("AudioOut");
                ResourceMonitor.getInstance().removeMessage(label);
            }
            if (tglWSAudioDev.isSelected()){

            }
        }
    }//GEN-LAST:event_tglAudioOutActionPerformed

    private void tglWSAudioDevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglWSAudioDevActionPerformed
        if (tglWSAudioDev.isSelected()){
            try {
                execPACTL("pactl load-module module-null-sink sink_name=WSAudioDevice sink_properties=device.description=\"WSAudioDevice\"");
            } catch (IOException ex) {
                Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            Tools.sleep(100);
        } else {
            try {
                execPACTL("pactl unload-module module-null-sink");
            } catch (IOException ex) {
                Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            Tools.sleep(100);
        }
    }//GEN-LAST:event_tglWSAudioDevActionPerformed

    private void btnAddFMEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFMEActionPerformed
        final FME fme = new FME();
        final FMEDialog fmeDiag = new FMEDialog(fme);
        fmeDiag.setLocationRelativeTo(WebcamStudio.cboAnimations);
        fmeDiag.setAlwaysOnTop(true);
        fmeDiag.setVisible(true);

        Thread addFME = new Thread(new Runnable() {

            @Override
            public void run() {
                while (fmeDiag.isVisible()) {
                    Tools.sleep(100);
                }
                if (FMEDialog.add.equals("ok")) {
                    fmes.put(fme.getName(), fme);
                    addButtonBroadcast(fme);
                }
            }
        });
        
        addFME.setPriority(Thread.MIN_PRIORITY);
        addFME.start();
    }//GEN-LAST:event_btnAddFMEActionPerformed

    public static void execPACTL(String command) throws IOException, InterruptedException {
//        String output;
//        System.out.println(command);
        Process p = Runtime.getRuntime().exec(command);
        try (InputStream in = p.getInputStream(); InputStreamReader isr = new InputStreamReader(in)) {
            BufferedReader reader = new BufferedReader(isr);
            reader.readLine();
            reader.close();
        }
        p.waitFor();
        p.destroy();
//        System.out.println("Output: " + output);
//        return output;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddFME;
    private javax.swing.JComboBox comboPixelFormat;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel labelPixelFormat;
    private javax.swing.JTabbedPane outputTabs;
    private javax.swing.JPanel panelDefault;
    private javax.swing.JPanel panelDefaultOutputs;
    private javax.swing.JPanel panelFME;
    private javax.swing.JPanel panelPixelFormat;
    private javax.swing.JPanel panelSetFME;
    private javax.swing.JPanel panelVirtualA;
    private javax.swing.JPanel panelVirtualAudio;
    private javax.swing.JPanel panelVirtualC;
    private javax.swing.JToggleButton tglAudioOut;
    private javax.swing.JToggleButton tglRecordToFile;
    final OutputPanel instanceSink = this;
    private javax.swing.JToggleButton tglUDP;
    private javax.swing.JCheckBox tglWSAudioDev;
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
        // nothing here.
    }

    @Override
    public void stopChTime(ActionEvent evt) {
        // nothing here.
    }

    @Override
    public void resetButtonsStates(ActionEvent evt) {
        comboPixelFormat.setEnabled(true);
        tglWSAudioDev.setEnabled(true);
        camCount = 0;
        fmeCount = 0;
    }

    @Override
    public void addLoadingChannel(String name) { // used addLoadingChannel to activate Output from command line.
        for (Component c : this.getComponents()) {
            // At this moment this workaround. After will make the proper fix.
            if (c instanceof JCheckBox) {
                // Nothing here.
            } else if (c instanceof JToggleButton) {
                JToggleButton b = (JToggleButton) c;
                if (b.getText().contains(name)) {
                    b.doClick();
                }
            }
        }
    }

    @Override
    public void removeChannels(String removeSc, int a) {

    }
}
