/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Main.java
 *
 * Created on 2009-09-02, 18:20:52
 */
package webcamstudio;

import java.awt.BorderLayout;
import webcamstudio.exporter.vloopback.VideoDevice;
import webcamstudio.exporter.vloopback.V4L2Loopback;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;
import org.gstreamer.*;
import webcamstudio.layout.transitions.Transition;
import webcamstudio.sources.*;
import webcamstudio.components.*;
import webcamstudio.components.LayoutManager2;
import webcamstudio.exporter.VideoExporter;
import webcamstudio.exporter.vloopback.V4LLoopback;
import webcamstudio.exporter.vloopback.VideoOutput;
import webcamstudio.layout.Layout;
import webcamstudio.studio.Studio;
import webcamstudio.visage.FaceDetector;

/**
 *
 * @author pballeux
 */
public class Main extends javax.swing.JFrame implements InfoListener, SourceListener, MediaListener {

    private VideoOutput output = null;
    private Mixer mixer = null;
    private boolean stopMe = false;
    private Preview preview = null;
    private int outputWidth = 320;
    private int outputHeight = 240;
    private File lastStudioFile = null;
    private File lastFolder = null;
    private File lastLoopbackUsed = null;
    public static int DesktopTaskbarHeight = 0;
    public static FaceDetector MainFaceDetector = new FaceDetector();
    private WebCamera webcamera = null;
    private java.util.TreeMap<String, VideoSource> devices = null;
    private java.util.TreeMap<String, VideoSource> movies = null;
    private java.util.TreeMap<String, VideoSource> images = null;
    private java.util.TreeMap<String, VideoSource> animations = null;
    private java.util.TreeMap<String, VideoSource> pipelines = null;
    private java.util.TreeMap<String, VideoSourceMusic> musics = null;
    private java.util.TreeMap<String, VideoExporter> exporters = null;
    private java.util.Vector<String> dirToScan = new java.util.Vector<String>();
    private javax.swing.tree.DefaultTreeModel sourceDir = null;
    private javax.swing.tree.DefaultMutableTreeNode root = null;
    private javax.swing.tree.DefaultMutableTreeNode nodeDevices = null;
    private LayoutManager2 layoutManager = null;
    private String lastDriverOutput = "";
    private String lastDriverOutputPath = "";
    private SystemMonitor monitor = null;
    private int outputStreamPort = 4888;
    //Flag to validate if we are in XMode or in Linux
    public static boolean XMODE = false;

    /** Creates new form Main */
    public Main() {
        Gst.init();

        initComponents();
        // if XMODE is already try, override OS Detection
        XMODE = !isLinux() || XMODE;
        if (XMODE) {
            System.out.println("X Mode activated");
            updateGUIForXmode();
        }
        output = null;
        loadPrefs();
        if (!XMODE) {
            initVideoDevices();
        }



        mixer = new Mixer();
        mixer.setSize(outputWidth, outputHeight);
        mixer.setOutput(output);
        if (mnuchkShowBackground.isSelected()) {
            Image img = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/webcamstudio/resources/splash.jpg"));
            mixer.setBackground(img);
        } else {
            mixer.setBackground(null);
        }
        layoutManager = new LayoutManager2(mixer);

        if (XMODE) {
            setTitle(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("WEBCAMSTUDIO_X") + " " + webcamstudio.Version.version + " (Build: " + new webcamstudio.Version().getBuild() + ")");
        } else {
            setTitle(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("WEBCAMSTUDIO_FOR_GNU/LINUX_") + " " + webcamstudio.Version.version + " (Build: " + new webcamstudio.Version().getBuild() + ")");
        }

        java.awt.Image img = getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/icon.png"));

        setIconImage(img);

        if (lastStudioFile != null) {
            mnuStudioLoadLast.setEnabled(true);
            mnuStudioLoadLast.setToolTipText(lastStudioFile.getAbsolutePath());
        } else {
            mnuStudioLoadLast.setEnabled(false);
        }

        panLayouts.add(layoutManager, BorderLayout.CENTER);

        javax.swing.DefaultListCellRenderer layoutrenderer = new javax.swing.DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
                Component comp = super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                JLabel label = (JLabel) comp;
                if (value instanceof Layout) {
                    Layout layout = (Layout) value;
                    label.setText(layout.toString());
                    label.setToolTipText(layout.toString());

                    label.setIcon(new ImageIcon(layout.getPreview(outputWidth,outputHeight).getScaledInstance(32, 32, BufferedImage.SCALE_FAST)));

                }
                return comp;
            }
        };

        webcamera = new WebCamera();
        new Thread(new Runnable() {

            @Override
            public void run() {
                initSourceDir();

            }
        }).start();
        monitor = new SystemMonitor(pgCPUUsage);
        if (XMODE) {
            mnuOutputLabelStreamPort.setText("Stream Port: " + outputStreamPort);
            mixer.activateStream(true, outputStreamPort);

        } else {
            if (mnuOutputchkActivateStream.isSelected()) {
                mnuOutputLabelStreamPort.setText("Stream Port: " + outputStreamPort);
            }
            mixer.activateStream(mnuOutputchkActivateStream.isSelected(), outputStreamPort);
        }


    }

    private void updateGUIForXmode() {
        cboVideoOutputs.setVisible(!XMODE);
        panelStatus.remove(cboVideoOutputs);
        mnuPixelFormat.setVisible(!XMODE);
        mnuOutputFlipImage.setVisible(!XMODE);
        mnuOutputchkActivateStream.setVisible(!XMODE);
        mnuVideoInfo.setVisible(!XMODE);
    }

    private void initVideoDevices() {
        VideoDevice[] vds = VideoDevice.getInputDevices();
        javax.swing.DefaultComboBoxModel cboDevOuts = new javax.swing.DefaultComboBoxModel(vds);
        cboVideoOutputs.setModel(cboDevOuts);
        VideoDevice selectedVd = null;
        for (VideoDevice vd : vds) {
            if (vd.getVersion() == VideoDevice.Version.V4L2 && lastDriverOutput.equals("v4l2")) {
                if (vd.getFile().getName().equals(lastDriverOutputPath)) {
                    selectedVd = vd;
                    break;
                }
            } else if (vd.getVersion() == VideoDevice.Version.V4L && lastDriverOutput.equals("v4l")) {
                if (vd.getFile().getName().equals(lastDriverOutputPath)) {
                    selectedVd = vd;
                    break;
                }
            }
        }
        if (selectedVd == null) {
            for (VideoDevice vd : vds) {
                if (vd.getVersion() == VideoDevice.Version.V4L2 && lastDriverOutput.equals("v4l2")) {
                    selectedVd = vd;
                    break;
                } else if (vd.getVersion() == VideoDevice.Version.V4L && lastDriverOutput.equals("v4l")) {
                    selectedVd = vd;
                    break;
                }
            }
        }
        if (selectedVd == null && vds.length > 0) {
            selectedVd = vds[0];
        }
        if (selectedVd != null) {
            cboVideoOutputs.setSelectedItem(selectedVd);
            selectOutputDevice(selectedVd);
        }
    }

    private boolean isLinux() {
        boolean retValue = false;
        String osName = System.getProperty("os.name");
        System.out.println("OS is " + osName);
        retValue = osName.toLowerCase().startsWith("linux");
        return retValue;
    }

    private void selectOutputDevice(VideoDevice dev) {

        if (output != null) {
            output.close();
        }
        if (dev.getVersion() == VideoDevice.Version.V4L2) {
            output = new V4L2Loopback(this);
        } else if (dev.getVersion() == VideoDevice.Version.V4L) {
            output = new V4LLoopback(this);
        }
        int pixFmt = VideoOutput.RGB24;
        if (mnurdPixelFormatRGB24.isSelected()) {
            pixFmt = VideoOutput.RGB24;
        } else if (mnurdPixelFormatUYVY.isSelected()) {
            pixFmt = VideoOutput.UYVY;
        }
        output.open(dev.getFile().getAbsolutePath(), outputWidth, outputHeight, pixFmt);
        System.out.println("DEBUG: " + dev.getName());
    }

    private void initSourceDir() {
        final java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages");
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        MediaPanel mediaPanel = new MediaPanel();

        mediaPanel.setAutoscrolls(true);

        if (!XMODE) {
            //Loading devices
            buildSourceDevices();
            MediaPanelList pdevices = new MediaPanelList(this);
            pdevices.setPanelName(bundle.getString("DEVICES"));
            for (VideoSource v : devices.values()) {
                pdevices.addMedia(v);
            }
            mediaPanel.addMedia(pdevices);
        }

        buildSourceImages();
        MediaPanelList pImages = new MediaPanelList(this);
        pImages.setPanelName(bundle.getString("IMAGES"));
        for (VideoSource v : images.values()) {
            pImages.addMedia(v);
        }
        mediaPanel.addMedia(pImages);

        buildSourceMovies();
        MediaPanelList pMovies = new MediaPanelList(this);
        pMovies.setPanelName(bundle.getString("MOVIES"));
        for (VideoSource v : movies.values()) {
            pMovies.addMedia(v);
        }
        mediaPanel.addMedia(pMovies);

        buildSourceMusics();
        MediaPanelList pMusics = new MediaPanelList(this);
        pMusics.setPanelName(bundle.getString("SONGS"));
        for (VideoSource v : musics.values()) {
            pMusics.addMedia(v);
        }
        mediaPanel.addMedia(pMusics);

        MediaPanelList pAnm = new MediaPanelList(this);
        pAnm.setPanelName(bundle.getString("ANIMATIONS"));
        WS4GLAnimations wsa = new WS4GLAnimations();
        try {
            wsa.updateSourceList();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (VideoSource v : wsa.getSources()) {
            pAnm.addMedia(v);
        }
        buildSourceAnimations();

        for (VideoSource v : animations.values()) {
            pAnm.addMedia(v);
        }
        mediaPanel.addMedia(pAnm);


        MediaPanelList pwid = new MediaPanelList(this);
        pwid.setPanelName(bundle.getString("WIDGETS"));


        WS4GLWidgets wsw = new WS4GLWidgets();
        try {
            wsw.updateSourceList();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (VideoSource v : wsw.getSources()) {
            pwid.addMedia(v);
        }
        mediaPanel.addMedia(pwid);

        buildSourcePipelines();
        MediaPanelList ppipes = new MediaPanelList(this);
        ppipes.setPanelName(bundle.getString("PIPELINES"));

        for (VideoSource v : pipelines.values()) {
            ppipes.addMedia(v);
        }
        mediaPanel.addMedia(ppipes);

        setCursor(Cursor.getDefaultCursor());
        mediaPanel.revalidate();
        panBrowser.removeAll();
        panBrowser.add(mediaPanel, BorderLayout.CENTER);
        panBrowser.revalidate();
    }

    private void buildSourceAnimations() {
        animations = new java.util.TreeMap<String, VideoSource>();
        for (String d : dirToScan) {
            File dir = new File(d);
            if (dir.exists()) {
                File[] fanimations = dir.listFiles();
                for (File a : fanimations) {
                    if (a.getName().endsWith(".anm")) {
                        VideoSourceAnimation va = new VideoSourceAnimation(a);
                        if (animations.containsKey(va.getName())) {
                            animations.put(va.getName() + "-" + va.getUUID(), va);
                        } else {
                            animations.put(va.getName(), va);
                        }
                    }
                }
            }
        }
    }

    private void buildSourceMovies() {
        movies = new java.util.TreeMap<String, VideoSource>();
        for (String d : dirToScan) {
            File dir = new File(d);
            if (dir.exists()) {
                File[] fmovies = dir.listFiles();
                for (File m : fmovies) {
                    if (isMovie(m)) {
                        VideoSourceMovie vm = new VideoSourceMovie(m);
                        if (movies.containsKey(vm.getName())) {
                            movies.put(vm.getName() + "-" + vm.getUUID(), vm);
                        } else {
                            movies.put(vm.getName(), vm);
                        }
                    }
                }
            }
        }
    }

    private void buildSourceMusics() {
        musics = new java.util.TreeMap<String, VideoSourceMusic>();
        for (String d : dirToScan) {
            File dir = new File(d);
            if (dir.exists()) {
                File[] fmusics = dir.listFiles();
                for (File m : fmusics) {
                    if (isMusic(m)) {
                        VideoSourceMusic vm = new VideoSourceMusic(m);
                        if (musics.containsKey(vm.getName())) {
                            musics.put(vm.getName() + "-" + vm.getUUID(), vm);
                        } else {
                            musics.put(vm.getName(), vm);
                        }
                    }
                }
            }
        }
    }

    private void buildSourcePipelines() {
        pipelines = new java.util.TreeMap<String, VideoSource>();
        exporters = new java.util.TreeMap<String, VideoExporter>();
        String type = "source";
        for (String d : dirToScan) {
            File dir = new File(d);
            if (dir.exists()) {
                File[] fplugins = dir.listFiles();
                for (File f : fplugins) {
                    if (f.getName().endsWith(".wspl")) {
                        try {
                            java.util.Properties plugin = new java.util.Properties();
                            plugin.load(f.toURI().toURL().openStream());
                            type = plugin.getProperty("type");
                            if (type == null || type.toLowerCase().equals("source")) {
                                VideoSourcePipeline vm = new VideoSourcePipeline(f);
                                if (pipelines.containsKey(vm.getName())) {
                                    pipelines.put(vm.getName() + "-" + vm.getUUID(), vm);
                                } else {
                                    pipelines.put(vm.getName(), vm);
                                }
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }

    private void buildSourceImages() {
        images = new java.util.TreeMap<String, VideoSource>();
        for (String d : dirToScan) {
            File dir = new File(d);
            if (dir.exists()) {
                File[] fimages = dir.listFiles();
                for (File f : fimages) {
                    if (isImage(f)) {
                        VideoSourceImage vm = new VideoSourceImage(f);
                        if (images.containsKey(vm.getName())) {
                            images.put(vm.getName() + "-" + vm.getUUID(), vm);
                        } else {
                            images.put(vm.getName(), vm);
                        }
                    }
                }
            }
        }
    }

    private boolean isMovie(File f) {
        boolean retValue = false;
        if (f.getName().endsWith(".avi")
                || f.getName().endsWith(".mov")
                || f.getName().endsWith(".mpg")
                || f.getName().endsWith(".ogv")
                || f.getName().endsWith(".ogg")
                || f.getName().endsWith(".flv")
                || f.getName().endsWith(".m4v")
                || f.getName().endsWith(".mp4")
                || f.getName().endsWith(".vob")
                || f.getName().endsWith(".mpeg")
                || f.getName().endsWith(".webm")) {
            retValue = true;
        }
        return retValue;
    }

    private boolean isMusic(File f) {
        boolean retValue = false;
        if (f.getName().endsWith(".mp3")
                || f.getName().endsWith(".MP3")
                || f.getName().endsWith(".wav")
                || f.getName().endsWith(".oga")
                || f.getName().endsWith(".m4a")
                || f.getName().endsWith(".mp2")) {
            retValue = true;
        }
        return retValue;
    }

    private boolean isImage(File f) {
        boolean retValue = false;
        if (f.getName().endsWith(".jpg")
                || f.getName().endsWith(".png")
                || f.getName().endsWith(".gif")
                || f.getName().endsWith(".jpeg")) {
            retValue = true;
        }
        return retValue;
    }

    private void buildSourceDevices() {
        devices = new java.util.TreeMap<String, VideoSource>();
        VideoDevice[] v = VideoDevice.getOutputDevices();
        VideoSourceV4L webcam = null;
        for (int i = 0; i < v.length; i++) {
            switch (v[i].getVersion()) {
                case V4L:
                    webcam = new VideoSourceV4L(v[i].getName(), v[i].getFile());
                    devices.put(webcam.getName() + webcam.getUUID(), webcam);
                    break;
                case V4L2:
                    webcam = new VideoSourceV4L2(v[i].getName(), v[i].getFile());
                    devices.put(webcam.getName() + webcam.getUUID(), webcam);
                    break;
            }
        }
        if (new File("/dev/fw1").exists()) {
            VideoSourceDV dv = new VideoSourceDV();
            devices.put(dv.getName() + dv.getUUID(), dv);
        }
        if (webcamera.getSource() != null) {
            devices.put(webcamera.getSource().getName(), webcamera.getSource());
        }
    }

    public void updateNodeDevices() {
        if (nodeDevices != null) {
            buildSourceDevices();
            nodeDevices.removeAllChildren();
            for (VideoSource v : devices.values()) {
                nodeDevices.add(new javax.swing.tree.DefaultMutableTreeNode(v));
            }
            sourceDir.nodeChanged(nodeDevices);
            sourceDir.nodeStructureChanged(nodeDevices);

            System.out.println("Device update");
        }
    }

    private void loadPrefs() {
        Preferences prefs = Preferences.userRoot().node("webcamstudio");
        if (prefs.get("laststudiofile", null) != null) {
            lastStudioFile = new File(prefs.get("laststudiofile", "."));
        }
        outputWidth = prefs.getInt("outputwidth", outputWidth);
        outputHeight = prefs.getInt("outputheight", outputHeight);

        this.setLocation(prefs.getInt("mainx", 0), prefs.getInt("mainy", 0));
        this.setSize(prefs.getInt("mainw", 800), prefs.getInt("mainh", 600));
        if (prefs.get("format", "rgb24").equals("rgb24")) {
            mnurdPixelFormatRGB24.setSelected(true);
            mnurdPixelFormatUYVY.setSelected(false);
        } else if (prefs.get("format", "uyvy").equals("uyvy")) {
            mnurdPixelFormatUYVY.setSelected(true);
            mnurdPixelFormatRGB24.setSelected(false);
            mnuOutputSize.setEnabled(false);
            outputWidth = 640;
            outputHeight = 480;
        }
        String outputSize = outputWidth + "x" + outputHeight;
        java.util.Enumeration<javax.swing.AbstractButton> list = grpOutputSize.getElements();
        while (list.hasMoreElements()) {
            javax.swing.AbstractButton button = list.nextElement();
            button.setSelected(button.getActionCommand().equals(outputSize));
        }
        list = grpQuality.getElements();
        while (list.hasMoreElements()) {
            javax.swing.AbstractButton button = list.nextElement();
            button.setSelected(button.getActionCommand().equals(prefs.get("quality", "NORMAL")));
        }

        mnuchkShowBackground.setSelected(prefs.getBoolean("showsplashbackground", mnuchkShowBackground.isSelected()));
        list = grpFramerate.getElements();
        while (list.hasMoreElements()) {
            javax.swing.AbstractButton button = list.nextElement();
            button.setSelected(button.getActionCommand().equals(prefs.get("fps", "15")));
        }
        if (prefs.get("lastloopback", null) != null) {
            lastLoopbackUsed = new File(prefs.get("lastloopback", "/dev/video1"));
        }
        lastFolder = new File(prefs.get("lastfolder", "."));
        String[] dir = prefs.get("sb_dirtoscan", "").split(";");
        dirToScan.clear();
        for (String d : dir) {
            dirToScan.add(d);
        }
        lastDriverOutput = prefs.get("lastdriveroutput", lastDriverOutput);
        lastDriverOutputPath = prefs.get("lastdriverpath", lastDriverOutputPath);
        mnuSourceschkShowBrowser.setSelected(prefs.getBoolean("showbrowser", mnuSourceschkShowBrowser.isSelected()));
        panBrowser.setVisible(mnuSourceschkShowBrowser.isSelected());
        outputStreamPort = prefs.getInt("streamport", outputStreamPort);
        mnuOutputchkActivateStream.setSelected(prefs.getBoolean("activatestream", mnuOutputchkActivateStream.isSelected()));

        prefs = null;
    }

    private void savePrefs() {
        Preferences prefs = Preferences.userRoot().node("webcamstudio");
        if (lastStudioFile != null) {
            prefs.put("laststudiofile", lastStudioFile.getAbsolutePath());
        }
        prefs.putInt("mainx", this.getX());
        prefs.putInt("mainy", this.getY());
        prefs.putInt("mainw", this.getWidth());
        prefs.putInt("mainh", this.getHeight());

        prefs.put("fps", grpFramerate.getSelection().getActionCommand());
        prefs.putInt("outputwidth", outputWidth);
        prefs.putInt("outputheight", outputHeight);
        if (mnurdPixelFormatRGB24.isSelected()) {
            prefs.put("format", "rgb24");
        } else if (mnurdPixelFormatUYVY.isSelected()) {
            prefs.put("format", "uyvy");
        }

        prefs.putBoolean("showsplashbackground", mnuchkShowBackground.isSelected());

        if (lastLoopbackUsed != null) {
            prefs.put("lastloopback", lastLoopbackUsed.getAbsolutePath());
        }
        if (lastFolder != null) {
            prefs.put("lastfolder", lastFolder.getAbsolutePath());
        }
        prefs.putBoolean("activatestream", mnuOutputchkActivateStream.isSelected());
        prefs.putInt("streamport", outputStreamPort);
        String allDirs = "";
        for (String d : dirToScan) {
            allDirs += d + ";";
        }
        allDirs = allDirs.substring(0, allDirs.length() - 1);
        prefs.put("sb_dirtoscan", allDirs);
        if (output != null) {
            if (output instanceof V4L2Loopback) {
                prefs.put("lastdriveroutput", "v4l2");
            } else if (output instanceof V4LLoopback) {
                prefs.put("lastdriveroutput", "v4l");
            }
            prefs.put("lastdriverpath", output.getDevice());
        }
        prefs.putBoolean("showbrowser", mnuSourceschkShowBrowser.isSelected());
        prefs = null;
    }

    private void saveStudio(java.io.File studio) {

        if (studio == null || studio.isDirectory()) {
            javax.swing.JFileChooser chooser = new javax.swing.JFileChooser(lastStudioFile);
            chooser.setToolTipText(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("SELECT_YOU_STUDIO_OUTPUT_FILE..."));

            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("WEBCAMSTUDIO_FILE"), "studio");
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(filter);

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File f = chooser.getSelectedFile();
                if (f != null) {
                    if (!f.getName().endsWith(".studio")) {
                        f = new java.io.File(f.getAbsolutePath() + ".studio");
                    }
                    studio = f;
                }
            }
        }

        if (studio != null) {
            try {
                if (studio.exists()) {
                    studio.delete();
                }
                Studio outStudio = new Studio();
                java.util.Vector<Layout> layouts = new java.util.Vector<Layout>();
                for (Object l : layoutManager.getLayouts()) {
                    layouts.add((Layout) l);
                }
                outStudio.setLayouts(layouts);
                outStudio.saveStudio(studio, mixer);
                lastStudioFile = studio;
                mnuStudioLoadLast.setEnabled(true);
                mnuStudioLoadLast.setToolTipText(lastStudioFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void loadStudioFromFile(java.io.File providedFile) {
        java.io.File currentStudioFile = null;

        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser(lastStudioFile);
        chooser.setToolTipText(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("SELECT_YOUR_STUDIO_FILE_TO_LOAD..."));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("WEBCAMSTUDIO_FILE"), "studio");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(filter);

        if (providedFile == null) {
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File f = chooser.getSelectedFile();
                if (f != null) {
                    currentStudioFile = f;
                }
            } else {
                currentStudioFile = null;
            }
        } else {
            currentStudioFile = providedFile;
        }
        if (currentStudioFile != null) {
            try {
                lastStudioFile = currentStudioFile;
                mnuStudioLoadLast.setEnabled(true);
                mnuStudioLoadLast.setToolTipText(lastStudioFile.getAbsolutePath());
                Studio studio = new Studio();
                studio.loadStudio(lastStudioFile);
                layoutManager.clearLayouts();
                for (Layout l : studio.getLayouts()) {
                    layoutManager.addLayout(l);
                }
                layoutManager.revalidate();
                studio = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    

    private void addSourceToDesktop(VideoSource source) {
        if (source.getOutputWidth() == 0) {
            source.setOutputWidth(outputWidth);
        }
        if (source.getOutputHeight() == 0) {
            source.setOutputHeight(outputHeight);
        }
        layoutManager.addSource(source);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpOutputSize = new javax.swing.ButtonGroup();
        grpQuality = new javax.swing.ButtonGroup();
        grpFramerate = new javax.swing.ButtonGroup();
        grpPixelFormat = new javax.swing.ButtonGroup();
        panelStatus = new javax.swing.JPanel();
        pgCPUUsage = new javax.swing.JProgressBar();
        cboVideoOutputs = new javax.swing.JComboBox();
        panLayouts = new javax.swing.JPanel();
        panBrowser = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        mnuStudios = new javax.swing.JMenu();
        mnuStudioNew = new javax.swing.JMenuItem();
        mnuStudiosLoad = new javax.swing.JMenuItem();
        mnuStudioLoadLast = new javax.swing.JMenuItem();
        mnuStudiosSave = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        mnuAnimationCreator = new javax.swing.JMenuItem();
        mnuSources = new javax.swing.JMenu();
        mnuSourceschkShowBrowser = new javax.swing.JCheckBoxMenuItem();
        mnuReloadSourceTree = new javax.swing.JMenuItem();
        mnuAddDir = new javax.swing.JMenuItem();
        mnuRemoveDir = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        mnuSourcesDesktop = new javax.swing.JMenuItem();
        mnuSourcesFullDesktop = new javax.swing.JMenuItem();
        mnuSourcesDV = new javax.swing.JMenuItem();
        mnuSourcesImage = new javax.swing.JMenuItem();
        mnuSourcesFreeText = new javax.swing.JMenuItem();
        mnuSourceIRC = new javax.swing.JMenuItem();
        mnuSourcesAnimation = new javax.swing.JMenuItem();
        mnuSourcesMovie = new javax.swing.JMenuItem();
        mnuSourcesStream = new javax.swing.JMenuItem();
        mnuSourcesMovieViewer = new javax.swing.JMenuItem();
        mnuSourcesWidget = new javax.swing.JMenuItem();
        mnuSourcesConsole = new javax.swing.JMenuItem();
        mnuSourcesQRCode = new javax.swing.JMenuItem();
        mnuOutput = new javax.swing.JMenu();
        mnuVideoRecorder = new javax.swing.JMenuItem();
        mnuOutputSpnashot = new javax.swing.JMenuItem();
        mnuOutputGISSCaster = new javax.swing.JMenuItem();
        mnuOutputSize = new javax.swing.JMenu();
        mnuOutputSize1 = new javax.swing.JRadioButtonMenuItem();
        mnuOutputSize2 = new javax.swing.JRadioButtonMenuItem();
        mnuOutputSize3 = new javax.swing.JRadioButtonMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        mnuOutputSize4 = new javax.swing.JRadioButtonMenuItem();
        mnuOutputSize7 = new javax.swing.JRadioButtonMenuItem();
        mnuOutputSize5 = new javax.swing.JRadioButtonMenuItem();
        mnuOutputSize6 = new javax.swing.JRadioButtonMenuItem();
        mnuOutputFramerate = new javax.swing.JMenu();
        mnuOutput5FPS = new javax.swing.JRadioButtonMenuItem();
        mnuOutput10FPS = new javax.swing.JRadioButtonMenuItem();
        mnuOutput15FPS = new javax.swing.JRadioButtonMenuItem();
        mnuOutput20FPS = new javax.swing.JRadioButtonMenuItem();
        mnuOutput25FPS = new javax.swing.JRadioButtonMenuItem();
        mnuOutput30FPS = new javax.swing.JRadioButtonMenuItem();
        mnuchkShowBackground = new javax.swing.JCheckBoxMenuItem();
        mnuPixelFormat = new javax.swing.JMenu();
        mnurdPixelFormatRGB24 = new javax.swing.JRadioButtonMenuItem();
        mnurdPixelFormatUYVY = new javax.swing.JRadioButtonMenuItem();
        mnuOutputFlipImage = new javax.swing.JCheckBoxMenuItem();
        mnuOutputchkActivateStream = new javax.swing.JCheckBoxMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        mnuOutputLabelStreamPort = new javax.swing.JMenuItem();
        mnuAbout = new javax.swing.JMenu();
        mnuAboutItem = new javax.swing.JMenuItem();
        mnuGoToWebsite = new javax.swing.JMenuItem();
        mnuVideoInfo = new javax.swing.JMenuItem();
        mnuSetFlashPermissions = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        setTitle(bundle.getString("WEBCAMSTUDIO_FOR_GNU/LINUX")); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        panelStatus.setName("panelStatus"); // NOI18N
        panelStatus.setLayout(new java.awt.GridLayout(1, 2, 10, 0));

        pgCPUUsage.setToolTipText(bundle.getString("CPUUSAGE")); // NOI18N
        pgCPUUsage.setName("pgCPUUsage"); // NOI18N
        pgCPUUsage.setStringPainted(true);
        panelStatus.add(pgCPUUsage);

        cboVideoOutputs.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboVideoOutputs.setName("cboVideoOutputs"); // NOI18N
        cboVideoOutputs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboVideoOutputsActionPerformed(evt);
            }
        });
        panelStatus.add(cboVideoOutputs);

        getContentPane().add(panelStatus, java.awt.BorderLayout.SOUTH);

        panLayouts.setName("panLayouts"); // NOI18N
        panLayouts.setLayout(new java.awt.BorderLayout());
        getContentPane().add(panLayouts, java.awt.BorderLayout.CENTER);

        panBrowser.setName("panBrowser"); // NOI18N
        panBrowser.setLayout(new java.awt.BorderLayout());
        getContentPane().add(panBrowser, java.awt.BorderLayout.WEST);

        menuBar.setName("menuBar"); // NOI18N

        mnuStudios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/icon.png"))); // NOI18N
        mnuStudios.setText(bundle.getString("STUDIOS")); // NOI18N
        mnuStudios.setName("mnuStudios"); // NOI18N
        mnuStudios.setPreferredSize(new java.awt.Dimension(100, 23));

        mnuStudioNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        mnuStudioNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/document-new.png"))); // NOI18N
        mnuStudioNew.setText(bundle.getString("NEWSTUDIO")); // NOI18N
        mnuStudioNew.setName("mnuStudioNew"); // NOI18N
        mnuStudioNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuStudioNewActionPerformed(evt);
            }
        });
        mnuStudios.add(mnuStudioNew);

        mnuStudiosLoad.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        mnuStudiosLoad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/document-open.png"))); // NOI18N
        mnuStudiosLoad.setText(bundle.getString("LOAD")); // NOI18N
        mnuStudiosLoad.setName("mnuStudiosLoad"); // NOI18N
        mnuStudiosLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuStudiosLoadActionPerformed(evt);
            }
        });
        mnuStudios.add(mnuStudiosLoad);

        mnuStudioLoadLast.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        mnuStudioLoadLast.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/document-open.png"))); // NOI18N
        mnuStudioLoadLast.setText(bundle.getString("LOADLASTSTUDIO")); // NOI18N
        mnuStudioLoadLast.setName("mnuStudioLoadLast"); // NOI18N
        mnuStudioLoadLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuStudioLoadLastActionPerformed(evt);
            }
        });
        mnuStudios.add(mnuStudioLoadLast);

        mnuStudiosSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        mnuStudiosSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/document-save.png"))); // NOI18N
        mnuStudiosSave.setText(bundle.getString("SAVE")); // NOI18N
        mnuStudiosSave.setName("mnuStudiosSave"); // NOI18N
        mnuStudiosSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuStudiosSaveActionPerformed(evt);
            }
        });
        mnuStudios.add(mnuStudiosSave);

        jSeparator2.setName("jSeparator2"); // NOI18N
        mnuStudios.add(jSeparator2);

        mnuAnimationCreator.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        mnuAnimationCreator.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/user-info.png"))); // NOI18N
        mnuAnimationCreator.setText(bundle.getString("ANIMATION_CREATOR")); // NOI18N
        mnuAnimationCreator.setName("mnuAnimationCreator"); // NOI18N
        mnuAnimationCreator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAnimationCreatorActionPerformed(evt);
            }
        });
        mnuStudios.add(mnuAnimationCreator);

        menuBar.add(mnuStudios);

        mnuSources.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/video-display.png"))); // NOI18N
        mnuSources.setText(bundle.getString("SOURCES")); // NOI18N
        mnuSources.setName("mnuSources"); // NOI18N
        mnuSources.setPreferredSize(new java.awt.Dimension(100, 23));

        mnuSourceschkShowBrowser.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        mnuSourceschkShowBrowser.setSelected(true);
        mnuSourceschkShowBrowser.setText(bundle.getString("SHOW_BROWSER")); // NOI18N
        mnuSourceschkShowBrowser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/folder.png"))); // NOI18N
        mnuSourceschkShowBrowser.setName("mnuSourceschkShowBrowser"); // NOI18N
        mnuSourceschkShowBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSourceschkShowBrowserActionPerformed(evt);
            }
        });
        mnuSources.add(mnuSourceschkShowBrowser);

        mnuReloadSourceTree.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        mnuReloadSourceTree.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/folder.png"))); // NOI18N
        mnuReloadSourceTree.setText(bundle.getString("RELOAD")); // NOI18N
        mnuReloadSourceTree.setName("mnuReloadSourceTree"); // NOI18N
        mnuReloadSourceTree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuReloadSourceTreeActionPerformed(evt);
            }
        });
        mnuSources.add(mnuReloadSourceTree);

        mnuAddDir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_EQUALS, java.awt.event.InputEvent.CTRL_MASK));
        mnuAddDir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/folder.png"))); // NOI18N
        mnuAddDir.setText(bundle.getString("ADDDIR")); // NOI18N
        mnuAddDir.setName("mnuAddDir"); // NOI18N
        mnuAddDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAddDirActionPerformed(evt);
            }
        });
        mnuSources.add(mnuAddDir);

        mnuRemoveDir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_MINUS, java.awt.event.InputEvent.CTRL_MASK));
        mnuRemoveDir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/folder.png"))); // NOI18N
        mnuRemoveDir.setText(bundle.getString("REMOVEDIR")); // NOI18N
        mnuRemoveDir.setName("mnuRemoveDir"); // NOI18N
        mnuRemoveDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRemoveDirActionPerformed(evt);
            }
        });
        mnuSources.add(mnuRemoveDir);

        jSeparator1.setName("jSeparator1"); // NOI18N
        mnuSources.add(jSeparator1);

        mnuSourcesDesktop.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.ALT_MASK));
        mnuSourcesDesktop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/user-desktop.png"))); // NOI18N
        mnuSourcesDesktop.setText(bundle.getString("DESKTOP")); // NOI18N
        mnuSourcesDesktop.setName("mnuSourcesDesktop"); // NOI18N
        mnuSourcesDesktop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSourcesDesktopActionPerformed(evt);
            }
        });
        mnuSources.add(mnuSourcesDesktop);

        mnuSourcesFullDesktop.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.SHIFT_MASK));
        mnuSourcesFullDesktop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/user-desktop.png"))); // NOI18N
        mnuSourcesFullDesktop.setText(bundle.getString("FULLDESKTOP")); // NOI18N
        mnuSourcesFullDesktop.setName("mnuSourcesFullDesktop"); // NOI18N
        mnuSourcesFullDesktop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSourcesFullDesktopActionPerformed(evt);
            }
        });
        mnuSources.add(mnuSourcesFullDesktop);

        mnuSourcesDV.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.ALT_MASK));
        mnuSourcesDV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/camera-video.png"))); // NOI18N
        mnuSourcesDV.setText(bundle.getString("DV")); // NOI18N
        mnuSourcesDV.setName("mnuSourcesDV"); // NOI18N
        mnuSourcesDV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSourcesDVActionPerformed(evt);
            }
        });
        mnuSources.add(mnuSourcesDV);

        mnuSourcesImage.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.ALT_MASK));
        mnuSourcesImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/image-x-generic.png"))); // NOI18N
        mnuSourcesImage.setText(bundle.getString("IMAGE")); // NOI18N
        mnuSourcesImage.setName("mnuSourcesImage"); // NOI18N
        mnuSourcesImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSourcesImageActionPerformed(evt);
            }
        });
        mnuSources.add(mnuSourcesImage);

        mnuSourcesFreeText.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.ALT_MASK));
        mnuSourcesFreeText.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/accessories-text-editor.png"))); // NOI18N
        mnuSourcesFreeText.setText(bundle.getString("FREE_TEXT")); // NOI18N
        mnuSourcesFreeText.setName("mnuSourcesFreeText"); // NOI18N
        mnuSourcesFreeText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSourcesFreeTextActionPerformed(evt);
            }
        });
        mnuSources.add(mnuSourcesFreeText);

        mnuSourceIRC.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.ALT_MASK));
        mnuSourceIRC.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/internet-group-chat.png"))); // NOI18N
        mnuSourceIRC.setText(bundle.getString("IRC")); // NOI18N
        mnuSourceIRC.setName("mnuSourceIRC"); // NOI18N
        mnuSourceIRC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSourceIRCActionPerformed(evt);
            }
        });
        mnuSources.add(mnuSourceIRC);

        mnuSourcesAnimation.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.ALT_MASK));
        mnuSourcesAnimation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/user-info.png"))); // NOI18N
        mnuSourcesAnimation.setText(bundle.getString("ANIMATION")); // NOI18N
        mnuSourcesAnimation.setName("mnuSourcesAnimation"); // NOI18N
        mnuSourcesAnimation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSourcesAnimationActionPerformed(evt);
            }
        });
        mnuSources.add(mnuSourcesAnimation);

        mnuSourcesMovie.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.ALT_MASK));
        mnuSourcesMovie.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/video-display.png"))); // NOI18N
        mnuSourcesMovie.setText(bundle.getString("MOVIE")); // NOI18N
        mnuSourcesMovie.setName("mnuSourcesMovie"); // NOI18N
        mnuSourcesMovie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSourcesMovieActionPerformed(evt);
            }
        });
        mnuSources.add(mnuSourcesMovie);

        mnuSourcesStream.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.SHIFT_MASK));
        mnuSourcesStream.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/video-display.png"))); // NOI18N
        mnuSourcesStream.setText(bundle.getString("MOVIESTREAM")); // NOI18N
        mnuSourcesStream.setName("mnuSourcesStream"); // NOI18N
        mnuSourcesStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSourcesStreamActionPerformed(evt);
            }
        });
        mnuSources.add(mnuSourcesStream);

        mnuSourcesMovieViewer.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        mnuSourcesMovieViewer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/video-display.png"))); // NOI18N
        mnuSourcesMovieViewer.setText(bundle.getString("MOVIE_VIEWER")); // NOI18N
        mnuSourcesMovieViewer.setName("mnuSourcesMovieViewer"); // NOI18N
        mnuSourcesMovieViewer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSourcesMovieViewerActionPerformed(evt);
            }
        });
        mnuSources.add(mnuSourcesMovieViewer);

        mnuSourcesWidget.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.ALT_MASK));
        mnuSourcesWidget.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/image-x-generic.png"))); // NOI18N
        mnuSourcesWidget.setText(bundle.getString("WIDGET")); // NOI18N
        mnuSourcesWidget.setName("mnuSourcesWidget"); // NOI18N
        mnuSourcesWidget.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSourcesWidgetActionPerformed(evt);
            }
        });
        mnuSources.add(mnuSourcesWidget);

        mnuSourcesConsole.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK));
        mnuSourcesConsole.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/accessories-text-editor.png"))); // NOI18N
        mnuSourcesConsole.setText(bundle.getString("CONSOLE")); // NOI18N
        mnuSourcesConsole.setName("mnuSourcesConsole"); // NOI18N
        mnuSourcesConsole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSourcesConsoleActionPerformed(evt);
            }
        });
        mnuSources.add(mnuSourcesConsole);

        mnuSourcesQRCode.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/image-x-generic.png"))); // NOI18N
        mnuSourcesQRCode.setText(bundle.getString("QRCODE")); // NOI18N
        mnuSourcesQRCode.setName("mnuSourcesQRCode"); // NOI18N
        mnuSourcesQRCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSourcesQRCodeActionPerformed(evt);
            }
        });
        mnuSources.add(mnuSourcesQRCode);

        menuBar.add(mnuSources);

        mnuOutput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/camera-video.png"))); // NOI18N
        mnuOutput.setText(bundle.getString("OUTPUT")); // NOI18N
        mnuOutput.setName("mnuOutput"); // NOI18N
        mnuOutput.setPreferredSize(new java.awt.Dimension(100, 23));

        mnuVideoRecorder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-record.png"))); // NOI18N
        mnuVideoRecorder.setText(bundle.getString("VIDEO_RECORDER")); // NOI18N
        mnuVideoRecorder.setName("mnuVideoRecorder"); // NOI18N
        mnuVideoRecorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuVideoRecorderActionPerformed(evt);
            }
        });
        mnuOutput.add(mnuVideoRecorder);

        mnuOutputSpnashot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/camera-photo.png"))); // NOI18N
        mnuOutputSpnashot.setText(bundle.getString("SNAPSHOT")); // NOI18N
        mnuOutputSpnashot.setName("mnuOutputSpnashot"); // NOI18N
        mnuOutputSpnashot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputSpnashotActionPerformed(evt);
            }
        });
        mnuOutput.add(mnuOutputSpnashot);

        mnuOutputGISSCaster.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/gisstv2-16x16.png"))); // NOI18N
        mnuOutputGISSCaster.setText(bundle.getString("GISSCASTER")); // NOI18N
        mnuOutputGISSCaster.setName("mnuOutputGISSCaster"); // NOI18N
        mnuOutputGISSCaster.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputGISSCasterActionPerformed(evt);
            }
        });
        mnuOutput.add(mnuOutputGISSCaster);

        mnuOutputSize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/zoom-fit-best.png"))); // NOI18N
        mnuOutputSize.setText(bundle.getString("OUTPUT_SIZE")); // NOI18N
        mnuOutputSize.setName("mnuOutputSize"); // NOI18N

        grpOutputSize.add(mnuOutputSize1);
        mnuOutputSize1.setText("160x120");
        mnuOutputSize1.setName("mnuOutputSize1"); // NOI18N
        mnuOutputSize1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputSizeActionPerformed(evt);
            }
        });
        mnuOutputSize.add(mnuOutputSize1);

        grpOutputSize.add(mnuOutputSize2);
        mnuOutputSize2.setText("320x240");
        mnuOutputSize2.setName("mnuOutputSize2"); // NOI18N
        mnuOutputSize2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputSizeActionPerformed(evt);
            }
        });
        mnuOutputSize.add(mnuOutputSize2);

        grpOutputSize.add(mnuOutputSize3);
        mnuOutputSize3.setText("640x480");
        mnuOutputSize3.setName("mnuOutputSize3"); // NOI18N
        mnuOutputSize3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputSizeActionPerformed(evt);
            }
        });
        mnuOutputSize.add(mnuOutputSize3);

        jSeparator3.setName("jSeparator3"); // NOI18N
        mnuOutputSize.add(jSeparator3);

        grpOutputSize.add(mnuOutputSize4);
        mnuOutputSize4.setText("480x270");
        mnuOutputSize4.setName("mnuOutputSize4"); // NOI18N
        mnuOutputSize4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputSizeActionPerformed(evt);
            }
        });
        mnuOutputSize.add(mnuOutputSize4);

        grpOutputSize.add(mnuOutputSize7);
        mnuOutputSize7.setText("720x480");
        mnuOutputSize7.setName("mnuOutputSize7"); // NOI18N
        mnuOutputSize7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputSizeActionPerformed(evt);
            }
        });
        mnuOutputSize.add(mnuOutputSize7);

        grpOutputSize.add(mnuOutputSize5);
        mnuOutputSize5.setText("960x540");
        mnuOutputSize5.setName("mnuOutputSize5"); // NOI18N
        mnuOutputSize5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputSizeActionPerformed(evt);
            }
        });
        mnuOutputSize.add(mnuOutputSize5);

        grpOutputSize.add(mnuOutputSize6);
        mnuOutputSize6.setText("1920x1080");
        mnuOutputSize6.setName("mnuOutputSize6"); // NOI18N
        mnuOutputSize6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputSizeActionPerformed(evt);
            }
        });
        mnuOutputSize.add(mnuOutputSize6);

        mnuOutput.add(mnuOutputSize);

        mnuOutputFramerate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/video-display.png"))); // NOI18N
        mnuOutputFramerate.setText(bundle.getString("FRAMERATE")); // NOI18N
        mnuOutputFramerate.setName("mnuOutputFramerate"); // NOI18N

        grpFramerate.add(mnuOutput5FPS);
        mnuOutput5FPS.setText("5 fps");
        mnuOutput5FPS.setActionCommand("5");
        mnuOutput5FPS.setName("mnuOutput5FPS"); // NOI18N
        mnuOutput5FPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputFPSActionPerformed(evt);
            }
        });
        mnuOutputFramerate.add(mnuOutput5FPS);

        grpFramerate.add(mnuOutput10FPS);
        mnuOutput10FPS.setText("10 fps");
        mnuOutput10FPS.setActionCommand("10");
        mnuOutput10FPS.setName("mnuOutput10FPS"); // NOI18N
        mnuOutput10FPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputFPSActionPerformed(evt);
            }
        });
        mnuOutputFramerate.add(mnuOutput10FPS);

        grpFramerate.add(mnuOutput15FPS);
        mnuOutput15FPS.setSelected(true);
        mnuOutput15FPS.setText("15 fps");
        mnuOutput15FPS.setActionCommand("15");
        mnuOutput15FPS.setName("mnuOutput15FPS"); // NOI18N
        mnuOutput15FPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputFPSActionPerformed(evt);
            }
        });
        mnuOutputFramerate.add(mnuOutput15FPS);

        grpFramerate.add(mnuOutput20FPS);
        mnuOutput20FPS.setText("20 fps");
        mnuOutput20FPS.setActionCommand("20");
        mnuOutput20FPS.setName("mnuOutput20FPS"); // NOI18N
        mnuOutput20FPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputFPSActionPerformed(evt);
            }
        });
        mnuOutputFramerate.add(mnuOutput20FPS);

        grpFramerate.add(mnuOutput25FPS);
        mnuOutput25FPS.setText("25 fps");
        mnuOutput25FPS.setActionCommand("25");
        mnuOutput25FPS.setName("mnuOutput25FPS"); // NOI18N
        mnuOutput25FPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputFPSActionPerformed(evt);
            }
        });
        mnuOutputFramerate.add(mnuOutput25FPS);

        grpFramerate.add(mnuOutput30FPS);
        mnuOutput30FPS.setText("30 fps");
        mnuOutput30FPS.setActionCommand("30");
        mnuOutput30FPS.setName("mnuOutput30FPS"); // NOI18N
        mnuOutput30FPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputFPSActionPerformed(evt);
            }
        });
        mnuOutputFramerate.add(mnuOutput30FPS);

        mnuOutput.add(mnuOutputFramerate);

        mnuchkShowBackground.setSelected(true);
        mnuchkShowBackground.setText(bundle.getString("MAIN_SHOW_SPLASH")); // NOI18N
        mnuchkShowBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/icon.png"))); // NOI18N
        mnuchkShowBackground.setName("mnuchkShowBackground"); // NOI18N
        mnuchkShowBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuchkShowBackgroundActionPerformed(evt);
            }
        });
        mnuOutput.add(mnuchkShowBackground);

        mnuPixelFormat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/utilities-system-monitor.png"))); // NOI18N
        mnuPixelFormat.setText(bundle.getString("PIXEL_FORMAT")); // NOI18N
        mnuPixelFormat.setName("mnuPixelFormat"); // NOI18N

        grpPixelFormat.add(mnurdPixelFormatRGB24);
        mnurdPixelFormatRGB24.setText("RGB24");
        mnurdPixelFormatRGB24.setName("mnurdPixelFormatRGB24"); // NOI18N
        mnurdPixelFormatRGB24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnurdPixelFormatRGB24ActionPerformed(evt);
            }
        });
        mnuPixelFormat.add(mnurdPixelFormatRGB24);

        grpPixelFormat.add(mnurdPixelFormatUYVY);
        mnurdPixelFormatUYVY.setSelected(true);
        mnurdPixelFormatUYVY.setText("UYVY");
        mnurdPixelFormatUYVY.setName("mnurdPixelFormatUYVY"); // NOI18N
        mnurdPixelFormatUYVY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnurdPixelFormatUYVYActionPerformed(evt);
            }
        });
        mnuPixelFormat.add(mnurdPixelFormatUYVY);

        mnuOutput.add(mnuPixelFormat);

        mnuOutputFlipImage.setText(bundle.getString("FLIP_IMAGE")); // NOI18N
        mnuOutputFlipImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/image-x-generic.png"))); // NOI18N
        mnuOutputFlipImage.setName("mnuOutputFlipImage"); // NOI18N
        mnuOutputFlipImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputFlipImageActionPerformed(evt);
            }
        });
        mnuOutput.add(mnuOutputFlipImage);

        mnuOutputchkActivateStream.setText(bundle.getString("ACTIVATE_OUTPUT_STREAM")); // NOI18N
        mnuOutputchkActivateStream.setName("mnuOutputchkActivateStream"); // NOI18N
        mnuOutputchkActivateStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOutputchkActivateStreamActionPerformed(evt);
            }
        });
        mnuOutput.add(mnuOutputchkActivateStream);

        jSeparator4.setName("jSeparator4"); // NOI18N
        mnuOutput.add(jSeparator4);

        mnuOutputLabelStreamPort.setText("Stream Port:  Disabled"); // NOI18N
        mnuOutputLabelStreamPort.setEnabled(false);
        mnuOutputLabelStreamPort.setName("mnuOutputLabelStreamPort"); // NOI18N
        mnuOutput.add(mnuOutputLabelStreamPort);

        menuBar.add(mnuOutput);

        mnuAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/dialog-information.png"))); // NOI18N
        mnuAbout.setText(bundle.getString("ABOUT")); // NOI18N
        mnuAbout.setName("mnuAbout"); // NOI18N
        mnuAbout.setPreferredSize(new java.awt.Dimension(100, 23));

        mnuAboutItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        mnuAboutItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/dialog-information.png"))); // NOI18N
        mnuAboutItem.setText(bundle.getString("ABOUT")); // NOI18N
        mnuAboutItem.setName("mnuAboutItem"); // NOI18N
        mnuAboutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAboutItemActionPerformed(evt);
            }
        });
        mnuAbout.add(mnuAboutItem);

        mnuGoToWebsite.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.CTRL_MASK));
        mnuGoToWebsite.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/icon.png"))); // NOI18N
        mnuGoToWebsite.setText(bundle.getString("GOTOWEBSITE")); // NOI18N
        mnuGoToWebsite.setName("mnuGoToWebsite"); // NOI18N
        mnuGoToWebsite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuGoToWebsiteActionPerformed(evt);
            }
        });
        mnuAbout.add(mnuGoToWebsite);

        mnuVideoInfo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        mnuVideoInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/utilities-system-monitor.png"))); // NOI18N
        mnuVideoInfo.setText(bundle.getString("VIDEODEVICEINFO")); // NOI18N
        mnuVideoInfo.setName("mnuVideoInfo"); // NOI18N
        mnuVideoInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuVideoInfoActionPerformed(evt);
            }
        });
        mnuAbout.add(mnuVideoInfo);

        mnuSetFlashPermissions.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        mnuSetFlashPermissions.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/adobe_flash.png"))); // NOI18N
        mnuSetFlashPermissions.setText(bundle.getString("SET_FLASH_PERMISSIONS")); // NOI18N
        mnuSetFlashPermissions.setName("mnuSetFlashPermissions"); // NOI18N
        mnuSetFlashPermissions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSetFlashPermissionsActionPerformed(evt);
            }
        });
        mnuAbout.add(mnuSetFlashPermissions);

        menuBar.add(mnuAbout);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mnuSourcesDesktopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSourcesDesktopActionPerformed
        VideoSourceDesktop s = new VideoSourceDesktop();
        addSourceToDesktop(s);
    }//GEN-LAST:event_mnuSourcesDesktopActionPerformed

    private void mnuSourcesImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSourcesImageActionPerformed
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser(lastFolder);
        chooser.setToolTipText(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("SELECT_YOUR_IMAGE_FILE"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        javax.swing.filechooser.FileFilter filter = null;
        chooser.setMultiSelectionEnabled(false);
        filter = new javax.swing.filechooser.FileNameExtensionFilter(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("IMAGE"), "JPG", "jpg", "JPEG", "jpeg", "PNG", "png", "GIF", "gif");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(filter);
        String txtLocation = "";
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File[] f = chooser.getSelectedFiles();
            String temp = "";
            if (f != null && f.length > 0) {
                for (int i = 0; i < f.length; i++) {
                    temp += (f[i].getAbsolutePath()) + java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString(",");
                    lastFolder = f[i].getParentFile();
                }
                temp = temp.substring(0, temp.length() - 1);
                txtLocation = temp;
            } else {
                java.io.File file = chooser.getSelectedFile();
                if (file != null) {
                    txtLocation = file.getAbsolutePath();
                    lastFolder = file.getParentFile();
                }
            }
            VideoSourceImage source = null;

            if (txtLocation.toLowerCase().indexOf(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("HTTP:/")) != -1) {
                try {
                    txtLocation = txtLocation.substring(txtLocation.toLowerCase().indexOf(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("HTTP:/")));
                    txtLocation = txtLocation.replaceFirst(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString(":"), java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString(":/"));
                    source = new VideoSourceImage(new java.net.URL(txtLocation));
                } catch (Exception e) {
                    System.out.println(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("EXCEPTION_ON_IMAGE_URL_-_") + e.getMessage());
                    source = null;
                }
            } else {
                String[] files = txtLocation.split(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString(","));
                java.io.File[] oFiles = new java.io.File[files.length];
                for (int i = 0; i < files.length; i++) {
                    oFiles[i] = new java.io.File(files[i]);
                }
                source = new VideoSourceImage(oFiles);
                oFiles = null;
                files = null;
            }
            if (source != null) {
                addSourceToDesktop(source);
            }
        }

    }//GEN-LAST:event_mnuSourcesImageActionPerformed

    private void mnuSourcesFreeTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSourcesFreeTextActionPerformed

        VideoSourceText s = new VideoSourceText("");

        addSourceToDesktop(s);
    }//GEN-LAST:event_mnuSourcesFreeTextActionPerformed

    private void mnuSourceIRCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSourceIRCActionPerformed

        VideoSourceIRC s = new VideoSourceIRC();

        addSourceToDesktop(s);
    }//GEN-LAST:event_mnuSourceIRCActionPerformed

    private void mnuSourcesAnimationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSourcesAnimationActionPerformed
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser(lastFolder);
        chooser.setToolTipText(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("SELECT_YOUR_ANIMATION_FILE"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        javax.swing.filechooser.FileFilter filter = null;
        chooser.setMultiSelectionEnabled(false);
        filter = new javax.swing.filechooser.FileNameExtensionFilter(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("ANIMATION"), "ANM", "anm");
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(filter);
        String txtLocation = "";

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            java.io.File file = chooser.getSelectedFile();
            if (file != null) {
                txtLocation = file.getAbsolutePath();
                lastFolder = file.getParentFile();
            }
            VideoSourceAnimation source = null;

            if (txtLocation.toLowerCase().startsWith(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("HTTP"))) {
                try {
                    source = new VideoSourceAnimation(new java.net.URL(txtLocation));
                } catch (Exception e) {
                    System.out.println(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("EXCEPTION_ON_ANIMATION_URL_-_") + e.getMessage());
                    source = null;
                }
            } else {
                source = new VideoSourceAnimation(file);
            }
            if (source != null) {

                addSourceToDesktop(source);
            }
        }

}//GEN-LAST:event_mnuSourcesAnimationActionPerformed

    private void mnuStudiosSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuStudiosSaveActionPerformed
        saveStudio(null);
    }//GEN-LAST:event_mnuStudiosSaveActionPerformed

    private void mnuStudiosLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuStudiosLoadActionPerformed
        loadStudioFromFile(null);
    }//GEN-LAST:event_mnuStudiosLoadActionPerformed

    private void mnuSourcesMovieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSourcesMovieActionPerformed
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser(lastFolder);
        chooser.setToolTipText(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("SELECT_YOUR_MOVIE_FILE"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        javax.swing.filechooser.FileFilter filter = null;
        chooser.setMultiSelectionEnabled(false);
        filter = new javax.swing.filechooser.FileNameExtensionFilter(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("MOVIE"), "AVI", "avi", "OGG", "ogg", "MOV", "mov", "MPG", "mpg", "FLV", "flv", "MP4", "mp4", "VOB", "vob", "OGV", "ogv", "rmvb");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(filter);
        String txtLocation = "";
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File[] f = chooser.getSelectedFiles();
            String temp = "";
            if (f != null && f.length > 0) {
                for (int i = 0; i < f.length; i++) {
                    temp += (f[i].getAbsolutePath()) + java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString(",");
                    lastFolder = f[i].getParentFile();
                }
                temp = temp.substring(0, temp.length() - 1);
                txtLocation = temp;
            } else {
                java.io.File file = chooser.getSelectedFile();
                if (file != null) {
                    txtLocation = file.getAbsolutePath();
                    lastFolder = file.getParentFile();
                }
            }
            VideoSourceMovie source = null;

            if (txtLocation.toLowerCase().indexOf(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("HTTP:/")) != -1) {
                try {
                    txtLocation = txtLocation.substring(txtLocation.toLowerCase().indexOf(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("HTTP:/")));
                    txtLocation = txtLocation.replaceFirst(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString(":"), java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString(":/"));
                    source = new VideoSourceMovie(new java.net.URL(txtLocation));
                } catch (Exception e) {
                    System.out.println(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("EXCEPTION_ON_MOVIE_URL_-_") + e.getMessage());
                    source = null;
                }
            } else {
                String[] files = txtLocation.split(",");
                java.io.File[] oFiles = new java.io.File[files.length];
                for (int i = 0; i < files.length; i++) {
                    oFiles[i] = new java.io.File(files[i]);
                }
                source = new VideoSourceMovie(oFiles[0]);
                oFiles = null;
                files = null;
            }
            if (source != null) {

                addSourceToDesktop(source);
            }
        }


    }//GEN-LAST:event_mnuSourcesMovieActionPerformed

    private void mnuAboutItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAboutItemActionPerformed
        webcamstudio.About about = new webcamstudio.About(this, true);
        about.pack();
        about.setLocationRelativeTo(this);
        about.setVisible(true);
    }//GEN-LAST:event_mnuAboutItemActionPerformed

    private void mnuGoToWebsiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuGoToWebsiteActionPerformed
        try {
            java.awt.Desktop.getDesktop().browse(URI.create("http://www.ws4gl.org"));
        } catch (IOException ex) {
            webcamstudio.components.Message msg = new webcamstudio.components.Message(this, true);
            msg.pack();
            msg.setLocationRelativeTo(this);
            msg.setMessage(ex.getMessage());
            msg.setVisible(true);
        }
    }//GEN-LAST:event_mnuGoToWebsiteActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        layoutManager.quitting();
        monitor.stopMe();
        savePrefs();
    }//GEN-LAST:event_formWindowClosing

    private void mnuStudioLoadLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuStudioLoadLastActionPerformed
        loadStudioFromFile(lastStudioFile);
    }//GEN-LAST:event_mnuStudioLoadLastActionPerformed

    private void mnuVideoRecorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuVideoRecorderActionPerformed
        VideoRecorder recorder = new VideoRecorder(mixer, this, false);
        recorder.pack();
        recorder.setLocationRelativeTo(this);
        recorder.setVisible(true);
    }//GEN-LAST:event_mnuVideoRecorderActionPerformed

    private void mnuOutputSpnashotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOutputSpnashotActionPerformed

        BufferedImage imgOut = new BufferedImage(outputWidth, outputHeight, java.awt.image.BufferedImage.TRANSLUCENT);
        imgOut.getGraphics().drawImage(mixer.getImage(), 0, 0, null);
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser(lastFolder);
        chooser.setToolTipText(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("SELECT_YOUR_IMAGE_FILE"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        javax.swing.filechooser.FileFilter filter = null;
        chooser.setMultiSelectionEnabled(false);
        filter = new javax.swing.filechooser.FileNameExtensionFilter(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("IMAGE"), "PNG", "png");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(filter);
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File f = chooser.getSelectedFile();
            if (f != null) {
                try {
                    javax.imageio.ImageIO.write(imgOut, "PNG", f);
                    lastFolder = f.getParentFile();
                } catch (IOException ex) {
                    error(ex.getMessage());
                }
            }
        }

    }//GEN-LAST:event_mnuOutputSpnashotActionPerformed

    private void mnuVideoInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuVideoInfoActionPerformed
        VideoDeviceInfo d = new VideoDeviceInfo(this, true);
        d.setLocationRelativeTo(this);
        d.pack();
        d.setVisible(true);
    }//GEN-LAST:event_mnuVideoInfoActionPerformed

    private void mnuSetFlashPermissionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSetFlashPermissionsActionPerformed
        try {
            java.awt.Desktop.getDesktop().browse(URI.create("http://www.macromedia.com/support/documentation/en/flashplayer/help/settings_manager06.html"));
        } catch (IOException ex) {
            webcamstudio.components.Message msg = new webcamstudio.components.Message(this, true);
            msg.pack();
            msg.setLocationRelativeTo(this);
            msg.setMessage(ex.getMessage());
            msg.setVisible(true);
        }
    }//GEN-LAST:event_mnuSetFlashPermissionsActionPerformed

    private void mnuSourcesStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSourcesStreamActionPerformed
        OpenURL url = new OpenURL(this, true);
        url.pack();
        url.setLocationRelativeTo(this);
        url.setIconImage(((ImageIcon) mnuSourcesStream.getIcon()).getImage());
        url.setVisible(true);
        String location = url.getURL();
        if (location.trim().length() != 0) {
            VideoSourceMovie source = new VideoSourceMovie(location);

            addSourceToDesktop(source);
        }
    }//GEN-LAST:event_mnuSourcesStreamActionPerformed

    private void mnuSourcesMovieViewerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSourcesMovieViewerActionPerformed
        MovieViewer viewer = new MovieViewer(this, false);
        viewer.setLocationRelativeTo(this);
        viewer.setIconImage(((ImageIcon) mnuSourcesMovieViewer.getIcon()).getImage());
        viewer.setVisible(true);
    }//GEN-LAST:event_mnuSourcesMovieViewerActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
    }//GEN-LAST:event_formWindowActivated

    private void mnuOutputSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOutputSizeActionPerformed
        String outputSize = evt.getActionCommand();
        outputWidth = new Integer(outputSize.split("x")[0]);
        outputHeight = new Integer(outputSize.split("x")[1]);
        if (output != null) {
            output.close();
            if (mnurdPixelFormatRGB24.isSelected()) {
                output.open(output.getDevice(), outputWidth, outputHeight, V4L2Loopback.RGB24);
            } else {
                output.open(output.getDevice(), outputWidth, outputHeight, V4L2Loopback.UYVY);
            }
            mixer.setOutput(output);
        }
        mixer.setSize(outputWidth, outputHeight);



    }//GEN-LAST:event_mnuOutputSizeActionPerformed

    private void mnuAnimationCreatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAnimationCreatorActionPerformed
        AnimationCreator anm = new AnimationCreator(this, false);
        anm.pack();
        anm.setLocationRelativeTo(this);
        anm.setVisible(true);
    }//GEN-LAST:event_mnuAnimationCreatorActionPerformed

    private void mnuSourcesDVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSourcesDVActionPerformed
        VideoSourceDV s = new VideoSourceDV();

        addSourceToDesktop(s);
    }//GEN-LAST:event_mnuSourcesDVActionPerformed

    private void mnuOutputFPSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOutputFPSActionPerformed
        mixer.setFramerate(new Integer(evt.getActionCommand()));
    }//GEN-LAST:event_mnuOutputFPSActionPerformed

    private void mnuOutputGISSCasterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOutputGISSCasterActionPerformed
        GISScaster g = new GISScaster(mixer, this, false);
        g.pack();
        g.setLocationRelativeTo(this);
        g.setVisible(true);

    }//GEN-LAST:event_mnuOutputGISSCasterActionPerformed

    private void mnuSourcesWidgetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSourcesWidgetActionPerformed
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser(lastFolder);
        chooser.setToolTipText(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("SELECT_YOUR_WIDGET_FILE"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        javax.swing.filechooser.FileFilter filter = null;
        filter = new javax.swing.filechooser.FileNameExtensionFilter("Widget", "xml", "XML");
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File f = chooser.getSelectedFile();
            VideoSourceWidget source = null;
            try {
                source = new VideoSourceWidget(f.toURI().toURL());
                lastFolder = f.getParentFile();
            } catch (MalformedURLException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

            addSourceToDesktop(source);
        }
    }//GEN-LAST:event_mnuSourcesWidgetActionPerformed

    private void mnuStudioNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuStudioNewActionPerformed
    }//GEN-LAST:event_mnuStudioNewActionPerformed

    private void mnuchkShowBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuchkShowBackgroundActionPerformed
        if (mnuchkShowBackground.isSelected()) {
            Image img = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/webcamstudio/resources/splash.jpg"));
            mixer.setBackground(img);
        } else {
            mixer.setBackground(null);
        }
    }//GEN-LAST:event_mnuchkShowBackgroundActionPerformed

    private void mnuAddDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAddDirActionPerformed
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser(".");
        fc.setMultiSelectionEnabled(false);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.showOpenDialog(this);
        if (fc.getSelectedFile() != null) {
            dirToScan.add(fc.getSelectedFile().getAbsolutePath());
            initSourceDir();

        }
    }//GEN-LAST:event_mnuAddDirActionPerformed

    private void mnuRemoveDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRemoveDirActionPerformed
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser(".");
        fc.setMultiSelectionEnabled(false);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.showOpenDialog(this);
        if (fc.getSelectedFile() != null) {
            dirToScan.remove(fc.getSelectedFile().getAbsolutePath());
            initSourceDir();

        }
}//GEN-LAST:event_mnuRemoveDirActionPerformed

    private void mnuReloadSourceTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuReloadSourceTreeActionPerformed
        initSourceDir();
    }//GEN-LAST:event_mnuReloadSourceTreeActionPerformed

    private void mnurdPixelFormatRGB24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnurdPixelFormatRGB24ActionPerformed
        if (output != null) {
            output.close();
            if (mnurdPixelFormatRGB24.isSelected()) {
                mnuOutputSize.setEnabled(true);
                output.open(output.getDevice(), outputWidth, outputHeight, V4L2Loopback.RGB24);
            } else {
                mnuOutputSize.setEnabled(false);
                output.open(output.getDevice(), outputWidth, outputHeight, V4L2Loopback.UYVY);
            }
        }
        mixer.setSize(outputWidth, outputHeight);
    }//GEN-LAST:event_mnurdPixelFormatRGB24ActionPerformed

    private void mnurdPixelFormatUYVYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnurdPixelFormatUYVYActionPerformed
        if (output != null) {
            output.close();

            if (mnurdPixelFormatRGB24.isSelected()) {
                output.open(output.getDevice(), outputWidth, outputHeight, V4L2Loopback.RGB24);
                mnuOutputSize.setEnabled(true);
            } else {
                mnuOutputSize3.setSelected(true);
                outputWidth = 640;
                outputHeight = 480;
                output.open(output.getDevice(), outputWidth, outputHeight, V4L2Loopback.UYVY);
                mnuOutputSize.setEnabled(false);
            }
        }
        mixer.setSize(outputWidth, outputHeight);
    }//GEN-LAST:event_mnurdPixelFormatUYVYActionPerformed

    private void mnuOutputFlipImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOutputFlipImageActionPerformed
        if (output != null) {
            output.setFlipImage(mnuOutputFlipImage.isSelected());
        }
    }//GEN-LAST:event_mnuOutputFlipImageActionPerformed

    private void mnuSourcesConsoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSourcesConsoleActionPerformed
        VideoSourceConsole s = new VideoSourceConsole("cal");
        addSourceToDesktop(s);
    }//GEN-LAST:event_mnuSourcesConsoleActionPerformed

    private void cboVideoOutputsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboVideoOutputsActionPerformed
        VideoDevice vd = (VideoDevice) cboVideoOutputs.getSelectedItem();
        if (vd != null) {
            selectOutputDevice(vd);
        }
    }//GEN-LAST:event_cboVideoOutputsActionPerformed

    private void mnuSourceschkShowBrowserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSourceschkShowBrowserActionPerformed
        panBrowser.setVisible(mnuSourceschkShowBrowser.isSelected());
    }//GEN-LAST:event_mnuSourceschkShowBrowserActionPerformed

    private void mnuOutputchkActivateStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOutputchkActivateStreamActionPerformed
        if (mnuOutputchkActivateStream.isSelected()) {
            mnuOutputLabelStreamPort.setText("Stream Port : " + outputStreamPort);
            mixer.activateStream(true, outputStreamPort);
        } else {
            mnuOutputLabelStreamPort.setText("Stream Port : Disabled");
            mixer.stopStream();
        }
    }//GEN-LAST:event_mnuOutputchkActivateStreamActionPerformed

    private void mnuSourcesQRCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSourcesQRCodeActionPerformed
        VideoSourceQRCode source = new VideoSourceQRCode("WebcamStudio");
        addSourceToDesktop(source);

    }//GEN-LAST:event_mnuSourcesQRCodeActionPerformed

    private void mnuSourcesFullDesktopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSourcesFullDesktopActionPerformed
        addSourceToDesktop(new VideoSourceFullDesktop());
    }//GEN-LAST:event_mnuSourcesFullDesktopActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        boolean nogui = false;
        boolean xmode = false;
        String studio = null;
        for (String arg : args) {
            if (arg.equals("-native")) {
                try {
                    // Set System L&F
                    UIManager.setLookAndFeel(
                            UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    // handle exception
                }
            }
            if (arg.endsWith(".studio")) {
                // Loading studio file
                studio = arg;
            }
            if (arg.equals("-nogui")) {
                nogui = true;
            }
            if (arg.equals("-xmode")) {
                xmode = true;
            }
        }
        Main.XMODE = xmode;
        Main m = new Main();
        if (studio != null) {
            m.loadStudioFromFile(new File(studio));
        }
        m.setVisible(!nogui);
        if (SplashScreen.getSplashScreen() != null) {
            SplashScreen.getSplashScreen().close();
        }
        if (nogui) {
            System.out.println("WebcamStudio For GNU/Linux " + Version.version + "(Build: " + new Version().getBuild() + ")");
            System.out.println("");
            System.out.println("Running in console mode... (CTRL-C to terminate)");

        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboVideoOutputs;
    private javax.swing.ButtonGroup grpFramerate;
    private javax.swing.ButtonGroup grpOutputSize;
    private javax.swing.ButtonGroup grpPixelFormat;
    private javax.swing.ButtonGroup grpQuality;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu mnuAbout;
    private javax.swing.JMenuItem mnuAboutItem;
    private javax.swing.JMenuItem mnuAddDir;
    private javax.swing.JMenuItem mnuAnimationCreator;
    private javax.swing.JMenuItem mnuGoToWebsite;
    private javax.swing.JMenu mnuOutput;
    private javax.swing.JRadioButtonMenuItem mnuOutput10FPS;
    private javax.swing.JRadioButtonMenuItem mnuOutput15FPS;
    private javax.swing.JRadioButtonMenuItem mnuOutput20FPS;
    private javax.swing.JRadioButtonMenuItem mnuOutput25FPS;
    private javax.swing.JRadioButtonMenuItem mnuOutput30FPS;
    private javax.swing.JRadioButtonMenuItem mnuOutput5FPS;
    private javax.swing.JCheckBoxMenuItem mnuOutputFlipImage;
    private javax.swing.JMenu mnuOutputFramerate;
    private javax.swing.JMenuItem mnuOutputGISSCaster;
    private javax.swing.JMenuItem mnuOutputLabelStreamPort;
    private javax.swing.JMenu mnuOutputSize;
    private javax.swing.JRadioButtonMenuItem mnuOutputSize1;
    private javax.swing.JRadioButtonMenuItem mnuOutputSize2;
    private javax.swing.JRadioButtonMenuItem mnuOutputSize3;
    private javax.swing.JRadioButtonMenuItem mnuOutputSize4;
    private javax.swing.JRadioButtonMenuItem mnuOutputSize5;
    private javax.swing.JRadioButtonMenuItem mnuOutputSize6;
    private javax.swing.JRadioButtonMenuItem mnuOutputSize7;
    private javax.swing.JMenuItem mnuOutputSpnashot;
    private javax.swing.JCheckBoxMenuItem mnuOutputchkActivateStream;
    private javax.swing.JMenu mnuPixelFormat;
    private javax.swing.JMenuItem mnuReloadSourceTree;
    private javax.swing.JMenuItem mnuRemoveDir;
    private javax.swing.JMenuItem mnuSetFlashPermissions;
    private javax.swing.JMenuItem mnuSourceIRC;
    private javax.swing.JMenu mnuSources;
    private javax.swing.JMenuItem mnuSourcesAnimation;
    private javax.swing.JMenuItem mnuSourcesConsole;
    private javax.swing.JMenuItem mnuSourcesDV;
    private javax.swing.JMenuItem mnuSourcesDesktop;
    private javax.swing.JMenuItem mnuSourcesFreeText;
    private javax.swing.JMenuItem mnuSourcesFullDesktop;
    private javax.swing.JMenuItem mnuSourcesImage;
    private javax.swing.JMenuItem mnuSourcesMovie;
    private javax.swing.JMenuItem mnuSourcesMovieViewer;
    private javax.swing.JMenuItem mnuSourcesQRCode;
    private javax.swing.JMenuItem mnuSourcesStream;
    private javax.swing.JMenuItem mnuSourcesWidget;
    private javax.swing.JCheckBoxMenuItem mnuSourceschkShowBrowser;
    private javax.swing.JMenuItem mnuStudioLoadLast;
    private javax.swing.JMenuItem mnuStudioNew;
    private javax.swing.JMenu mnuStudios;
    private javax.swing.JMenuItem mnuStudiosLoad;
    private javax.swing.JMenuItem mnuStudiosSave;
    private javax.swing.JMenuItem mnuVideoInfo;
    private javax.swing.JMenuItem mnuVideoRecorder;
    private javax.swing.JCheckBoxMenuItem mnuchkShowBackground;
    private javax.swing.JRadioButtonMenuItem mnurdPixelFormatRGB24;
    private javax.swing.JRadioButtonMenuItem mnurdPixelFormatUYVY;
    private javax.swing.JPanel panBrowser;
    private javax.swing.JPanel panLayouts;
    private javax.swing.JPanel panelStatus;
    private javax.swing.JProgressBar pgCPUUsage;
    // End of variables declaration//GEN-END:variables

    @Override
    public void info(String info) {
        System.out.println(info);
    }

    @Override
    public void error(String message) {
        System.out.println(message);
    }

    @Override
    public void newTextLine(String line) {
        System.out.println(line);
    }

    public void addSource(VideoSource source) {
        if (source != null) {
            addSourceToDesktop(source);
        }
    }

    @Override
    public void sourceUpdate(VideoSource source) {
    }

    @Override
    public void sourceSetX(VideoSource source, int x) {
    }

    @Override
    public void sourceSetY(VideoSource source, int y) {
    }

    @Override
    public void sourceSetWidth(VideoSource source, int w) {
    }

    @Override
    public void sourceSetHeight(VideoSource source, int h) {
    }

    @Override
    public void sourceMoveUp(VideoSource source) {
    }

    @Override
    public void sourceMoveDown(VideoSource source) {
    }

    @Override
    public void sourceSetTransIn(VideoSource source, Transition in) {
    }

    @Override
    public void sourceSetTransOut(VideoSource source, Transition out) {
    }

    @Override
    public void sourceRemoved(VideoSource source) {
    }
}
