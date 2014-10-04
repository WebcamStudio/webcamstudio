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
import webcamstudio.streams.Stream;



/**
 *
 * @author patrick (modified by karl)
 */
public class StreamPanelDVB extends javax.swing.JPanel implements Stream.Listener, StreamDesktop.Listener{

    Stream stream = null;
    Viewer viewer = new Viewer();
    private float volume = 0;
    private float vol = 0;
    BufferedImage icon = null;
    boolean lockRatio = false;
    boolean muted = false;
    int oldW ;
    int oldH ;

    /** Creates new form StreamPanel
     * @param stream */
    public StreamPanelDVB(Stream stream) {

        initComponents();
        
        oldW = stream.getWidth();
        oldH = stream.getHeight();
        volume = stream.getVolume();
        vol = stream.getVolume();
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
        frequency.setValue(stream.getDVBFrequency()/1000000);
        prgNumber.setValue(stream.getDVBChannelNumber());
        bandwidth.setValue(stream.getDVBBandwidth());
        txtChName.setText(stream.getChName());//stream.hasAudio() && !stream.getName().contains("Desktop") && !stream.getClass().getName().endsWith("SourceWebcam"));
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
            spinH1.setEnabled(false);
            jSlSpinCH.setEnabled(false);
            spinW1.setEnabled(false);
            jSlSpinCW.setEnabled(false);
            spinVDelay.setEnabled(false);
            jSlSpinVD.setEnabled(false);
            spinADelay.setEnabled(false);
            jSlSpinAD.setEnabled(false);
            frequency.setEnabled(false);
            bandwidth.setEnabled(false);
            prgNumber.setEnabled(false);
            txtChName.setEditable(false);
            tglPause.setEnabled(true);
            tglPreview.setEnabled(false);
        } else {
            tglPreview.setEnabled(true);
            this.setBorder(BorderFactory.createEmptyBorder());
            spinH1.setEnabled(stream.hasVideo());
            jSlSpinCH.setEnabled(stream.hasVideo());
            spinW1.setEnabled(stream.hasVideo());
            jSlSpinCW.setEnabled(stream.hasVideo());
            spinVDelay.setEnabled(stream.hasVideo());
            jSlSpinVD.setEnabled(stream.hasVideo());
            spinADelay.setEnabled(stream.hasAudio());
            jSlSpinAD.setEnabled(stream.hasAudio());
            frequency.setEnabled(true);
            bandwidth.setEnabled(true);
            prgNumber.setEnabled(true);
            txtChName.setEditable(true);
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
        frequency = new javax.swing.JSpinner();
        labelfreq = new javax.swing.JLabel();
        labelInv = new javax.swing.JLabel();
        bandwidth = new javax.swing.JSpinner();
        labelBand = new javax.swing.JLabel();
        prgNumber = new javax.swing.JSpinner();
        txtChName = new javax.swing.JTextField();
        labelfreq1 = new javax.swing.JLabel();
        labelVD1 = new javax.swing.JLabel();
        labelAD1 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
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
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator7 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        tglPause = new javax.swing.JToggleButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jSeparator6 = new javax.swing.JSeparator();
        tglPreview = new javax.swing.JToggleButton();

        setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        setMaximumSize(new java.awt.Dimension(298, 522));
        setMinimumSize(new java.awt.Dimension(140, 341));
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(124, 406));
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
        panPreview.add(lblCurtain, java.awt.BorderLayout.LINE_START);

        add(panPreview, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 7, 110, 100));

        spinX.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinX.setName("spinX"); // NOI18N
        spinX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinXStateChanged(evt);
            }
        });
        add(spinX, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 250, 50, -1));

        spinY.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinY.setName("spinY"); // NOI18N
        spinY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinYStateChanged(evt);
            }
        });
        add(spinY, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 270, 50, -1));

        spinW.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinW.setName("spinW"); // NOI18N
        spinW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinWStateChanged(evt);
            }
        });
        add(spinW, new org.netbeans.lib.awtextra.AbsoluteConstraints(58, 290, 60, -1));

        spinH.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinH.setName("spinH"); // NOI18N
        spinH.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinHStateChanged(evt);
            }
        });
        add(spinH, new org.netbeans.lib.awtextra.AbsoluteConstraints(58, 310, 60, -1));

        spinOpacity.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinOpacity.setName("spinOpacity"); // NOI18N
        spinOpacity.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinOpacityStateChanged(evt);
            }
        });
        add(spinOpacity, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 350, 50, -1));

        spinVolume.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinVolume.setName("spinVolume"); // NOI18N
        spinVolume.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinVolumeStateChanged(evt);
            }
        });
        add(spinVolume, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 10, 50, -1));

        tglActiveStream.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-start.png"))); // NOI18N
        tglActiveStream.setName("tglActiveStream"); // NOI18N
        tglActiveStream.setRolloverEnabled(false);
        tglActiveStream.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglActiveStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglActiveStreamActionPerformed(evt);
            }
        });
        add(tglActiveStream, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 114, 78, 20));

        spinZOrder.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinZOrder.setName("spinZOrder"); // NOI18N
        spinZOrder.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinZOrderStateChanged(evt);
            }
        });
        add(spinZOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 370, 50, -1));

        labelX.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        labelX.setText(bundle.getString("X")); // NOI18N
        labelX.setName("labelX"); // NOI18N
        add(labelX, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 260, 60, -1));

        labelY.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelY.setText(bundle.getString("Y")); // NOI18N
        labelY.setName("labelY"); // NOI18N
        add(labelY, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 280, 50, -1));

        labelW.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelW.setText(bundle.getString("WIDTH")); // NOI18N
        labelW.setName("labelW"); // NOI18N
        add(labelW, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, 52, -1));

        labelH.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelH.setText(bundle.getString("HEIGHT")); // NOI18N
        labelH.setName("labelH"); // NOI18N
        add(labelH, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 320, 40, -1));

        labelO.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelO.setText(bundle.getString("OPACITY")); // NOI18N
        labelO.setName("labelO"); // NOI18N
        add(labelO, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 360, 40, -1));

        labelZ.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelZ.setText(bundle.getString("LAYER")); // NOI18N
        labelZ.setMaximumSize(new java.awt.Dimension(30, 10));
        labelZ.setMinimumSize(new java.awt.Dimension(30, 10));
        labelZ.setName("labelZ"); // NOI18N
        labelZ.setPreferredSize(new java.awt.Dimension(30, 10));
        add(labelZ, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 380, 40, 9));

        labelCW.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelCW.setText(bundle.getString("CAPTUREWIDTH")); // NOI18N
        labelCW.setName("labelCW"); // NOI18N
        add(labelCW, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 410, 50, -1));

        spinW1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinW1.setName("spinW1"); // NOI18N
        spinW1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinW1StateChanged(evt);
            }
        });
        add(spinW1, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 400, 50, -1));

        labelCH.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelCH.setText(bundle.getString("CAPTUREHEIGHT")); // NOI18N
        labelCH.setName("labelCH"); // NOI18N
        add(labelCH, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 430, 60, -1));

        spinH1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinH1.setName("spinH1"); // NOI18N
        spinH1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinH1StateChanged(evt);
            }
        });
        add(spinH1, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 420, 50, -1));

        spinVDelay.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinVDelay.setToolTipText("Milliseconds");
        spinVDelay.setName("spinVDelay"); // NOI18N
        spinVDelay.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinVDelayStateChanged(evt);
            }
        });
        add(spinVDelay, new org.netbeans.lib.awtextra.AbsoluteConstraints(58, 440, 60, -1));

        spinADelay.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinADelay.setToolTipText("Milliseconds");
        spinADelay.setName("spinADelay"); // NOI18N
        spinADelay.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinADelayStateChanged(evt);
            }
        });
        add(spinADelay, new org.netbeans.lib.awtextra.AbsoluteConstraints(58, 460, 60, -1));

        frequency.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        frequency.setToolTipText("Mhz");
        frequency.setName("frequency"); // NOI18N
        frequency.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                frequencyStateChanged(evt);
            }
        });
        add(frequency, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 190, 50, -1));

        labelfreq.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelfreq.setText(bundle.getString("FREQUENCY")); // NOI18N
        labelfreq.setName("labelfreq"); // NOI18N
        add(labelfreq, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 200, 80, -1));

        labelInv.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelInv.setText(bundle.getString("PROGRAM_NUMBER")); // NOI18N
        labelInv.setName("labelInv"); // NOI18N
        add(labelInv, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, 50, -1));

        bandwidth.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        bandwidth.setToolTipText("(7/8)");
        bandwidth.setName("bandwidth"); // NOI18N
        bandwidth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                bandwidthStateChanged(evt);
            }
        });
        add(bandwidth, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 210, 50, -1));

        labelBand.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelBand.setText(bundle.getString("BANDWIDTH")); // NOI18N
        labelBand.setName("labelBand"); // NOI18N
        add(labelBand, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 220, 70, -1));

        prgNumber.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        prgNumber.setName("prgNumber"); // NOI18N
        prgNumber.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                prgNumberStateChanged(evt);
            }
        });
        add(prgNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(58, 230, 60, -1));

        txtChName.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtChName.setToolTipText("Enter Channel Name");
        txtChName.setName("txtChName"); // NOI18N
        txtChName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtChNameActionPerformed(evt);
            }
        });
        txtChName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtChNameFocusLost(evt);
            }
        });
        add(txtChName, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 140, 110, 30));

        labelfreq1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelfreq1.setText(bundle.getString("CHANNEL_NAME")); // NOI18N
        labelfreq1.setName("labelfreq1"); // NOI18N
        add(labelfreq1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 170, 60, 10));

        labelVD1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelVD1.setText(bundle.getString("VIDEO_DELAY")); // NOI18N
        labelVD1.setName("labelVD1"); // NOI18N
        add(labelVD1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 450, 60, 9));

        labelAD1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelAD1.setText(bundle.getString("AUDIO_DELAY")); // NOI18N
        labelAD1.setName("labelAD1"); // NOI18N
        add(labelAD1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 470, 60, 9));

        jSeparator3.setName("jSeparator3"); // NOI18N
        jSeparator3.setPreferredSize(new java.awt.Dimension(48, 10));
        add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 394, 110, 10));

        jSlSpinX.setMajorTickSpacing(10);
        jSlSpinX.setMaximum(1920);
        jSlSpinX.setMinimum(-1920);
        jSlSpinX.setMinorTickSpacing(1);
        jSlSpinX.setSnapToTicks(true);
        jSlSpinX.setValue(0);
        jSlSpinX.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinX.setName("jSlSpinX"); // NOI18N
        jSlSpinX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinXStateChanged(evt);
            }
        });
        add(jSlSpinX, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 250, 150, 20));

        jSlSpinY.setMajorTickSpacing(10);
        jSlSpinY.setMaximum(1080);
        jSlSpinY.setMinimum(-1080);
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
        add(jSlSpinY, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 270, 150, 20));

        jSlSpinCW.setMaximum(1920);
        jSlSpinCW.setValue(0);
        jSlSpinCW.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinCW.setName("jSlSpinCW"); // NOI18N
        jSlSpinCW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinCWStateChanged(evt);
            }
        });
        add(jSlSpinCW, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 400, 150, 20));

        jSlSpinCH.setMaximum(1080);
        jSlSpinCH.setValue(0);
        jSlSpinCH.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinCH.setName("jSlSpinCH"); // NOI18N
        jSlSpinCH.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinCHStateChanged(evt);
            }
        });
        add(jSlSpinCH, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 420, 150, 20));

        jSlSpinW.setMaximum(1920);
        jSlSpinW.setMinimum(1);
        jSlSpinW.setSnapToTicks(true);
        jSlSpinW.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinW.setName("jSlSpinW"); // NOI18N
        jSlSpinW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinWStateChanged(evt);
            }
        });
        add(jSlSpinW, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 290, 150, 20));

        jSlSpinH.setMaximum(1080);
        jSlSpinH.setSnapToTicks(true);
        jSlSpinH.setValue(0);
        jSlSpinH.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinH.setName("jSlSpinH"); // NOI18N
        jSlSpinH.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinHStateChanged(evt);
            }
        });
        add(jSlSpinH, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 310, 150, 20));

        jSlSpinO.setValue(100);
        jSlSpinO.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinO.setName("jSlSpinO"); // NOI18N
        jSlSpinO.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinOStateChanged(evt);
            }
        });
        add(jSlSpinO, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 350, 150, 20));

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
        add(jSlSpinVD, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 440, 150, 20));

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
        add(jSlSpinAD, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 460, 150, 20));

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
        add(jSlSpinZOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 369, 150, 30));

        jSeparator1.setName("jSeparator1"); // NOI18N
        jSeparator1.setPreferredSize(new java.awt.Dimension(48, 10));
        add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(126, 240, 150, 10));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/DVBLogo.png"))); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(153, 135, 120, 110));

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setName("jSeparator2"); // NOI18N
        add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 401, 10, 80));

        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator5.setName("jSeparator5"); // NOI18N
        add(jSeparator5, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 140, 10, 250));

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator4.setName("jSeparator4"); // NOI18N
        add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 7, 10, 126));

        jSeparator7.setName("jSeparator7"); // NOI18N
        jSeparator7.setPreferredSize(new java.awt.Dimension(48, 10));
        add(jSeparator7, new org.netbeans.lib.awtextra.AbsoluteConstraints(126, 135, 150, 10));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/splash100.png"))); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(143, 14, 120, 110));

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
        add(tglPause, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 114, 30, 20));

        jCheckBox1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        jCheckBox1.setText("Lock A/R ");
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
        add(jCheckBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(61, 331, -1, -1));

        jSeparator6.setName("jSeparator6"); // NOI18N
        jSeparator6.setPreferredSize(new java.awt.Dimension(48, 10));
        add(jSeparator6, new org.netbeans.lib.awtextra.AbsoluteConstraints(126, 339, 150, 10));

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
        add(tglPreview, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 336, 30, 20));

        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents
    private void tglActiveStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglActiveStreamActionPerformed
        if (tglActiveStream.isSelected()) {
            this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.green));
            if (txtChName.getText() != null) {
                stream.setChName(txtChName.getText());
            }
            spinW1.setEnabled(false);
            jSlSpinCW.setEnabled(false);
            spinH1.setEnabled(false);
            jSlSpinCH.setEnabled(false);
            spinVDelay.setEnabled(false);
            jSlSpinVD.setEnabled(false);
            spinADelay.setEnabled(false);
            jSlSpinAD.setEnabled(false);
            frequency.setEnabled(false);
            bandwidth.setEnabled(false);
            prgNumber.setEnabled(false);
            txtChName.setEditable(false);
            tglPause.setEnabled(true);
            tglPreview.setEnabled(false);
            stream.read();
        } else {
            this.setBorder(BorderFactory.createEmptyBorder());
            tglPreview.setEnabled(true);
            spinW1.setEnabled(true);
            jSlSpinCH.setEnabled(stream.hasVideo());
            spinH1.setEnabled(true);
            jSlSpinCW.setEnabled(stream.hasVideo());
            spinVDelay.setEnabled(stream.hasVideo());
            jSlSpinVD.setEnabled(stream.hasVideo());
            spinADelay.setEnabled(stream.hasAudio());
            jSlSpinAD.setEnabled(stream.hasAudio());
            frequency.setEnabled(true);
            bandwidth.setEnabled(true);
            prgNumber.setEnabled(true);
            txtChName.setEditable(true);
            tglPause.setSelected(false);
            tglPause.setEnabled(false);
            stream.setisPaused(false);
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
        stream.setHeight(h);
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

    private void frequencyStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_frequencyStateChanged
        stream.setDVBFrequency((Integer) frequency.getValue()*1000000);
    }//GEN-LAST:event_frequencyStateChanged

    private void bandwidthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_bandwidthStateChanged
        stream.setDVBBandwidth((Integer) bandwidth.getValue());
    }//GEN-LAST:event_bandwidthStateChanged

    private void prgNumberStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_prgNumberStateChanged
        stream.setDVBChannelNumber((Integer) prgNumber.getValue());
    }//GEN-LAST:event_prgNumberStateChanged

    private void txtChNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtChNameActionPerformed
        stream.setName(txtChName.getText());
        setToolTipText(txtChName.getText());
    }//GEN-LAST:event_txtChNameActionPerformed

    private void txtChNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtChNameFocusLost
        stream.setName(txtChName.getText());
        setToolTipText(txtChName.getText());
    }//GEN-LAST:event_txtChNameFocusLost

    private void jSlSpinXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinXStateChanged
        stream.setX(jSlSpinX.getValue());
        spinX.setValue(jSlSpinX.getValue());
    }//GEN-LAST:event_jSlSpinXStateChanged

    private void jSlSpinYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinYStateChanged
        stream.setY(jSlSpinY.getValue());
        spinY.setValue(jSlSpinY.getValue());
    }//GEN-LAST:event_jSlSpinYStateChanged

    private void jSlSpinCWStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinCWStateChanged
        stream.setCaptureWidth(jSlSpinCW.getValue());
        spinW1.setValue(jSlSpinCW.getValue());
    }//GEN-LAST:event_jSlSpinCWStateChanged

    private void jSlSpinCHStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinCHStateChanged
        stream.setCaptureHeight(jSlSpinCH.getValue());
        spinH1.setValue(jSlSpinCH.getValue());
    }//GEN-LAST:event_jSlSpinCHStateChanged

    private void jSlSpinWStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinWStateChanged
        int w = (Integer) jSlSpinW.getValue();
        spinW.setValue(w);
        int h = oldH;
        if (lockRatio){
            h = (oldH * w) / oldW; 
            spinH.setValue(h);
            jSlSpinH.setValue(h);
        }
        stream.setWidth(w);
        stream.setHeight(h);       
    }//GEN-LAST:event_jSlSpinWStateChanged

    private void jSlSpinHStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinHStateChanged
        stream.setHeight(jSlSpinH.getValue());
        spinH.setValue(jSlSpinH.getValue());
        if (!lockRatio){
            oldH = stream.getHeight();
        }
    }//GEN-LAST:event_jSlSpinHStateChanged

    private void jSlSpinOStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinOStateChanged
        stream.setOpacity(jSlSpinO.getValue());
        spinOpacity.setValue(jSlSpinO.getValue());
    }//GEN-LAST:event_jSlSpinOStateChanged

    private void jSlSpinVDStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinVDStateChanged
        stream.setVDelay(jSlSpinVD.getValue());
        spinVDelay.setValue(jSlSpinVD.getValue());
    }//GEN-LAST:event_jSlSpinVDStateChanged

    private void jSlSpinADStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinADStateChanged
        stream.setADelay(jSlSpinAD.getValue());
        spinADelay.setValue(jSlSpinAD.getValue());
    }//GEN-LAST:event_jSlSpinADStateChanged

    private void jSlSpinZOrderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinZOrderStateChanged
        stream.setZOrder(jSlSpinZOrder.getValue());
        spinZOrder.setValue(jSlSpinZOrder.getValue());
    }//GEN-LAST:event_jSlSpinZOrderStateChanged

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
        if (!stream.getisPaused()) {
            volume = jSlSpinV.getValue()/100f;
        }
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
        }
    }//GEN-LAST:event_jCheckBox1ActionPerformed

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

    private void jSlSpinVFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jSlSpinVFocusLost
        if (jSlSpinV.getValue()/100f != 0) {
            vol = jSlSpinV.getValue()/100f;
        }
    }//GEN-LAST:event_jSlSpinVFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner bandwidth;
    private javax.swing.JSpinner frequency;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
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
    private javax.swing.JLabel labelAD1;
    private javax.swing.JLabel labelBand;
    private javax.swing.JLabel labelCH;
    private javax.swing.JLabel labelCW;
    private javax.swing.JLabel labelH;
    private javax.swing.JLabel labelInv;
    private javax.swing.JLabel labelO;
    private javax.swing.JLabel labelVD1;
    private javax.swing.JLabel labelW;
    private javax.swing.JLabel labelX;
    private javax.swing.JLabel labelY;
    private javax.swing.JLabel labelZ;
    private javax.swing.JLabel labelfreq;
    private javax.swing.JLabel labelfreq1;
    private javax.swing.JLabel lblCurtain;
    private javax.swing.JPanel panPreview;
    private javax.swing.JSpinner prgNumber;
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
    private javax.swing.JToggleButton tglPause;
    private javax.swing.JToggleButton tglPreview;
    private javax.swing.JTextField txtChName;
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
}