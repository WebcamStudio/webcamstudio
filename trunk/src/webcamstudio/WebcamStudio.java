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
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;
import webcamstudio.channels.MasterChannels;
import webcamstudio.components.MasterPanel;
import static webcamstudio.components.MasterPanel.spinFPS;
import static webcamstudio.components.MasterPanel.spinHeight;
import static webcamstudio.components.MasterPanel.spinWidth;
import webcamstudio.components.OutputPanel;
import static webcamstudio.components.OutputPanel.execPACTL;
import webcamstudio.components.ResourceMonitor;
import webcamstudio.components.ResourceMonitorLabel;
import webcamstudio.components.SourceControls;
import webcamstudio.components.StreamDesktop;
import webcamstudio.components.VideoDeviceInfo;
import webcamstudio.exporter.vloopback.VideoDevice;
import webcamstudio.externals.ProcessRenderer;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.mixers.PrePlayer;
import webcamstudio.mixers.PreviewMixer;
import webcamstudio.mixers.SystemPlayer;
import webcamstudio.streams.SourceAudioSource;
import webcamstudio.streams.SourceChannel;
import webcamstudio.streams.SourceCustom;
import webcamstudio.streams.SourceDV;
import webcamstudio.streams.SourceDVB;
import webcamstudio.streams.SourceDesktop;
import webcamstudio.streams.SourceIPCam;
import webcamstudio.streams.SourceImage;
import webcamstudio.streams.SourceImageGif;
import webcamstudio.streams.SourceImageU;
import webcamstudio.streams.SourceMovie;
import webcamstudio.streams.SourceMusic;
import webcamstudio.streams.SourceText;
import webcamstudio.streams.SourceURL;
import webcamstudio.streams.SourceWebcam;
import webcamstudio.streams.Stream;
import webcamstudio.studio.Studio;
import webcamstudio.util.Screen;
import webcamstudio.util.Tools;
import webcamstudio.util.Tools.OS;

/**
 *
 * @author patrick (modified by karl)
 */
public class WebcamStudio extends JFrame implements StreamDesktop.Listener {

    public static Preferences prefs = null;
    public static Properties animations = new Properties();
    public static Properties facesW = new Properties();
    // FF = 0 ; AV = 1 ; GS = 2
    public static int outFMEbe = 1;
    private final static String userHomeDir = Tools.getUserHome();
    OutputPanel recorder = new OutputPanel(this);
    Frame about = new Frame();
    Frame vDevInfo = new Frame();
    Stream stream = null;
    private static File cmdFile = null;
    private static String cmdOut = null;
    private static boolean cmdAutoStart = false;
    private static boolean cmdRemote = false;
    public static int audioFreq = 22050;
    public static String theme = "Classic";
    ArrayList<Stream> streamS = MasterChannels.getInstance().getStreams();
    private File lastFolder = null;
    boolean ffmpeg = Screen.ffmpegDetected();
    boolean avconv = Screen.avconvDetected();
    boolean firstRun = true;
    static boolean autoAR = false;
    
    @SuppressWarnings("unchecked") 
    private void initFaceDetection() throws IOException {
        File dir = new File(System.getProperty("user.home"), ".webcamstudio/faces");
        if (!dir.exists()) {
            dir.mkdir();
        }
        facesW.load(getClass().getResourceAsStream("/webcamstudio/resources/faces/Faces.properties"));
        ArrayList faceNames = new ArrayList();
        String faceL = null;
        for (Object o : facesW.keySet()) {
            faceNames.add(o); 
        }
        for (int i=0 ; i < faceNames.size(); i++){ 
            faceL = faceNames.get(i).toString();
//            System.out.println(faceL);
            File destination = new File(System.getProperty("user.home")+"/.webcamstudio/faces/"+faceL+".png");
            InputStream is = getClass().getResourceAsStream("/webcamstudio/resources/faces/"+faceL+".png");
            OutputStream os = new FileOutputStream(destination);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.close();
            is.close();
        }        
        faceNames.clear();
        File destination = new File(System.getProperty("user.home")+"/.webcamstudio/faces/haarcascade_frontalface_alt2.xml");
        InputStream is = getClass().getResourceAsStream("/webcamstudio/resources/haarcascade_frontalface_alt2.xml");
        OutputStream os = new FileOutputStream(destination);
        byte[] buffer = new byte[4096];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
        os.close();
        is.close();
        destination = new File(System.getProperty("user.home")+"/.webcamstudio/faces/lbpcascade_frontalface.xml");
        is = getClass().getResourceAsStream("/webcamstudio/resources/lbpcascade_frontalface.xml");
        os = new FileOutputStream(destination);
        buffer = new byte[4096];
        int length2;
        while ((length2 = is.read(buffer)) > 0) {
            os.write(buffer, 0, length2);
        }
        os.close();
        is.close();
    }

    @Override
    public void closeSource() {
        lblSourceSelected.setText("");
    }
    
    public interface Listener {
        public void stopChTime(java.awt.event.ActionEvent evt);
        public void resetBtnStates(java.awt.event.ActionEvent evt);
        public void resetAutoPLBtnState(java.awt.event.ActionEvent evt);
        public void resetSinks(java.awt.event.ActionEvent evt);
        public void addLoadingChannel(String name);
        public void removeChannels(String removeSc, int a);
        public void setRemoteOn();
    }
    
    static Listener listenerCP = null;
    
    public static void setListenerCP(Listener l) {
        listenerCP = l;
    }
    
    static Listener listenerOP = null;
    
    public static void setListenerOP(Listener l) {
        listenerOP = l;
    }

    /**
     * Creates new form WebcamStudio
     * @throws java.io.IOException
     */
    
    public WebcamStudio() throws IOException {
        
        initComponents();
        
        if (theme.equals("Dark")) {
            // setting WS Dark Theme
            UIManager.put("text", Color.WHITE);
            UIManager.put("control", Color.darkGray);
            UIManager.put("nimbusBlueGrey", Color.darkGray);
            UIManager.put("nimbusBase", Color.darkGray);
            UIManager.put("nimbusLightBackground", new Color(134,137,143));
            UIManager.put("info", new Color(195,160,0));
            UIManager.put("nimbusDisabledText", Color.black);
            UIManager.put("nimbusSelectionBackground", Color.yellow);
            UIManager.put("nimbusSelectedText", Color.blue);
            UIManager.put("nimbusSelectionBackground", new Color(255,220,35));
        }
        
        setTitle("WebcamStudio " + Version.version);
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
                                    if (stream instanceof SourceMovie || stream instanceof SourceMusic || stream instanceof SourceImage || stream instanceof SourceImageU || stream instanceof SourceImageGif) {
                                        getVideoParams(stream, file, null);
                                    }
                                    ArrayList<String> allChan = new ArrayList<>();
                                    for (String scn : MasterChannels.getInstance().getChannels()){
                                        allChan.add(scn); 
                                    } 
                                    for (String sc : allChan){
                                        stream.addChannel(SourceChannel.getChannel(sc, stream));
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
        
        if (theme.equals("Dark")) {
            // setting WS Dark Theme
            UIManager.put("text", Color.WHITE);
            UIManager.put("control", Color.darkGray);
            UIManager.put("nimbusBlueGrey", Color.darkGray);
            UIManager.put("nimbusBase", Color.darkGray);
            UIManager.put("nimbusLightBackground", new Color(134,137,143));
            UIManager.put("info", new Color(195,160,0));
            UIManager.put("nimbusDisabledText", Color.black);
            UIManager.put("nimbusSelectionBackground", Color.yellow);
            UIManager.put("nimbusSelectedText", Color.blue);
            UIManager.put("nimbusSelectionBackground", new Color(255,220,35));
        }
        
//        if (theme.equals("Green")) {
//            // setting WS Dark Theme
//            UIManager.put("text", Color.WHITE);
//            UIManager.put("control", new Color(0,120,1));
//            UIManager.put("nimbusBlueGrey", new Color(0,120,30));
//            UIManager.put("nimbusBase", new Color(10,110,10));
//            UIManager.put("nimbusLightBackground", new Color(0,150,1));
//            UIManager.put("info", new Color(195,160,0));
//            UIManager.put("nimbusDisabledText", Color.black);
//            UIManager.put("nimbusSelectionBackground", Color.yellow);
//            UIManager.put("nimbusSelectedText", Color.blue);
//            UIManager.put("nimbusSelectionBackground", new Color(255,220,35));
//        }
        
        MasterMixer.getInstance().start();
        PreviewMixer.getInstance().start();
        this.add(new MasterPanel(), BorderLayout.WEST);
        initAnimations();
        initFaceDetection();
        initWebcam();
        initAudioMainSW();
        initThemeMainSW();
        initMainOutBE();
        tglAutoAR.setSelected(autoAR);
        listenerOP.resetSinks(null);
        loadCustomSources();
        if (cmdFile != null){
            loadAtStart(cmdFile,null);
            btnMinimizeAllActionPerformed(null);
        }
        if (cmdOut != null) {
            listenerOP.addLoadingChannel(cmdOut); // used addLoadingChannel to activate Output from command line.
        }
        if (cmdAutoStart) {
            listenerCP.resetSinks(null); // used resetSinks to AutoPlay from command line.
        }
        if (cmdRemote) {
            listenerCP.setRemoteOn();
        }
        firstRun = false;
    }

    private StreamDesktop getNewStreamDesktop(Stream s) {
        return new StreamDesktop(s, this);
    }
    
    private void loadCustomSources() {
        File userSettings = new File(userHomeDir + "/.webcamstudio");
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
    private void initAudioMainSW() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("22050Hz");
        model.addElement("44100Hz");
        cboAudioHz.setModel(model);
        if (audioFreq == 22050) {
            cboAudioHz.setSelectedItem("22050Hz");
        } else {
            cboAudioHz.setSelectedItem("44100Hz");
        }
    }
    
    @SuppressWarnings("unchecked")
    private void initThemeMainSW() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("Classic");
        model.addElement("Dark");
        cboTheme.setModel(model);
        if (theme.equals("Classic")) {
            cboTheme.setSelectedItem("Classic");
        } else {
            cboTheme.setSelectedItem("Dark");
        }
    }
    
    private void initMainOutBE() {
        // FF = 0 ; AV = 1 ; GS = 2
        if (ffmpeg && !avconv){
            if (outFMEbe == 0 || outFMEbe == 1) {
                outFMEbe = 0;
                tglFFmpeg.setSelected(true);
                tglAVconv.setEnabled(false);
                tglGst.setEnabled(true);
            } else if (outFMEbe == 2) {
                tglFFmpeg.setSelected(false);
                tglFFmpeg.setEnabled(true);
                tglAVconv.setEnabled(false);
                tglGst.setSelected(true);
            }
        } else if (ffmpeg && avconv) {
            switch (outFMEbe) {
                case 0:
                    tglFFmpeg.setSelected(true);
                    tglAVconv.setEnabled(true);
                    tglGst.setEnabled(true);
                    break;
                case 1:
                    tglFFmpeg.setEnabled(true);
                    tglAVconv.setSelected(true);
                    tglGst.setEnabled(true);
                    break;
                case 2:
                    tglFFmpeg.setEnabled(true);
                    tglAVconv.setEnabled(true);
                    tglGst.setSelected(true);
                    break;
            }
        } else if (!ffmpeg && avconv){
            if (outFMEbe == 1 || outFMEbe == 0) {
                outFMEbe = 1;
                tglAVconv.setSelected(true);
                tglFFmpeg.setEnabled(false);
                tglGst.setEnabled(true);
            } else if (outFMEbe == 2) {
                tglFFmpeg.setEnabled(false);
                tglAVconv.setEnabled(true);
                tglGst.setSelected(true);
            }
        }
//        System.out.println("OutFMEbe: "+outFMEbe);
    }

    private void loadPrefs() {
        int x = prefs.getInt("main-x", 100);
        int y = prefs.getInt("main-y", 100);
        int w = prefs.getInt("main-w", 800);
        int h = prefs.getInt("main-h", 400);
        MasterMixer.getInstance().setWidth(prefs.getInt("mastermixer-w", MasterMixer.getInstance().getWidth()));
        MasterMixer.getInstance().setHeight(prefs.getInt("mastermixer-h", MasterMixer.getInstance().getHeight()));
        MasterMixer.getInstance().setRate(prefs.getInt("mastermixer-r", MasterMixer.getInstance().getRate()));
        PreviewMixer.getInstance().setWidth(prefs.getInt("mastermixer-w", MasterMixer.getInstance().getWidth()));
        PreviewMixer.getInstance().setHeight(prefs.getInt("mastermixer-h", MasterMixer.getInstance().getHeight()));
//        PreviewMixer.getInstance().setRate(prefs.getInt("mastermixer-r", MasterMixer.getInstance().getRate()));
        PreviewMixer.getInstance().setRate(5);
        mainSplit.setDividerLocation(prefs.getInt("split-x", mainSplit.getDividerLocation()));
        mainSplit.setDividerLocation(prefs.getInt("split-last-x", mainSplit.getLastDividerLocation()));
        lastFolder = new File(prefs.get("lastfolder", "."));
        audioFreq = prefs.getInt("audio-freq", audioFreq);
        theme = prefs.get("theme", theme);
        outFMEbe = prefs.getInt("out-FME", outFMEbe);
        autoAR = prefs.getBoolean("autoar", autoAR);
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
        prefs.putInt("audio-freq", audioFreq);
        prefs.put("theme", theme);
//        System.out.println("Theme:"+theme);
        prefs.putInt("out-FME", outFMEbe);
        prefs.putBoolean("autoar", autoAR);
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
        btnAddFolder = new javax.swing.JButton();
        tglAutoAR = new javax.swing.JToggleButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        btnAddDVB = new javax.swing.JButton();
        btnAddURL = new javax.swing.JButton();
        btnAddIPCam = new javax.swing.JButton();
        btnAddDVCam = new javax.swing.JButton();
        btnAddDesktop = new javax.swing.JButton();
        btnAddText = new javax.swing.JButton();
        btnAddAudioSrc = new javax.swing.JButton();
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
        btnNewStudio = new javax.swing.JButton();
        btnImportStudio = new javax.swing.JButton();
        btnSaveStudio = new javax.swing.JButton();
        WCSAbout = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        cboWebcam = new javax.swing.JComboBox();
        btnAddWebcams = new javax.swing.JButton();
        btnRefreshWebcam = new javax.swing.JButton();
        btnVideoDevInfo = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jLabel2 = new javax.swing.JLabel();
        cboAudioHz = new javax.swing.JComboBox();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        lblFFmpeg3 = new javax.swing.JLabel();
        tglFFmpeg = new javax.swing.JToggleButton();
        lblFFmpeg = new javax.swing.JLabel();
        tglAVconv = new javax.swing.JToggleButton();
        lblAVconv = new javax.swing.JLabel();
        tglGst = new javax.swing.JToggleButton();
        lblGst = new javax.swing.JLabel();
        jSeparator10 = new javax.swing.JToolBar.Separator();
        jLabel3 = new javax.swing.JLabel();
        cboTheme = new javax.swing.JComboBox();
        jSeparator11 = new javax.swing.JToolBar.Separator();
        btnSysGC = new javax.swing.JButton();
        lblClrRam = new javax.swing.JLabel();
        jSeparator12 = new javax.swing.JToolBar.Separator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("WebcamStudio");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        mainSplit.setDividerLocation(500);
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
        btnAddFile.setMaximumSize(new java.awt.Dimension(29, 28));
        btnAddFile.setMinimumSize(new java.awt.Dimension(25, 25));
        btnAddFile.setName("btnAddFile"); // NOI18N
        btnAddFile.setPreferredSize(new java.awt.Dimension(28, 28));
        btnAddFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFileActionPerformed(evt);
            }
        });
        toolbar.add(btnAddFile);

        btnAddFolder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-add-folder.png"))); // NOI18N
        btnAddFolder.setToolTipText("Load Media Folder");
        btnAddFolder.setFocusable(false);
        btnAddFolder.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddFolder.setMaximumSize(new java.awt.Dimension(29, 28));
        btnAddFolder.setMinimumSize(new java.awt.Dimension(25, 25));
        btnAddFolder.setName("btnAddFolder"); // NOI18N
        btnAddFolder.setPreferredSize(new java.awt.Dimension(28, 28));
        btnAddFolder.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFolderActionPerformed(evt);
            }
        });
        toolbar.add(btnAddFolder);

        tglAutoAR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/ar_button.png"))); // NOI18N
        tglAutoAR.setToolTipText("Automatic A/R detection Switch.");
        tglAutoAR.setFocusable(false);
        tglAutoAR.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglAutoAR.setMaximumSize(new java.awt.Dimension(29, 28));
        tglAutoAR.setMinimumSize(new java.awt.Dimension(25, 25));
        tglAutoAR.setName("tglAutoAR"); // NOI18N
        tglAutoAR.setPreferredSize(new java.awt.Dimension(28, 29));
        tglAutoAR.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/ar_button.png"))); // NOI18N
        tglAutoAR.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/ar_button_selected.png"))); // NOI18N
        tglAutoAR.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tglAutoAR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglAutoARActionPerformed(evt);
            }
        });
        toolbar.add(tglAutoAR);

        jSeparator3.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jSeparator3.setName("jSeparator3"); // NOI18N
        jSeparator3.setOpaque(true);
        toolbar.add(jSeparator3);

        btnAddDVB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/dvb.png"))); // NOI18N
        btnAddDVB.setToolTipText("Add DVB-T Stream");
        btnAddDVB.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnAddDVB.setFocusable(false);
        btnAddDVB.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddDVB.setMaximumSize(new java.awt.Dimension(29, 28));
        btnAddDVB.setMinimumSize(new java.awt.Dimension(25, 25));
        btnAddDVB.setName("btnAddDVB"); // NOI18N
        btnAddDVB.setPreferredSize(new java.awt.Dimension(28, 28));
        btnAddDVB.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddDVB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDVBActionPerformed(evt);
            }
        });
        toolbar.add(btnAddDVB);

        btnAddURL.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/url5.png"))); // NOI18N
        btnAddURL.setToolTipText("Add URL Stream");
        btnAddURL.setFocusable(false);
        btnAddURL.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddURL.setMaximumSize(new java.awt.Dimension(29, 28));
        btnAddURL.setMinimumSize(new java.awt.Dimension(25, 25));
        btnAddURL.setName("btnAddURL"); // NOI18N
        btnAddURL.setPreferredSize(new java.awt.Dimension(28, 28));
        btnAddURL.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddURLActionPerformed(evt);
            }
        });
        toolbar.add(btnAddURL);

        btnAddIPCam.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/ip-cam-2.png"))); // NOI18N
        btnAddIPCam.setToolTipText("Add IPCam Stream");
        btnAddIPCam.setFocusable(false);
        btnAddIPCam.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddIPCam.setMaximumSize(new java.awt.Dimension(29, 28));
        btnAddIPCam.setMinimumSize(new java.awt.Dimension(25, 25));
        btnAddIPCam.setName("btnAddIPCam"); // NOI18N
        btnAddIPCam.setPreferredSize(new java.awt.Dimension(28, 28));
        btnAddIPCam.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddIPCam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddIPCamActionPerformed(evt);
            }
        });
        toolbar.add(btnAddIPCam);

        btnAddDVCam.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/Firewire.png"))); // NOI18N
        btnAddDVCam.setToolTipText("Add DVCam Stream");
        btnAddDVCam.setFocusable(false);
        btnAddDVCam.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddDVCam.setMaximumSize(new java.awt.Dimension(29, 28));
        btnAddDVCam.setMinimumSize(new java.awt.Dimension(25, 25));
        btnAddDVCam.setName("btnAddDVCam"); // NOI18N
        btnAddDVCam.setPreferredSize(new java.awt.Dimension(28, 28));
        btnAddDVCam.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddDVCam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDVCamActionPerformed(evt);
            }
        });
        toolbar.add(btnAddDVCam);

        btnAddDesktop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/user-desktop.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        btnAddDesktop.setToolTipText(bundle.getString("DESKTOP")); // NOI18N
        btnAddDesktop.setFocusable(false);
        btnAddDesktop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddDesktop.setMaximumSize(new java.awt.Dimension(29, 28));
        btnAddDesktop.setMinimumSize(new java.awt.Dimension(25, 25));
        btnAddDesktop.setName("btnAddDesktop"); // NOI18N
        btnAddDesktop.setPreferredSize(new java.awt.Dimension(28, 28));
        btnAddDesktop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddDesktop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDesktopActionPerformed(evt);
            }
        });
        toolbar.add(btnAddDesktop);

        btnAddText.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/accessories-text-editor.png"))); // NOI18N
        btnAddText.setToolTipText("Text/QRCode");
        btnAddText.setFocusable(false);
        btnAddText.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddText.setMaximumSize(new java.awt.Dimension(29, 28));
        btnAddText.setMinimumSize(new java.awt.Dimension(25, 25));
        btnAddText.setName("btnAddText"); // NOI18N
        btnAddText.setPreferredSize(new java.awt.Dimension(28, 28));
        btnAddText.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTextActionPerformed(evt);
            }
        });
        toolbar.add(btnAddText);

        btnAddAudioSrc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/audio-volume-high.png"))); // NOI18N
        btnAddAudioSrc.setToolTipText("AudioSource");
        btnAddAudioSrc.setFocusable(false);
        btnAddAudioSrc.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddAudioSrc.setMaximumSize(new java.awt.Dimension(29, 28));
        btnAddAudioSrc.setMinimumSize(new java.awt.Dimension(25, 25));
        btnAddAudioSrc.setName("btnAddAudioSrc"); // NOI18N
        btnAddAudioSrc.setPreferredSize(new java.awt.Dimension(28, 28));
        btnAddAudioSrc.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddAudioSrc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddAudioSrcActionPerformed(evt);
            }
        });
        toolbar.add(btnAddAudioSrc);

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
        btnAddAnimation.setMaximumSize(new java.awt.Dimension(29, 28));
        btnAddAnimation.setMinimumSize(new java.awt.Dimension(25, 25));
        btnAddAnimation.setName("btnAddAnimation"); // NOI18N
        btnAddAnimation.setPreferredSize(new java.awt.Dimension(28, 28));
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
        btnMinimizeAll.setMaximumSize(new java.awt.Dimension(29, 28));
        btnMinimizeAll.setMinimumSize(new java.awt.Dimension(25, 25));
        btnMinimizeAll.setName("btnMinimizeAll"); // NOI18N
        btnMinimizeAll.setPreferredSize(new java.awt.Dimension(28, 28));
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
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );
        panSourcesLayout.setVerticalGroup(
            panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panSourcesLayout.createSequentialGroup()
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(desktop, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainSplit.setLeftComponent(panSources);

        panControls.setName("panControls"); // NOI18N
        panControls.setPreferredSize(new java.awt.Dimension(200, 455));
        panControls.setLayout(new java.awt.BorderLayout());

        tabControls.setBorder(javax.swing.BorderFactory.createTitledBorder("Source Properties"));
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

        btnNewStudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/document-new.png"))); // NOI18N
        btnNewStudio.setToolTipText("New Studio");
        btnNewStudio.setFocusable(false);
        btnNewStudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNewStudio.setMaximumSize(new java.awt.Dimension(29, 28));
        btnNewStudio.setMinimumSize(new java.awt.Dimension(25, 25));
        btnNewStudio.setName("btnNewStudio"); // NOI18N
        btnNewStudio.setPreferredSize(new java.awt.Dimension(28, 28));
        btnNewStudio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNewStudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewStudioActionPerformed(evt);
            }
        });
        mainToolbar.add(btnNewStudio);

        btnLoadStudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/document-open.png"))); // NOI18N
        btnLoadStudio.setToolTipText(bundle.getString("LOAD")); // NOI18N
        btnLoadStudio.setFocusable(false);
        btnLoadStudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLoadStudio.setMaximumSize(new java.awt.Dimension(29, 28));
        btnLoadStudio.setMinimumSize(new java.awt.Dimension(25, 25));
        btnLoadStudio.setName("btnLoadStudio"); // NOI18N
        btnLoadStudio.setPreferredSize(new java.awt.Dimension(28, 28));
        btnLoadStudio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnLoadStudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadStudioActionPerformed(evt);
            }
        });
        mainToolbar.add(btnLoadStudio);

        btnImportStudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/chan-add.png"))); // NOI18N
        btnImportStudio.setToolTipText("Import Studio");
        btnImportStudio.setFocusable(false);
        btnImportStudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnImportStudio.setMaximumSize(new java.awt.Dimension(29, 28));
        btnImportStudio.setMinimumSize(new java.awt.Dimension(25, 25));
        btnImportStudio.setName("btnImportStudio"); // NOI18N
        btnImportStudio.setPreferredSize(new java.awt.Dimension(28, 28));
        btnImportStudio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnImportStudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportStudioActionPerformed(evt);
            }
        });
        mainToolbar.add(btnImportStudio);

        btnSaveStudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/document-save.png"))); // NOI18N
        btnSaveStudio.setToolTipText(bundle.getString("SAVE")); // NOI18N
        btnSaveStudio.setFocusable(false);
        btnSaveStudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSaveStudio.setMaximumSize(new java.awt.Dimension(29, 28));
        btnSaveStudio.setMinimumSize(new java.awt.Dimension(25, 25));
        btnSaveStudio.setName("btnSaveStudio"); // NOI18N
        btnSaveStudio.setPreferredSize(new java.awt.Dimension(28, 28));
        btnSaveStudio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSaveStudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveStudioActionPerformed(evt);
            }
        });
        mainToolbar.add(btnSaveStudio);

        WCSAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/user-info.png"))); // NOI18N
        WCSAbout.setToolTipText("About");
        WCSAbout.setFocusable(false);
        WCSAbout.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        WCSAbout.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        WCSAbout.setMaximumSize(new java.awt.Dimension(29, 28));
        WCSAbout.setMinimumSize(new java.awt.Dimension(25, 25));
        WCSAbout.setName("WCSAbout"); // NOI18N
        WCSAbout.setPreferredSize(new java.awt.Dimension(28, 28));
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
        btnAddWebcams.setMaximumSize(new java.awt.Dimension(29, 28));
        btnAddWebcams.setMinimumSize(new java.awt.Dimension(25, 25));
        btnAddWebcams.setName("btnAddWebcams"); // NOI18N
        btnAddWebcams.setPreferredSize(new java.awt.Dimension(28, 28));
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
        btnRefreshWebcam.setMaximumSize(new java.awt.Dimension(29, 28));
        btnRefreshWebcam.setMinimumSize(new java.awt.Dimension(25, 25));
        btnRefreshWebcam.setName("btnRefreshWebcam"); // NOI18N
        btnRefreshWebcam.setPreferredSize(new java.awt.Dimension(28, 28));
        btnRefreshWebcam.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRefreshWebcam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshWebcamActionPerformed(evt);
            }
        });
        mainToolbar.add(btnRefreshWebcam);

        btnVideoDevInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/camera-info.png"))); // NOI18N
        btnVideoDevInfo.setToolTipText(bundle.getString("VIDEO_DEVICE_INFO")); // NOI18N
        btnVideoDevInfo.setFocusable(false);
        btnVideoDevInfo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnVideoDevInfo.setMaximumSize(new java.awt.Dimension(29, 28));
        btnVideoDevInfo.setMinimumSize(new java.awt.Dimension(25, 25));
        btnVideoDevInfo.setName("btnVideoDevInfo"); // NOI18N
        btnVideoDevInfo.setPreferredSize(new java.awt.Dimension(28, 28));
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

        jSeparator7.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSeparator7.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jSeparator7.setName("jSeparator7"); // NOI18N
        jSeparator7.setOpaque(true);
        mainToolbar.add(jSeparator7);

        lblFFmpeg3.setBackground(new java.awt.Color(102, 102, 102));
        lblFFmpeg3.setFont(new java.awt.Font("Ubuntu Condensed", 1, 14)); // NOI18N
        lblFFmpeg3.setText("OUT BackEnd: ");
        lblFFmpeg3.setToolTipText("Select Available Back-Ends");
        lblFFmpeg3.setName("lblFFmpeg3"); // NOI18N
        mainToolbar.add(lblFFmpeg3);

        tglFFmpeg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/FFmpeg.png"))); // NOI18N
        tglFFmpeg.setToolTipText("Use FFmpeg Output Backend.");
        tglFFmpeg.setFocusable(false);
        tglFFmpeg.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglFFmpeg.setMaximumSize(new java.awt.Dimension(29, 28));
        tglFFmpeg.setMinimumSize(new java.awt.Dimension(25, 25));
        tglFFmpeg.setName("tglFFmpeg"); // NOI18N
        tglFFmpeg.setPreferredSize(new java.awt.Dimension(28, 29));
        tglFFmpeg.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/FFmpeg.png"))); // NOI18N
        tglFFmpeg.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/FFmpegSelected.png"))); // NOI18N
        tglFFmpeg.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tglFFmpeg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglFFmpegActionPerformed(evt);
            }
        });
        mainToolbar.add(tglFFmpeg);

        lblFFmpeg.setFont(new java.awt.Font("Ubuntu Condensed", 0, 12)); // NOI18N
        lblFFmpeg.setText("FFmpeg  ");
        lblFFmpeg.setName("lblFFmpeg"); // NOI18N
        mainToolbar.add(lblFFmpeg);

        tglAVconv.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/FFmpeg.png"))); // NOI18N
        tglAVconv.setToolTipText("Use Libav Output Backend.");
        tglAVconv.setFocusable(false);
        tglAVconv.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglAVconv.setMaximumSize(new java.awt.Dimension(29, 28));
        tglAVconv.setMinimumSize(new java.awt.Dimension(25, 25));
        tglAVconv.setName("tglAVconv"); // NOI18N
        tglAVconv.setPreferredSize(new java.awt.Dimension(28, 29));
        tglAVconv.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/FFmpeg.png"))); // NOI18N
        tglAVconv.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/FFmpegSelected.png"))); // NOI18N
        tglAVconv.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tglAVconv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglAVconvActionPerformed(evt);
            }
        });
        mainToolbar.add(tglAVconv);

        lblAVconv.setFont(new java.awt.Font("Ubuntu Condensed", 0, 12)); // NOI18N
        lblAVconv.setText("Libav ");
        lblAVconv.setName("lblAVconv"); // NOI18N
        mainToolbar.add(lblAVconv);

        tglGst.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/gstreamer.png"))); // NOI18N
        tglGst.setToolTipText("Use GStreamer Output Backend.");
        tglGst.setFocusable(false);
        tglGst.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglGst.setMaximumSize(new java.awt.Dimension(29, 28));
        tglGst.setMinimumSize(new java.awt.Dimension(25, 25));
        tglGst.setName("tglGst"); // NOI18N
        tglGst.setPreferredSize(new java.awt.Dimension(28, 29));
        tglGst.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/gstreamer.png"))); // NOI18N
        tglGst.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/gstreamerSelected.png"))); // NOI18N
        tglGst.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tglGst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglGstActionPerformed(evt);
            }
        });
        mainToolbar.add(tglGst);

        lblGst.setFont(new java.awt.Font("Ubuntu Condensed", 0, 12)); // NOI18N
        lblGst.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblGst.setText("GStreamer");
        lblGst.setName("lblGst"); // NOI18N
        mainToolbar.add(lblGst);

        jSeparator10.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSeparator10.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jSeparator10.setName("jSeparator10"); // NOI18N
        jSeparator10.setOpaque(true);
        mainToolbar.add(jSeparator10);

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/image-x-generic.png"))); // NOI18N
        jLabel3.setToolTipText("Master Theme Selector");
        jLabel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 1));
        jLabel3.setName("jLabel3"); // NOI18N
        mainToolbar.add(jLabel3);

        cboTheme.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboTheme.setToolTipText("Choose Default Theme.");
        cboTheme.setName("cboTheme"); // NOI18N
        cboTheme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboThemeActionPerformed(evt);
            }
        });
        mainToolbar.add(cboTheme);

        jSeparator11.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSeparator11.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jSeparator11.setName("jSeparator11"); // NOI18N
        jSeparator11.setOpaque(true);
        mainToolbar.add(jSeparator11);

        btnSysGC.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/button-small-clear.png"))); // NOI18N
        btnSysGC.setToolTipText("Try to Clean Up some memory");
        btnSysGC.setFocusable(false);
        btnSysGC.setName("btnSysGC"); // NOI18N
        btnSysGC.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSysGC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSysGCActionPerformed(evt);
            }
        });
        mainToolbar.add(btnSysGC);

        lblClrRam.setFont(new java.awt.Font("Ubuntu Condensed", 0, 12)); // NOI18N
        lblClrRam.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblClrRam.setText("RAM");
        lblClrRam.setToolTipText("Try to Clean Up some memory");
        lblClrRam.setName("lblClrRam"); // NOI18N
        mainToolbar.add(lblClrRam);

        jSeparator12.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSeparator12.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jSeparator12.setName("jSeparator12"); // NOI18N
        jSeparator12.setOpaque(true);
        mainToolbar.add(jSeparator12);

        getContentPane().add(mainToolbar, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        boolean close = true;
        ArrayList<Stream> streamzI = MasterChannels.getInstance().getStreams();
        ArrayList<String> sourceChI = MasterChannels.getInstance().getChannels();
        if (streamzI.size()>0 || sourceChI.size()>0) {
            int result = JOptionPane.showConfirmDialog(this,"Really Close WebcamStudio ?","Save Studio Remainder",JOptionPane.YES_NO_CANCEL_OPTION);
            switch(result){
                case JOptionPane.YES_OPTION:
                    close = true;
                    break;
                case JOptionPane.NO_OPTION:
                    close = false;
                    break;
                case JOptionPane.CANCEL_OPTION:
                    close = false;
                    break;
                case JOptionPane.CLOSED_OPTION:
                    close = false;
                    break;
            }
            if (close) {
                savePrefs();
                SystemPlayer.getInstance(null).stop();
                Tools.sleep(10);
                PrePlayer.getPreInstance(null).stop();
                Tools.sleep(10);
                MasterChannels.getInstance().stopAllStream();
                Tools.sleep(10);
                MasterMixer.getInstance().stop();
                PreviewMixer.getInstance().stop();
                try {
                    execPACTL("pactl unload-module module-null-sink");
                } catch (IOException ex) {
                    Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                Tools.sleep(100);   
                listenerOP.resetBtnStates(null);
                listenerOP.resetSinks(null);
                tabControls.removeAll();
                tabControls.repaint();
                Tools.sleep(300);
                desktop.removeAll();
                Tools.sleep(10);
                System.out.println("Cleaning up ...");
                File directory = new File(userHomeDir+"/.webcamstudio");
                for(File f: directory.listFiles()) {
                    if(f.getName().startsWith("WSU") || f.getName().startsWith("WSC")) {
                        f.delete();
                    }
                }
                System.out.println("Thanks for using WebcamStudio ...");
                System.out.println("GoodBye!");
                System.exit(0);
            } else {
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "WebcamStudio Quit Action Cancelled.");
                ResourceMonitor.getInstance().addMessage(label);
            }
        } else {
            savePrefs();
            SystemPlayer.getInstance(null).stop();
            Tools.sleep(10);
            PrePlayer.getPreInstance(null).stop();
            Tools.sleep(10);
            MasterChannels.getInstance().stopAllStream();
            Tools.sleep(10);
            MasterMixer.getInstance().stop();
            PreviewMixer.getInstance().stop();
            System.out.println("Thanks for using WebcamStudio ...");
            System.out.println("GoodBye!");
            System.exit(0);
        }
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
        streamTXT = new SourceText("ws");
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
                    if (s instanceof SourceMovie || s instanceof SourceMusic || s instanceof SourceImage || s instanceof SourceImageU || s instanceof SourceImageGif) {
                        getVideoParams(s, file, null);
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
        try {                                                
            String key = cboAnimations.getSelectedItem().toString();
            String res = animations.getProperty(key);
            URL url = getClass().getResource("/webcamstudio/resources/animations/" + res);
            Stream streamAnm;
            streamAnm = new SourceImageGif(key, url);
            BufferedImage gifImage = ImageIO.read(url);
            getVideoParams(streamAnm, null, gifImage);
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
        } catch (IOException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddAnimationActionPerformed

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
            if (retval == JFileChooser.APPROVE_OPTION && file!=null && file.exists()) {
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
            if (retval == JFileChooser.APPROVE_OPTION && overWrite) {
                final WaitingDialog waitingD = new WaitingDialog(this);
                final File fileF = file;
                lblSourceSelected.setText("");
                waitingD.setModal(true);
                SwingWorker<?,?> worker = new SwingWorker<Void,Integer>(){  
                    @Override
                    protected Void doInBackground() throws InterruptedException{
                        if (fileF!=null){
                            File fileS = fileF;
                            lastFolder = fileS.getParentFile();
                            SystemPlayer.getInstance(null).stop();
                            Tools.sleep(100);
                            PrePlayer.getPreInstance(null).stop();
                            Tools.sleep(100);
                            MasterChannels.getInstance().stopAllStream();
                            Tools.sleep(100);
                            listenerCP.stopChTime(sEvt);
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
//                            String build = new Version().getBuild();
                            setTitle("WebcamStudio " + Version.version + " ("+fileS.getName()+")");
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
            workingLabel.setIcon(new ImageIcon(getClass().getResource("/webcamstudio/resources/tango/working-4.png"))); // NOI18N        
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
    
    public static void getWebcamParams(Stream stream, VideoDevice d) {
        String infoCmd;
        Runtime rt = Runtime.getRuntime();
        infoCmd = "v4l2-ctl --get-fmt-video --device " + d.getFile();
        System.out.println("infoCmd: "+infoCmd);
        File fileD = new File(userHomeDir+"/.webcamstudio/"+"dSize.sh");
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
            dosD.writeBytes(infoCmd+"\n");
            dosD.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
        fileD.setExecutable(true);
        String batchDurationComm = userHomeDir+"/.webcamstudio/"+"dSize.sh";
        try {
            Process infoP = rt.exec(batchDurationComm);
            Tools.sleep(10);
            infoP.waitFor(); //Author spoonybard896
            InputStream lsOut = infoP.getInputStream();
            InputStreamReader isr = new InputStreamReader(lsOut);
            BufferedReader in = new BufferedReader(isr);
            String lineR;
            while ((lineR = in.readLine()) != null) {
                System.out.println("lineR: "+lineR);
                if(lineR.contains("Width")) {
                    lineR = lineR.trim();
                    String[] temp;
                    temp = lineR.split(":");
                    System.out.println("Split:"+temp[0]+" Split:"+temp[1]);
                    String Res = temp[1].replaceAll(" ", "");
                    String[] wh;
                    wh = Res.split("/");
                    
                    int w = Integer.parseInt(wh[0]);
                    int h = Integer.parseInt(wh[1]);
                    System.out.println("W:"+w+" H:"+h);
                    int mixerW = MasterMixer.getInstance().getWidth();
                    int mixerH = MasterMixer.getInstance().getHeight();
                    int hAR = (mixerW*h)/w;
                    int wAR = (mixerH*w)/h;
                    if (hAR > mixerH) {
                        hAR = mixerH;
                        int xPos = (mixerW - wAR)/2;
                        stream.setX(xPos);
                        stream.setWidth(wAR);
                    }
                    if (w > mixerW) {
                         int yPos = (mixerH- hAR)/2;
                         stream.setY(yPos);
                         stream.setHeight(hAR);
                    } else {
                        if (h < mixerH) {
                            int yPos = (mixerH- hAR)/2;
                            stream.setY(yPos);
                        } else {
                           hAR = mixerH;
                        }
                    }
                    stream.setHeight(hAR);
                }
            }
        } catch (IOException | InterruptedException | NumberFormatException e) {
        }
    }
    
    public static void getVideoParams(Stream stream, File file, BufferedImage image) {
        
        if (image != null) {
            if (autoAR) {
                int w = image.getWidth();
                int h = image.getHeight();
                int mixerW = MasterMixer.getInstance().getWidth();
                int mixerH = MasterMixer.getInstance().getHeight();
                int hAR = (mixerW*h)/w;
                int wAR = (mixerH*w)/h;
                if (hAR > mixerH) {
                    hAR = mixerH;
                    int xPos = (mixerW - wAR)/2;
                    stream.setX(xPos);
                    stream.setWidth(wAR);
                }
                if (w > mixerW) {
                     int yPos = (mixerH- hAR)/2;
                     stream.setY(yPos);
                     stream.setHeight(hAR);
                } else {
                    if (h < mixerH) {
                        int yPos = (mixerH- hAR)/2;
                        stream.setY(yPos);
                    } else {
                       hAR = mixerH;
                    }
                }
                stream.setHeight(hAR);
            }
        } else {
            String infoCmd;
            Runtime rt = Runtime.getRuntime();
            if (Screen.avconvDetected()){
                infoCmd = "avconv -i " + "\"" + file.getAbsolutePath() + "\"";
            } else {
                infoCmd = "ffmpeg -i " + "\"" + file.getAbsolutePath() + "\"";    
            }
            File fileD = new File(userHomeDir+"/.webcamstudio/"+"DCalc.sh");
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
                dosD.writeBytes(infoCmd+"\n");
                dosD.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
            fileD.setExecutable(true);
            String batchDurationComm = userHomeDir+"/.webcamstudio/"+"DCalc.sh";
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
                        int totalTime = hours*3600 + minutes*60 + seconds;
                        String strDuration = Integer.toString(totalTime);
                        stream.setStreamTime(strDuration+"s");
                    }
                    if (autoAR) {
                        if (lineR.contains("Video:")) {
                            String [] lineRParts = lineR.split(",");
                            String [] tempNativeSize = lineRParts[2].split(" ");
                            String [] videoNativeSize = tempNativeSize[1].split("x");
                            int w = Integer.parseInt(videoNativeSize[0]);
                            int h = Integer.parseInt(videoNativeSize[1]);
                            int mixerW = MasterMixer.getInstance().getWidth();
                            int mixerH = MasterMixer.getInstance().getHeight();
                            int hAR = (mixerW*h)/w;
                            int wAR = (mixerH*w)/h;
                            if (hAR > mixerH) {
                                hAR = mixerH;
                                int xPos = (mixerW - wAR)/2;
                                stream.setX(xPos);
                                stream.setWidth(wAR);
                            }
                            if (w > mixerW) {
                                 int yPos = (mixerH- hAR)/2;
                                 stream.setY(yPos);
                                 stream.setHeight(hAR);
                            } else {
                                if (h < mixerH) {
                                    int yPos = (mixerH- hAR)/2;
                                    stream.setY(yPos);
                                } else {
                                   hAR = mixerH;
                                }
                            }
                            stream.setHeight(hAR);
                        }
                    }
                }
            } catch (IOException | InterruptedException | NumberFormatException e) {
            }
        }
    }
    
    public static String wsDistroWatch() {
        String distro = null;
        Runtime rt = Runtime.getRuntime();
        String distroCmd = "uname -a";
        try {
            Process distroProc = rt.exec(distroCmd);
            Tools.sleep(10);
            distroProc.waitFor();
            BufferedReader buf = new BufferedReader(new InputStreamReader(
            distroProc.getInputStream()));
            String lineR;
            while ((lineR = buf.readLine()) != null) {
                if(lineR.toLowerCase().contains("ubuntu")) {
                    distro = "ubuntu";
                } else {
                    distro = "others";
                }
            } 
        } catch (IOException | InterruptedException | NumberFormatException e) {
        }
        return distro;
    }
    
    @SuppressWarnings("unchecked")
    private void btnLoadStudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadStudioActionPerformed
        final java.awt.event.ActionEvent fEvt = evt;
        ArrayList<Stream> streamzI = MasterChannels.getInstance().getStreams();
        ArrayList<String> sourceChI = MasterChannels.getInstance().getChannels();
        int sinkStream = 0;
        for (Stream s : streamzI) {
            if (s.getClass().toString().contains("Sink")) {
                sinkStream ++;
            }
        }
        if (streamzI.size() - sinkStream > 0 || sourceChI.size() > 0) {
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
                        Tools.sleep(10);
                        PrePlayer.getPreInstance(null).stop();
                        Tools.sleep(10);
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
                                listenerCP.removeChannels(removeSc, a);
                            }
                        } while (streamz.size()>0 || sourceCh.size()>0);
                        SystemPlayer.getInstance(null).stop();
                        Tools.sleep(10);
                        PrePlayer.getPreInstance(null).stop();
                        Tools.sleep(10);
                        MasterChannels.getInstance().stopAllStream();
                        listenerCP.stopChTime(fEvt);
                        listenerCP.resetBtnStates(fEvt);
                        listenerOP.resetBtnStates(fEvt);
                        tabControls.removeAll();
                        lblSourceSelected.setText("");
                        tabControls.repaint();
                        Tools.sleep(300);
                        desktop.removeAll();
                        desktop.repaint();
                        Tools.sleep(50);
                        try {
                            Studio.LText = new ArrayList<>();
                            Studio.extstream = new ArrayList<>();
                            Studio.ImgMovMus = new ArrayList<>();                          
                            Studio.load(file, "load");
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
                            PreviewMixer.getInstance().stop();
                            PreviewMixer.getInstance().setWidth(mW);
                            PreviewMixer.getInstance().setHeight(mH);
//                            PreviewMixer.getInstance().setRate((Integer) spinFPS.getValue());
                            PreviewMixer.getInstance().start();
                        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
                            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        // loading studio streams
                        for (int u = 0; u < Studio.ImgMovMus.size(); u++) {
                            Stream s = Studio.extstream.get(u);
                            if (s != null) {
                                StreamDesktop frame = new StreamDesktop(s, WebcamStudio.this);
                                frame.setLocation(s.getPanelX(), s.getPanelY());
                                desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                                s.setLoaded(false);
                            }
                            System.out.println("Adding Source: "+s.getName());
                        }
                        Studio.extstream.clear();
                        Studio.extstream = null;
                        Studio.ImgMovMus.clear();
                        Studio.ImgMovMus = null;
                        for (SourceText text : Studio.LText) {
                            if (text != null) {
                                StreamDesktop frame = new StreamDesktop(text, WebcamStudio.this);
                                frame.setLocation(text.getPanelX(), text.getPanelY());
                                desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                                text.setLoaded(false);
                            }
                            System.out.println("Adding Source: "+text.getName());
                        }
                        Studio.LText.clear();
                        Studio.LText = null;
                        Tools.sleep(300);
                        // loading studio channels
                        for (String chsc : MasterChannels.getInstance().getChannels()) {
                            Tools.sleep(10);
                            listenerCP.addLoadingChannel(chsc);
                        }
                        Studio.chanLoad.clear();
                        listenerOP.resetSinks(fEvt);
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Studio is loaded!");
                        ResourceMonitor.getInstance().addMessage(label);
                        setTitle("WebcamStudio " + Version.version + " ("+file.getName()+")");
                    }
                return null;  
                }  
                @Override
                protected void done(){
                    waitingD.dispose();
                }  
            }; 
            worker.execute();
            waitingD.toFront();
            waitingD.setVisible(true);
        } else {
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Loading Cancelled!");
            ResourceMonitor.getInstance().addMessage(label); 
        }
    }//GEN-LAST:event_btnLoadStudioActionPerformed

    private void WCSAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WCSAboutActionPerformed
        About TAbout = new About(about, true);
        TAbout.setLocationRelativeTo(WebcamStudio.cboAnimations);
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
                    getWebcamParams(webcam, d);
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
        int sinkStream = 0;
        for (Stream s : streamzI) {
//            System.out.println("Stream: "+s);
            if (s.getClass().toString().contains("Sink")) {
                sinkStream ++;
            }
        }
        ArrayList<String> sourceChI = MasterChannels.getInstance().getChannels();
        if (streamzI.size() - sinkStream > 0 || sourceChI.size() > 0) {
            int result = JOptionPane.showConfirmDialog(this,"Current Studio will be closed !!!","Attention",JOptionPane.YES_NO_CANCEL_OPTION);
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
        }
        if (doNew) {            
            SystemPlayer.getInstance(null).stop();
            Tools.sleep(10);
            PrePlayer.getPreInstance(null).stop();
            Tools.sleep(10);
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
                    listenerCP.removeChannels(removeSc, a);
                }
            } while (streamz.size()>0 || sourceCh.size()>0);
            listenerCP.stopChTime(evt);
            listenerCP.resetBtnStates(evt);
            listenerOP.resetBtnStates(evt);
            listenerOP.resetSinks(evt);
            tabControls.removeAll();
            lblSourceSelected.setText("");
            tabControls.repaint();
            Tools.sleep(300);
            desktop.removeAll();
            desktop.repaint();
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "New Studio Created.");
            ResourceMonitor.getInstance().addMessage(label);
            setTitle("WebcamStudio " + Version.version);
        } else {
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "New Studio Action Cancelled.");
            ResourceMonitor.getInstance().addMessage(label);    
        }
        System.gc();
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
        vDevsI.setLocationRelativeTo(WebcamStudio.cboAnimations);
        vDevsI.setVisible(true);
    }//GEN-LAST:event_btnVideoDevInfoActionPerformed

    private void btnRefreshWebcamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshWebcamActionPerformed
        initWebcam();
    }//GEN-LAST:event_btnRefreshWebcamActionPerformed

    private void btnAddAudioSrcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAudioSrcActionPerformed
        SourceAudioSource source = new SourceAudioSource();
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
    }//GEN-LAST:event_btnAddAudioSrcActionPerformed

    private void cboAudioHzActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboAudioHzActionPerformed
        final String audioHz = cboAudioHz.getSelectedItem().toString();
        if (audioHz.equals("22050Hz")) {
            audioFreq = 22050;
        } else {
            audioFreq = 44100;
        }
        MasterMixer.getInstance().stop();
        PreviewMixer.getInstance().stop();
        Tools.sleep(100);
        SystemPlayer.getInstance(null).stop();
        Tools.sleep(30);
        PrePlayer.getPreInstance(null).stop();
        Tools.sleep(30);
        MasterChannels.getInstance().stopAllStream();
        for (Stream s : streamS){
            s.updateStatus();
        }
        MasterMixer.getInstance().start();
        PreviewMixer.getInstance().start();
        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Overall Audio Output set to: "+audioFreq+"Hz");
        ResourceMonitor.getInstance().addMessage(label);
    }//GEN-LAST:event_cboAudioHzActionPerformed

   @SuppressWarnings("unchecked") 
    private void btnImportStudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportStudioActionPerformed
        JFileChooser chooser = new JFileChooser(lastFolder);
        FileNameExtensionFilter studioFilter = new FileNameExtensionFilter("Studio files (*.studio)", "studio");
        chooser.setFileFilter(studioFilter);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Import a Studio ...");
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
                        try {
                            Studio.LText = new ArrayList<>();
                            Studio.extstream = new ArrayList<>();
                            Studio.ImgMovMus = new ArrayList<>();
                            Studio.load(file, "add");
                            Studio.main();
                        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
                            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        for (int u = 0; u < Studio.ImgMovMus.size(); u++) {
                            Tools.sleep(10);
                            Stream s = Studio.extstream.get(u);
                            if (s != null) {
//                            System.out.println("Stream Ch: "+s.getChannels());
                                // to fix 0 channels .studio import
                                if (s.getChannels().isEmpty()) {
                                    ArrayList<String> allChan = new ArrayList<>();
                                    for (String scn : MasterChannels.getInstance().getChannels()){
                                        allChan.add(scn);
//                                        System.out.println("Current Studio Ch: "+scn+" added.");
                                    } 
                                    for (String sc : allChan){
                                        s.addChannel(SourceChannel.getChannel(sc, s));
                                    }
                                }

                            StreamDesktop frame = new StreamDesktop(s, WebcamStudio.this);
                            frame.setLocation(s.getPanelX(), s.getPanelY());
                            desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                            }
                        }
                        Studio.extstream.clear();
                        Studio.extstream = null;
                        Studio.ImgMovMus.clear();
                        Studio.ImgMovMus = null;
                        for (int t = 0; t < Studio.LText.size(); t++) {
                            SourceText text = Studio.LText.get(t);
                            // to fix 0 channels .studio import
                            if (text.getChannels().isEmpty()) {
                                ArrayList<String> allChan = new ArrayList<>();
                                for (String scn : MasterChannels.getInstance().getChannels()){
                                    allChan.add(scn);
//                                    System.out.println("Current Studio Ch: "+scn+" added.");
                                } 
                                for (String sc : allChan){
                                    text.addChannel(SourceChannel.getChannel(sc, text));
                                }
                            }
                            
                            StreamDesktop frame = new StreamDesktop(text, WebcamStudio.this);
                            frame.setLocation(text.getPanelX(), text.getPanelY());
                            desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
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
                                listenerCP.addLoadingChannel(chsct);
                                master.insertStudio(chsct);
                        }
                        Studio.chanLoad.clear();  
                    }
                    return null;  
                }  
                @Override
                protected void done(){
                    waitingD.dispose();
                } 
            };
            worker.execute();
            waitingD.toFront();
            waitingD.setVisible(true);
            if (file!=null){
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Studio is Imported!");
                ResourceMonitor.getInstance().addMessage(label);
            } 
        } else {
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Studio Import Cancelled!");
            ResourceMonitor.getInstance().addMessage(label); 
        }
    }//GEN-LAST:event_btnImportStudioActionPerformed

    private void btnAddIPCamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddIPCamActionPerformed
        SourceIPCam streamIPCam;
        streamIPCam = new SourceIPCam();
        ArrayList<String> allChan = new ArrayList<>();
        for (String scn : MasterChannels.getInstance().getChannels()){
            allChan.add(scn); 
        } 
        for (String sc : allChan){
            streamIPCam.addChannel(SourceChannel.getChannel(sc, streamIPCam));
        }
        StreamDesktop frame = new StreamDesktop(streamIPCam, this);
        desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddIPCamActionPerformed

    private void tglFFmpegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglFFmpegActionPerformed
        if (tglFFmpeg.isSelected()){
            tglAVconv.setSelected(false);
            tglGst.setSelected(false);
            outFMEbe = 0;
            listenerOP.resetSinks(evt);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Output Backend Switched to FFmpeg.");
            ResourceMonitor.getInstance().addMessage(label);
        } else {
            outFMEbe = 2;
            tglAVconv.setEnabled(avconv);
            tglGst.setEnabled(true);
            tglGst.setSelected(true);
            listenerOP.resetSinks(evt);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Output Backend Switched to GStreamer.");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_tglFFmpegActionPerformed

    private void btnAddFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFolderActionPerformed
        final java.awt.event.ActionEvent fEvt = evt;
        JFileChooser chooser = new JFileChooser(lastFolder);
        FileNameExtensionFilter mediaFilter = new FileNameExtensionFilter("Supported Media files", "avi", "ogg", "jpeg", "ogv", "mp4", "m4v", "mpg", "divx", "wmv", "flv", "mov", "mkv", "vob", "jpg", "bmp", "png", "gif", "mp3", "wav", "wma", "m4a", ".mp2");
        chooser.setFileFilter(mediaFilter);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Add Media Folder ...");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int retVal = chooser.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            final File dir = chooser.getSelectedFile();
//            System.out.println("Dir: "+dir);
            final WaitingDialog waitingD = new WaitingDialog(this);
            waitingD.setModal(true);
            SwingWorker<?,?> worker = new SwingWorker<Void,Integer>(){  
                @Override
                protected Void doInBackground() throws InterruptedException {
                    boolean noStreams = false;
                    ArrayList<Stream> allStreams = MasterChannels.getInstance().getStreams();
                    
                    for (Stream str : allStreams) {
//                        System.out.println("NoStreams Check: "+str.getClass().toString());
                        if (!str.getClass().toString().contains("Sink")) {
                            noStreams = false;
                            break;
                        } else {
                            noStreams = true;
                        }
                    }
                    File[] contents = null;
                    if (dir != null) {
                        lastFolder = dir.getAbsoluteFile();
                        contents = dir.listFiles();
//                        for ( File f : contents) {
//                            String fileName = f.getName();
//                            System.out.println("Name: " + fileName);
//                        }
                    }
                    if (dir != null) {
                        for ( File file : contents) {
                            Stream s = Stream.getInstance(file);
                            if (s != null) {
                                if (s instanceof SourceMovie || s instanceof SourceMusic || s instanceof SourceImage || s instanceof SourceImageU || stream instanceof SourceImageGif) {
                                    getVideoParams(s, file, null);
                                }
                                StreamDesktop frame = new StreamDesktop(s, WebcamStudio.this);
                                desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                                
                            }
                        }
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Media Folder Imported!");
                        ResourceMonitor.getInstance().addMessage(label);
                    } else {
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "No Directory Selected!");
                        ResourceMonitor.getInstance().addMessage(label);
                    }
                    if (noStreams) { 
                        listenerCP.resetAutoPLBtnState(fEvt);
                    }
                return null;
                }  
                @Override
                protected void done(){
                    waitingD.dispose();
                }  
            }; 
        worker.execute();
        waitingD.toFront();
        waitingD.setVisible(true);
        } else {
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Loading Cancelled!");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_btnAddFolderActionPerformed

    private void btnAddDVCamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddDVCamActionPerformed
        SourceDV streamDV;
        streamDV = new SourceDV();
        ArrayList<String> allChan = new ArrayList<>();
        for (String scn : MasterChannels.getInstance().getChannels()){
            allChan.add(scn); 
        } 
        for (String sc : allChan){
            streamDV.addChannel(SourceChannel.getChannel(sc, streamDV));
        }
        StreamDesktop frame = new StreamDesktop(streamDV, this);
        desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddDVCamActionPerformed

    private void tglAVconvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglAVconvActionPerformed
        if (tglAVconv.isSelected()){
            tglFFmpeg.setSelected(false);
            tglGst.setSelected(false);
            outFMEbe = 1;
            listenerOP.resetSinks(evt);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Output Backend Switched to Libav.");
            ResourceMonitor.getInstance().addMessage(label);
        } else {
            outFMEbe = 2;
            tglFFmpeg.setEnabled(ffmpeg);
            tglGst.setEnabled(true);
            tglGst.setSelected(true);
            listenerOP.resetSinks(evt);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Output Backend Switched to Gstreamer.");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_tglAVconvActionPerformed

    private void tglGstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglGstActionPerformed
        if (tglGst.isSelected()){
            tglFFmpeg.setSelected(false);
            tglAVconv.setSelected(false);
            outFMEbe = 2;
            listenerOP.resetSinks(evt);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Output Backend Switched to GStreamer.");
            ResourceMonitor.getInstance().addMessage(label);
        } else {
            if (ffmpeg && !avconv){
                outFMEbe = 0;
                tglFFmpeg.setSelected(true);
                tglAVconv.setEnabled(false);
                tglGst.setEnabled(true);
            } else if (ffmpeg && avconv) {
                outFMEbe = 1;
                tglFFmpeg.setEnabled(true);
                tglAVconv.setSelected(true);
                tglGst.setEnabled(true);
                    
            } else {
                outFMEbe = 1;
                tglFFmpeg.setEnabled(false);
                tglAVconv.setSelected(true);
                tglGst.setEnabled(true);
            }
            
            listenerOP.resetSinks(evt);
            if (outFMEbe == 1) {
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Output Backend Switched to Libav.");
                ResourceMonitor.getInstance().addMessage(label);
            } else {
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Output Backend Switched to FFmpeg.");
                ResourceMonitor.getInstance().addMessage(label);
            }
        }
    }//GEN-LAST:event_tglGstActionPerformed

    private void btnSysGCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSysGCActionPerformed
        System.gc();
    }//GEN-LAST:event_btnSysGCActionPerformed

    private void cboThemeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboThemeActionPerformed
        final String themeSW = cboTheme.getSelectedItem().toString();
        if (themeSW.equals("Classic")) {
            theme = "Classic";
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Master Theme set to \""+theme+"\"");
            ResourceMonitor.getInstance().addMessage(label);
        } else {
            theme = "Dark";
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Master Theme set to \""+theme+"\"");
            ResourceMonitor.getInstance().addMessage(label);
        }
        Thread wsRestart = new Thread(new Runnable() {
            @Override
            public void run() {
                restartDialog();
            }
        });
        if (!firstRun) {
            wsRestart.start();
        }
    }//GEN-LAST:event_cboThemeActionPerformed

    private void tglAutoARActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglAutoARActionPerformed
        if (tglAutoAR.isSelected()) {
            autoAR = true;
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Automatic Aspect Ratio detection \"On\"");
            ResourceMonitor.getInstance().addMessage(label);
        } else {
            autoAR = false;
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Automatic Aspect Ratio detection \"Off\"");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_tglAutoARActionPerformed
    
    /**
     *
     */
    public void restartDialog(){
        Object[] options = {"OK"};
        JOptionPane.showOptionDialog(this,
        "You need to restart WebcamStudio for the changes to take effect.","Information",
        JOptionPane.PLAIN_MESSAGE,
        JOptionPane.INFORMATION_MESSAGE,
        null,
        options,
        options[0]);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws IOException { 
        if (System.getProperty("jna.nosys") == null) {
            System.setProperty("jna.nosys", "true");
        }
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true"); // Java 8 Drag'n'Drop Fix
        File dir = new File(userHomeDir, ".webcamstudio");
        if (!dir.exists()) {
            dir.mkdir();
        }
        System.out.println("Welcome to WebcamStudio "+Version.version + " build "+ new Version().getBuild()+" ...");
        
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
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {            
                try {
                    new WebcamStudio().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        if (args != null){
            int c = 0;
            for (String arg : args){
                System.out.println("Argument: "+arg);
                if (arg.endsWith("studio")){
                    cmdFile = new File(arg);
                }
                if (arg.equals("-o")) {
                    cmdOut = args[c+1];
                }
                if (arg.equals("-autoplay")) {
                    cmdAutoStart = true;
                }
                if (arg.equals("-remote")) {
                    cmdRemote = true;
                }
                c++;
            }
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton WCSAbout;
    private javax.swing.JButton btnAddAnimation;
    private javax.swing.JButton btnAddAudioSrc;
    private javax.swing.JButton btnAddDVB;
    private javax.swing.JButton btnAddDVCam;
    private javax.swing.JButton btnAddDesktop;
    private javax.swing.JButton btnAddFile;
    private javax.swing.JButton btnAddFolder;
    private javax.swing.JButton btnAddIPCam;
    private javax.swing.JButton btnAddText;
    private javax.swing.JButton btnAddURL;
    private javax.swing.JButton btnAddWebcams;
    private javax.swing.JButton btnImportStudio;
    private final javax.swing.JButton btnLoadStudio = new javax.swing.JButton();
    private javax.swing.JButton btnMinimizeAll;
    private javax.swing.JButton btnNewStudio;
    private javax.swing.JButton btnRefreshWebcam;
    private javax.swing.JButton btnSaveStudio;
    private javax.swing.JButton btnSysGC;
    private javax.swing.JButton btnVideoDevInfo;
    public static javax.swing.JComboBox cboAnimations;
    private javax.swing.JComboBox cboAudioHz;
    private javax.swing.JComboBox cboTheme;
    private javax.swing.JComboBox cboWebcam;
    private javax.swing.JDesktopPane desktop;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator10;
    private javax.swing.JToolBar.Separator jSeparator11;
    private javax.swing.JToolBar.Separator jSeparator12;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JLabel lblAVconv;
    private javax.swing.JLabel lblClrRam;
    private javax.swing.JLabel lblFFmpeg;
    private javax.swing.JLabel lblFFmpeg3;
    private javax.swing.JLabel lblGst;
    private javax.swing.JLabel lblSourceSelected;
    private javax.swing.JSplitPane mainSplit;
    private javax.swing.JToolBar mainToolbar;
    private javax.swing.JPanel panControls;
    private javax.swing.JPanel panSources;
    public static javax.swing.JTabbedPane tabControls;
    private javax.swing.JToggleButton tglAVconv;
    private javax.swing.JToggleButton tglAutoAR;
    private javax.swing.JToggleButton tglFFmpeg;
    private javax.swing.JToggleButton tglGst;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
      
    @Override
    public void selectedSource(Stream source) {
        String sourceName = source.getName();
        String shortName = "";
        if (sourceName.length() > 30) {
            shortName = source.getName().substring(0, 30)+" ...";
        } else {
            shortName = sourceName;
        }
        lblSourceSelected.setText(shortName);     
        lblSourceSelected.setToolTipText(source.getName());      
        tabControls.removeAll();
        tabControls.repaint();
        ArrayList<Component> comps = SourceControls.getControls(source);        
        for (Component c : comps) {
            String cName = c.getName();
            tabControls.add(cName, c);
        }
    }
    
    public void loadAtStart(final File file, final java.awt.event.ActionEvent fEvt){
        final WaitingDialog waitingD = new WaitingDialog(this);
        waitingD.setModal(true);
        SwingWorker<?,?> worker = new SwingWorker<Void,Integer>(){  
            @Override
            protected Void doInBackground() throws InterruptedException{  
                if (file != null) {
                    lastFolder = file.getParentFile();
                    SystemPlayer.getInstance(null).stop();
                    Tools.sleep(10);
                    PrePlayer.getPreInstance(null).stop();
                    Tools.sleep(10);
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
                            listenerCP.removeChannels(removeSc, a);
                        }
                    } while (streamz.size()>0 || sourceCh.size()>0);
                    SystemPlayer.getInstance(null).stop();
                    Tools.sleep(10);
                    PrePlayer.getPreInstance(null).stop();
                    Tools.sleep(10);
                    MasterChannels.getInstance().stopAllStream();
                    listenerCP.stopChTime(fEvt);
                    listenerCP.resetBtnStates(fEvt);
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
                        Studio.load(file, "load");
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
                        PreviewMixer.getInstance().stop();
                        PreviewMixer.getInstance().setWidth(mW);
                        PreviewMixer.getInstance().setHeight(mH);
//                            PreviewMixer.getInstance().setRate((Integer) spinFPS.getValue());
                        PreviewMixer.getInstance().start();
                    } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
                        Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
// loading studio streams
                    for (int u = 0; u < Studio.ImgMovMus.size(); u++) {
                        Stream s = Studio.extstream.get(u);
                        if (s != null) {
                            StreamDesktop frame = new StreamDesktop(s, WebcamStudio.this);
                            desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                        }
                        System.out.println("Adding Source: "+s.getName());
                    }
                    Studio.extstream.clear();
                    Studio.extstream = null;
                    Studio.ImgMovMus.clear();
                    Studio.ImgMovMus = null;
                    for (SourceText text : Studio.LText) {
                        if (text != null) {
                            StreamDesktop frame = new StreamDesktop(text, WebcamStudio.this);
                            desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                        }
                        System.out.println("Adding Source: "+text.getName());
                    }
                    Studio.LText.clear();
                    Studio.LText = null;
                    Tools.sleep(300);
// loading studio channels
                    for (String chsc : MasterChannels.getInstance().getChannels()) {
                        Tools.sleep(10);
                        listenerCP.addLoadingChannel(chsc);
                    }
                    Studio.chanLoad.clear();
                    listenerOP.resetSinks(fEvt);
                    ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Studio is loaded!");
                    ResourceMonitor.getInstance().addMessage(label);
                    setTitle("WebcamStudio " + Version.version + " ("+file.getName()+")");
                }
            return null;  
            }  
            @Override
            protected void done(){
                waitingD.dispose();
            }  
        }; 
        worker.execute();
        waitingD.toFront();
        waitingD.setVisible(true);
    }
}
