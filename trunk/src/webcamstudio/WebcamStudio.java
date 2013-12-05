/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * WebcamStudio.java
 *
 * Created on 4-Apr-2012, 3:48:07 PM
 */
package webcamstudio;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;
import webcamstudio.channels.MasterChannels;
import webcamstudio.components.*;
import static webcamstudio.components.MasterPanel.*;
import webcamstudio.exporter.vloopback.VideoDevice;
import webcamstudio.externals.ProcessRenderer;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.mixers.SystemPlayer;
import webcamstudio.streams.*;
import webcamstudio.studio.Studio;
import webcamstudio.util.Tools;
import webcamstudio.util.Tools.OS;

/**
 *
 * @author patrick (modified by karl)
 */
public class WebcamStudio extends javax.swing.JFrame implements StreamDesktop.Listener, StreamFullDesktop.Listener {

    public static Preferences prefs = null;
    public static Properties animations = new Properties();
    OutputPanel recorder = new OutputPanel(this);
    Frame about = new Frame();
    Frame vDevInfo = new Frame();
    Stream stream = null;
    public static int audioFreq = 22050;
    ArrayList<Stream> streamS = MasterChannels.getInstance().getStreams();
    private File lastFolder = null;
    public interface Listener {
        public void stopChTime(java.awt.event.ActionEvent evt);
        public void resetBtnStates(java.awt.event.ActionEvent evt);
        public void addLoadingChannel(String name);
        public void removeChannels(String removeSc, int a);
    }
    static Listener listener = null;
    public static void setListener(Listener l) {
        listener = l;
    }
    static Listener listenerOP = null;
    public static void setListenerOP(Listener l) {
        listenerOP = l;
    }
    
    /**
     * Creates new form WebcamStudio
     */
    
    public WebcamStudio() {
        
        initComponents(); 
        String build = new Version().getBuild();
        setTitle("WebcamStudio " + Version.version + " (" + build + ")");
        ImageIcon icon = new ImageIcon(this.getClass().getResource("/webcamstudio/resources/icon.png"));
        this.setIconImage(icon.getImage());
        
        desktop.setDropTarget(new DropTarget() {

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
                            if (file.exists()) {
                                fileName = file.getName();
                                Stream stream = Stream.getInstance(file);
                                if (stream != null) {
                                    if (stream instanceof SourceMovie || stream instanceof SourceMusic) {
                                        durationCalc(stream, file);
                                    }
                                    StreamDesktop frame = getNewStreamDesktop(stream);
                                    desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                                    frame.setLocation(evt.getLocation());
                                    try {
                                        frame.setSelected(true);
                                    } catch (PropertyVetoException ex) {
                                        Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    success = true;
                                }
                            }
                        }
                    }
                    evt.dropComplete(success);
                    if (!success) {
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 5000, "Unsupported file: " + fileName);
                        ResourceMonitor.getInstance().addMessage(label);
                    }
                } catch (UnsupportedFlavorException | IOException | URISyntaxException ex) {
                    Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        this.add(ResourceMonitor.getInstance(), BorderLayout.SOUTH);
        prefs = Preferences.userNodeForPackage(this.getClass());
        panControls.add(recorder, BorderLayout.NORTH);

        loadPrefs();
        MasterMixer.getInstance().start();
        this.add(new MasterPanel(), BorderLayout.WEST);
        initAnimations();
        initWebcam();
        initAudioSW();
        loadCustomSources();       
    }

    private StreamDesktop getNewStreamDesktop(Stream s) {
        return new StreamDesktop(s, this);
    }
    
    private void loadCustomSources() {
        File userSettings = new File(System.getProperty("user.home") + "/.webcamstudio");
        if (userSettings.exists() && userSettings.isDirectory()) {
            File sources = new File(userSettings, "sources");
            if (sources.exists() && sources.isDirectory()) {
                File[] custom = sources.listFiles();
                for (File f : custom) {
                    if (f.getName().toLowerCase().endsWith(".wss")) {
                        SourceCustom streamCST;
                        streamCST = new SourceCustom(f);
                        StreamDesktop frame = new StreamDesktop(streamCST, this);
                        desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                        frame.setClosable(false);
                        try {
                            frame.setIcon(true);
                        } catch (PropertyVetoException ex) {
                            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")  
    private void initAnimations() {
        try {
            animations.load(getClass().getResourceAsStream("/webcamstudio/resources/animations/animations.properties"));
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (Object o : animations.keySet()) {
                model.addElement(o); 
            }
            cboAnimations.setModel(model);
        } catch (IOException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void initWebcam() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        if (Tools.getOS() == OS.LINUX) {
            for (VideoDevice d : VideoDevice.getOutputDevices()) {
                model.addElement(d.getName());
            }
        }
        cboWebcam.setModel(model);            
    }
    
    @SuppressWarnings("unchecked")
    private void initAudioSW() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("22050Hz");
        model.addElement("44100Hz");
        cboAudioHz.setModel(model);            
    }

    private void loadPrefs() {
        int x = prefs.getInt("main-x", 100);
        int y = prefs.getInt("main-y", 100);
        int w = prefs.getInt("main-w", 800);
        int h = prefs.getInt("main-h", 400);
        MasterMixer.getInstance().setWidth(prefs.getInt("mastermixer-w", MasterMixer.getInstance().getWidth()));
        MasterMixer.getInstance().setHeight(prefs.getInt("mastermixer-h", MasterMixer.getInstance().getHeight()));
        MasterMixer.getInstance().setRate(prefs.getInt("mastermixer-r", MasterMixer.getInstance().getRate()));
        mainSplit.setDividerLocation(prefs.getInt("split-x", mainSplit.getDividerLocation()));
        mainSplit.setDividerLocation(prefs.getInt("split-last-x", mainSplit.getLastDividerLocation()));
        lastFolder = new File(prefs.get("lastfolder", "."));
        this.setLocation(x, y);
        this.setSize(w, h);
        recorder.loadPrefs(prefs);
    }

    private void savePrefs() {
        prefs.putInt("main-x", this.getX());
        prefs.putInt("main-y", this.getY());
        prefs.putInt("main-w", this.getWidth());
        prefs.putInt("main-h", this.getHeight());
        prefs.putInt("mastermixer-w", MasterMixer.getInstance().getWidth());
        prefs.putInt("mastermixer-h", MasterMixer.getInstance().getHeight());
        prefs.putInt("mastermixer-r", MasterMixer.getInstance().getRate());
        prefs.putInt("split-x", mainSplit.getDividerLocation());
        prefs.putInt("split-last-x", mainSplit.getLastDividerLocation());
        if (lastFolder != null) {
            prefs.put("lastfolder", lastFolder.getAbsolutePath());
        }
        recorder.savePrefs(prefs);
        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainSplit = new javax.swing.JSplitPane();
        panSources = new javax.swing.JPanel();
        toolbar = new javax.swing.JToolBar();
        btnAddFile = new javax.swing.JButton();
        btnAddDVB = new javax.swing.JButton();
        btnAddURL = new javax.swing.JButton();
        btnAddDesktop = new javax.swing.JButton();
        btnAddText = new javax.swing.JButton();
        btnAddQRCode = new javax.swing.JButton();
        btnAddMic = new javax.swing.JButton();
        btnAddMon = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        cboAnimations = new javax.swing.JComboBox();
        btnAddAnimation = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnMinimizeAll = new javax.swing.JButton();
        desktop = new javax.swing.JDesktopPane();
        panControls = new javax.swing.JPanel();
        tabControls = new javax.swing.JTabbedPane();
        lblSourceSelected = new javax.swing.JLabel();
        mainToolbar = new javax.swing.JToolBar();
        btnSaveStudio = new javax.swing.JButton();
        btnJoinStudio = new javax.swing.JButton();
        btnNewStudio = new javax.swing.JButton();
        WCSAbout = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        cboWebcam = new javax.swing.JComboBox();
        btnAddWebcams = new javax.swing.JButton();
        btnRefreshWebcam = new javax.swing.JButton();
        btnVideoDevInfo = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jLabel2 = new javax.swing.JLabel();
        cboAudioHz = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("WebcamStudio");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        mainSplit.setDividerLocation(400);
        mainSplit.setName("mainSplit"); // NOI18N
        mainSplit.setOneTouchExpandable(true);

        panSources.setName("panSources"); // NOI18N

        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setMinimumSize(new java.awt.Dimension(200, 34));
        toolbar.setName("toolbar"); // NOI18N

        btnAddFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/studio-add.png"))); // NOI18N
        btnAddFile.setToolTipText("Load Media");
        btnAddFile.setFocusable(false);
        btnAddFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddFile.setName("btnAddFile"); // NOI18N
        btnAddFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFileActionPerformed(evt);
            }
        });
        toolbar.add(btnAddFile);

        btnAddDVB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/image-x-generic.png"))); // NOI18N
        btnAddDVB.setToolTipText("Add DVB-T Stream");
        btnAddDVB.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnAddDVB.setFocusable(false);
        btnAddDVB.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddDVB.setName("btnAddDVB"); // NOI18N
        btnAddDVB.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddDVB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDVBActionPerformed(evt);
            }
        });
        toolbar.add(btnAddDVB);

        btnAddURL.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/rss.png"))); // NOI18N
        btnAddURL.setToolTipText("Add URL Stream");
        btnAddURL.setFocusable(false);
        btnAddURL.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddURL.setName("btnAddURL"); // NOI18N
        btnAddURL.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddURLActionPerformed(evt);
            }
        });
        toolbar.add(btnAddURL);

        btnAddDesktop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/user-desktop.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        btnAddDesktop.setToolTipText(bundle.getString("DESKTOP")); // NOI18N
        btnAddDesktop.setFocusable(false);
        btnAddDesktop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddDesktop.setName("btnAddDesktop"); // NOI18N
        btnAddDesktop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddDesktop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDesktopActionPerformed(evt);
            }
        });
        toolbar.add(btnAddDesktop);

        btnAddText.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/accessories-text-editor.png"))); // NOI18N
        btnAddText.setToolTipText(bundle.getString("TEXT")); // NOI18N
        btnAddText.setFocusable(false);
        btnAddText.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddText.setName("btnAddText"); // NOI18N
        btnAddText.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTextActionPerformed(evt);
            }
        });
        toolbar.add(btnAddText);

        btnAddQRCode.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/dialog-information.png"))); // NOI18N
        btnAddQRCode.setToolTipText(bundle.getString("QRCODE")); // NOI18N
        btnAddQRCode.setFocusable(false);
        btnAddQRCode.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddQRCode.setName("btnAddQRCode"); // NOI18N
        btnAddQRCode.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddQRCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddQRCodeActionPerformed(evt);
            }
        });
        toolbar.add(btnAddQRCode);

        btnAddMic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/audio-input-microphone.png"))); // NOI18N
        btnAddMic.setToolTipText(bundle.getString("MICROPHONE")); // NOI18N
        btnAddMic.setFocusable(false);
        btnAddMic.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddMic.setName("btnAddMic"); // NOI18N
        btnAddMic.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddMic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddMicActionPerformed(evt);
            }
        });
        toolbar.add(btnAddMic);

        btnAddMon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/audio-volume-high.png"))); // NOI18N
        btnAddMon.setToolTipText("Sound Monitor");
        btnAddMon.setFocusable(false);
        btnAddMon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddMon.setName("btnAddMon"); // NOI18N
        btnAddMon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddMon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddMonActionPerformed(evt);
            }
        });
        toolbar.add(btnAddMon);

        jSeparator1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSeparator1.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jSeparator1.setName("jSeparator1"); // NOI18N
        jSeparator1.setOpaque(true);
        toolbar.add(jSeparator1);

        cboAnimations.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboAnimations.setToolTipText(bundle.getString("ANIMATIONS")); // NOI18N
        cboAnimations.setName("cboAnimations"); // NOI18N
        toolbar.add(cboAnimations);

        btnAddAnimation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/Anim-add.png"))); // NOI18N
        btnAddAnimation.setToolTipText(bundle.getString("ADD_ANIMATION")); // NOI18N
        btnAddAnimation.setFocusable(false);
        btnAddAnimation.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddAnimation.setName("btnAddAnimation"); // NOI18N
        btnAddAnimation.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddAnimation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddAnimationActionPerformed(evt);
            }
        });
        toolbar.add(btnAddAnimation);

        jSeparator2.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jSeparator2.setName("jSeparator2"); // NOI18N
        jSeparator2.setOpaque(true);
        toolbar.add(jSeparator2);

        btnMinimizeAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/go-down.png"))); // NOI18N
        btnMinimizeAll.setToolTipText(bundle.getString("ICON_ALL")); // NOI18N
        btnMinimizeAll.setFocusable(false);
        btnMinimizeAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnMinimizeAll.setName("btnMinimizeAll"); // NOI18N
        btnMinimizeAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnMinimizeAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMinimizeAllActionPerformed(evt);
            }
        });
        toolbar.add(btnMinimizeAll);

        desktop.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("SOURCES"))); // NOI18N
        desktop.setToolTipText(bundle.getString("DROP_SOURCSE")); // NOI18N
        desktop.setAutoscrolls(true);
        desktop.setName("desktop"); // NOI18N

        javax.swing.GroupLayout panSourcesLayout = new javax.swing.GroupLayout(panSources);
        panSources.setLayout(panSourcesLayout);
        panSourcesLayout.setHorizontalGroup(
            panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(desktop)
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        panSourcesLayout.setVerticalGroup(
            panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panSourcesLayout.createSequentialGroup()
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(desktop, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainSplit.setLeftComponent(panSources);

        panControls.setName("panControls"); // NOI18N
        panControls.setPreferredSize(new java.awt.Dimension(200, 455));
        panControls.setLayout(new java.awt.BorderLayout());

        tabControls.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("PROPERTIES"))); // NOI18N
        tabControls.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tabControls.setName("tabControls"); // NOI18N
        tabControls.setPreferredSize(new java.awt.Dimension(200, 455));
        panControls.add(tabControls, java.awt.BorderLayout.CENTER);

        lblSourceSelected.setName("lblSourceSelected"); // NOI18N
        panControls.add(lblSourceSelected, java.awt.BorderLayout.SOUTH);

        mainSplit.setRightComponent(panControls);

        getContentPane().add(mainSplit, java.awt.BorderLayout.CENTER);

        mainToolbar.setFloatable(false);
        mainToolbar.setName("mainToolbar"); // NOI18N

        btnSaveStudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/document-save.png"))); // NOI18N
        btnSaveStudio.setToolTipText(bundle.getString("SAVE")); // NOI18N
        btnSaveStudio.setFocusable(false);
        btnSaveStudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSaveStudio.setName("btnSaveStudio"); // NOI18N
        btnSaveStudio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSaveStudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveStudioActionPerformed(evt);
            }
        });
        mainToolbar.add(btnSaveStudio);

        btnLoadStudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/document-open.png"))); // NOI18N
        btnLoadStudio.setToolTipText(bundle.getString("LOAD")); // NOI18N
        btnLoadStudio.setFocusable(false);
        btnLoadStudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLoadStudio.setName("btnLoadStudio"); // NOI18N
        btnLoadStudio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnLoadStudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadStudioActionPerformed(evt);
            }
        });
        mainToolbar.add(btnLoadStudio);

        btnJoinStudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/chan-add.png"))); // NOI18N
        btnJoinStudio.setToolTipText("Import Studio");
        btnJoinStudio.setFocusable(false);
        btnJoinStudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnJoinStudio.setName("btnJoinStudio"); // NOI18N
        btnJoinStudio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnJoinStudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnJoinStudioActionPerformed(evt);
            }
        });
        mainToolbar.add(btnJoinStudio);

        btnNewStudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/document-new.png"))); // NOI18N
        btnNewStudio.setToolTipText("New Studio");
        btnNewStudio.setFocusable(false);
        btnNewStudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNewStudio.setName("btnNewStudio"); // NOI18N
        btnNewStudio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNewStudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewStudioActionPerformed(evt);
            }
        });
        mainToolbar.add(btnNewStudio);

        WCSAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/user-info.png"))); // NOI18N
        WCSAbout.setToolTipText("About");
        WCSAbout.setFocusable(false);
        WCSAbout.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        WCSAbout.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        WCSAbout.setName("WCSAbout"); // NOI18N
        WCSAbout.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        WCSAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WCSAboutActionPerformed(evt);
            }
        });
        mainToolbar.add(WCSAbout);

        jSeparator5.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSeparator5.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jSeparator5.setName("jSeparator5"); // NOI18N
        jSeparator5.setOpaque(true);
        mainToolbar.add(jSeparator5);

        cboWebcam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboWebcam.setToolTipText("Detected Video Devices");
        cboWebcam.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        cboWebcam.setName("cboWebcam"); // NOI18N
        mainToolbar.add(cboWebcam);

        btnAddWebcams.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/camera-video.png"))); // NOI18N
        btnAddWebcams.setToolTipText("Add Selected Device");
        btnAddWebcams.setFocusable(false);
        btnAddWebcams.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddWebcams.setName("btnAddWebcams"); // NOI18N
        btnAddWebcams.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddWebcams.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddWebcamsActionPerformed(evt);
            }
        });
        mainToolbar.add(btnAddWebcams);

        btnRefreshWebcam.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/view-refresh.png"))); // NOI18N
        btnRefreshWebcam.setToolTipText("Refresh Video Devices Detection");
        btnRefreshWebcam.setFocusable(false);
        btnRefreshWebcam.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRefreshWebcam.setName("btnRefreshWebcam"); // NOI18N
        btnRefreshWebcam.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRefreshWebcam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshWebcamActionPerformed(evt);
            }
        });
        mainToolbar.add(btnRefreshWebcam);

        btnVideoDevInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/camera-photo.png"))); // NOI18N
        btnVideoDevInfo.setToolTipText(bundle.getString("VIDEO_DEVICE_INFO")); // NOI18N
        btnVideoDevInfo.setFocusable(false);
        btnVideoDevInfo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnVideoDevInfo.setName("btnVideoDevInfo"); // NOI18N
        btnVideoDevInfo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnVideoDevInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVideoDevInfoActionPerformed(evt);
            }
        });
        mainToolbar.add(btnVideoDevInfo);

        jSeparator6.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSeparator6.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jSeparator6.setName("jSeparator6"); // NOI18N
        jSeparator6.setOpaque(true);
        mainToolbar.add(jSeparator6);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/audio-Hz.png"))); // NOI18N
        jLabel2.setToolTipText("Master Audio Sample Rate");
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 1));
        jLabel2.setName("jLabel2"); // NOI18N
        mainToolbar.add(jLabel2);

        cboAudioHz.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboAudioHz.setToolTipText("Choose Default Audio Output Quality.");
        cboAudioHz.setName("cboAudioHz"); // NOI18N
        cboAudioHz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboAudioHzActionPerformed(evt);
            }
        });
        mainToolbar.add(cboAudioHz);

        getContentPane().add(mainToolbar, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        savePrefs();
        SystemPlayer.getInstance(null).stop();
        Tools.sleep(10);
        MasterChannels.getInstance().stopAllStream();
        Tools.sleep(10);
        MasterMixer.getInstance().stop();
        System.out.println("Thanks for using WebcamStudio ...");
        System.out.println("GoodBye!");
        System.exit(0);
    }//GEN-LAST:event_formWindowClosing

    private void btnAddDesktopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddDesktopActionPerformed
        SourceDesktop streamDesk;
        streamDesk = new SourceDesktop();
        ArrayList<String> allChan = new ArrayList<>();
        for (String scn : MasterChannels.getInstance().getChannels()){
            allChan.add(scn); 
        } 
        for (String sc : allChan){
            streamDesk.addChannel(SourceChannel.getChannel(sc, streamDesk));
        }
        StreamDesktop frame = new StreamDesktop(streamDesk, this);
        desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddDesktopActionPerformed
 
    private void btnAddTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTextActionPerformed
        SourceText streamTXT;
        streamTXT = new SourceText("");
        ArrayList<String> allChan = new ArrayList<>();
        for (String scn : MasterChannels.getInstance().getChannels()){
            allChan.add(scn); 
        } 
        for (String sc : allChan){
            streamTXT.addChannel(SourceChannel.getChannel(sc, streamTXT));
        }
        StreamDesktop frame = new StreamDesktop(streamTXT, this);
        desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddTextActionPerformed

    private void btnAddQRCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddQRCodeActionPerformed
        SourceQRCode streamQR;
        streamQR = new SourceQRCode("WebcamStudio");
        ArrayList<String> allChan = new ArrayList<>();
        for (String scn : MasterChannels.getInstance().getChannels()){
            allChan.add(scn); 
        } 
        for (String sc : allChan){
            streamQR.addChannel(SourceChannel.getChannel(sc, streamQR));
        }
        StreamDesktop frame = new StreamDesktop(streamQR, this);
        desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddQRCodeActionPerformed

    private void btnAddFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFileActionPerformed
        JFileChooser chooser = new JFileChooser(lastFolder);
        FileNameExtensionFilter mediaFilter = new FileNameExtensionFilter("Supported Media files", "avi", "ogg", "jpeg", "ogv", "mp4", "m4v", "mpg", "divx", "wmv", "flv", "mov", "mkv", "vob", "jpg", "bmp", "png", "gif", "mp3", "wav", "wma", "m4a", ".mp2");
        chooser.setFileFilter(mediaFilter);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Add Media file ...");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int retVal = chooser.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                lastFolder = file.getParentFile();
                String FileName = file.getName();
                System.out.println("Name: " + FileName);
            }
            if (file != null) {
                Stream s = Stream.getInstance(file);
                if (s != null) {
                    if (s instanceof SourceMovie || s instanceof SourceMusic) {
                        durationCalc(s, file);
                    }
                    ArrayList<String> allChan = new ArrayList<>();
                    for (String scn : MasterChannels.getInstance().getChannels()){
                        allChan.add(scn); 
                    } 
                    for (String sc : allChan){
                        s.addChannel(SourceChannel.getChannel(sc, s));
                    }
                    StreamDesktop frame = new StreamDesktop(s, this);
                    desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                       
                    try {
                        frame.setSelected(true);
                    } catch (PropertyVetoException ex) {
                        Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "No File Selected!");
                ResourceMonitor.getInstance().addMessage(label);
            }
        } else {
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Loading Cancelled!");
                ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_btnAddFileActionPerformed

    private void btnAddAnimationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAnimationActionPerformed
        String key = cboAnimations.getSelectedItem().toString();
        String res = animations.getProperty(key);
        URL url = getClass().getResource("/webcamstudio/resources/animations/" + res);
        Stream streamAnm;
        streamAnm = new SourceImageGif(key, url);
        ArrayList<String> allChan = new ArrayList<>();
        for (String scn : MasterChannels.getInstance().getChannels()){
            allChan.add(scn); 
        } 
        for (String sc : allChan){
            streamAnm.addChannel(SourceChannel.getChannel(sc, streamAnm));
        }
        StreamDesktop frame = new StreamDesktop(streamAnm, this);
        desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddAnimationActionPerformed

    private void btnAddMicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddMicActionPerformed
        SourceMicrophone source = new SourceMicrophone();
        ArrayList<String> allChan = new ArrayList<>();
        for (String scn : MasterChannels.getInstance().getChannels()){
            allChan.add(scn); 
        } 
        for (String sc : allChan){
            source.addChannel(SourceChannel.getChannel(sc, source));
        }
        StreamDesktop frame = new StreamDesktop(source, this);
        desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddMicActionPerformed

    private void btnMinimizeAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMinimizeAllActionPerformed
        for (Component c : desktop.getComponents()) {
            if (c instanceof StreamDesktop) {
                StreamDesktop d = (StreamDesktop) c;
                try {
                    Tools.sleep(20);
                    d.setIcon(true);
                } catch (PropertyVetoException ex) {
                    Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_btnMinimizeAllActionPerformed

    private void btnSaveStudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveStudioActionPerformed
        final java.awt.event.ActionEvent sEvt = evt;
        try {
            File file;
            boolean overWrite = true;
            ArrayList<Stream> streamzI = MasterChannels.getInstance().getStreams();
            ArrayList<String> sourceChI = MasterChannels.getInstance().getChannels();
            if (streamzI.size()>0 || sourceChI.size()>0) {
                Object[] options = {"OK"};
                JOptionPane.showOptionDialog(this,
                       "All Playing Streams will be Stopped !!!","Attention",
                       JOptionPane.PLAIN_MESSAGE,
                       JOptionPane.INFORMATION_MESSAGE,
                       null,
                       options,
                       options[0]);
            }
            JFileChooser chooser = new JFileChooser(lastFolder);
            FileNameExtensionFilter studioFilter = new FileNameExtensionFilter("Studio files (*.studio)", "studio");
            chooser.setFileFilter(studioFilter);
            chooser.setDialogTitle("Save a Studio ...");
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int retval = chooser.showSaveDialog(this);
            file = chooser.getSelectedFile();
            if (retval == JFileChooser.APPROVE_OPTION) {
                if (file!=null){
                    if(file.exists()){
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
            }
            if ((retval == JFileChooser.APPROVE_OPTION) && overWrite) {
                final WaitingDialog waitingD = new WaitingDialog(this);
                final File fileF = file;
                waitingD.setModal(true);
                SwingWorker<?,?> worker = new SwingWorker<Void,Integer>(){  
                    @Override
                    protected Void doInBackground() throws InterruptedException{
                        if (fileF!=null){
                            File fileS = fileF;
                            lastFolder = fileS.getParentFile();
                            SystemPlayer.getInstance(null).stop();
                            Tools.sleep(50);
                            MasterChannels.getInstance().stopAllStream();
                            listener.stopChTime(sEvt);
                            for (Stream s : MasterChannels.getInstance().getStreams()){
                                s.updateStatus();
                            }
                            if (!fileS.getName().endsWith(".studio")){
                                fileS = new File(fileS.getParent(),fileS.getName()+".studio");
                            }
                            try {
                                Studio.save(fileS);
                            } catch (IOException | XMLStreamException | IllegalArgumentException | IllegalAccessException | TransformerException ex) {
                                Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Studio is saved!");
                            ResourceMonitor.getInstance().addMessage(label);    
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
            } else {
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Saving Cancelled!");
                ResourceMonitor.getInstance().addMessage(label);
            }
        } catch (HeadlessException ex) {
                Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Error: " + ex.getMessage());
                ResourceMonitor.getInstance().addMessage(label);
        } 
    
    }//GEN-LAST:event_btnSaveStudioActionPerformed
    
    public static class WaitingDialog extends JDialog {
        private final JLabel workingLabel = new JLabel();
        public WaitingDialog(JFrame owner) {
            workingLabel.setBorder(BorderFactory.createLineBorder(Color.black));
            workingLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/view-fullscreen.png"))); // NOI18N        
            workingLabel.setText(" Working... ");
            this.setUndecorated(true);           
            this.add(workingLabel);
            this.pack();
            // move window to center of owner
            int x = owner.getX()
                + (owner.getWidth() - this.getPreferredSize().width) / 2;
            int y = owner.getY()
                + (owner.getHeight() - this.getPreferredSize().height) / 2;
            this.setLocation(x, y);
            this.repaint();
        }
    } 
    
    public static void durationCalc(Stream stream, File file) {
        Runtime rt = Runtime.getRuntime();
        String commandDuration = "avconv -i " + "\"" + file.getAbsolutePath() + "\"";
        File fileD=new File(System.getProperty("user.home")+"/.webcamstudio/"+"DurationCalc.sh");
        FileOutputStream fosD;
        DataOutputStream dosD = null;
        try {
            fosD = new FileOutputStream(fileD);
            dosD= new DataOutputStream(fosD);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            if (dosD != null) {
            dosD.writeBytes("#!/bin/bash\n");
            dosD.writeBytes(commandDuration+"\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
        String batchDurationComm = "sh "+System.getProperty("user.home")+"/.webcamstudio/"+"DurationCalc.sh";
        try {
            Process duration = rt.exec(batchDurationComm);
            Tools.sleep(10);
            duration.waitFor(); //Author spoonybard896
            InputStream lsOut = duration.getErrorStream();
            InputStreamReader isr = new InputStreamReader(lsOut);
            BufferedReader in = new BufferedReader(isr);
            String lineR;
            while ((lineR = in.readLine()) != null) {
                if(lineR.contains("Duration:")) {
                    lineR = lineR.replaceFirst("Duration: ", "");
                    lineR = lineR.trim();
                    String resu = lineR.substring(0, 8);
                    String[] temp;
                    temp = resu.split(":");
                    int hours = Integer.parseInt(temp[0]);
                    int minutes = Integer.parseInt(temp[1]);
                    int seconds = Integer.parseInt(temp[2]);
                    int totalTime = (hours*3600)+(minutes*60)+seconds;
                    String strDuration = Integer.toString(totalTime);
                    stream.setStreamTime(strDuration+"s");
                }
            } //Author spoonybard896
        } catch (IOException | InterruptedException | NumberFormatException e) {
        }
    }
    @SuppressWarnings("unchecked")
    private void btnLoadStudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadStudioActionPerformed
        final java.awt.event.ActionEvent fEvt = evt;
        ArrayList<Stream> streamzI = MasterChannels.getInstance().getStreams();
        ArrayList<String> sourceChI = MasterChannels.getInstance().getChannels();
        if (streamzI.size()>0 || sourceChI.size()>0) {
            Object[] options = {"OK"};
                JOptionPane.showOptionDialog(this,
                       "Current Studio will be closed !!!","Attention",
                       JOptionPane.PLAIN_MESSAGE,
                       JOptionPane.INFORMATION_MESSAGE,
                       null,
                       options,
                       options[0]);
        }
        JFileChooser chooser = new JFileChooser(lastFolder);
        FileNameExtensionFilter studioFilter = new FileNameExtensionFilter("Studio files (*.studio)", "studio");
        chooser.setFileFilter(studioFilter);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Load a Studio ...");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int retval = chooser.showOpenDialog(this);
        final File file = chooser.getSelectedFile();
        if (retval == JFileChooser.APPROVE_OPTION) { 
            final WaitingDialog waitingD = new WaitingDialog(this);
            waitingD.setModal(true);
            SwingWorker<?,?> worker = new SwingWorker<Void,Integer>(){  
                @Override
                protected Void doInBackground() throws InterruptedException{  
                    if (file != null) {
                        lastFolder = file.getParentFile();
                        SystemPlayer.getInstance(null).stop();
                        MasterChannels.getInstance().stopAllStream();
                        for (Stream s : MasterChannels.getInstance().getStreams()){              
                            s.updateStatus();
                        }
                        ArrayList<Stream> streamz = MasterChannels.getInstance().getStreams();
                        ArrayList<String> sourceCh = MasterChannels.getInstance().getChannels();
                        do {        
                            for (int l=0; l< streamz.size(); l++) {
                                Stream removeS = streamz.get(l);
                                Tools.sleep(20);
                                removeS.destroy();
                                removeS = null;
                            }
                            for (int a=0; a< sourceCh.size(); a++) {
                                String removeSc = sourceCh.get(a);
                                MasterChannels.getInstance().removeChannel(removeSc);
                                Tools.sleep(20);
                                listener.removeChannels(removeSc, a);
                            }
                        } while (streamz.size()>0 || sourceCh.size()>0);
                        SystemPlayer.getInstance(null).stop();
                        MasterChannels.getInstance().stopAllStream();
                        listener.stopChTime(fEvt);
                        listener.resetBtnStates(fEvt);
                        listenerOP.resetBtnStates(fEvt);
                        tabControls.removeAll();
                        tabControls.repaint();
                        Tools.sleep(300);
                        desktop.removeAll();
                        desktop.repaint();
                        Tools.sleep(50);
                        try {
                            Studio.LText = new ArrayList<>();
                            Studio.extstream = new ArrayList<>();
                            Studio.ImgMovMus = new ArrayList<>();                          
                            Studio.load(file);
                            Studio.main();
                            spinWidth.setValue(MasterMixer.getInstance().getWidth());
                            spinHeight.setValue(MasterMixer.getInstance().getHeight());
                            spinFPS.setValue(MasterMixer.getInstance().getRate());
                            int mW = (Integer) spinWidth.getValue();
                            int mH = (Integer) spinHeight.getValue();
                            MasterMixer.getInstance().stop();
                            MasterMixer.getInstance().setWidth(mW);
                            MasterMixer.getInstance().setHeight(mH);
                            MasterMixer.getInstance().setRate((Integer) spinFPS.getValue());
                            MasterMixer.getInstance().start();
                        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
                            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                        }
// loading studio streams
                        for (int u = 0; u < Studio.ImgMovMus.size(); u++) {
                            Stream s = Studio.extstream.get(u);
                            if (s != null) {
                                StreamDesktop frame = new StreamDesktop(s, WebcamStudio.this);
//                                Tools.sleep(40);
                                desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                            }
                                System.out.println("Adding Source: "+s.getName());
                        }
                        Studio.extstream.clear();
                        Studio.extstream = null;
                        Studio.ImgMovMus.clear();
                        Studio.ImgMovMus = null;
// loading studio texts
                        for (int t = 0; t < Studio.LText.size(); t++) {
                            SourceText text = Studio.LText.get(t);
                            StreamDesktop frame = new StreamDesktop(text, WebcamStudio.this);
//                            Tools.sleep(40);
                            desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                            System.out.println("Adding Source: "+text.getName());
                        }
                        Studio.LText.clear();
                        Studio.LText = null;
                        Tools.sleep(300);
// loading studio channels
                        for (String chsc : MasterChannels.getInstance().getChannels()) {
                            Tools.sleep(10);
                            listener.addLoadingChannel(chsc);               
                        }
                        Studio.chanLoad.clear();
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Studio is loaded!");
                        ResourceMonitor.getInstance().addMessage(label);
                    }
                return null;  
                }  
                @Override
                protected void done(){
//                    Tools.sleep(50);
                    waitingD.dispose();
                }  
            }; 
        worker.execute();
        waitingD.toFront();
        waitingD.setVisible(true);
//        waitingD.revalidate();
//        waitingD.repaint();
        } else {
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Loading Cancelled!");
            ResourceMonitor.getInstance().addMessage(label); 
        }
    }//GEN-LAST:event_btnLoadStudioActionPerformed

    private void WCSAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WCSAboutActionPerformed
        About TAbout = new About(about, true); 
        TAbout.setVisible(true);
    }//GEN-LAST:event_WCSAboutActionPerformed

    private void btnAddWebcamsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddWebcamsActionPerformed
        final String wCam = cboWebcam.getSelectedItem().toString();
        if (Tools.getOS() == OS.LINUX) {
            for (VideoDevice d : VideoDevice.getOutputDevices()) {
                if (d.getName().equals(wCam)){
                    Stream webcam = new SourceWebcam(d.getFile());
                    webcam.setName(d.getName());
                    ArrayList<String> allChan = new ArrayList<>();
                    for (String scn : MasterChannels.getInstance().getChannels()){
                        allChan.add(scn); 
                    } 
                    for (String sc : allChan){
                        webcam.addChannel(SourceChannel.getChannel(sc, webcam));
                    }
                    StreamDesktop frame = new StreamDesktop(webcam, this);
                    desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                    try {
                        frame.setSelected(true);
                    } catch (PropertyVetoException ex) {
                        Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }//GEN-LAST:event_btnAddWebcamsActionPerformed
    private void btnNewStudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewStudioActionPerformed
        boolean doNew = true;
        ArrayList<Stream> streamzI = MasterChannels.getInstance().getStreams();
        ArrayList<String> sourceChI = MasterChannels.getInstance().getChannels();
        if (streamzI.size()>0 || sourceChI.size()>0) {
            int result = JOptionPane.showConfirmDialog(this,"Current Studio will be deleted !!!","Attention",JOptionPane.YES_NO_CANCEL_OPTION);
            switch(result){
                case JOptionPane.YES_OPTION:
                    doNew = true;
                    break;
                case JOptionPane.NO_OPTION:
                    doNew = false;
                    break;
                case JOptionPane.CANCEL_OPTION:
                    doNew = false;
                    break;
                case JOptionPane.CLOSED_OPTION:
                    doNew = false;
                    break;
            }
            if (doNew) {            
                SystemPlayer.getInstance(null).stop();
                MasterChannels.getInstance().stopAllStream();
                for (Stream s : MasterChannels.getInstance().getStreams()){
                    s.updateStatus();
                }
                ArrayList<Stream> streamz = MasterChannels.getInstance().getStreams();
                ArrayList<String> sourceCh = MasterChannels.getInstance().getChannels();
                do {          
                    for (int l=0; l< streamz.size(); l++) {
                        Stream removeS = streamz.get(l);
                        removeS.destroy();
                        removeS = null;
                    }
                    for (int a=0; a< sourceCh.size(); a++) {
                        String removeSc = sourceCh.get(a);
                        MasterChannels.getInstance().removeChannel(removeSc);
                        listener.removeChannels(removeSc, a);
                    }
                } while (streamz.size()>0 || sourceCh.size()>0);
                listener.stopChTime(evt);
                listener.resetBtnStates(evt);
                listenerOP.resetBtnStates(evt);
                tabControls.removeAll();
                tabControls.repaint();
                Tools.sleep(300);
                desktop.removeAll();
                desktop.repaint();
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "New Studio Created.");
                ResourceMonitor.getInstance().addMessage(label);
            } else {
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "New Studio Action Cancelled.");
                ResourceMonitor.getInstance().addMessage(label);    
            }
            System.gc();
        }
    }//GEN-LAST:event_btnNewStudioActionPerformed

    private void btnAddDVBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddDVBActionPerformed
        SourceDVB streamDVB;
        streamDVB = new SourceDVB();
        ArrayList<String> allChan = new ArrayList<>();
        for (String scn : MasterChannels.getInstance().getChannels()){
            allChan.add(scn); 
        } 
        for (String sc : allChan){
            streamDVB.addChannel(SourceChannel.getChannel(sc, streamDVB));
        }
        StreamDesktop frame = new StreamDesktop(streamDVB, this);
        desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddDVBActionPerformed

    private void btnAddURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddURLActionPerformed
        SourceURL streamURL;
        streamURL = new SourceURL();
        ArrayList<String> allChan = new ArrayList<>();
        for (String scn : MasterChannels.getInstance().getChannels()){
            allChan.add(scn); 
        } 
        for (String sc : allChan){
            streamURL.addChannel(SourceChannel.getChannel(sc, streamURL));
        }
        StreamDesktop frame = new StreamDesktop(streamURL, this);
        desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddURLActionPerformed

    private void btnVideoDevInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVideoDevInfoActionPerformed
        VideoDeviceInfo vDevsI = new VideoDeviceInfo(vDevInfo, true); 
        vDevsI.setVisible(true);
    }//GEN-LAST:event_btnVideoDevInfoActionPerformed

    private void btnRefreshWebcamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshWebcamActionPerformed
        initWebcam();
    }//GEN-LAST:event_btnRefreshWebcamActionPerformed

    private void btnAddMonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddMonActionPerformed
        SourceSoundMonitor source = new SourceSoundMonitor();
        ArrayList<String> allChan = new ArrayList<>();
        for (String scn : MasterChannels.getInstance().getChannels()){
            allChan.add(scn); 
        } 
        for (String sc : allChan){
            source.addChannel(SourceChannel.getChannel(sc, source));
        }
        StreamDesktop frame = new StreamDesktop(source, this);
        desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddMonActionPerformed

    private void cboAudioHzActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboAudioHzActionPerformed
        final String audioHz = cboAudioHz.getSelectedItem().toString();
        if (audioHz.equals("22050Hz")) {
            audioFreq = 22050;
        } else {
            audioFreq = 44100;
        }
        MasterMixer.getInstance().stop();
        Tools.sleep(100);
        SystemPlayer.getInstance(null).stop();
        Tools.sleep(30);
        MasterChannels.getInstance().stopAllStream();
        for (Stream s : streamS){
            s.updateStatus();
        }
        MasterMixer.getInstance().start();
        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Overall Audio Output set to: "+audioFreq+"Hz");
        ResourceMonitor.getInstance().addMessage(label);
    }//GEN-LAST:event_cboAudioHzActionPerformed

   @SuppressWarnings("unchecked") 
    private void btnJoinStudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnJoinStudioActionPerformed
        JFileChooser chooser = new JFileChooser(lastFolder);
        FileNameExtensionFilter studioFilter = new FileNameExtensionFilter("Studio files (*.studio)", "studio");
        chooser.setFileFilter(studioFilter);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Import a Studio ...");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int retval = chooser.showOpenDialog(this);
        File file = chooser.getSelectedFile();
        if (retval == JFileChooser.APPROVE_OPTION) {     
        if (file != null) {
               lastFolder = file.getParentFile();
                try {
                    Studio.LText = new ArrayList<>();
                    Studio.extstream = new ArrayList<>();
                    Studio.ImgMovMus = new ArrayList<>();
                    Studio.load(file);
                    Studio.main();
                } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
                    Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                }
        
                for (int u = 0; u < Studio.ImgMovMus.size(); u++) {
                    Tools.sleep(10);
                    Stream s = Studio.extstream.get(u);
                    if (s != null) {
                    StreamDesktop frame = new StreamDesktop(s, this);
                    desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                    try {
                        frame.setSelected(true);
                    } catch (PropertyVetoException ex) {
                        Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    }
                }
                Studio.extstream.clear();
                Studio.extstream = null;
                Studio.ImgMovMus.clear();
                Studio.ImgMovMus = null;
                for (int t = 0; t < Studio.LText.size(); t++) {
                    SourceText text = Studio.LText.get(t);
                    StreamDesktop frame = new StreamDesktop(text, this);
                    desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                    try {
                        frame.setSelected(true);
                    } catch (PropertyVetoException ex) {
                        Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                Studio.LText.clear();
                Studio.LText = null;
                Tools.sleep(300);
                MasterChannels master = MasterChannels.getInstance(); //
                ArrayList<String> chNameL = new ArrayList<>();
                for (SourceChannel chsct : Studio.chanLoad) {
                        chNameL.add(chsct.getName());
                }
                LinkedHashSet<String> hs = new LinkedHashSet<>(chNameL);
                chNameL.clear();
                chNameL.addAll(hs);
                for (String chsct : chNameL) {
                        listener.addLoadingChannel(chsct);
                        master.insertStudio(chsct); // If Uncommented the attacched Studio will stop playing if other channels than his are selected.
                }
                Studio.chanLoad.clear();  
        }
        if (file!=null){
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Studio is Imported!");
            ResourceMonitor.getInstance().addMessage(label);
        } 
        } else {
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Studio Import Cancelled!");
            ResourceMonitor.getInstance().addMessage(label); 
        }
    }//GEN-LAST:event_btnJoinStudioActionPerformed
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) { 
        if (System.getProperty("jna.nosys") == null) {
            System.setProperty("jna.nosys", "true");
        }
        File dir = new File(System.getProperty("user.home"), ".webcamstudio");
        if (!dir.exists()) {
            dir.mkdir();
        }
        System.out.println("Welcome to WebcamStudio ...");
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(WebcamStudio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {            
                new WebcamStudio().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton WCSAbout;
    private javax.swing.JButton btnAddAnimation;
    private javax.swing.JButton btnAddDVB;
    private javax.swing.JButton btnAddDesktop;
    private javax.swing.JButton btnAddFile;
    private javax.swing.JButton btnAddMic;
    private javax.swing.JButton btnAddMon;
    private javax.swing.JButton btnAddQRCode;
    private javax.swing.JButton btnAddText;
    private javax.swing.JButton btnAddURL;
    private javax.swing.JButton btnAddWebcams;
    private javax.swing.JButton btnJoinStudio;
    private final javax.swing.JButton btnLoadStudio = new javax.swing.JButton();
    private javax.swing.JButton btnMinimizeAll;
    private javax.swing.JButton btnNewStudio;
    private javax.swing.JButton btnRefreshWebcam;
    private javax.swing.JButton btnSaveStudio;
    private javax.swing.JButton btnVideoDevInfo;
    public static javax.swing.JComboBox cboAnimations;
    private javax.swing.JComboBox cboAudioHz;
    private javax.swing.JComboBox cboWebcam;
    private javax.swing.JDesktopPane desktop;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JLabel lblSourceSelected;
    private javax.swing.JSplitPane mainSplit;
    private javax.swing.JToolBar mainToolbar;
    private javax.swing.JPanel panControls;
    private javax.swing.JPanel panSources;
    public static javax.swing.JTabbedPane tabControls;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
      
    @Override
    public void selectedSource(Stream source) {
        lblSourceSelected.setText(source.getName());     
        lblSourceSelected.setToolTipText(source.getName());      
        tabControls.removeAll();
        tabControls.repaint();
        ArrayList<Component> comps = SourceControls.getControls(source);        
        for (Component c : comps) {
            String cName = c.getName();
            tabControls.add(cName, c);
        }
    } 
}
