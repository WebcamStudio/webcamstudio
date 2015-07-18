/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * StreamPanel.java
 *
 * Created on 4-Apr-2012, 4:07:51 PM
 */
package webcamstudio.components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.Painter;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIDefaults;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.streams.SourceAudioSource;
import webcamstudio.streams.SourceImage;
import webcamstudio.streams.SourceImageGif;
import webcamstudio.streams.SourceImageU;
import webcamstudio.streams.SourceMovie;
import webcamstudio.streams.SourceMusic;
import webcamstudio.streams.SourceWebcam;
import webcamstudio.streams.Stream;



/**
 *
 * @author patrick (modified by karl)
 */
public class StreamPanel extends javax.swing.JPanel implements Stream.Listener, StreamDesktop.Listener {

    Stream stream = null;
    Viewer viewer = new Viewer();
    PreViewer preViewer = new PreViewer();
    private float volume = 0;
    private float vol = 0;
    BufferedImage icon = null;
    boolean lockRatio = false;
    boolean muted = false;
    int oldW ;
    int oldH ;
    

    /** Creates new form StreamPanel
     * @param stream */
    public StreamPanel(Stream stream) {

        initComponents();
        
        oldW = stream.getWidth();
        oldH = stream.getHeight();
        volume = stream.getVolume();
        vol = stream.getVolume();
//        System.out.println("Volume: " + volume);
        try {
            icon = ImageIO.read(getClass().getResource("/webcamstudio/resources/tango/speaker4.png"));
        } catch (IOException ex) {
            Logger.getLogger(StreamPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        UIDefaults sliderDefaults = new UIDefaults();
        sliderDefaults.put("Slider.paintValue", true);
        sliderDefaults.put("Slider.thumbHeight", 13);
        sliderDefaults.put("Slider.thumbWidth", 13);
        
        sliderDefaults.put("Slider:SliderThumb.backgroundPainter", new Painter() {

            @Override
            public void paint(Graphics2D g, Object object, int w, int h) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.drawImage(icon, 0, -5, null);
            }
            
        });
        
        sliderDefaults.put("Slider:SliderTrack.backgroundPainter", new Painter() {
            
            @Override
                public void paint(Graphics2D g, Object object, int w, int h) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(new BasicStroke(2f));
                g.setColor(Color.WHITE);
                g.drawRoundRect(0, 2, w-1, 1, 1, 1);
            }

        });
        
        jSlSpinV.putClientProperty("JComponent.sizeVariant", "small");
        jSlSpinV.putClientProperty("Nimbus.Overrides",sliderDefaults);
        jSlSpinV.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
        jSlSpinV.setOpaque(true);
        
        spinVolume.setVisible(false);
        jSlSpinV.setVisible(stream.hasAudio());
        viewer.setOpaque(true);
        viewer.setVisible(true);
        viewer.setBackground(Color.black);
        panPreview.add(viewer, BorderLayout.CENTER);
        this.stream = stream;
        spinX.setValue(stream.getX());
        spinY.setValue(stream.getY());
        spinW.setValue(stream.getWidth());
        spinH.setValue(stream.getHeight());
        spinOpacity.setModel(new SpinnerNumberModel(100, 0, 100, 1));
        spinOpacity.setValue(stream.getOpacity());
        spinVolume.setModel(new SpinnerNumberModel(50, 0, 300, 1));
        spinVolume.setValue(stream.getVolume() * 100);
        jSlSpinV.setEnabled(stream.hasAudio());
        spinZOrder.setValue(stream.getZOrder());
        spinH1.setValue(stream.getCaptureHeight());
        spinW1.setValue(stream.getCaptureWidth());
        spinVDelay.setValue(stream.getVDelay());
        spinADelay.setValue(stream.getADelay());
        spinVDelay.setEnabled(stream.hasVideo());
        jSlSpinVD.setEnabled(stream.hasVideo());
        spinADelay.setEnabled(stream.hasAudio());
        jSlSpinAD.setEnabled(stream.hasAudio());
        spinSeek.setValue(stream.getSeek());
        spinSeek.setVisible(stream.needSeekCTRL());
        jSlSpinSeek.setVisible(stream.needSeekCTRL());
        if (!stream.needSeekCTRL()) {
            jSeparator2.setVisible(false);
            jSeparator2 = new javax.swing.JSeparator();
            jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
            jSeparator2.setName("jSeparator2"); // NOI18N
            add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 290, 10, 80));
        }
        jlbDuration.setText("Play Time "+stream.getStreamTime());
        
        stream.setListener(this);
        if (!stream.hasVideo()){
            spinX.setEnabled(false);
            jSlSpinX.setEnabled(false);
            spinY.setEnabled(false);
            jSlSpinY.setEnabled(false);
            spinW.setEnabled(false);
            jSlSpinW.setEnabled(false);
            spinH.setEnabled(false);
            jSlSpinH.setEnabled(false);
            spinH1.setEnabled(false);
            jSlSpinCH.setEnabled(false);
            spinW1.setEnabled(false);
            jSlSpinCW.setEnabled(false);
            spinOpacity.setEnabled(false);
            jSlSpinO.setEnabled(false);
        }
        if (stream instanceof SourceWebcam) {
            jlbDuration.setVisible(false);
            tglAudio.setVisible(false);
            tglVideo.setVisible(false);
            tglPause.setVisible(false);
            this.add(tglActiveStream, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 120, 110, 20));
        } else if (stream instanceof SourceAudioSource) {
            jlbDuration.setVisible(false);
            tglAudio.setVisible(true);
            tglPause.setVisible(false);
            this.add(tglAudio, new org.netbeans.lib.awtextra.AbsoluteConstraints(87, 120, 30, 20));
            this.add(tglVideo, new org.netbeans.lib.awtextra.AbsoluteConstraints(57, 120, 30, 20));
        } else if (stream instanceof SourceMusic) {
            tglAudio.setVisible(false);
            tglPause.setVisible(true);
            this.add(tglVideo, new org.netbeans.lib.awtextra.AbsoluteConstraints(57, 120, 30, 20));
        } else if (stream instanceof SourceMovie) {
            if (stream.isOnlyVideo()){
                tglAudio.setVisible(false);
                tglVideo.setVisible(false);
                this.add(tglActiveStream, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 120, 80, 20));
            } else {
            tglAudio.setVisible(true);
            tglVideo.setVisible(false);
            }
        } else if (stream instanceof SourceImage || stream instanceof SourceImageU || stream instanceof SourceImageGif){
            jlbDuration.setText(" ");
            jlbDuration.setVisible(!jSlSpinV.isVisible());
            tglAudio.setVisible(false);
            tglPause.setVisible(false);
            tglVideo.setVisible(false);
            this.add(tglActiveStream, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 120, 110, 20));
        } else {
            jlbDuration.setText(" ");
            jlbDuration.setVisible(!jSlSpinV.isVisible());
            tglAudio.setVisible(false);
            tglVideo.setVisible(false);
            this.add(tglActiveStream, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 120, 78, 20));
        }
        tglVideo.setSelected(stream.isOnlyAudio());
        tglAudio.setSelected(!stream.hasAudio());
        if (tglAudio.isSelected()) {
                tglAudio.setEnabled(true);
                tglVideo.setEnabled(false);
            } else if (tglVideo.isSelected()) {
                tglVideo.setEnabled(true);
                tglAudio.setEnabled(false);
            } else {
                tglAudio.setEnabled(true);
                tglVideo.setEnabled(true);
            }
    }
    
    public ImageIcon getIcon(){
        ImageIcon icon = null;
        if (stream.getPreview()!=null){
            icon = new ImageIcon(stream.getPreview().getScaledInstance(32, 32, BufferedImage.SCALE_FAST));
        }
        
        return icon;
    }
    
    public void remove() {
        stream.stop();
        stream = null;

    }

    @Override
    public void sourceUpdated(Stream stream){
        int mixerW = MasterMixer.getInstance().getWidth();
        int mixerH = MasterMixer.getInstance().getHeight();
        
        if (jSlSpinX.getValue() > mixerW) {
            spinX.setValue(stream.getX());
        }
        jSlSpinX.setMaximum(mixerW);
        
        if (jSlSpinX.getValue() < - mixerW) {
            spinX.setValue(stream.getX());
        }
        jSlSpinX.setMinimum(- mixerW);
        
        if (jSlSpinY.getValue() > mixerH) {
            spinY.setValue(stream.getY());
        }
        jSlSpinY.setMaximum(mixerH);
        
        if (jSlSpinY.getValue() < - mixerH) {
            spinY.setValue(stream.getY());
        }
        jSlSpinY.setMinimum(- mixerH);
        
        if (jSlSpinW.getValue() > mixerW) {
            spinW.setValue(stream.getWidth());
        }
        jSlSpinW.setMaximum(mixerW);
        
        if (jSlSpinH.getValue() > mixerH) {
            spinH.setValue(stream.getHeight());
        }
        jSlSpinH.setMaximum(mixerH);
        
        spinX.setValue(stream.getX());
        spinY.setValue(stream.getY());
        spinH.setValue(stream.getHeight());
        spinW.setValue(stream.getWidth());
        spinW1.setValue(stream.getCaptureWidth());
        spinH1.setValue(stream.getCaptureHeight());
        spinOpacity.setValue(stream.getOpacity());
        spinVolume.setValue(stream.getVolume() * 100);
        spinZOrder.setValue(stream.getZOrder());
        tglActiveStream.setSelected(stream.isPlaying());
        if (stream.isPlaying()) {
            tglPause.setSelected(stream.getisPaused());
        } else {
            tglPause.setSelected(false);
            stream.setisPaused(false);
        }
        if (stream.isPlaying()){
            this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.green));
            spinW1.setEnabled(false);
            jSlSpinCW.setEnabled(false);
            spinH1.setEnabled(false);
            jSlSpinCH.setEnabled(false);
            spinVDelay.setEnabled(false);
            jSlSpinVD.setEnabled(false);
            spinADelay.setEnabled(false);
            jSlSpinAD.setEnabled(false);
            spinSeek.setEnabled(false);
            jSlSpinSeek.setEnabled(false);
            tglAudio.setEnabled(false);
            tglPreview.setEnabled(false);
            tglVideo.setEnabled(false);
            spinVolume.setEnabled(stream.hasAudio());
            jSlSpinV.setEnabled(stream.hasAudio());
            tglPause.setEnabled(true);
        } else {
            this.setBorder(BorderFactory.createEmptyBorder());
            spinH1.setEnabled(stream.hasVideo());
            jSlSpinCH.setEnabled(stream.hasVideo());
            spinW1.setEnabled(stream.hasVideo());
            jSlSpinCW.setEnabled(stream.hasVideo());
            spinVDelay.setEnabled(stream.hasVideo());
            jSlSpinVD.setEnabled(stream.hasVideo());
            spinADelay.setEnabled(stream.hasAudio());
            jSlSpinAD.setEnabled(stream.hasAudio());
            spinSeek.setEnabled(stream.needSeekCTRL());
            jSlSpinSeek.setEnabled(stream.needSeekCTRL());
            tglPreview.setEnabled(true);
            if (tglAudio.isSelected()) {
                tglAudio.setEnabled(true);
            } else if (tglVideo.isSelected()) {
                tglVideo.setEnabled(true);
            } else {
                tglAudio.setEnabled(true);
                tglVideo.setEnabled(true);
            }
            spinVolume.setEnabled(stream.hasAudio());
            jSlSpinV.setEnabled(stream.hasAudio());
            tglPause.setSelected(false);
            tglPause.setEnabled(false);
        }
        tglActiveStream.revalidate();
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panPreview = new javax.swing.JPanel();
        jlbDuration = new javax.swing.JLabel();
        jSlSpinV = new javax.swing.JSlider();
        lblCurtain = new javax.swing.JLabel();
        spinX = new javax.swing.JSpinner();
        spinY = new javax.swing.JSpinner();
        spinW = new javax.swing.JSpinner();
        spinH = new javax.swing.JSpinner();
        spinOpacity = new javax.swing.JSpinner();
        spinVolume = new javax.swing.JSpinner();
        tglActiveStream = new javax.swing.JToggleButton();
        spinZOrder = new javax.swing.JSpinner();
        labelX = new javax.swing.JLabel();
        labelY = new javax.swing.JLabel();
        labelW = new javax.swing.JLabel();
        labelH = new javax.swing.JLabel();
        labelO = new javax.swing.JLabel();
        labelZ = new javax.swing.JLabel();
        labelCW = new javax.swing.JLabel();
        spinW1 = new javax.swing.JSpinner();
        labelCH = new javax.swing.JLabel();
        spinH1 = new javax.swing.JSpinner();
        spinVDelay = new javax.swing.JSpinner();
        spinADelay = new javax.swing.JSpinner();
        spinSeek = new javax.swing.JSpinner();
        labelSeek = new javax.swing.JLabel();
        jSlSpinX = new javax.swing.JSlider();
        jSlSpinY = new javax.swing.JSlider();
        jSlSpinCW = new javax.swing.JSlider();
        jSlSpinCH = new javax.swing.JSlider();
        jSlSpinW = new javax.swing.JSlider();
        jSlSpinH = new javax.swing.JSlider();
        jSlSpinO = new javax.swing.JSlider();
        jSlSpinVD = new javax.swing.JSlider();
        jSlSpinAD = new javax.swing.JSlider();
        jSlSpinSeek = new javax.swing.JSlider();
        jSlSpinZOrder = new javax.swing.JSlider();
        labelVD = new javax.swing.JLabel();
        labelAD = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        tglPause = new javax.swing.JToggleButton();
        tglAudio = new javax.swing.JToggleButton();
        jcbLockAR = new javax.swing.JCheckBox();
        jSeparator6 = new javax.swing.JSeparator();
        tglVideo = new javax.swing.JToggleButton();
        tglPreview = new javax.swing.JToggleButton();

        setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        setMaximumSize(new java.awt.Dimension(298, 440));
        setMinimumSize(new java.awt.Dimension(122, 298));
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(124, 296));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panPreview.setBackground(new java.awt.Color(113, 113, 113));
        panPreview.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        panPreview.setToolTipText("Click on the video to Hide/Unhide");
        panPreview.setMaximumSize(new java.awt.Dimension(90, 60));
        panPreview.setMinimumSize(new java.awt.Dimension(90, 60));
        panPreview.setName("panPreview"); // NOI18N
        panPreview.setPreferredSize(new java.awt.Dimension(90, 60));
        panPreview.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                panPreviewMouseClicked(evt);
            }
        });
        panPreview.setLayout(new java.awt.BorderLayout());

        jlbDuration.setBackground(java.awt.Color.black);
        jlbDuration.setFont(new java.awt.Font("Ubuntu Mono", 0, 12)); // NOI18N
        jlbDuration.setForeground(java.awt.Color.white);
        jlbDuration.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlbDuration.setText("Sec.");
        jlbDuration.setName("jlbDuration"); // NOI18N
        jlbDuration.setOpaque(true);
        panPreview.add(jlbDuration, java.awt.BorderLayout.PAGE_END);

        jSlSpinV.setBackground(java.awt.Color.black);
        jSlSpinV.setForeground(java.awt.Color.white);
        jSlSpinV.setMaximum(200);
        jSlSpinV.setToolTipText("Volume Control - Double Click to Mute/Unmute");
        jSlSpinV.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinV.setMaximumSize(new java.awt.Dimension(110, 30));
        jSlSpinV.setMinimumSize(new java.awt.Dimension(110, 30));
        jSlSpinV.setName("jSlSpinV"); // NOI18N
        jSlSpinV.setPreferredSize(new java.awt.Dimension(110, 25));
        jSlSpinV.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jSlSpinVMouseClicked(evt);
            }
        });
        jSlSpinV.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinVStateChanged(evt);
            }
        });
        jSlSpinV.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jSlSpinVFocusLost(evt);
            }
        });
        panPreview.add(jSlSpinV, java.awt.BorderLayout.PAGE_START);

        lblCurtain.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/curtain_small.png"))); // NOI18N
        lblCurtain.setToolTipText("Click on the video to Hide/Unhide");
        lblCurtain.setName("lblCurtain"); // NOI18N
        lblCurtain.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCurtainMouseClicked(evt);
            }
        });
        panPreview.add(lblCurtain, java.awt.BorderLayout.CENTER);

        add(panPreview, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 7, 110, 111));

        spinX.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinX.setName("spinX"); // NOI18N
        spinX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinXStateChanged(evt);
            }
        });
        add(spinX, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 140, 50, -1));

        spinY.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinY.setName("spinY"); // NOI18N
        spinY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinYStateChanged(evt);
            }
        });
        add(spinY, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 160, 50, -1));

        spinW.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinW.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        spinW.setInputVerifier(jSlSpinW.getInputVerifier());
        spinW.setName("spinW"); // NOI18N
        spinW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinWStateChanged(evt);
            }
        });
        add(spinW, new org.netbeans.lib.awtextra.AbsoluteConstraints(58, 180, 60, -1));

        spinH.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinH.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        spinH.setName("spinH"); // NOI18N
        spinH.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinHStateChanged(evt);
            }
        });
        add(spinH, new org.netbeans.lib.awtextra.AbsoluteConstraints(58, 200, 60, -1));

        spinOpacity.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinOpacity.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        spinOpacity.setName("spinOpacity"); // NOI18N
        spinOpacity.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinOpacityStateChanged(evt);
            }
        });
        add(spinOpacity, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 241, 50, -1));

        spinVolume.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinVolume.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        spinVolume.setName("spinVolume"); // NOI18N
        spinVolume.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinVolumeStateChanged(evt);
            }
        });
        add(spinVolume, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 7, 50, -1));

        tglActiveStream.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-start.png"))); // NOI18N
        tglActiveStream.setName("tglActiveStream"); // NOI18N
        tglActiveStream.setRolloverEnabled(false);
        tglActiveStream.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglActiveStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglActiveStreamActionPerformed(evt);
            }
        });
        add(tglActiveStream, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 120, 50, 20));

        spinZOrder.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinZOrder.setName("spinZOrder"); // NOI18N
        spinZOrder.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinZOrderStateChanged(evt);
            }
        });
        add(spinZOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 261, 50, -1));

        labelX.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        labelX.setText(bundle.getString("X")); // NOI18N
        labelX.setName("labelX"); // NOI18N
        add(labelX, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, 10, 10));

        labelY.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelY.setText(bundle.getString("Y")); // NOI18N
        labelY.setName("labelY"); // NOI18N
        add(labelY, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, 10, -1));

        labelW.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelW.setText(bundle.getString("WIDTH")); // NOI18N
        labelW.setName("labelW"); // NOI18N
        add(labelW, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 52, -1));

        labelH.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelH.setText(bundle.getString("HEIGHT")); // NOI18N
        labelH.setName("labelH"); // NOI18N
        add(labelH, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 40, -1));

        labelO.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelO.setText(bundle.getString("OPACITY")); // NOI18N
        labelO.setName("labelO"); // NOI18N
        add(labelO, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 251, 40, -1));

        labelZ.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelZ.setText(bundle.getString("LAYER")); // NOI18N
        labelZ.setMaximumSize(new java.awt.Dimension(30, 10));
        labelZ.setMinimumSize(new java.awt.Dimension(30, 10));
        labelZ.setName("labelZ"); // NOI18N
        labelZ.setPreferredSize(new java.awt.Dimension(30, 10));
        add(labelZ, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 271, 40, 9));

        labelCW.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelCW.setText(bundle.getString("CAPTUREWIDTH")); // NOI18N
        labelCW.setName("labelCW"); // NOI18N
        add(labelCW, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, 50, -1));

        spinW1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinW1.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        spinW1.setName("spinW1"); // NOI18N
        spinW1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinW1StateChanged(evt);
            }
        });
        add(spinW1, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 290, 50, -1));

        labelCH.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelCH.setText(bundle.getString("CAPTUREHEIGHT")); // NOI18N
        labelCH.setName("labelCH"); // NOI18N
        add(labelCH, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 320, 60, -1));

        spinH1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinH1.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        spinH1.setName("spinH1"); // NOI18N
        spinH1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinH1StateChanged(evt);
            }
        });
        add(spinH1, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 310, 50, -1));

        spinVDelay.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinVDelay.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        spinVDelay.setToolTipText("Milliseconds");
        spinVDelay.setName("spinVDelay"); // NOI18N
        spinVDelay.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinVDelayStateChanged(evt);
            }
        });
        add(spinVDelay, new org.netbeans.lib.awtextra.AbsoluteConstraints(58, 330, 60, -1));

        spinADelay.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinADelay.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        spinADelay.setToolTipText("Milliseconds");
        spinADelay.setName("spinADelay"); // NOI18N
        spinADelay.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinADelayStateChanged(evt);
            }
        });
        add(spinADelay, new org.netbeans.lib.awtextra.AbsoluteConstraints(58, 350, 60, -1));

        spinSeek.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinSeek.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        spinSeek.setName("spinSeek"); // NOI18N
        spinSeek.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinSeekStateChanged(evt);
            }
        });
        add(spinSeek, new org.netbeans.lib.awtextra.AbsoluteConstraints(58, 370, 60, -1));

        labelSeek.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelSeek.setText(bundle.getString("SEEK")); // NOI18N
        labelSeek.setMaximumSize(new java.awt.Dimension(30, 10));
        labelSeek.setMinimumSize(new java.awt.Dimension(30, 10));
        labelSeek.setName("labelSeek"); // NOI18N
        labelSeek.setPreferredSize(new java.awt.Dimension(30, 10));
        add(labelSeek, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 380, 50, 9));

        jSlSpinX.setMaximum(MasterMixer.getInstance().getWidth());
        jSlSpinX.setMinimum(- MasterMixer.getInstance().getWidth());
        jSlSpinX.setValue(0);
        jSlSpinX.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinX.setName("jSlSpinX"); // NOI18N
        jSlSpinX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinXStateChanged(evt);
            }
        });
        add(jSlSpinX, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 140, 150, 20));

        jSlSpinY.setMaximum(MasterMixer.getInstance().getHeight());
        jSlSpinY.setMinimum(- MasterMixer.getInstance().getHeight());
        jSlSpinY.setValue(0);
        jSlSpinY.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinY.setInverted(true);
        jSlSpinY.setName("jSlSpinY"); // NOI18N
        jSlSpinY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinYStateChanged(evt);
            }
        });
        add(jSlSpinY, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 160, 150, 20));

        jSlSpinCW.setMajorTickSpacing(10);
        jSlSpinCW.setMaximum(1920);
        jSlSpinCW.setMinimum(1);
        jSlSpinCW.setMinorTickSpacing(1);
        jSlSpinCW.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinCW.setName("jSlSpinCW"); // NOI18N
        jSlSpinCW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinCWStateChanged(evt);
            }
        });
        add(jSlSpinCW, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 290, 150, 20));

        jSlSpinCH.setMajorTickSpacing(10);
        jSlSpinCH.setMaximum(1080);
        jSlSpinCH.setMinimum(1);
        jSlSpinCH.setMinorTickSpacing(1);
        jSlSpinCH.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinCH.setName("jSlSpinCH"); // NOI18N
        jSlSpinCH.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinCHStateChanged(evt);
            }
        });
        add(jSlSpinCH, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 310, 150, 20));

        jSlSpinW.setMajorTickSpacing(10);
        jSlSpinW.setMaximum(MasterMixer.getInstance().getWidth());
        jSlSpinW.setMinimum(1);
        jSlSpinW.setMinorTickSpacing(1);
        jSlSpinW.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinW.setName("jSlSpinW"); // NOI18N
        jSlSpinW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinWStateChanged(evt);
            }
        });
        add(jSlSpinW, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 180, 150, 20));

        jSlSpinH.setMajorTickSpacing(10);
        jSlSpinH.setMaximum(MasterMixer.getInstance().getHeight());
        jSlSpinH.setMinimum(1);
        jSlSpinH.setMinorTickSpacing(1);
        jSlSpinH.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinH.setName("jSlSpinH"); // NOI18N
        jSlSpinH.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinHStateChanged(evt);
            }
        });
        add(jSlSpinH, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 200, 150, 20));

        jSlSpinO.setValue(100);
        jSlSpinO.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinO.setName("jSlSpinO"); // NOI18N
        jSlSpinO.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinOStateChanged(evt);
            }
        });
        add(jSlSpinO, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 240, 150, 20));

        jSlSpinVD.setMaximum(10000);
        jSlSpinVD.setPaintLabels(true);
        jSlSpinVD.setValue(0);
        jSlSpinVD.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinVD.setName("jSlSpinVD"); // NOI18N
        jSlSpinVD.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinVDStateChanged(evt);
            }
        });
        add(jSlSpinVD, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 330, 150, 20));
        jSlSpinVD.getAccessibleContext().setAccessibleDescription("");

        jSlSpinAD.setMaximum(10000);
        jSlSpinAD.setPaintLabels(true);
        jSlSpinAD.setValue(0);
        jSlSpinAD.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinAD.setName("jSlSpinAD"); // NOI18N
        jSlSpinAD.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinADStateChanged(evt);
            }
        });
        add(jSlSpinAD, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 350, 150, 20));

        jSlSpinSeek.setMaximum(10000);
        jSlSpinSeek.setPaintLabels(true);
        jSlSpinSeek.setValue(0);
        jSlSpinSeek.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinSeek.setName("jSlSpinSeek"); // NOI18N
        jSlSpinSeek.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinSeekStateChanged(evt);
            }
        });
        add(jSlSpinSeek, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 370, 150, 20));

        jSlSpinZOrder.setMajorTickSpacing(10);
        jSlSpinZOrder.setMaximum(10);
        jSlSpinZOrder.setMinimum(-10);
        jSlSpinZOrder.setMinorTickSpacing(1);
        jSlSpinZOrder.setPaintTicks(true);
        jSlSpinZOrder.setSnapToTicks(true);
        jSlSpinZOrder.setValue(0);
        jSlSpinZOrder.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinZOrder.setName("jSlSpinZOrder"); // NOI18N
        jSlSpinZOrder.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinZOrderStateChanged(evt);
            }
        });
        add(jSlSpinZOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 259, 150, 30));

        labelVD.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelVD.setText(bundle.getString("VIDEO_DELAY")); // NOI18N
        labelVD.setName("labelVD"); // NOI18N
        add(labelVD, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 60, 9));

        labelAD.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelAD.setText(bundle.getString("AUDIO_DELAY")); // NOI18N
        labelAD.setName("labelAD"); // NOI18N
        add(labelAD, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 360, 60, 9));

        jSeparator1.setName("jSeparator1"); // NOI18N
        jSeparator1.setPreferredSize(new java.awt.Dimension(48, 10));
        add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(126, 229, 150, 10));

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setName("jSeparator2"); // NOI18N
        add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 290, 10, 100));

        jSeparator3.setName("jSeparator3"); // NOI18N
        jSeparator3.setPreferredSize(new java.awt.Dimension(48, 10));
        add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 284, 110, 10));

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator4.setName("jSeparator4"); // NOI18N
        add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 7, 10, 126));

        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator5.setName("jSeparator5"); // NOI18N
        add(jSeparator5, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 141, 10, 140));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/splash100.png"))); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(143, 14, 120, 110));

        tglPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-pause.png"))); // NOI18N
        tglPause.setEnabled(false);
        tglPause.setName("tglPause"); // NOI18N
        tglPause.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-pause.png"))); // NOI18N
        tglPause.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-play.png"))); // NOI18N
        tglPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglPauseActionPerformed(evt);
            }
        });
        add(tglPause, new org.netbeans.lib.awtextra.AbsoluteConstraints(87, 120, 30, 20));

        tglAudio.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        tglAudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/audio-volume-muted.png"))); // NOI18N
        tglAudio.setToolTipText("No Audio Switch (Force Only Video Source)");
        tglAudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglAudio.setMaximumSize(new java.awt.Dimension(40, 32));
        tglAudio.setMinimumSize(new java.awt.Dimension(0, 0));
        tglAudio.setName("tglAudio"); // NOI18N
        tglAudio.setPreferredSize(new java.awt.Dimension(29, 53));
        tglAudio.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/audio-volume-muted.png"))); // NOI18N
        tglAudio.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/audio-volume-selected-muted.png"))); // NOI18N
        tglAudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglAudioActionPerformed(evt);
            }
        });
        add(tglAudio, new org.netbeans.lib.awtextra.AbsoluteConstraints(57, 120, 30, 20));

        jcbLockAR.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        jcbLockAR.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jcbLockAR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/LockButton-open_small.png"))); // NOI18N
        jcbLockAR.setName("jcbLockAR"); // NOI18N
        jcbLockAR.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/LockButton-open_small.png"))); // NOI18N
        jcbLockAR.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/LockButton-close_small.png"))); // NOI18N
        jcbLockAR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbLockARActionPerformed(evt);
            }
        });
        add(jcbLockAR, new org.netbeans.lib.awtextra.AbsoluteConstraints(44, 190, -1, -1));

        jSeparator6.setName("jSeparator6"); // NOI18N
        jSeparator6.setPreferredSize(new java.awt.Dimension(48, 10));
        add(jSeparator6, new org.netbeans.lib.awtextra.AbsoluteConstraints(126, 135, 150, 10));

        tglVideo.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        tglVideo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/edit-delete.png"))); // NOI18N
        tglVideo.setToolTipText("No Video Switch (Force Only Audio Source)");
        tglVideo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglVideo.setMaximumSize(new java.awt.Dimension(40, 32));
        tglVideo.setMinimumSize(new java.awt.Dimension(26, 30));
        tglVideo.setName("tglVideo"); // NOI18N
        tglVideo.setPreferredSize(new java.awt.Dimension(20, 20));
        tglVideo.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/edit-delete.png"))); // NOI18N
        tglVideo.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/edit-delete-selected.png"))); // NOI18N
        tglVideo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglVideoActionPerformed(evt);
            }
        });
        add(tglVideo, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 40, -1, -1));

        tglPreview.setFont(new java.awt.Font("Ubuntu", 0, 5)); // NOI18N
        tglPreview.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/PreviewButton2.png"))); // NOI18N
        tglPreview.setToolTipText("Preview Mode");
        tglPreview.setName("tglPreview"); // NOI18N
        tglPreview.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/PreviewButton2.png"))); // NOI18N
        tglPreview.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/PreviewButtonSelected4.png"))); // NOI18N
        tglPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglPreviewActionPerformed(evt);
            }
        });
        add(tglPreview, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 222, 112, 20));

        getAccessibleContext().setAccessibleDescription("");
        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents
    private void tglActiveStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglActiveStreamActionPerformed
        if (tglActiveStream.isSelected()) {
            if (tglVideo.isSelected()){
                stream.setOnlyAudio(true);
            } else {
                stream.setOnlyAudio(false);
            }
//            System.out.println("Play Volume: " + volume);
            tglVideo.setEnabled(false);
            this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.green));
            spinW1.setEnabled(false);
            jSlSpinCW.setEnabled(false);
            spinH1.setEnabled(false);
            jSlSpinCH.setEnabled(false);
            spinVDelay.setEnabled(false);
            jSlSpinVD.setEnabled(false);
            spinADelay.setEnabled(false);
            jSlSpinAD.setEnabled(false);
            spinSeek.setEnabled(false);
            jSlSpinSeek.setEnabled(false);
            tglAudio.setEnabled(false);
            tglPreview.setEnabled(false);
            tglPause.setEnabled(true);
            stream.read();
        } else {
            this.setBorder(BorderFactory.createEmptyBorder());
            spinH1.setEnabled(stream.hasVideo());
            jSlSpinCH.setEnabled(stream.hasVideo());
            spinW1.setEnabled(stream.hasVideo());
            jSlSpinCW.setEnabled(stream.hasVideo());
            spinVDelay.setEnabled(stream.hasVideo());
            jSlSpinVD.setEnabled(stream.hasVideo());
            spinADelay.setEnabled(stream.hasAudio());
            jSlSpinAD.setEnabled(stream.hasAudio());
            spinSeek.setEnabled(stream.needSeekCTRL());
            jSlSpinSeek.setEnabled(stream.needSeekCTRL());
            tglPreview.setEnabled(true);
            if (tglAudio.isSelected()) {
                tglAudio.setEnabled(true);
            } else if (tglVideo.isSelected()) {
                tglVideo.setEnabled(true);
            } else {
                tglAudio.setEnabled(true);
                tglVideo.setEnabled(true);
            }
            tglPause.setSelected(false);
            tglPause.setEnabled(false);
            stream.setisPaused(false);
            if (stream.getLoop()) {
                stream.setLoop(false);
                stream.stop();
                stream.setLoop(true);
                stream.setVolume(volume);
            } else {
                stream.stop();
                if (stream.getVolume() == 0) {
                    stream.setVolume(0);
                } else {
                    stream.setVolume(volume);
                }
            }
        }
    }//GEN-LAST:event_tglActiveStreamActionPerformed

    private void spinOpacityStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinOpacityStateChanged
        stream.setOpacity((Integer) spinOpacity.getValue());
        jSlSpinO.setValue((Integer) spinOpacity.getValue());
    }//GEN-LAST:event_spinOpacityStateChanged

    private void spinZOrderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinZOrderStateChanged
        stream.setZOrder((Integer) spinZOrder.getValue());
        jSlSpinZOrder.setValue((Integer) spinZOrder.getValue());      
    }//GEN-LAST:event_spinZOrderStateChanged

    private void spinWStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinWStateChanged
        int w = (Integer) spinW.getValue();
        jSlSpinW.setValue(w);
        int h = oldH;
        if (lockRatio){
            h = (oldH * w) / oldW;
            spinH.setValue(h);
        }
        stream.setWidth(w);
//        stream.setHeight(h);
    }//GEN-LAST:event_spinWStateChanged

    private void spinHStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinHStateChanged
        int h = (Integer) spinH.getValue();
        jSlSpinH.setValue(h);
        if (!lockRatio){
            oldH = stream.getHeight();
        }
        stream.setHeight(h);
    }//GEN-LAST:event_spinHStateChanged

    private void spinXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinXStateChanged
        stream.setX((Integer)spinX.getValue());
        jSlSpinX.setValue((Integer)spinX.getValue());
    }//GEN-LAST:event_spinXStateChanged

    private void spinYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinYStateChanged
        stream.setY((Integer)spinY.getValue());
        jSlSpinY.setValue((Integer)spinY.getValue());
    }//GEN-LAST:event_spinYStateChanged

    private void spinVolumeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinVolumeStateChanged
        String jSVol = spinVolume.getValue().toString().replace(".0", "");
        int jVol = Integer.parseInt(jSVol);
        jSlSpinV.setValue(jVol);
        Object value = spinVolume.getValue();
        float v = 0;
        if (value instanceof Float){
            v = (Float)value;
        } else if (value instanceof Integer){
            v = ((Number)value).floatValue();
        }
        if (stream.getisPaused()) {
            if (v/100f != 0) {
                vol = v/100f;
            }
        } else {
            stream.setVolume(v/100f);
            volume = v/100f;
        }
    }//GEN-LAST:event_spinVolumeStateChanged

    private void spinW1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinW1StateChanged
       stream.setCaptureWidth((Integer)spinW1.getValue());
       jSlSpinCW.setValue((Integer)spinW1.getValue());
    }//GEN-LAST:event_spinW1StateChanged

    private void spinH1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinH1StateChanged
        stream.setCaptureHeight((Integer)spinH1.getValue());
        jSlSpinCH.setValue((Integer)spinH1.getValue());
    }//GEN-LAST:event_spinH1StateChanged

    private void spinVDelayStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinVDelayStateChanged
        stream.setVDelay((Integer)spinVDelay.getValue()); 
        jSlSpinVD.setValue((Integer)spinVDelay.getValue());        
    }//GEN-LAST:event_spinVDelayStateChanged

    private void spinADelayStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinADelayStateChanged
        stream.setADelay((Integer)spinADelay.getValue());
        jSlSpinAD.setValue((Integer)spinADelay.getValue());  
    }//GEN-LAST:event_spinADelayStateChanged

    private void spinSeekStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinSeekStateChanged
        stream.setSeek((Integer)spinSeek.getValue());
        jSlSpinSeek.setValue((Integer)spinSeek.getValue());     
    }//GEN-LAST:event_spinSeekStateChanged

    private void jSlSpinXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinXStateChanged
        spinX.setValue(jSlSpinX.getValue());
    }//GEN-LAST:event_jSlSpinXStateChanged

    private void jSlSpinYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinYStateChanged
        spinY.setValue(jSlSpinY.getValue());
    }//GEN-LAST:event_jSlSpinYStateChanged

    private void jSlSpinCWStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinCWStateChanged
        spinW1.setValue(jSlSpinCW.getValue());
    }//GEN-LAST:event_jSlSpinCWStateChanged

    private void jSlSpinCHStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinCHStateChanged
        spinH1.setValue(jSlSpinCH.getValue());
    }//GEN-LAST:event_jSlSpinCHStateChanged

    private void jSlSpinWStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinWStateChanged
        int w = (Integer) jSlSpinW.getValue();
        spinW.setValue(w);
    }//GEN-LAST:event_jSlSpinWStateChanged

    private void jSlSpinHStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinHStateChanged
        int h = (Integer) jSlSpinH.getValue();
        spinH.setValue(h);
    }//GEN-LAST:event_jSlSpinHStateChanged

    private void jSlSpinOStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinOStateChanged
        spinOpacity.setValue(jSlSpinO.getValue());        
    }//GEN-LAST:event_jSlSpinOStateChanged

    private void jSlSpinVStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinVStateChanged
        spinVolume.setValue(jSlSpinV.getValue());
    }//GEN-LAST:event_jSlSpinVStateChanged

    private void jSlSpinVDStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinVDStateChanged
        spinVDelay.setValue(jSlSpinVD.getValue());        
    }//GEN-LAST:event_jSlSpinVDStateChanged

    private void jSlSpinADStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinADStateChanged
        spinADelay.setValue(jSlSpinAD.getValue());      
    }//GEN-LAST:event_jSlSpinADStateChanged

    private void jSlSpinSeekStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinSeekStateChanged
        spinSeek.setValue(jSlSpinSeek.getValue());      
    }//GEN-LAST:event_jSlSpinSeekStateChanged

    private void jSlSpinZOrderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinZOrderStateChanged
        spinZOrder.setValue(jSlSpinZOrder.getValue());      
    }//GEN-LAST:event_jSlSpinZOrderStateChanged

    private void tglAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglAudioActionPerformed
        if (tglAudio.isSelected()){
            stream.setHasAudio(false);
            stream.setOnlyVideo(true);
            tglVideo.setEnabled(false);
        } else {
            stream.setHasAudio(true);
            stream.setOnlyVideo(false);
            tglVideo.setEnabled(true);
        }
    }//GEN-LAST:event_tglAudioActionPerformed

    private void tglPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglPauseActionPerformed
        if (tglPause.isSelected()){
            stream.setVolume(0);
            stream.setisPaused(true);
            stream.pause();
        } else {
            stream.setVolume(vol);
            spinVolume.setValue(vol*100f);
            stream.setisPaused(false);
            stream.play();
        }
    }//GEN-LAST:event_tglPauseActionPerformed

    private void jcbLockARActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbLockARActionPerformed
        if (jcbLockAR.isSelected()){
            spinH.setEnabled(false);
            jSlSpinH.setEnabled(false);
            lockRatio = true;
            oldW = stream.getWidth();
            oldH = stream.getHeight();
        } else {
            spinH.setEnabled(true);
            jSlSpinH.setEnabled(true);
            lockRatio = false;
            oldW = stream.getWidth();
            oldH = stream.getHeight();
        }
    }//GEN-LAST:event_jcbLockARActionPerformed

    private void jSlSpinVMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jSlSpinVMouseClicked
        if (evt.getClickCount() == 2 && !evt.isConsumed()) {
            evt.consume();
            if (muted) {
                stream.setVolume(volume);
//                System.out.println("Reset Volume to = "+volume);
                jSlSpinV.setEnabled(true);
                try {
                icon = ImageIO.read(getClass().getResource("/webcamstudio/resources/tango/speaker4.png"));
            } catch (IOException ex) {
                Logger.getLogger(StreamPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            UIDefaults sliderDefaults = new UIDefaults();
            sliderDefaults.put("Slider.paintValue", true);
            sliderDefaults.put("Slider.thumbHeight", 13);
            sliderDefaults.put("Slider.thumbWidth", 13);

            sliderDefaults.put("Slider:SliderThumb.backgroundPainter", new Painter() {

                @Override
                public void paint(Graphics2D g, Object object, int w, int h) {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.drawImage(icon, 0, -5, null);
                }
            });

            sliderDefaults.put("Slider:SliderTrack.backgroundPainter", new Painter() {
                @Override
                    public void paint(Graphics2D g, Object object, int w, int h) {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setStroke(new BasicStroke(2f));
                    g.setColor(Color.WHITE);
                    g.drawRoundRect(0, 2, w-1, 1, 1, 1);
                }
            });
            
            jSlSpinV.putClientProperty("JComponent.sizeVariant", "small");
            jSlSpinV.putClientProperty("Nimbus.Overrides",sliderDefaults);
            jSlSpinV.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
            muted = false;

            } else {
                jSlSpinV.setEnabled(false);
                Object value = spinVolume.getValue();
                float v = 0;
                if (value instanceof Float){
                    v = (Float)value;
                } else if (value instanceof Integer){
                    v = ((Number)value).floatValue();
                }
                volume = v/100f;
//                System.out.println("Stored Volume = "+volume);
                stream.setVolume(0);
                try {
                    icon = ImageIO.read(getClass().getResource("/webcamstudio/resources/tango/speaker4-mute.png"));
                } catch (IOException ex) {
                    Logger.getLogger(StreamPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                UIDefaults sliderDefaults = new UIDefaults();
                
                sliderDefaults.put("Slider.paintValue", true);
                sliderDefaults.put("Slider.thumbHeight", 13);
                sliderDefaults.put("Slider.thumbWidth", 13);
                sliderDefaults.put("Slider:SliderThumb.backgroundPainter", new Painter() {

                    @Override
                    public void paint(Graphics2D g, Object object, int w, int h) {
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.drawImage(icon, 0, -5, null);
                    }
                });

                sliderDefaults.put("Slider:SliderTrack.backgroundPainter", new Painter() {
                    @Override
                        public void paint(Graphics2D g, Object object, int w, int h) {
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setStroke(new BasicStroke(2f));
                        g.setColor(Color.GRAY);
                        g.fillRoundRect(0, 2, w-1, 2, 2, 2);
                        g.setColor(Color.WHITE);
                        g.drawRoundRect(0, 2, w-1, 1, 1, 1);
                    }
                });	
                
                jSlSpinV.putClientProperty("JComponent.sizeVariant", "small");
                jSlSpinV.putClientProperty("Nimbus.Overrides",sliderDefaults);
                jSlSpinV.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
                muted = true;
            }
        }
    }//GEN-LAST:event_jSlSpinVMouseClicked

    private void tglVideoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglVideoActionPerformed
        if (tglVideo.isSelected()) {
            tglAudio.setEnabled(false);
            stream.setOnlyAudio(true);
        } else {
            tglAudio.setEnabled(true);
            stream.setOnlyAudio(false);
        }
    }//GEN-LAST:event_tglVideoActionPerformed

    private void panPreviewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panPreviewMouseClicked
        panPreview.remove(viewer);
        lblCurtain.setOpaque(true);
        lblCurtain.setVisible(true);
        panPreview.add(lblCurtain);
        this.repaint();
        this.revalidate();
    }//GEN-LAST:event_panPreviewMouseClicked

    private void lblCurtainMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCurtainMouseClicked
        lblCurtain.setVisible(false);
        viewer.setOpaque(true);
        panPreview.add(viewer, BorderLayout.CENTER);
        this.repaint();
        this.revalidate();
    }//GEN-LAST:event_lblCurtainMouseClicked

    private void tglPreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglPreviewActionPerformed
        if (tglPreview.isSelected()) {
            stream.setPreView(true);
        } else {
            stream.setPreView(false);
            }
    }//GEN-LAST:event_tglPreviewActionPerformed

    private void jSlSpinVFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jSlSpinVFocusLost
        if (jSlSpinV.getValue()/100f != 0) {
            vol = jSlSpinV.getValue()/100f;
        }
    }//GEN-LAST:event_jSlSpinVFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSlider jSlSpinAD;
    private javax.swing.JSlider jSlSpinCH;
    private javax.swing.JSlider jSlSpinCW;
    private javax.swing.JSlider jSlSpinH;
    private javax.swing.JSlider jSlSpinO;
    private javax.swing.JSlider jSlSpinSeek;
    private javax.swing.JSlider jSlSpinV;
    private javax.swing.JSlider jSlSpinVD;
    private javax.swing.JSlider jSlSpinW;
    private javax.swing.JSlider jSlSpinX;
    private javax.swing.JSlider jSlSpinY;
    private javax.swing.JSlider jSlSpinZOrder;
    private javax.swing.JCheckBox jcbLockAR;
    private javax.swing.JLabel jlbDuration;
    private javax.swing.JLabel labelAD;
    private javax.swing.JLabel labelCH;
    private javax.swing.JLabel labelCW;
    private javax.swing.JLabel labelH;
    private javax.swing.JLabel labelO;
    private javax.swing.JLabel labelSeek;
    private javax.swing.JLabel labelVD;
    private javax.swing.JLabel labelW;
    private javax.swing.JLabel labelX;
    private javax.swing.JLabel labelY;
    private javax.swing.JLabel labelZ;
    private javax.swing.JLabel lblCurtain;
    private javax.swing.JPanel panPreview;
    private javax.swing.JSpinner spinADelay;
    private javax.swing.JSpinner spinH;
    private javax.swing.JSpinner spinH1;
    private javax.swing.JSpinner spinOpacity;
    private javax.swing.JSpinner spinSeek;
    private javax.swing.JSpinner spinVDelay;
    private javax.swing.JSpinner spinVolume;
    private javax.swing.JSpinner spinW;
    private javax.swing.JSpinner spinW1;
    private javax.swing.JSpinner spinX;
    private javax.swing.JSpinner spinY;
    private javax.swing.JSpinner spinZOrder;
    private javax.swing.JToggleButton tglActiveStream;
    private javax.swing.JToggleButton tglAudio;
    private javax.swing.JToggleButton tglPause;
    private javax.swing.JToggleButton tglPreview;
    private javax.swing.JToggleButton tglVideo;
    // End of variables declaration//GEN-END:variables

    @Override
    public void updatePreview(BufferedImage image) {
        viewer.setImage(image);
        viewer.setAudioLevel(stream.getAudioLevelLeft(), stream.getAudioLevelRight());
        viewer.repaint();
    }

    @Override
    public void selectedSource(Stream source) {
        // nothing here.
    }

    @Override
    public void closeSource() {
        // nothing here.
    }
}