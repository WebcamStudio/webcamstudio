/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * StreamDesktop.java
 *
 * Created on 15-Apr-2012, 12:29:14 AM
 */
package webcamstudio.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import static webcamstudio.WebcamStudio.wsDistroWatch;
import webcamstudio.mixers.MasterFrameBuilder;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.mixers.PreviewFrameBuilder;
import webcamstudio.streams.SourceAudioSource;
import webcamstudio.streams.SourceCustom;
import webcamstudio.streams.SourceDV;
import webcamstudio.streams.SourceDVB;
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
import webcamstudio.util.AudioSource;
import webcamstudio.util.FindFires;
import webcamstudio.util.FireDevices;
import webcamstudio.util.PaCTL;
import webcamstudio.util.Screen;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class StreamDesktop extends javax.swing.JInternalFrame {

    StreamPanel panel = null;
    StreamPanelDVB panelDVB = null;
    StreamPanelURL panelURL = null;
    StreamPanelIPCam panelIPCam = null;
    StreamPanelText panelText = null;
    Stream stream = null;
    Listener listener = null;
    private boolean runMe = true;
    private int speed = 5; // - is faster + is slower
    AudioSource[] sourcesAudio;
    FireDevices[] fireDevices;
    String distro = wsDistroWatch();

    public interface Listener{
        public void selectedSource(Stream source);
        public void closeSource();
    }
    /** Creates new form StreamDesktop
     * @param s
     * @param l */
    public StreamDesktop(final Stream s,Listener l) {
        listener = l;
        stream = s;
        initComponents();
        
        jCBAVConv.setVisible(Screen.avconvDetected());
        jCBFFmpeg.setVisible(Screen.ffmpegDetected());
        if (s instanceof SourceText) {
            StreamPanelText p = new StreamPanelText((Stream)s);
            this.setLayout(new BorderLayout());
            this.add(p, BorderLayout.CENTER);
            this.setTitle(s.getName());
            this.setVisible(true);
            jMControls.setVisible(false);
            jMBackEnd.setVisible(false);
            jMAudioSource.setVisible(false);
            jMFireDevice.setVisible(false);
            jMLoop.setVisible(false);
            panelText = p;
            s.setPanelType("PanelText");
        } else if (s instanceof SourceDVB) {
            StreamPanelDVB p = new StreamPanelDVB(s);
            this.setLayout(new BorderLayout());
            this.add(p, BorderLayout.CENTER);
            this.setTitle(s.getName());
            this.setVisible(true);
            jMBackEnd.setVisible(false);
            jMIPCBrand.setVisible(false);
            jMAudioSource.setVisible(false);
            jMFireDevice.setVisible(false);
            jMLoop.setVisible(false);
            stream.setComm("GS");
            panelDVB = p;
            s.setPanelType("PanelDVB");
        } else if (s instanceof SourceDV) {
            final ArrayList<JCheckBoxMenuItem> aSMenuItem = new ArrayList<>();
            try {
                fireDevices = FindFires.getSources();
            } catch (IOException ex) {
                Logger.getLogger(StreamDesktop.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (FireDevices fireDevice : fireDevices){
                final JCheckBoxMenuItem jCBMenuItem = new JCheckBoxMenuItem();
                jCBMenuItem.setText(fireDevice.description);
                jCBMenuItem.setName(fireDevice.guid);
//                System.out.println("Desktop Name: "+fireDevice.description);
//                System.out.println("Desktop Guid: "+fireDevice.guid);
                jCBMenuItem.addActionListener(new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        s.setGuid(jCBMenuItem.getName());
                        for (JCheckBoxMenuItem jCb : aSMenuItem) {
                            if (!jCb.getName().equals(s.getGuid())) {
                                jCb.setSelected(false);
                            }
                        }
                    }
                });
                jMFireDevice.add(jCBMenuItem);
                aSMenuItem.add(jCBMenuItem);
                if (s.getLoaded()){
                    for (JCheckBoxMenuItem jCb : aSMenuItem) {
                        if (jCb.getName().equals(s.getGuid())) {
                            jCb.setSelected(true);
                        }
                    }
                } else {
                    JCheckBoxMenuItem initJCb = aSMenuItem.get(0);
                    initJCb.setSelected(true);
                    s.setGuid(initJCb.getName());
                }
            }
            StreamPanel p = new StreamPanel(s);
            this.setLayout(new BorderLayout());
            this.add(p, BorderLayout.CENTER);
            this.setTitle(s.getName());
            this.setVisible(true);
            jMBackEnd.setVisible(false);
            jMIPCBrand.setVisible(false);
            jMAudioSource.setVisible(false);
            jMFireDevice.setVisible(true);
            jMLoop.setVisible(false);
            stream.setComm("GS");
            panel = p;
            s.setPanelType("Panel");
        } else if (s instanceof SourceURL) {
            StreamPanelURL p = new StreamPanelURL(s);
            this.setLayout(new BorderLayout());
            this.add(p, BorderLayout.CENTER);
            this.setTitle(s.getName());
            this.setVisible(true);
            if (stream.getLoaded()){
                switch (stream.getComm()) {
                    case "AV":
                        jCBAVConv.setSelected(true);
                        stream.setComm("AV");
                        jCBGStreamer.setSelected(false);
                        jCBFFmpeg.setSelected(false);
                        break;
                    case "GS":
                        jCBGStreamer.setSelected(true);
                        stream.setComm("GS");
                        jCBAVConv.setSelected(false);
                        jCBFFmpeg.setSelected(false);
                        break;
                    case "FF":
                        jCBFFmpeg.setSelected(true);
                        stream.setComm("FF");                        
                        jCBAVConv.setSelected(false);
                        jCBGStreamer.setSelected(false);
                        break;
                }
            } else {
                if (distro.toLowerCase().equals("ubuntu")){
                    jCBAVConv.setSelected(true);
                    stream.setComm("AV");
                    jCBGStreamer.setSelected(false);
                    jCBFFmpeg.setSelected(false);
                } else {
                    jCBAVConv.setSelected(false);
                    stream.setComm("FF");
                    stream.setBackFF(true);
                    jCBGStreamer.setSelected(false);
                    jCBFFmpeg.setSelected(true);
                }
            }
            jMBackEnd.setEnabled(true);
            panelURL = p;
            s.setPanelType("PanelURL");
            jCBShowSliders.setVisible(false);
            jMIPCBrand.setVisible(false);
            jMAudioSource.setVisible(false);
            jMFireDevice.setVisible(false);
            jMLoop.setVisible(true);
            jCBLoop.setSelected(s.getLoop());
        } else if (s instanceof SourceIPCam) {
            StreamPanelIPCam p = new StreamPanelIPCam(s);
            this.setLayout(new BorderLayout());
            this.add(p, BorderLayout.CENTER);
            this.setTitle(s.getName());
            this.setVisible(true);
            if (stream.getLoaded()){
                switch (stream.getComm()) {
                    case "AV":
                        jCBAVConv.setSelected(true);
                        stream.setComm("AV");
                        jCBGStreamer.setSelected(false);
                        jCBFFmpeg.setSelected(false);
                        break;
                    case "GS":
                        jCBGStreamer.setSelected(true);
                        stream.setComm("GS");
                        jCBAVConv.setSelected(false);
                        jCBFFmpeg.setSelected(false);
                        break;
                    case "FF":
                        jCBFFmpeg.setSelected(true);
                        stream.setComm("FF");
                        stream.setBackFF(true);
                        jCBAVConv.setSelected(false);
                        jCBGStreamer.setSelected(false);
                        break;
                }
                switch (stream.getPtzBrand()) {
                    case "foscam":
                        jCBoxFosCamPtz.setSelected(true);
                        stream.setPtzBrand("foscam");
                        jCBoxAxisPtz.setSelected(false);
                        jCBoxWansCamPtz.setSelected(false);
                        break;
                    case "axis":
                        jCBoxAxisPtz.setSelected(true);
                        stream.setPtzBrand("axis");
                        jCBoxFosCamPtz.setSelected(false);
                        jCBoxWansCamPtz.setSelected(false);
                        break;
                    case "wanscam":
                        jCBoxWansCamPtz.setSelected(true);
                        stream.setPtzBrand("wanscam");
                        jCBoxFosCamPtz.setSelected(false);
                        jCBoxAxisPtz.setSelected(false);
                        break;
                }     
            } else {
                    jCBAVConv.setSelected(false);
                    jCBFFmpeg.setSelected(false);
                    stream.setComm("GS");
                    jCBGStreamer.setSelected(true);
                    jCBoxFosCamPtz.setSelected(true);
                    stream.setPtzBrand("foscam");
                    jCBoxAxisPtz.setSelected(false);
            }
            jMBackEnd.setVisible(false);
            panelIPCam = p;
            s.setPanelType("PanelIPCam");
            jCBShowSliders.setVisible(false);
            jMAudioSource.setVisible(false);
            jMFireDevice.setVisible(false);
            jMLoop.setVisible(false);
        } else {
            
            if (s instanceof SourceAudioSource) {
                final ArrayList<JCheckBoxMenuItem> aSMenuItem = new ArrayList<>();
                try {
                    sourcesAudio = PaCTL.getSources();
                } catch (IOException ex) {
                    Logger.getLogger(StreamDesktop.class.getName()).log(Level.SEVERE, null, ex);
                }
                for (AudioSource audioSource : sourcesAudio){
                    final JCheckBoxMenuItem jCBMenuItem = new JCheckBoxMenuItem();
                    jCBMenuItem.setText(audioSource.description);
                    jCBMenuItem.setName(audioSource.device); // NOI18N
                    jCBMenuItem.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            s.setAudioSource(jCBMenuItem.getName());
                            for (JCheckBoxMenuItem jCb : aSMenuItem) {
                                if (!jCb.getName().equals(s.getAudioSource())) {
                                    jCb.setSelected(false);
                                }
                            }
                        }
                    });
                    jMAudioSource.add(jCBMenuItem);
                    aSMenuItem.add(jCBMenuItem);
                    if (s.getLoaded()){
                        for (JCheckBoxMenuItem jCb : aSMenuItem) {
                            if (jCb.getName().equals(s.getAudioSource())) {
                                jCb.setSelected(true);
                            }
                        }
                    } else {
                        JCheckBoxMenuItem initJCb = aSMenuItem.get(0);
                        initJCb.setSelected(true);
                        s.setAudioSource(initJCb.getName());
                    }
                }
            }
            
            StreamPanel p = new StreamPanel(s);
            this.setLayout(new BorderLayout());
            this.add(p, BorderLayout.CENTER);
            this.setTitle(s.getName());
            this.setVisible(true);
            if (stream.getLoaded()){
                switch (stream.getComm()) {
                    case "AV":
                        jCBAVConv.setSelected(true);
                        stream.setComm("AV");
                        jCBGStreamer.setSelected(false);
                        break;
                    case "GS":
                        jCBGStreamer.setSelected(true);
                        stream.setComm("GS");
                        jCBAVConv.setSelected(false);
                        break;
                    case "FF":
                        jCBFFmpeg.setSelected(true);
                        stream.setComm("FF");
                        stream.setBackFF(true);
                        jCBAVConv.setSelected(false);
                        jCBGStreamer.setSelected(false);
                        break;
                    default:
                        if (stream instanceof SourceWebcam || stream instanceof SourceAudioSource ||stream instanceof SourceImageU) {
                            jCBGStreamer.setSelected(true);
                            stream.setComm("GS");
                            jCBAVConv.setSelected(false);
                            jCBFFmpeg.setSelected(false);
                        } else {
                            jCBAVConv.setSelected(true);
                            stream.setComm("AV");
                            jCBGStreamer.setSelected(false);
                            jCBFFmpeg.setSelected(false);
                        }
                        break;
                }
            } else {
                if (stream instanceof SourceWebcam || stream instanceof SourceAudioSource ||stream instanceof SourceImageU) {
                    jCBGStreamer.setSelected(true);
                    stream.setComm("GS");
                    jCBAVConv.setSelected(false);
                    jCBFFmpeg.setSelected(false);
                } else {
                    if (distro.toLowerCase().equals("ubuntu")){
                        jCBAVConv.setSelected(true);
                        stream.setComm("AV");
                        jCBGStreamer.setSelected(false);
                        jCBFFmpeg.setSelected(false);
                    } else {
                        jCBAVConv.setSelected(false);
                        stream.setComm("FF");
                        stream.setBackFF(true);
                        jCBGStreamer.setSelected(false);
                        jCBFFmpeg.setSelected(true);
                    }
                }
                jMLoop.setVisible(false);
            }
            if (stream instanceof SourceImageGif  || stream instanceof SourceImage){
                jMBackEnd.setVisible(false);
                jMLoop.setVisible(false);
            }
            panel = p;
            s.setPanelType("Panel");
            jCBMoreOptions.setEnabled(true);
            jMIPCBrand.setVisible(false);
            jMFireDevice.setVisible(false);
            if (s instanceof SourceAudioSource) {
                jMAudioSource.setVisible(true);
                jMLoop.setVisible(false);
            } else {
                jMAudioSource.setVisible(false);
            }
            if (s instanceof SourceCustom) {
                jMBackEnd.setVisible(false);
            }
            if (s instanceof SourceMovie || s instanceof SourceMusic) {
                jMLoop.setVisible(true);
                jCBLoop.setSelected(s.getLoop());
            }
        }
        this.setVisible(true);
        this.setDesktopIcon(new DesktopIcon(this,s));
        this.setClosable(true);
        this.setToolTipText(stream.getName());
        pack();
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMBOptions = new javax.swing.JMenuBar();
        jMControls = new javax.swing.JMenu();
        jMIPCBrand = new javax.swing.JMenu();
        jCBoxFosCamPtz = new javax.swing.JCheckBoxMenuItem();
        jCBoxAxisPtz = new javax.swing.JCheckBoxMenuItem();
        jCBoxWansCamPtz = new javax.swing.JCheckBoxMenuItem();
        jCBShowSliders = new javax.swing.JCheckBoxMenuItem();
        jCBMoreOptions = new javax.swing.JCheckBoxMenuItem();
        jMScroll = new javax.swing.JMenu();
        jCBRightToLeft = new javax.swing.JCheckBoxMenuItem();
        jCBLeftToRight = new javax.swing.JCheckBoxMenuItem();
        jCBBottomToTop = new javax.swing.JCheckBoxMenuItem();
        jCBTopToBottom = new javax.swing.JCheckBoxMenuItem();
        jCBBouncingRight = new javax.swing.JCheckBoxMenuItem();
        jMLoop = new javax.swing.JMenu();
        jCBLoop = new javax.swing.JCheckBoxMenuItem();
        jMRefresh = new javax.swing.JMenu();
        jMAudioSource = new javax.swing.JMenu();
        jMFireDevice = new javax.swing.JMenu();
        jMBackEnd = new javax.swing.JMenu();
        jCBGStreamer = new javax.swing.JCheckBoxMenuItem();
        jCBAVConv = new javax.swing.JCheckBoxMenuItem();
        jCBFFmpeg = new javax.swing.JCheckBoxMenuItem();

        setClosable(true);
        setIconifiable(true);
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/user-desktop.png"))); // NOI18N
        setVisible(true);
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosing(evt);
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameIconified(evt);
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameActivated(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
        });
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });

        jMBOptions.setName("jMBOptions"); // NOI18N
        jMBOptions.setPreferredSize(new java.awt.Dimension(74, 17));

        jMControls.setForeground(new java.awt.Color(74, 7, 1));
        jMControls.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/settings2-button-small.png"))); // NOI18N
        jMControls.setToolTipText("Settings");
        jMControls.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jMControls.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jMControls.setName("jMControls"); // NOI18N

        jMIPCBrand.setText("IPCam PTZ");
        jMIPCBrand.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jMIPCBrand.setName("jMIPCBrand"); // NOI18N

        jCBoxFosCamPtz.setText("FosCam");
        jCBoxFosCamPtz.setName("jCBoxFosCamPtz"); // NOI18N
        jCBoxFosCamPtz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBoxFosCamPtzActionPerformed(evt);
            }
        });
        jMIPCBrand.add(jCBoxFosCamPtz);

        jCBoxAxisPtz.setText("Axis");
        jCBoxAxisPtz.setName("jCBoxAxisPtz"); // NOI18N
        jCBoxAxisPtz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBoxAxisPtzActionPerformed(evt);
            }
        });
        jMIPCBrand.add(jCBoxAxisPtz);

        jCBoxWansCamPtz.setText("WansCam");
        jCBoxWansCamPtz.setName("jCBoxWansCamPtz"); // NOI18N
        jCBoxWansCamPtz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBoxWansCamPtzActionPerformed(evt);
            }
        });
        jMIPCBrand.add(jCBoxWansCamPtz);

        jMControls.add(jMIPCBrand);

        jCBShowSliders.setText("Show Control Sliders");
        jCBShowSliders.setName("jCBShowSliders"); // NOI18N
        jCBShowSliders.setPreferredSize(new java.awt.Dimension(177, 15));
        jCBShowSliders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBShowSlidersActionPerformed(evt);
            }
        });
        jMControls.add(jCBShowSliders);
        jCBShowSliders.getAccessibleContext().setAccessibleParent(jMControls);

        jCBMoreOptions.setText("Show more Options");
        jCBMoreOptions.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jCBMoreOptions.setName("jCBMoreOptions"); // NOI18N
        jCBMoreOptions.setPreferredSize(new java.awt.Dimension(169, 15));
        jCBMoreOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBMoreOptionsActionPerformed(evt);
            }
        });
        jMControls.add(jCBMoreOptions);
        jCBMoreOptions.getAccessibleContext().setAccessibleParent(jMControls);

        jMBOptions.add(jMControls);

        jMScroll.setForeground(new java.awt.Color(74, 7, 1));
        jMScroll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/cruz-small-2.png"))); // NOI18N
        jMScroll.setToolTipText("Scroll");
        jMScroll.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jMScroll.setName("jMScroll"); // NOI18N

        jCBRightToLeft.setText("RightToLeft");
        jCBRightToLeft.setName("jCBRightToLeft"); // NOI18N
        jCBRightToLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBRightToLeftActionPerformed(evt);
            }
        });
        jMScroll.add(jCBRightToLeft);

        jCBLeftToRight.setText("LeftToRight");
        jCBLeftToRight.setName("jCBLeftToRight"); // NOI18N
        jCBLeftToRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBLeftToRightActionPerformed(evt);
            }
        });
        jMScroll.add(jCBLeftToRight);

        jCBBottomToTop.setText("BottomToTop");
        jCBBottomToTop.setName("jCBBottomToTop"); // NOI18N
        jCBBottomToTop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBBottomToTopActionPerformed(evt);
            }
        });
        jMScroll.add(jCBBottomToTop);

        jCBTopToBottom.setText("TopToBottom");
        jCBTopToBottom.setName("jCBTopToBottom"); // NOI18N
        jCBTopToBottom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBTopToBottomActionPerformed(evt);
            }
        });
        jMScroll.add(jCBTopToBottom);

        jCBBouncingRight.setText("BouncingRight");
        jCBBouncingRight.setName("jCBBouncingRight"); // NOI18N
        jCBBouncingRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBBouncingRightActionPerformed(evt);
            }
        });
        jMScroll.add(jCBBouncingRight);

        //jMBOptions.add(Box.createHorizontalGlue());

        jMBOptions.add(jMScroll);

        jMLoop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/loop4-small.png"))); // NOI18N
        jMLoop.setToolTipText("Loop Source");
        jMLoop.setName("jMLoop"); // NOI18N

        jCBLoop.setText("On");
        jCBLoop.setName("jCBLoop"); // NOI18N
        jCBLoop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBLoopActionPerformed(evt);
            }
        });
        jMLoop.add(jCBLoop);

        jMBOptions.add(jMLoop);

        jMRefresh.setForeground(new java.awt.Color(1, 188, 3));
        jMRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/view-refresh-small.png"))); // NOI18N
        jMRefresh.setToolTipText("Refresh Properties");
        jMRefresh.setFont(new java.awt.Font("Ubuntu Condensed", 1, 8)); // NOI18N
        jMRefresh.setName("jMRefresh"); // NOI18N
        jMRefresh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMRefreshMouseClicked(evt);
            }
        });
        jMBOptions.add(jMRefresh);

        jMAudioSource.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/audiosource.png"))); // NOI18N
        jMAudioSource.setToolTipText("Audio Source Selector");
        jMAudioSource.setName("jMAudioSource"); // NOI18N
        jMBOptions.add(jMAudioSource);

        jMFireDevice.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/firewire_small.png"))); // NOI18N
        jMFireDevice.setToolTipText("FireWire Devices");
        jMFireDevice.setName("jMFireDevice"); // NOI18N
        jMBOptions.add(jMFireDevice);

        jMBackEnd.setForeground(new java.awt.Color(74, 7, 1));
        jMBackEnd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/bkend-switch5-button-small.png"))); // NOI18N
        jMBackEnd.setToolTipText("Back-End Switch");
        jMBackEnd.setBorderPainted(true);
        jMBackEnd.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jMBackEnd.setName("jMBackEnd"); // NOI18N

        jCBGStreamer.setText("GStreamer");
        jCBGStreamer.setName("jCBGStreamer"); // NOI18N
        jCBGStreamer.setPreferredSize(new java.awt.Dimension(107, 15));
        jCBGStreamer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBGStreamerActionPerformed(evt);
            }
        });
        jMBackEnd.add(jCBGStreamer);

        jCBAVConv.setText("AVConv");
        jCBAVConv.setName("jCBAVConv"); // NOI18N
        jCBAVConv.setPreferredSize(new java.awt.Dimension(107, 15));
        jCBAVConv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBAVConvActionPerformed(evt);
            }
        });
        jMBackEnd.add(jCBAVConv);

        jCBFFmpeg.setText("FFmpeg");
        jCBFFmpeg.setName("jCBFFmpeg"); // NOI18N
        jCBFFmpeg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBFFmpegActionPerformed(evt);
            }
        });
        jMBackEnd.add(jCBFFmpeg);

        jMBOptions.add(Box.createHorizontalGlue());

        jMBOptions.add(jMBackEnd);

        setJMenuBar(jMBOptions);
        jMBOptions.getAccessibleContext().setAccessibleParent(this);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formInternalFrameIconified(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameIconified
       if (panel!=null){
        this.setFrameIcon(panel.getIcon());
       }
    }//GEN-LAST:event_formInternalFrameIconified

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        listener.closeSource();
        stream.setLoop(false);
        PreviewFrameBuilder.unregister(stream);
        MasterFrameBuilder.unregister(stream);
        stream.destroy();
        stream = null;
        panel = null;
        webcamstudio.WebcamStudio.tabControls.removeAll();
        webcamstudio.WebcamStudio.tabControls.repaint();
    }//GEN-LAST:event_formInternalFrameClosing

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        
    }//GEN-LAST:event_formFocusGained

    private void formInternalFrameActivated(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameActivated
        if (listener!=null){
            new Thread(new Runnable(){
                
                @Override
                public void run() {
                    listener.selectedSource(stream);
                }
            }).start();
            
        }
    }//GEN-LAST:event_formInternalFrameActivated

    private void jCBMoreOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBMoreOptionsActionPerformed
        switch (stream.getPanelType()) {
            case "Panel":
                if (jCBMoreOptions.isSelected()){
                    if (stream.needSeekCTRL()) {
                        this.setSize(new Dimension(this.getWidth(),443));
                        this.revalidate();
                        this.repaint();
                    } else {
                        this.setSize(new Dimension(this.getWidth(),423));
                        this.revalidate();
                        this.repaint();
                    }
                } else {
                    this.setSize(new Dimension(this.getWidth(),338));
                    this.revalidate();
                    this.repaint();
                }   break;
            case "PanelDVB":
                if (jCBMoreOptions.isSelected()){
                    this.setSize(new Dimension(this.getWidth(),533));
                    this.revalidate();
                    this.repaint();
                } else {
                    this.setSize(new Dimension(this.getWidth(),448));
                    this.revalidate();
                    this.repaint();
                }   break;
            case "PanelURL":
                if (jCBMoreOptions.isSelected()){
                    this.setSize(new Dimension(this.getWidth(),463));
                    this.revalidate();
                    this.repaint();
                } else {
                    this.setSize(new Dimension(this.getWidth(),378));
                    this.revalidate();
                    this.repaint();
                }   break;
            case "PanelIPCam":
                if (jCBMoreOptions.isSelected()){
                    this.setSize(new Dimension(this.getWidth(),463));
                    this.revalidate();
                    this.repaint();
                } else {
                    this.setSize(new Dimension(this.getWidth(),378));
                    this.revalidate();
                    this.repaint();
                }   break;
        }
    }//GEN-LAST:event_jCBMoreOptionsActionPerformed

    private void jCBGStreamerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBGStreamerActionPerformed
        if (jCBGStreamer.isSelected()){
            stream.setComm("GS");
            stream.setBackFF(false);
            jCBAVConv.setSelected(false);
            jCBFFmpeg.setSelected(false);
            if (listener!=null){
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        listener.selectedSource(stream);
                    }
                }).start();

            }
        } else {
            jCBAVConv.setSelected(true);
            jCBGStreamer.setSelected(false);
            jCBFFmpeg.setSelected(false);
            stream.setBackFF(false);
            stream.setComm("AV");
        }
    }//GEN-LAST:event_jCBGStreamerActionPerformed

    private void jCBAVConvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBAVConvActionPerformed
        if (jCBAVConv.isSelected()){
            stream.setComm("AV");
            stream.setBackFF(false);
            jCBGStreamer.setSelected(false);
            jCBFFmpeg.setSelected(false);
            if (listener!=null){
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        listener.selectedSource(stream);
                    }
                }).start();

            }
        } else {
            jCBGStreamer.setSelected(true);
            jCBAVConv.setSelected(false);
            jCBFFmpeg.setSelected(false);
            stream.setComm("GS");
            stream.setBackFF(false);
        }
    }//GEN-LAST:event_jCBAVConvActionPerformed

    private void jCBShowSlidersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBShowSlidersActionPerformed
        if (jCBShowSliders.isSelected()){
            this.setSize(new Dimension(298,this.getHeight()));
            this.revalidate();
            this.repaint();
        } else {
            this.setSize(new Dimension(136,this.getHeight()));
            this.revalidate();
            this.repaint();
        }
    }//GEN-LAST:event_jCBShowSlidersActionPerformed

    private void jCBoxFosCamPtzActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBoxFosCamPtzActionPerformed
        if (jCBoxFosCamPtz.isSelected()){
            stream.setPtzBrand("foscam");
            jCBoxAxisPtz.setSelected(false);
            jCBoxWansCamPtz.setSelected(false);
        } else {
            jCBoxAxisPtz.setSelected(true);
            jCBoxFosCamPtz.setSelected(false);
            jCBoxWansCamPtz.setSelected(false);
            stream.setPtzBrand("axis");
        }
    }//GEN-LAST:event_jCBoxFosCamPtzActionPerformed

    private void jCBoxAxisPtzActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBoxAxisPtzActionPerformed
        if (jCBoxAxisPtz.isSelected()){
            stream.setPtzBrand("axis");
            jCBoxFosCamPtz.setSelected(false);
            jCBoxWansCamPtz.setSelected(false);
        } else {
            jCBoxFosCamPtz.setSelected(true);
            jCBoxAxisPtz.setSelected(false);
            jCBoxWansCamPtz.setSelected(false);
            stream.setPtzBrand("foscam");
        }
    }//GEN-LAST:event_jCBoxAxisPtzActionPerformed

    private void jCBRightToLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBRightToLeftActionPerformed
        final int oldBkX = stream.getX();
        runMe = true;
        if (jCBRightToLeft.isSelected()) {
            
            jCBLeftToRight.setEnabled(false);
            jCBBottomToTop.setEnabled(false);
            jCBTopToBottom.setEnabled(false);
            jCBBouncingRight.setEnabled(false);
            
            
            Thread scrollRtL = new Thread(new Runnable() {
                int deltaX = 0;
            @Override
            public void run() {
                while (runMe && stream.isPlaying()){
                    final int oldX = stream.getX();
                    final int newX = MasterMixer.getInstance().getWidth();
                    if (oldX <= 0) {
                        deltaX = newX - oldX;
                    } else {
                        deltaX = newX + oldX;
                    }
                    final int rate = stream.getRate();
                    final int totalFrames = rate * speed;
                    for (int i = 0; i<totalFrames;i++){
                        if (runMe) {
                            stream.setX(oldX - i*deltaX/totalFrames);
                            Tools.sleep(1000/rate);
                        } else {
                            stream.setX(oldBkX);
                            break;
                        }
                    }
                    stream.setX(newX);
                    deltaX = newX - oldX;
                    for (int i = 0; i<totalFrames;i++){
                        if (runMe) {
                            stream.setX(newX - i*deltaX/totalFrames);
                            Tools.sleep(1000/rate);
                        } else {
                            stream.setX(oldBkX);
                            break;
                        }
                    }
                }
            }
        }); 
        scrollRtL.setPriority(Thread.MIN_PRIORITY);
        scrollRtL.start();
        } else {
            runMe = false;
            stream.setX(oldBkX);
            jCBLeftToRight.setEnabled(true);
            jCBBottomToTop.setEnabled(true);
            jCBTopToBottom.setEnabled(true);
            jCBBouncingRight.setEnabled(true);
        }
    }//GEN-LAST:event_jCBRightToLeftActionPerformed

    private void jCBLeftToRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBLeftToRightActionPerformed
        final int oldBkX = stream.getX();      
        runMe = true;
        if (jCBLeftToRight.isSelected()) {
            
            jCBRightToLeft.setEnabled(false);
            jCBBottomToTop.setEnabled(false);
            jCBTopToBottom.setEnabled(false);
            jCBBouncingRight.setEnabled(false);
            
            Thread scrollRtL = new Thread(new Runnable() {

            @Override
            public void run() {
                while (runMe && stream.isPlaying()){
                    int oldX = stream.getX();
                    int newX = MasterMixer.getInstance().getWidth(); 
                    int deltaX = newX - oldX;
                    final int rate = stream.getRate();
                    final int totalFrames = rate * speed;
                    for (int i = 0; i<totalFrames;i++){
                        if (runMe && stream.isPlaying()) {
                            stream.setX(oldX + i*deltaX/totalFrames);
                            Tools.sleep(1000/rate);
                        } else {
                            stream.setX(oldBkX);
                            break;
                        }
                    }
                    oldX = -MasterMixer.getInstance().getWidth();
                    stream.setX(oldX);
                    newX = oldBkX;
                    deltaX = newX - oldX;
                    for (int i = 0; i<totalFrames;i++){
                        if (runMe) {
                            stream.setX(oldX + i*deltaX/totalFrames);
                            Tools.sleep(1000/rate);
                        } else {
                            stream.setX(oldBkX);
                            break;
                        }
                    }
                }
            }
        }); 
        scrollRtL.setPriority(Thread.MIN_PRIORITY);
        scrollRtL.start();
        } else {
            runMe = false;
            stream.setX(oldBkX);            
            jCBRightToLeft.setEnabled(true);
            jCBBottomToTop.setEnabled(true);
            jCBTopToBottom.setEnabled(true);
            jCBBouncingRight.setEnabled(true);
        }
    }//GEN-LAST:event_jCBLeftToRightActionPerformed

    private void jCBBottomToTopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBBottomToTopActionPerformed
        final int oldBkY = stream.getY();     
        runMe = true;
        if (jCBBottomToTop.isSelected()) {
            
            jCBLeftToRight.setEnabled(false);
            jCBRightToLeft.setEnabled(false);
            jCBTopToBottom.setEnabled(false);
            jCBBouncingRight.setEnabled(false);

            Thread scrollRtL = new Thread(new Runnable() {
                int deltaY = 0;
            @Override
            public void run() {
                while (runMe && stream.isPlaying()){
                    final int oldY = stream.getY();
                    final int newY = MasterMixer.getInstance().getHeight();
                    if (oldY <= 0) {
                        deltaY = newY - oldY;
                    } else {
                        deltaY = newY + oldY;
                    }
                    final int rate = stream.getRate();
                    final int totalFrames = rate * speed;
                    for (int i = 0; i<totalFrames;i++){
                        if (runMe) {
                            stream.setY(oldY - i*deltaY/totalFrames);
                            Tools.sleep(1000/rate);
                        } else {
                            stream.setY(oldBkY);
                            break;
                        }
                    }
                    stream.setY(newY);
                    deltaY = newY - oldY;
                    for (int i = 0; i<totalFrames;i++){
                        if (runMe){
                            stream.setY(newY - i*deltaY/totalFrames);
                            Tools.sleep(1000/rate);
                        } else {
                            stream.setY(oldBkY);
                            break;
                        }
                    }
                }
            }
        }); 
        scrollRtL.setPriority(Thread.MIN_PRIORITY);
        scrollRtL.start();
        } else {
            runMe = false;
            stream.setY(oldBkY);
            jCBLeftToRight.setEnabled(true);
            jCBRightToLeft.setEnabled(true);
            jCBTopToBottom.setEnabled(true);
            jCBBouncingRight.setEnabled(true);
        }
    }//GEN-LAST:event_jCBBottomToTopActionPerformed

    private void jCBTopToBottomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBTopToBottomActionPerformed
        final int oldBkY = stream.getY();
        runMe = true;
        if (jCBTopToBottom.isSelected()) {
            
            jCBLeftToRight.setEnabled(false);
            jCBBottomToTop.setEnabled(false);
            jCBRightToLeft.setEnabled(false);
            jCBBouncingRight.setEnabled(false);

            Thread scrollRtL = new Thread(new Runnable() {

            @Override
            public void run() {
                while (runMe && stream.isPlaying()){
                    int oldY = stream.getY();
                    int newY = MasterMixer.getInstance().getHeight(); 
                    int deltaY = newY - oldY;
                    final int rate = stream.getRate();
                    final int totalFrames = rate * speed;
                    for (int i = 0; i<totalFrames;i++){
                        if (runMe){
                            stream.setY(oldY+i*deltaY/totalFrames);
                            Tools.sleep(1000/rate);
                        } else {
                            stream.setY(oldBkY);
                            break;
                        }
                    }
                    oldY = -MasterMixer.getInstance().getHeight();
                    stream.setY(oldY);  
                    newY = oldBkY;
                    deltaY = newY - oldY;
                    for (int i = 0; i<totalFrames;i++){
                        if (runMe){
                            stream.setY(oldY+i*deltaY/totalFrames);
                            Tools.sleep(1000/rate);
                        } else {
                            stream.setY(oldBkY);
                            break;
                        }
                    }
                }
            }
        }); 
        scrollRtL.setPriority(Thread.MIN_PRIORITY);
        scrollRtL.start();
        } else {
            runMe = false;
            stream.setY(oldBkY);
            jCBLeftToRight.setEnabled(true);
            jCBBottomToTop.setEnabled(true);
            jCBRightToLeft.setEnabled(true);
            jCBBouncingRight.setEnabled(true);
        }
    }//GEN-LAST:event_jCBTopToBottomActionPerformed

    private void jCBBouncingRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBBouncingRightActionPerformed
        final int oldBkX = stream.getX();
        runMe = true;
        if (jCBBouncingRight.isSelected()) {
            
            jCBLeftToRight.setEnabled(false);
            jCBBottomToTop.setEnabled(false);
            jCBTopToBottom.setEnabled(false);
            jCBRightToLeft.setEnabled(false);
            
            Thread scrollRtL = new Thread(new Runnable() {
                int deltaX = 0;
            @Override
            public void run() {
                while (runMe && stream.isPlaying()){
                    int oldX = stream.getX();
                    int newX = MasterMixer.getInstance().getWidth();
                    if (oldX <= 0) {
                        deltaX = newX - oldX;
                    } else {
                        deltaX = newX + oldX;
                    }
                    final int rate = stream.getRate();
                    final int totalFrames = rate * speed;
                    for (int i = 0; i<totalFrames;i++){
                        if (runMe){
                            stream.setX(oldX - i*deltaX/totalFrames);
                            Tools.sleep(1000/rate);
                        } else {
                            stream.setX(oldBkX);
                            break;
                        }
                    }
                    oldX = -MasterMixer.getInstance().getWidth();
                    stream.setX(oldX);
                    newX = oldBkX;
                    deltaX = newX - oldX;
                    for (int i = 0; i<totalFrames;i++){
                        if (runMe){
                            stream.setX(oldX + i*deltaX/totalFrames);
                            Tools.sleep(1000/rate);
                        } else {
                            stream.setX(oldBkX);
                            break;
                        }
                    }
                }
            }
        }); 
        scrollRtL.setPriority(Thread.MIN_PRIORITY);
        scrollRtL.start();
        } else {
            runMe = false;
            stream.setX(oldBkX);
            jCBLeftToRight.setEnabled(true);
            jCBBottomToTop.setEnabled(true);
            jCBTopToBottom.setEnabled(true);
            jCBRightToLeft.setEnabled(true);
        }
    }//GEN-LAST:event_jCBBouncingRightActionPerformed

    private void jCBoxWansCamPtzActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBoxWansCamPtzActionPerformed
        if (jCBoxWansCamPtz.isSelected()){
            stream.setPtzBrand("wanscam");
            jCBoxFosCamPtz.setSelected(false);
            jCBoxAxisPtz.setSelected(false);
        } else {
            jCBoxFosCamPtz.setSelected(true);
            jCBoxAxisPtz.setSelected(false);
            stream.setPtzBrand("foscam");
        }
    }//GEN-LAST:event_jCBoxWansCamPtzActionPerformed

    private void jCBFFmpegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBFFmpegActionPerformed
        if (jCBFFmpeg.isSelected()){
            stream.setComm("FF");
            stream.setBackFF(true);
            jCBAVConv.setSelected(false);
            jCBGStreamer.setSelected(false);
            if (listener!=null){
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        listener.selectedSource(stream);
                    }
                }).start();

            }
        } else {
            stream.setComm("AV");
            stream.setBackFF(false);
            jCBAVConv.setSelected(true);
            jCBGStreamer.setSelected(false);
        }
    }//GEN-LAST:event_jCBFFmpegActionPerformed

    private void jMRefreshMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMRefreshMouseClicked
        if (listener!=null){
            new Thread(new Runnable(){

                @Override
                public void run() {
                    listener.selectedSource(stream);
                }
            }).start();

        }
    }//GEN-LAST:event_jMRefreshMouseClicked

    private void jCBLoopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBLoopActionPerformed
        if (jCBLoop.isSelected()){
            stream.setLoop(true);
        } else {
            stream.setLoop(false);
        }
    }//GEN-LAST:event_jCBLoopActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem jCBAVConv;
    private javax.swing.JCheckBoxMenuItem jCBBottomToTop;
    private javax.swing.JCheckBoxMenuItem jCBBouncingRight;
    private javax.swing.JCheckBoxMenuItem jCBFFmpeg;
    private javax.swing.JCheckBoxMenuItem jCBGStreamer;
    private javax.swing.JCheckBoxMenuItem jCBLeftToRight;
    private javax.swing.JCheckBoxMenuItem jCBLoop;
    private javax.swing.JCheckBoxMenuItem jCBMoreOptions;
    private javax.swing.JCheckBoxMenuItem jCBRightToLeft;
    private javax.swing.JCheckBoxMenuItem jCBShowSliders;
    private javax.swing.JCheckBoxMenuItem jCBTopToBottom;
    private javax.swing.JCheckBoxMenuItem jCBoxAxisPtz;
    private javax.swing.JCheckBoxMenuItem jCBoxFosCamPtz;
    private javax.swing.JCheckBoxMenuItem jCBoxWansCamPtz;
    private javax.swing.JMenu jMAudioSource;
    private javax.swing.JMenuBar jMBOptions;
    private javax.swing.JMenu jMBackEnd;
    private javax.swing.JMenu jMControls;
    private javax.swing.JMenu jMFireDevice;
    private javax.swing.JMenu jMIPCBrand;
    private javax.swing.JMenu jMLoop;
    private javax.swing.JMenu jMRefresh;
    private javax.swing.JMenu jMScroll;
    // End of variables declaration//GEN-END:variables
}
