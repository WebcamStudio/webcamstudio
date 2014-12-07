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
import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;



/**
 *
 * @author patrick (modified by karl)
 */
public class StreamPanelIPCam extends javax.swing.JPanel implements Stream.Listener, StreamDesktop.Listener{

    Stream stream = null;
    Viewer viewer = new Viewer();
    float volume = 0;
    BufferedImage icon = null;
    boolean lockRatio = false;
    boolean muted = false;
    int oldW ;
    int oldH ;
    
    

    /** Creates new form StreamPanel
     * @param stream */
    public StreamPanelIPCam(Stream stream) {

        initComponents();
        
        oldW = stream.getWidth();
        oldH = stream.getHeight();
        lblCurtain.setVisible(false);
        
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
        stream.setIsIPCam(true);
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
        spinZOrder.setValue(stream.getZOrder());
        spinH1.setValue(stream.getCaptureHeight());
        spinW1.setValue(stream.getCaptureWidth());
        spinVDelay.setValue(stream.getVDelay());
        spinADelay.setValue(stream.getADelay());
        spinVDelay.setEnabled(stream.hasVideo());
        jSlSpinVD.setEnabled(stream.hasVideo());
        spinADelay.setEnabled(stream.hasAudio());
        tglAudio.setSelected(true);
        jSlSpinV.setEnabled(!tglAudio.isSelected());
        txtWebURL.setText(stream.getWebURL());
        ckbProtected.setSelected(stream.getProtected());
        txtUser.setEnabled(stream.getProtected());
        txtPwd.setEnabled(stream.getProtected());
        txtUser.setText(stream.getIPUser());
        txtPwd.setText(stream.getIPPwd());
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
        if (stream.isPlaying()){
            this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.green));
            spinH1.setEnabled(false);
            jSlSpinCH.setEnabled(false);
            spinW1.setEnabled(false);
            jSlSpinCW.setEnabled(false);
            spinVDelay.setEnabled(false);
            jSlSpinVD.setEnabled(false);
            spinADelay.setEnabled(false);
            jSlSpinAD.setEnabled(false);
            txtWebURL.setEditable(false);
            tglAudio.setEnabled(false);
            tglPreview.setEnabled(false);
        } else {
            this.setBorder(BorderFactory.createEmptyBorder());
            tglPreview.setEnabled(true);
            spinH1.setEnabled(true);
            jSlSpinCH.setEnabled(true);
            spinW1.setEnabled(true);
            jSlSpinCW.setEnabled(true);
            spinVDelay.setEnabled(true);
            jSlSpinVD.setEnabled(true);
            spinADelay.setEnabled(true);
            jSlSpinAD.setEnabled(true);
            txtWebURL.setEditable(true);
            tglAudio.setEnabled(true);
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
        tglAudio = new javax.swing.JToggleButton();
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
        labelURL = new javax.swing.JLabel();
        txtWebURL = new javax.swing.JTextField();
        jSeparator4 = new javax.swing.JSeparator();
        jSlSpinX = new javax.swing.JSlider();
        jSlSpinY = new javax.swing.JSlider();
        jSlSpinCW = new javax.swing.JSlider();
        jSlSpinCH = new javax.swing.JSlider();
        jSlSpinW = new javax.swing.JSlider();
        jSlSpinH = new javax.swing.JSlider();
        jSlSpinO = new javax.swing.JSlider();
        jSlSpinVD = new javax.swing.JSlider();
        jSlSpinAD = new javax.swing.JSlider();
        jSlSpinZOrder = new javax.swing.JSlider();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        labelVD = new javax.swing.JLabel();
        labelAD = new javax.swing.JLabel();
        btnUp = new javax.swing.JToggleButton();
        btnDown = new javax.swing.JToggleButton();
        btnLeft = new javax.swing.JToggleButton();
        btnRight = new javax.swing.JToggleButton();
        btnPreset = new javax.swing.JButton();
        btnZoomIn = new javax.swing.JToggleButton();
        btnZoomOut = new javax.swing.JToggleButton();
        txtUser = new javax.swing.JTextField();
        txtPwd = new javax.swing.JPasswordField();
        labelUser = new javax.swing.JLabel();
        labelPwd = new javax.swing.JLabel();
        ckbProtected = new javax.swing.JCheckBox();
        labelProtected = new javax.swing.JLabel();
        labelPTZPanel = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jSeparator7 = new javax.swing.JSeparator();
        tglPreview = new javax.swing.JToggleButton();

        setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        setMaximumSize(new java.awt.Dimension(286, 356));
        setMinimumSize(new java.awt.Dimension(277, 336));
        setPreferredSize(new java.awt.Dimension(286, 336));
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

        tglAudio.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        tglAudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/audio-volume-muted.png"))); // NOI18N
        tglAudio.setToolTipText("No Audio Switch (Only Video Source)");
        tglAudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglAudio.setMaximumSize(new java.awt.Dimension(40, 32));
        tglAudio.setMinimumSize(new java.awt.Dimension(26, 30));
        tglAudio.setName("tglAudio"); // NOI18N
        tglAudio.setPreferredSize(new java.awt.Dimension(20, 20));
        tglAudio.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/audio-volume-muted.png"))); // NOI18N
        tglAudio.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/audio-volume-selected-muted.png"))); // NOI18N
        tglAudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglAudioActionPerformed(evt);
            }
        });
        panPreview.add(tglAudio, java.awt.BorderLayout.PAGE_END);

        jSlSpinV.setBackground(new java.awt.Color(0, 0, 0));
        jSlSpinV.setForeground(new java.awt.Color(255, 255, 255));
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
        panPreview.add(jSlSpinV, java.awt.BorderLayout.PAGE_START);

        lblCurtain.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/curtain_small.png"))); // NOI18N
        lblCurtain.setToolTipText("Click on the video to Hide/Unhide");
        lblCurtain.setName("lblCurtain"); // NOI18N
        lblCurtain.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCurtainMouseClicked(evt);
            }
        });
        panPreview.add(lblCurtain, java.awt.BorderLayout.LINE_START);

        add(panPreview, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 7, 110, 120));

        spinX.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinX.setName("spinX"); // NOI18N
        spinX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinXStateChanged(evt);
            }
        });
        add(spinX, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 180, 50, -1));

        spinY.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinY.setName("spinY"); // NOI18N
        spinY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinYStateChanged(evt);
            }
        });
        add(spinY, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 200, 50, -1));

        spinW.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinW.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        spinW.setName("spinW"); // NOI18N
        spinW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinWStateChanged(evt);
            }
        });
        add(spinW, new org.netbeans.lib.awtextra.AbsoluteConstraints(58, 220, 60, -1));

        spinH.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinH.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        spinH.setName("spinH"); // NOI18N
        spinH.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinHStateChanged(evt);
            }
        });
        add(spinH, new org.netbeans.lib.awtextra.AbsoluteConstraints(58, 240, 60, -1));

        spinOpacity.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinOpacity.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        spinOpacity.setName("spinOpacity"); // NOI18N
        spinOpacity.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinOpacityStateChanged(evt);
            }
        });
        add(spinOpacity, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 280, 50, -1));

        spinVolume.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinVolume.setName("spinVolume"); // NOI18N
        spinVolume.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinVolumeStateChanged(evt);
            }
        });
        add(spinVolume, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 80, 50, -1));

        tglActiveStream.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-start.png"))); // NOI18N
        tglActiveStream.setName("tglActiveStream"); // NOI18N
        tglActiveStream.setRolloverEnabled(false);
        tglActiveStream.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglActiveStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglActiveStreamActionPerformed(evt);
            }
        });
        add(tglActiveStream, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 129, 110, 20));

        spinZOrder.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinZOrder.setName("spinZOrder"); // NOI18N
        spinZOrder.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinZOrderStateChanged(evt);
            }
        });
        add(spinZOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 300, 50, -1));

        labelX.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        labelX.setText(bundle.getString("X")); // NOI18N
        labelX.setName("labelX"); // NOI18N
        add(labelX, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 60, -1));

        labelY.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelY.setText(bundle.getString("Y")); // NOI18N
        labelY.setName("labelY"); // NOI18N
        add(labelY, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 50, -1));

        labelW.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelW.setText(bundle.getString("WIDTH")); // NOI18N
        labelW.setName("labelW"); // NOI18N
        add(labelW, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 52, -1));

        labelH.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelH.setText(bundle.getString("HEIGHT")); // NOI18N
        labelH.setName("labelH"); // NOI18N
        add(labelH, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 250, 40, -1));

        labelO.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelO.setText(bundle.getString("OPACITY")); // NOI18N
        labelO.setName("labelO"); // NOI18N
        add(labelO, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 290, 40, -1));

        labelZ.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelZ.setText(bundle.getString("LAYER")); // NOI18N
        labelZ.setMaximumSize(new java.awt.Dimension(30, 10));
        labelZ.setMinimumSize(new java.awt.Dimension(30, 10));
        labelZ.setName("labelZ"); // NOI18N
        labelZ.setPreferredSize(new java.awt.Dimension(30, 10));
        add(labelZ, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 310, 40, 9));

        labelCW.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelCW.setText(bundle.getString("CAPTUREWIDTH")); // NOI18N
        labelCW.setName("labelCW"); // NOI18N
        add(labelCW, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 50, -1));

        spinW1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinW1.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        spinW1.setName("spinW1"); // NOI18N
        spinW1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinW1StateChanged(evt);
            }
        });
        add(spinW1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 330, 50, -1));

        labelCH.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelCH.setText(bundle.getString("CAPTUREHEIGHT")); // NOI18N
        labelCH.setName("labelCH"); // NOI18N
        add(labelCH, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 360, 60, -1));

        spinH1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinH1.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        spinH1.setName("spinH1"); // NOI18N
        spinH1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinH1StateChanged(evt);
            }
        });
        add(spinH1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 350, 50, -1));

        spinVDelay.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinVDelay.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        spinVDelay.setToolTipText("Milliseconds");
        spinVDelay.setName("spinVDelay"); // NOI18N
        spinVDelay.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinVDelayStateChanged(evt);
            }
        });
        add(spinVDelay, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 370, 60, -1));

        spinADelay.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinADelay.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        spinADelay.setToolTipText("Milliseconds");
        spinADelay.setName("spinADelay"); // NOI18N
        spinADelay.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinADelayStateChanged(evt);
            }
        });
        add(spinADelay, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 390, 60, -1));

        labelURL.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelURL.setText(bundle.getString("ENTER_CAM_URL")); // NOI18N
        labelURL.setToolTipText("");
        labelURL.setName("labelURL"); // NOI18N
        add(labelURL, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 140, 80, -1));

        txtWebURL.setFont(new java.awt.Font("Ubuntu Condensed", 0, 12)); // NOI18N
        txtWebURL.setToolTipText("Enter Url ...");
        txtWebURL.setName("txtWebURL"); // NOI18N
        txtWebURL.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtWebURLFocusLost(evt);
            }
        });
        add(txtWebURL, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 152, 272, -1));

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator4.setName("jSeparator4"); // NOI18N
        add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 7, 10, 126));

        jSlSpinX.setMajorTickSpacing(10);
        jSlSpinX.setMaximum(MasterMixer.getInstance().getWidth());
        jSlSpinX.setMinimum(- MasterMixer.getInstance().getWidth());
        jSlSpinX.setMinorTickSpacing(1);
        jSlSpinX.setValue(0);
        jSlSpinX.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinX.setName("jSlSpinX"); // NOI18N
        jSlSpinX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinXStateChanged(evt);
            }
        });
        add(jSlSpinX, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 180, 150, 20));

        jSlSpinY.setMajorTickSpacing(10);
        jSlSpinY.setMaximum(MasterMixer.getInstance().getHeight());
        jSlSpinY.setMinimum(- MasterMixer.getInstance().getHeight());
        jSlSpinY.setMinorTickSpacing(1);
        jSlSpinY.setValue(0);
        jSlSpinY.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinY.setInverted(true);
        jSlSpinY.setName("jSlSpinY"); // NOI18N
        jSlSpinY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinYStateChanged(evt);
            }
        });
        add(jSlSpinY, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 200, 150, 20));

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
        add(jSlSpinCW, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 330, 150, 20));

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
        add(jSlSpinCH, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 350, 150, 20));

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
        add(jSlSpinW, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 220, 150, 20));

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
        add(jSlSpinH, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 240, 150, 20));

        jSlSpinO.setValue(100);
        jSlSpinO.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinO.setName("jSlSpinO"); // NOI18N
        jSlSpinO.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinOStateChanged(evt);
            }
        });
        add(jSlSpinO, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 280, 150, 20));

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
        add(jSlSpinVD, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 370, 150, 20));

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
        add(jSlSpinAD, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 390, 150, 20));

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
        add(jSlSpinZOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 299, 150, 30));

        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator5.setName("jSeparator5"); // NOI18N
        add(jSeparator5, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 181, 10, 140));

        jSeparator3.setName("jSeparator3"); // NOI18N
        jSeparator3.setPreferredSize(new java.awt.Dimension(48, 10));
        add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 324, 110, 10));

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setName("jSeparator2"); // NOI18N
        add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 330, 10, 80));

        labelVD.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelVD.setText(bundle.getString("VIDEO_DELAY")); // NOI18N
        labelVD.setName("labelVD"); // NOI18N
        add(labelVD, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 380, 60, 9));

        labelAD.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelAD.setText(bundle.getString("AUDIO_DELAY")); // NOI18N
        labelAD.setName("labelAD"); // NOI18N
        add(labelAD, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 400, 60, 9));

        btnUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/go-up.png"))); // NOI18N
        btnUp.setName("btnUp"); // NOI18N
        btnUp.setRolloverEnabled(false);
        btnUp.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        btnUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpActionPerformed(evt);
            }
        });
        add(btnUp, new org.netbeans.lib.awtextra.AbsoluteConstraints(183, 12, 40, -1));

        btnDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/go-down.png"))); // NOI18N
        btnDown.setName("btnDown"); // NOI18N
        btnDown.setRolloverEnabled(false);
        btnDown.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        btnDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownActionPerformed(evt);
            }
        });
        add(btnDown, new org.netbeans.lib.awtextra.AbsoluteConstraints(183, 72, 40, -1));

        btnLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/go-previous.png"))); // NOI18N
        btnLeft.setName("btnLeft"); // NOI18N
        btnLeft.setRolloverEnabled(false);
        btnLeft.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        btnLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLeftActionPerformed(evt);
            }
        });
        add(btnLeft, new org.netbeans.lib.awtextra.AbsoluteConstraints(145, 42, 40, -1));

        btnRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/go-next.png"))); // NOI18N
        btnRight.setName("btnRight"); // NOI18N
        btnRight.setRolloverEnabled(false);
        btnRight.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        btnRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRightActionPerformed(evt);
            }
        });
        add(btnRight, new org.netbeans.lib.awtextra.AbsoluteConstraints(221, 42, 40, -1));

        btnPreset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-record.png"))); // NOI18N
        btnPreset.setToolTipText("(Foscam-Axis) Preset1 or (WansCam) HRoll");
        btnPreset.setName("btnPreset"); // NOI18N
        btnPreset.setRolloverEnabled(false);
        btnPreset.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        btnPreset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPresetActionPerformed(evt);
            }
        });
        add(btnPreset, new org.netbeans.lib.awtextra.AbsoluteConstraints(183, 42, 40, -1));

        btnZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/list-add.png"))); // NOI18N
        btnZoomIn.setToolTipText("Zoom In");
        btnZoomIn.setName("btnZoomIn"); // NOI18N
        btnZoomIn.setRolloverEnabled(false);
        btnZoomIn.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        btnZoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZoomInActionPerformed(evt);
            }
        });
        add(btnZoomIn, new org.netbeans.lib.awtextra.AbsoluteConstraints(146, 18, 40, 20));

        btnZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/list-remove.png"))); // NOI18N
        btnZoomOut.setToolTipText("Zoom Out");
        btnZoomOut.setName("btnZoomOut"); // NOI18N
        btnZoomOut.setRolloverEnabled(false);
        btnZoomOut.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        btnZoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZoomOutActionPerformed(evt);
            }
        });
        add(btnZoomOut, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 18, 40, 20));

        txtUser.setFont(new java.awt.Font("Ubuntu Condensed", 0, 10)); // NOI18N
        txtUser.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtUser.setName("txtUser"); // NOI18N
        add(txtUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 103, 70, 24));

        txtPwd.setFont(new java.awt.Font("Ubuntu Condensed", 0, 10)); // NOI18N
        txtPwd.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtPwd.setName("txtPwd"); // NOI18N
        add(txtPwd, new org.netbeans.lib.awtextra.AbsoluteConstraints(205, 103, 70, 24));

        labelUser.setFont(new java.awt.Font("Ubuntu Condensed", 0, 10)); // NOI18N
        labelUser.setForeground(java.awt.Color.white);
        labelUser.setText(bundle.getString("USER")); // NOI18N
        labelUser.setName("labelUser"); // NOI18N
        add(labelUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(158, 123, 30, -1));

        labelPwd.setFont(new java.awt.Font("Ubuntu Condensed", 0, 10)); // NOI18N
        labelPwd.setForeground(java.awt.Color.white);
        labelPwd.setText(bundle.getString("PWD")); // NOI18N
        labelPwd.setName("labelPwd"); // NOI18N
        add(labelPwd, new org.netbeans.lib.awtextra.AbsoluteConstraints(222, 123, 50, -1));

        ckbProtected.setFont(new java.awt.Font("Ubuntu Condensed", 0, 10)); // NOI18N
        ckbProtected.setName("ckbProtected"); // NOI18N
        ckbProtected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckbProtectedActionPerformed(evt);
            }
        });
        add(ckbProtected, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 78, -1, -1));

        labelProtected.setFont(new java.awt.Font("Ubuntu Condensed", 0, 10)); // NOI18N
        labelProtected.setForeground(java.awt.Color.white);
        labelProtected.setText("Protected");
        labelProtected.setName("labelProtected"); // NOI18N
        add(labelProtected, new org.netbeans.lib.awtextra.AbsoluteConstraints(147, 83, -1, -1));

        labelPTZPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/IPCam.png"))); // NOI18N
        labelPTZPanel.setToolTipText("PTZ Panel");
        labelPTZPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        labelPTZPanel.setName("labelPTZPanel"); // NOI18N
        labelPTZPanel.setOpaque(true);
        add(labelPTZPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(126, 7, 153, 130));

        jCheckBox1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        jCheckBox1.setText("Lock A/R");
        jCheckBox1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jCheckBox1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/LockButton-open_small.png"))); // NOI18N
        jCheckBox1.setName("jCheckBox1"); // NOI18N
        jCheckBox1.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/LockButton-open_small.png"))); // NOI18N
        jCheckBox1.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/LockButton-close_small.png"))); // NOI18N
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });
        add(jCheckBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(64, 261, -1, -1));

        jSeparator7.setName("jSeparator7"); // NOI18N
        jSeparator7.setPreferredSize(new java.awt.Dimension(48, 10));
        add(jSeparator7, new org.netbeans.lib.awtextra.AbsoluteConstraints(126, 269, 150, 10));

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
        add(tglPreview, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 265, 40, 20));

        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents
    private void tglActiveStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglActiveStreamActionPerformed
        if (tglActiveStream.isSelected()) {
            this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.green));
            if (txtWebURL.getText() != null) {
            stream.setWebURL(txtWebURL.getText());
            setToolTipText(txtWebURL.getText());
            }
            if (stream.getProtected()) {
                if (txtUser.getText() != null) {
                stream.setIPUser(txtUser.getText());
                }
                if (txtPwd.getPassword() != null) {
                    char[] pass = txtPwd.getPassword();
                    String pwd = new String(pass);
                    stream.setIPPwd(pwd);
                }
            }
            if (tglAudio.isSelected()) {
                stream.setHasAudio(false);
                stream.setFakeAudio(false);
            } else {
                stream.setHasVideo(true);
                stream.setHasAudio(true);
            }
            spinX.setEnabled(stream.hasVideo());
            jSlSpinX.setEnabled(stream.hasVideo());
            spinY.setEnabled(stream.hasVideo());
            jSlSpinY.setEnabled(stream.hasVideo());
            spinW1.setEnabled(false);
            jSlSpinCW.setEnabled(false);
            spinH1.setEnabled(false);
            jSlSpinCH.setEnabled(false);
            spinW.setEnabled(stream.hasVideo());
            jSlSpinW.setEnabled(stream.hasVideo());
            spinH.setEnabled(stream.hasVideo());
            jSlSpinH.setEnabled(stream.hasVideo());
            spinOpacity.setEnabled(stream.hasVideo());
            jSlSpinO.setEnabled(stream.hasVideo());
            spinVDelay.setEnabled(false);
            jSlSpinVD.setEnabled(false);
            spinADelay.setEnabled(false);
            jSlSpinAD.setEnabled(false);
            txtWebURL.setEditable(false);
            tglAudio.setEnabled(false);
            tglPreview.setEnabled(false);
            stream.read();
        } else {
            this.setBorder(BorderFactory.createEmptyBorder());
            tglPreview.setEnabled(true);
            spinW1.setEnabled(stream.hasVideo());
            jSlSpinCW.setEnabled(stream.hasVideo());
            spinH1.setEnabled(stream.hasVideo());
            jSlSpinCH.setEnabled(stream.hasVideo());
            spinW.setEnabled(stream.hasVideo());
            jSlSpinW.setEnabled(stream.hasVideo());
            spinH.setEnabled(stream.hasVideo());
            jSlSpinH.setEnabled(stream.hasVideo());
            spinVDelay.setEnabled(stream.hasVideo());
            jSlSpinVD.setEnabled(stream.hasVideo());
            spinADelay.setEnabled(stream.hasAudio());
            jSlSpinAD.setEnabled(stream.hasAudio());
            txtWebURL.setEditable(true);
            tglAudio.setEnabled(true);
            stream.stop();
            
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
            jSlSpinH.setValue(h);
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
        stream.setVolume(v/100f);
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

    private void txtWebURLFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtWebURLFocusLost
        setToolTipText(txtWebURL.getText());
    }//GEN-LAST:event_txtWebURLFocusLost

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
        spinH.setValue(jSlSpinH.getValue());
    }//GEN-LAST:event_jSlSpinHStateChanged

    private void jSlSpinOStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinOStateChanged
        spinOpacity.setValue(jSlSpinO.getValue());
    }//GEN-LAST:event_jSlSpinOStateChanged

    private void jSlSpinVDStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinVDStateChanged
        spinVDelay.setValue(jSlSpinVD.getValue());
    }//GEN-LAST:event_jSlSpinVDStateChanged

    private void jSlSpinADStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinADStateChanged
        spinADelay.setValue(jSlSpinAD.getValue());
    }//GEN-LAST:event_jSlSpinADStateChanged

    private void jSlSpinZOrderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinZOrderStateChanged
        spinZOrder.setValue(jSlSpinZOrder.getValue());
    }//GEN-LAST:event_jSlSpinZOrderStateChanged

    private void btnRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRightActionPerformed
        Process camPTZon = null;
        Process camPTZoff = null;
        String soloURL = null;
        String[] temp = null;
        
        if (stream.getWebURL() != null){
            soloURL = stream.getWebURL().replace("http://", "");
            temp = soloURL.split("/");
        
            if (btnRight.isSelected()){

                if (camPTZoff != null){
                    camPTZoff.destroy();
                }
                switch (stream.getPtzBrand()) {
                    case "foscam":
                        {
                            // Foscam compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/decoder_control.cgi?command=4&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdRight: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                    case "axis":
                        {
                            // Axis compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/axis-cgi/com/ptz.cgi?continuouspantiltmove=1,0&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdRight: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                    case "wanscam":
                        {
                            // Wanscam compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/moveptz.xml?dir=right&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdRight: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                }
            } else {

                if (camPTZon != null){
                    camPTZon.destroy();
                }
                switch (stream.getPtzBrand()) {
                    case "foscam":
                        {
                            // Foscam compatible commands
                            String camCmdStopRight = "wget -qO- http://"+temp[0]+"/decoder_control.cgi?command=5&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdStopRight: "+camCmdStopRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZoff = rt.exec(camCmdStopRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       camPTZoff.destroy();
                            break;
                        }
                    case "axis":
                        {
                            // Axis compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/axis-cgi/com/ptz.cgi?continuouspantiltmove=0,0&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdRight: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                    case "wanscam":
                        {
                            // Wanscam compatible commands
                            String camCmdStopLeft = "wget -qO- http://"+temp[0]+"/moveptz.xml?dir=stop&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdStopRight: "+camCmdStopLeft);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZoff = rt.exec(camCmdStopLeft);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       Tools.sleep(250);
                    camPTZoff.destroy();
                            break;
                        }
                }
            }
        }
    }//GEN-LAST:event_btnRightActionPerformed

    private void ckbProtectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckbProtectedActionPerformed
        if (ckbProtected.isSelected()){
            txtUser.setEnabled(true);
            txtPwd.setEnabled(true);
            if (txtUser.getText() != null) {
                stream.setIPUser(txtUser.getText());
            }
            if (txtPwd.getPassword() != null) {
                char[] pass = txtPwd.getPassword();
                String pwd = new String(pass);
                stream.setIPPwd(pwd);
            }
            stream.setProtected(true);
        } else {
            txtUser.setEnabled(false);
            txtPwd.setEnabled(false);
            stream.setIPPwd(null);
            stream.setIPUser(null);
            stream.setProtected(false);
        }
    }//GEN-LAST:event_ckbProtectedActionPerformed

    private void btnUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpActionPerformed
        Process camPTZon = null;
        Process camPTZoff = null;
        String soloURL = null;
        String[] temp = null;
        
        if (stream.getWebURL() != null){
            soloURL = stream.getWebURL().replace("http://", "");
            temp = soloURL.split("/");
            if (btnUp.isSelected()){

                if (camPTZoff != null){
                    camPTZoff.destroy();
                }
                switch (stream.getPtzBrand()) {
                    case "foscam":
                        {
                            // Foscam compatible commands
                            String camCmdUp = "wget -qO- http://"+temp[0]+"/decoder_control.cgi?command=0&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdUP: "+camCmdUp);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdUp);
                                Tools.sleep(250);
                                
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                    case "axis":
                        {
                            // Axis compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/axis-cgi/com/ptz.cgi?continuouspantiltmove=0,1&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdUP: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                    case "wanscam":
                        {
                            // Wanscam compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/moveptz.xml?dir=up&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdUP: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                }
            } else {
                if (camPTZon != null){
                    camPTZon.destroy();
                }
                switch (stream.getPtzBrand()) {
                    case "foscam":
                        {
                            // Foscam compatible commands
                            String camCmdStopUp = "wget -qO- http://"+temp[0]+"/decoder_control.cgi?command=1&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdStopUP: "+camCmdStopUp);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZoff = rt.exec(camCmdStopUp);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       Tools.sleep(250);
                    camPTZoff.destroy();
                            break;
                        }
                    case "axis":
                        {
                            // Axis compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/axis-cgi/com/ptz.cgi?continuouspantiltmove=0,0&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdStopUP: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                    case "wanscam":
                        {
                            // Wanscam compatible commands
                            String camCmdStopLeft = "wget -qO- http://"+temp[0]+"/moveptz.xml?dir=stop&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdStopUP: "+camCmdStopLeft);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZoff = rt.exec(camCmdStopLeft);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       Tools.sleep(250);
                    camPTZoff.destroy();
                            break;
                        }
                }
            }
        }
    }//GEN-LAST:event_btnUpActionPerformed

    private void btnDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownActionPerformed
        Process camPTZon = null;
        Process camPTZoff = null;
        String soloURL = null;
        String[] temp = null;
        
        if (stream.getWebURL() != null){
            soloURL = stream.getWebURL().replace("http://", "");
            temp = soloURL.split("/");
            if (btnDown.isSelected()){

                if (camPTZoff != null){
                    camPTZoff.destroy();
                }
                switch (stream.getPtzBrand()) {
                    case "foscam":
                        {
                            // Foscam compatible commands
                            String camCmdDown = "wget -qO- http://"+temp[0]+"/decoder_control.cgi?command=2&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdDown: "+camCmdDown);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdDown);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                    case "axis":
                        {
                            // Axis compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/axis-cgi/com/ptz.cgi?continuouspantiltmove=0,-1&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdDown: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                    case "wanscam":
                        {
                            // Wanscam compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/moveptz.xml?dir=down&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdDown: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                }
            } else {
                if (camPTZon != null){
                    camPTZon.destroy();
                }
                switch (stream.getPtzBrand()) {
                    case "foscam":
                        {
                            // Foscam compatible commands
                            String camCmdStopDown = "wget -qO- http://"+temp[0]+"/decoder_control.cgi?command=3&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdStopDown: "+camCmdStopDown);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZoff = rt.exec(camCmdStopDown);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       Tools.sleep(250);
                    camPTZoff.destroy();
                            break;
                        }
                    case "axis":
                        {
                            // Axis compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/axis-cgi/com/ptz.cgi?continuouspantiltmove=0,0&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdStopDown: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                    case "wanscam":
                        {
                            // Wanscam compatible commands
                            String camCmdStopLeft = "wget -qO- http://"+temp[0]+"/moveptz.xml?dir=stop&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdStopUP: "+camCmdStopLeft);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZoff = rt.exec(camCmdStopLeft);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       Tools.sleep(250);
                    camPTZoff.destroy();
                            break;
                        }
                }
            }
        }
    }//GEN-LAST:event_btnDownActionPerformed

    private void btnLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLeftActionPerformed
        Process camPTZon = null;
        Process camPTZoff = null;
        String soloURL = null;
        String[] temp = null;
        
        if (stream.getWebURL() != null){
            soloURL = stream.getWebURL().replace("http://", "");
            temp = soloURL.split("/");
            if (btnLeft.isSelected()){

                if (camPTZoff != null){
                    camPTZoff.destroy();
                }
                switch (stream.getPtzBrand()) {
                    case "foscam":
                        {
                            // Foscam compatible commands
                            String camCmdLeft = "wget -qO- http://"+temp[0]+"/decoder_control.cgi?command=6&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdLeft: "+camCmdLeft);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdLeft);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                    case "axis":
                        {
                            // Axis compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/axis-cgi/com/ptz.cgi?continuouspantiltmove=-1,0&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdLeft: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                    case "wanscam":
                        {
                            // Wanscam compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/moveptz.xml?dir=left&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdLeft: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                }
            } else {
                if (camPTZon != null){
                    camPTZon.destroy();
                }
                switch (stream.getPtzBrand()) {
                    case "foscam":
                        {
                            // Foscam compatible commands
                            String camCmdStopLeft = "wget -qO- http://"+temp[0]+"/decoder_control.cgi?command=7&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdStopLeft: "+camCmdStopLeft);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZoff = rt.exec(camCmdStopLeft);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       Tools.sleep(250);
                    camPTZoff.destroy();
                            break;
                        }
                    case "axis":
                        {
                            // Axis compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/axis-cgi/com/ptz.cgi?continuouspantiltmove=0,0&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdStopLeft: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                    case "wanscam":
                        {
                            // Wanscam compatible commands
                            String camCmdStopLeft = "wget -qO- http://"+temp[0]+"/moveptz.xml?dir=stop&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdStopLeft: "+camCmdStopLeft);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZoff = rt.exec(camCmdStopLeft);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       Tools.sleep(250);
                    camPTZoff.destroy();
                            break;
                        }
                }
            }
        }
    }//GEN-LAST:event_btnLeftActionPerformed

    private void btnZoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZoomInActionPerformed
        Process camPTZon = null;
        Process camPTZoff = null;
        String soloURL = null;
        String[] temp = null;
        
        if (stream.getWebURL() != null){
            soloURL = stream.getWebURL().replace("http://", "");
            temp = soloURL.split("/");
            if (btnZoomIn.isSelected()){

                if (camPTZoff != null){
                    camPTZoff.destroy();
                }
                switch (stream.getPtzBrand()) {
                    case "foscam":
                        {
                            // Foscam compatible commands
                            String camCmdZoomIn = "wget -qO- http://"+temp[0]+"/decoder_control.cgi?command=16&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdZoomIn: "+camCmdZoomIn);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdZoomIn);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                    case "axis":
                        {
                            // Axis compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/axis-cgi/com/ptz.cgi?continuouszoommove=1&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdZoomIn: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                }
            } else {
                if (camPTZon != null){
                    camPTZon.destroy();
                }
                switch (stream.getPtzBrand()) {
                    case "foscam":
                        {
                            // Foscam compatible commands
                            String camCmdStopZoomIn = "wget -qO- http://"+temp[0]+"/decoder_control.cgi?command=17&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdStopZoomIn: "+camCmdStopZoomIn);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZoff = rt.exec(camCmdStopZoomIn);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       Tools.sleep(250);
                    camPTZoff.destroy();
                            break;
                        }
                    case "axis":
                        {
                            // Axis compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/axis-cgi/com/ptz.cgi?continuouszoommove=0&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdStopZoomIn: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                }
            }
        }
    }//GEN-LAST:event_btnZoomInActionPerformed

    private void btnZoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZoomOutActionPerformed
        Process camPTZon = null;
        Process camPTZoff = null;
        String soloURL = null;
        String[] temp = null;
        
        if (stream.getWebURL() != null){
            soloURL = stream.getWebURL().replace("http://", "");
            temp = soloURL.split("/");
            if (btnZoomOut.isSelected()){

                if (camPTZoff != null){
                    camPTZoff.destroy();
                }
                switch (stream.getPtzBrand()) {
                    case "foscam":
                        {
                            // Foscam compatible commands
                            String camCmdZoomOut = "wget -qO- http://"+temp[0]+"/decoder_control.cgi?command=18&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdZoomOut: "+camCmdZoomOut);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdZoomOut);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                    case "axis":
                        {
                            // Axis compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/axis-cgi/com/ptz.cgi?continuouszoommove=-1&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdZoomOut: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                }
            } else {
                if (camPTZon != null){
                    camPTZon.destroy();
                }
                switch (stream.getPtzBrand()) {
                    case "foscam":
                        {
                            // Foscam compatible commands
                            String camCmdStopZoomOut = "wget -qO- http://"+temp[0]+"/decoder_control.cgi?command=19&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdStopZoomOut: "+camCmdStopZoomOut);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZoff = rt.exec(camCmdStopZoomOut);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       Tools.sleep(250);
                    camPTZoff.destroy();
                            break;
                        }
                    case "axis":
                        {
                            // Axis compatible commands
                            String camCmdRight = "wget -qO- http://"+temp[0]+"/axis-cgi/com/ptz.cgi?continuouszoommove=0&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                            System.out.println("cmdStopZoomOut: "+camCmdRight);
                            Runtime rt = Runtime.getRuntime();
                            try {
                                camPTZon = rt.exec(camCmdRight);
                                Tools.sleep(250);
                            } catch (IOException ex) {
                                Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                            }       break;
                        }
                }
            }
        }
    }//GEN-LAST:event_btnZoomOutActionPerformed

    private void btnPresetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPresetActionPerformed
        Process camPTZon = null;
        String soloURL = null;
        String[] temp = null;
        
        if (stream.getWebURL() != null){
            soloURL = stream.getWebURL().replace("http://", "");
            temp = soloURL.split("/");
            switch (stream.getPtzBrand()) {
                case "foscam":
                    {
                        // Foscam compatible commands
                        String camCmdZoomOut = "wget -qO- http://"+temp[0]+"/decoder_control.cgi?command=31&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                        System.out.println("cmdPreset1: "+camCmdZoomOut);
                        Runtime rt = Runtime.getRuntime();
                        try {
                            camPTZon = rt.exec(camCmdZoomOut);
                            Tools.sleep(250);
                        } catch (IOException ex) {
                            Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                        }   camPTZon.destroy();
                        break;
                    }
                case "axis":
                    {
                        // Axis compatible commands
                        String camCmdRight = "wget -qO- http://"+temp[0]+"/axis-cgi/com/ptz.cgi?gotoserverpresetno=1&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                        System.out.println("cmdPreset1: "+camCmdRight);
                        Runtime rt = Runtime.getRuntime();
                        try {
                            camPTZon = rt.exec(camCmdRight);
                            Tools.sleep(250);
                        } catch (IOException ex) {
                            Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                        }   camPTZon.destroy();
                        break;
                    }
                case "wanscam":
                    {
                        // Wanscam compatible commands
                        String camCmdStopLeft = "wget -qO- http://"+temp[0]+"/moveptz.xml?dir=leftright&user="+stream.getIPUser()+"&pwd="+stream.getIPPwd();
//                        System.out.println("cmdPreset1: "+camCmdStopLeft);
                        Runtime rt = Runtime.getRuntime();
                        try {
                            camPTZon = rt.exec(camCmdStopLeft);
                        } catch (IOException ex) {
                            Logger.getLogger(StreamPanelIPCam.class.getName()).log(Level.SEVERE, null, ex);
                        }   Tools.sleep(250);
                    camPTZon.destroy();
                        break;
                    }
            }
            
        }
    }//GEN-LAST:event_btnPresetActionPerformed

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

    private void jSlSpinVStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinVStateChanged
        spinVolume.setValue(jSlSpinV.getValue());
    }//GEN-LAST:event_jSlSpinVStateChanged

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        if (jCheckBox1.isSelected()){
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
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void tglAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglAudioActionPerformed
        if (tglAudio.isSelected()){
            jSlSpinV.setEnabled(false);
        } else {
            jSlSpinV.setEnabled(true);
        }
    }//GEN-LAST:event_tglAudioActionPerformed

    private void lblCurtainMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCurtainMouseClicked
        lblCurtain.setVisible(false);
        viewer.setOpaque(true);
        panPreview.add(viewer, BorderLayout.CENTER);
        this.repaint();
        this.revalidate();
    }//GEN-LAST:event_lblCurtainMouseClicked

    private void panPreviewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panPreviewMouseClicked
        panPreview.remove(viewer);
        lblCurtain.setOpaque(true);
        lblCurtain.setVisible(true);
        panPreview.add(lblCurtain);
        this.repaint();
        this.revalidate();
    }//GEN-LAST:event_panPreviewMouseClicked

    private void tglPreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglPreviewActionPerformed
        if (tglPreview.isSelected()) {
            stream.setPreView(true);
        } else {
            stream.setPreView(false);
        }
    }//GEN-LAST:event_tglPreviewActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnDown;
    private javax.swing.JToggleButton btnLeft;
    private javax.swing.JButton btnPreset;
    private javax.swing.JToggleButton btnRight;
    private javax.swing.JToggleButton btnUp;
    private javax.swing.JToggleButton btnZoomIn;
    private javax.swing.JToggleButton btnZoomOut;
    private javax.swing.JCheckBox ckbProtected;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSlider jSlSpinAD;
    private javax.swing.JSlider jSlSpinCH;
    private javax.swing.JSlider jSlSpinCW;
    private javax.swing.JSlider jSlSpinH;
    private javax.swing.JSlider jSlSpinO;
    private javax.swing.JSlider jSlSpinV;
    private javax.swing.JSlider jSlSpinVD;
    private javax.swing.JSlider jSlSpinW;
    private javax.swing.JSlider jSlSpinX;
    private javax.swing.JSlider jSlSpinY;
    private javax.swing.JSlider jSlSpinZOrder;
    private javax.swing.JLabel labelAD;
    private javax.swing.JLabel labelCH;
    private javax.swing.JLabel labelCW;
    private javax.swing.JLabel labelH;
    private javax.swing.JLabel labelO;
    private javax.swing.JLabel labelPTZPanel;
    private javax.swing.JLabel labelProtected;
    private javax.swing.JLabel labelPwd;
    private javax.swing.JLabel labelURL;
    private javax.swing.JLabel labelUser;
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
    private javax.swing.JSpinner spinVDelay;
    private javax.swing.JSpinner spinVolume;
    private javax.swing.JSpinner spinW;
    private javax.swing.JSpinner spinW1;
    private javax.swing.JSpinner spinX;
    private javax.swing.JSpinner spinY;
    private javax.swing.JSpinner spinZOrder;
    private javax.swing.JToggleButton tglActiveStream;
    private javax.swing.JToggleButton tglAudio;
    private javax.swing.JToggleButton tglPreview;
    private javax.swing.JPasswordField txtPwd;
    private javax.swing.JTextField txtUser;
    private javax.swing.JTextField txtWebURL;
    // End of variables declaration//GEN-END:variables

    @Override
    public void updatePreview(BufferedImage image) {
        viewer.setImage(image);
        viewer.setAudioLevel(stream.getAudioLevelLeft(), stream.getAudioLevelRight());
        viewer.repaint();
    }

    @Override
    public void selectedSource(Stream source) {
    }

    @Override
    public void closeSource() {
        // nothing here.
    }
}