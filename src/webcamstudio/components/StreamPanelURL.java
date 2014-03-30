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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.SpinnerNumberModel;
import webcamstudio.streams.Stream;



/**
 *
 * @author patrick (modified by karl)
 */
public class StreamPanelURL extends javax.swing.JPanel implements Stream.Listener, StreamDesktop.Listener{

    Stream stream = null;
    Viewer viewer = new Viewer();
    float volume = 0;
    

    /** Creates new form StreamPanel
     * @param stream */
    public StreamPanelURL(Stream stream) {

        initComponents();
        
        stream.setIsIPCam(false);
        viewer.setOpaque(true);
        viewer.setVisible(true);
        viewer.setBackground(Color.black);
        panPreview.add(viewer, BorderLayout.CENTER);
        this.stream = stream;
        spinX.setValue(stream.getX());
        jSlSpinX.setValue(stream.getX());
        spinY.setValue(stream.getY());
        jSlSpinY.setValue(stream.getY());
        spinW.setValue(stream.getWidth());
        jSlSpinW.setValue(stream.getWidth());
        spinH.setValue(stream.getHeight());
        jSlSpinH.setValue(stream.getHeight());
        spinOpacity.setModel(new SpinnerNumberModel(100, 0, 100, 1));
        spinOpacity.setValue(stream.getOpacity());
        jSlSpinO.setValue(stream.getOpacity());
        spinVolume.setModel(new SpinnerNumberModel(50, 0, 300, 1));
        spinVolume.setValue(stream.getVolume() * 100);
        String jSVol = spinVolume.getValue().toString().replace(".0", "");
        int jVol = Integer.parseInt(jSVol);
        jSlSpinV.setValue(jVol);
        spinVolume.setEnabled(stream.hasAudio());
        jSlSpinV.setEnabled(stream.hasAudio());
        spinZOrder.setValue(stream.getZOrder());
        jSlSpinZOrder.setValue(stream.getZOrder());
        spinH1.setValue(stream.getCaptureHeight());
        jSlSpinCH.setValue(stream.getCaptureHeight());
        spinW1.setValue(stream.getCaptureWidth());
        jSlSpinCW.setValue(stream.getCaptureWidth());
        spinVDelay.setValue(stream.getVDelay());
        jSlSpinVD.setValue(stream.getVDelay());
        spinADelay.setValue(stream.getADelay());
        jSlSpinAD.setValue(stream.getADelay());
        spinVDelay.setEnabled(stream.hasVideo());
        jSlSpinVD.setEnabled(stream.hasVideo());
        spinADelay.setEnabled(stream.hasAudio());
        tglAudio.setSelected(!stream.hasAudio());
        tglVideo.setSelected(!stream.hasVideo());
        spinSeek.setValue(stream.getSeek());
        jSlSpinSeek.setValue(stream.getSeek());
        spinSeek.setEnabled(stream.needSeekCTRL());
        txtWebURL.setText(stream.getWebURL());
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
//    public Viewer detachViewer(){
//        panPreview.remove(viewer);
//        panPreview.revalidate();
//        return viewer;
//    }
//    public Viewer attachViewer(){
//        panPreview.add(viewer, BorderLayout.CENTER);
//        panPreview.revalidate();
//        return viewer;
//    }
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
        jSlSpinX.setValue(stream.getX());
        spinY.setValue(stream.getY());
        jSlSpinY.setValue(stream.getY());
        spinW.setValue(stream.getWidth());
        jSlSpinW.setValue(stream.getWidth());
        spinH.setValue(stream.getHeight());
        jSlSpinH.setValue(stream.getHeight());
        spinW1.setValue(stream.getCaptureWidth());
        jSlSpinCW.setValue(stream.getCaptureWidth());
        spinH1.setValue(stream.getCaptureHeight());
        jSlSpinCH.setValue(stream.getCaptureHeight());
        spinOpacity.setValue(stream.getOpacity());
        jSlSpinO.setValue(stream.getOpacity());
        spinVolume.setValue(stream.getVolume() * 100);
        String jSVol = spinVolume.getValue().toString().replace(".0", "");
        int jVol = Integer.parseInt(jSVol);
        jSlSpinV.setValue(jVol);
        spinZOrder.setValue(stream.getZOrder());
        jSlSpinZOrder.setValue(stream.getZOrder());
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
            spinSeek.setEnabled(false);
            jSlSpinSeek.setEnabled(false);
            txtWebURL.setEditable(false);
            tglAudio.setEnabled(false);
            tglVideo.setEnabled(false);
            tglPause.setEnabled(true);
        } else {
            this.setBorder(BorderFactory.createEmptyBorder());
            spinH1.setEnabled(true);
            jSlSpinCH.setEnabled(true);
            spinW1.setEnabled(true);
            jSlSpinCW.setEnabled(true);
            spinVDelay.setEnabled(true);
            jSlSpinVD.setEnabled(true);
            spinADelay.setEnabled(true);
            jSlSpinAD.setEnabled(true);
            spinSeek.setEnabled(true);
            jSlSpinSeek.setEnabled(true);
            txtWebURL.setEditable(true);
            tglPause.setSelected(false);
            tglPause.setEnabled(false);
            if (tglAudio.isSelected()) {
                tglAudio.setEnabled(true);
            } else if (tglVideo.isSelected()) {
                tglVideo.setEnabled(true);
            } else {
                tglAudio.setEnabled(true);
                tglVideo.setEnabled(true);
            }
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
        tglVideo = new javax.swing.JToggleButton();
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
        labelV = new javax.swing.JLabel();
        labelZ = new javax.swing.JLabel();
        labelCW = new javax.swing.JLabel();
        spinW1 = new javax.swing.JSpinner();
        labelCH = new javax.swing.JLabel();
        spinH1 = new javax.swing.JSpinner();
        spinVDelay = new javax.swing.JSpinner();
        spinADelay = new javax.swing.JSpinner();
        spinSeek = new javax.swing.JSpinner();
        labelSeek = new javax.swing.JLabel();
        labelURL = new javax.swing.JLabel();
        txtWebURL = new javax.swing.JTextField();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator7 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jSlSpinX = new javax.swing.JSlider();
        jSlSpinY = new javax.swing.JSlider();
        jSlSpinCW = new javax.swing.JSlider();
        jSlSpinCH = new javax.swing.JSlider();
        jSlSpinW = new javax.swing.JSlider();
        jSlSpinH = new javax.swing.JSlider();
        jSlSpinO = new javax.swing.JSlider();
        jSlSpinV = new javax.swing.JSlider();
        jSlSpinVD = new javax.swing.JSlider();
        jSlSpinAD = new javax.swing.JSlider();
        jSlSpinSeek = new javax.swing.JSlider();
        jSlSpinZOrder = new javax.swing.JSlider();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        labelVD = new javax.swing.JLabel();
        labelAD = new javax.swing.JLabel();
        tglPause = new javax.swing.JToggleButton();

        setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        setMaximumSize(new java.awt.Dimension(286, 356));
        setMinimumSize(new java.awt.Dimension(277, 336));
        setPreferredSize(new java.awt.Dimension(286, 336));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panPreview.setBackground(new java.awt.Color(113, 113, 113));
        panPreview.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        panPreview.setMaximumSize(new java.awt.Dimension(90, 60));
        panPreview.setMinimumSize(new java.awt.Dimension(90, 60));
        panPreview.setName("panPreview"); // NOI18N
        panPreview.setPreferredSize(new java.awt.Dimension(90, 60));
        panPreview.setLayout(new java.awt.BorderLayout());

        tglAudio.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        tglAudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/audio-volume-muted.png"))); // NOI18N
        tglAudio.setToolTipText("No Audio Switch (Force Only Video Source)");
        tglAudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglAudio.setMaximumSize(new java.awt.Dimension(40, 32));
        tglAudio.setMinimumSize(new java.awt.Dimension(26, 30));
        tglAudio.setName("tglAudio"); // NOI18N
        tglAudio.setPreferredSize(new java.awt.Dimension(20, 20));
        tglAudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglAudioActionPerformed(evt);
            }
        });
        panPreview.add(tglAudio, java.awt.BorderLayout.PAGE_START);

        tglVideo.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        tglVideo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/edit-delete.png"))); // NOI18N
        tglVideo.setToolTipText("No Video Switch (Force Only Audio Source)");
        tglVideo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglVideo.setMaximumSize(new java.awt.Dimension(40, 32));
        tglVideo.setMinimumSize(new java.awt.Dimension(26, 30));
        tglVideo.setName("tglVideo"); // NOI18N
        tglVideo.setPreferredSize(new java.awt.Dimension(20, 20));
        tglVideo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglVideoActionPerformed(evt);
            }
        });
        panPreview.add(tglVideo, java.awt.BorderLayout.PAGE_END);

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
        spinW.setName("spinW"); // NOI18N
        spinW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinWStateChanged(evt);
            }
        });
        add(spinW, new org.netbeans.lib.awtextra.AbsoluteConstraints(58, 220, 60, -1));

        spinH.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinH.setName("spinH"); // NOI18N
        spinH.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinHStateChanged(evt);
            }
        });
        add(spinH, new org.netbeans.lib.awtextra.AbsoluteConstraints(58, 240, 60, -1));

        spinOpacity.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinOpacity.setName("spinOpacity"); // NOI18N
        spinOpacity.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinOpacityStateChanged(evt);
            }
        });
        add(spinOpacity, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 260, 50, -1));

        spinVolume.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinVolume.setName("spinVolume"); // NOI18N
        spinVolume.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinVolumeStateChanged(evt);
            }
        });
        add(spinVolume, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 280, 50, -1));

        tglActiveStream.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-start.png"))); // NOI18N
        tglActiveStream.setName("tglActiveStream"); // NOI18N
        tglActiveStream.setRolloverEnabled(false);
        tglActiveStream.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglActiveStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglActiveStreamActionPerformed(evt);
            }
        });
        add(tglActiveStream, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 129, 78, 20));

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
        add(labelO, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 270, 40, -1));

        labelV.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelV.setText(bundle.getString("VOLUME")); // NOI18N
        labelV.setName("labelV"); // NOI18N
        add(labelV, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 290, 40, 9));

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
        spinH1.setName("spinH1"); // NOI18N
        spinH1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinH1StateChanged(evt);
            }
        });
        add(spinH1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 350, 50, -1));

        spinVDelay.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinVDelay.setToolTipText("Milliseconds");
        spinVDelay.setName("spinVDelay"); // NOI18N
        spinVDelay.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinVDelayStateChanged(evt);
            }
        });
        add(spinVDelay, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 370, 60, -1));

        spinADelay.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinADelay.setToolTipText("Milliseconds");
        spinADelay.setName("spinADelay"); // NOI18N
        spinADelay.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinADelayStateChanged(evt);
            }
        });
        add(spinADelay, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 390, 60, -1));

        spinSeek.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinSeek.setName("spinSeek"); // NOI18N
        spinSeek.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinSeekStateChanged(evt);
            }
        });
        add(spinSeek, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 410, 50, -1));

        labelSeek.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelSeek.setText(bundle.getString("SEEK")); // NOI18N
        labelSeek.setMaximumSize(new java.awt.Dimension(30, 10));
        labelSeek.setMinimumSize(new java.awt.Dimension(30, 10));
        labelSeek.setName("labelSeek"); // NOI18N
        labelSeek.setPreferredSize(new java.awt.Dimension(30, 10));
        add(labelSeek, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 420, 50, 9));

        labelURL.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelURL.setText(bundle.getString("ENTER_URL")); // NOI18N
        labelURL.setToolTipText("");
        labelURL.setName("labelURL"); // NOI18N
        add(labelURL, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 142, 60, -1));

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

        jSeparator7.setName("jSeparator7"); // NOI18N
        jSeparator7.setPreferredSize(new java.awt.Dimension(48, 10));
        add(jSeparator7, new org.netbeans.lib.awtextra.AbsoluteConstraints(126, 135, 150, 10));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/splash100.png"))); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(143, 14, 120, 110));

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
        add(jSlSpinX, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 180, 150, 20));

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
        add(jSlSpinY, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 200, 150, 20));

        jSlSpinCW.setMaximum(1920);
        jSlSpinCW.setValue(0);
        jSlSpinCW.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinCW.setName("jSlSpinCW"); // NOI18N
        jSlSpinCW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinCWStateChanged(evt);
            }
        });
        add(jSlSpinCW, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 330, 150, 20));

        jSlSpinCH.setMaximum(1080);
        jSlSpinCH.setValue(0);
        jSlSpinCH.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinCH.setName("jSlSpinCH"); // NOI18N
        jSlSpinCH.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinCHStateChanged(evt);
            }
        });
        add(jSlSpinCH, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 350, 150, 20));

        jSlSpinW.setMaximum(1920);
        jSlSpinW.setSnapToTicks(true);
        jSlSpinW.setValue(0);
        jSlSpinW.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinW.setName("jSlSpinW"); // NOI18N
        jSlSpinW.setOpaque(true);
        jSlSpinW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinWStateChanged(evt);
            }
        });
        add(jSlSpinW, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 220, 150, 20));

        jSlSpinH.setMaximum(1080);
        jSlSpinH.setSnapToTicks(true);
        jSlSpinH.setValue(0);
        jSlSpinH.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinH.setName("jSlSpinH"); // NOI18N
        jSlSpinH.setOpaque(true);
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
        add(jSlSpinO, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 260, 150, 20));

        jSlSpinV.setMaximum(200);
        jSlSpinV.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinV.setName("jSlSpinV"); // NOI18N
        jSlSpinV.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinVStateChanged(evt);
            }
        });
        add(jSlSpinV, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 280, 150, 20));

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
        add(jSlSpinSeek, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 410, 150, 20));

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
        add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 330, 10, 99));

        labelVD.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelVD.setText(bundle.getString("VIDEO_DELAY")); // NOI18N
        labelVD.setName("labelVD"); // NOI18N
        add(labelVD, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 380, 60, 9));

        labelAD.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelAD.setText(bundle.getString("AUDIO_DELAY")); // NOI18N
        labelAD.setName("labelAD"); // NOI18N
        add(labelAD, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 400, 60, 9));

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
        add(tglPause, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 129, 30, 20));

        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents
    private void tglActiveStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglActiveStreamActionPerformed
        if (tglActiveStream.isSelected()) {
            this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.green));
            if (txtWebURL.getText() != null) {
                stream.setWebURL((String) txtWebURL.getText());
                setToolTipText(txtWebURL.getText());
            }
            String webURL = stream.getWebURL();
            if (tglAudio.isSelected()) {
                stream.setHasAudio(false);
                stream.setHasVideo(true);
                stream.setOnlyVideo(true);
                stream.setOnlyAudio(false);
            } else if (tglVideo.isSelected()) {
                stream.setHasAudio(true);
                stream.setHasVideo(false);
                stream.setOnlyAudio(true);
                stream.setOnlyVideo(false);
            } else if (webURL.endsWith("mp3")){
                stream.setHasVideo(false);
                stream.setHasAudio(true);
            } else if (webURL.endsWith("jpg") || webURL.endsWith("png")){
                stream.setIsStillPicture(true);
                stream.setHasAudio(false);
                stream.setHasVideo(true);
            } else {
                stream.setHasVideo(true);
                stream.setHasAudio(true);
                stream.setOnlyAudio(false);
                stream.setOnlyVideo(false);
            }
            
            if (webURL.startsWith("rtsp")) {
                stream.setRTSP(true);
            } else if (webURL.startsWith("rtmp")) {
                stream.setRTMP(true);
            }
            spinX.setEnabled(!tglVideo.isSelected());
            jSlSpinX.setEnabled(!tglVideo.isSelected());
            spinY.setEnabled(!tglVideo.isSelected());
            jSlSpinY.setEnabled(!tglVideo.isSelected());
            spinW1.setEnabled(false);
            jSlSpinCW.setEnabled(false);
            spinH1.setEnabled(false);
            jSlSpinCH.setEnabled(false);
            spinW.setEnabled(!tglVideo.isSelected());
            jSlSpinW.setEnabled(!tglVideo.isSelected());
            spinH.setEnabled(!tglVideo.isSelected());
            jSlSpinH.setEnabled(!tglVideo.isSelected());
            spinOpacity.setEnabled(!tglVideo.isSelected());
            jSlSpinO.setEnabled(!tglVideo.isSelected());
            spinVDelay.setEnabled(false);
            jSlSpinVD.setEnabled(false);
            spinADelay.setEnabled(false);
            jSlSpinAD.setEnabled(false);
            spinSeek.setEnabled(false);
            jSlSpinSeek.setEnabled(false);
            txtWebURL.setEditable(false);
            tglAudio.setEnabled(false);
            tglVideo.setEnabled(false);
            tglPause.setEnabled(true);
            stream.read();
        } else {
            this.setBorder(BorderFactory.createEmptyBorder());
            spinX.setEnabled(!tglVideo.isSelected());
            jSlSpinX.setEnabled(!tglVideo.isSelected());
            spinY.setEnabled(!tglVideo.isSelected());
            jSlSpinY.setEnabled(!tglVideo.isSelected());
            spinW1.setEnabled(!tglVideo.isSelected());
            jSlSpinCW.setEnabled(!tglVideo.isSelected());
            spinH1.setEnabled(!tglVideo.isSelected());
            jSlSpinCH.setEnabled(!tglVideo.isSelected());
            spinW.setEnabled(!tglVideo.isSelected());
            jSlSpinW.setEnabled(!tglVideo.isSelected());
            spinH.setEnabled(!tglVideo.isSelected());
            jSlSpinH.setEnabled(!tglVideo.isSelected());
            spinVDelay.setEnabled(!tglVideo.isSelected());
            jSlSpinVD.setEnabled(!tglVideo.isSelected());
            spinADelay.setEnabled(stream.hasAudio());
            jSlSpinAD.setEnabled(stream.hasAudio());
            spinSeek.setEnabled(stream.needSeekCTRL());
            jSlSpinSeek.setEnabled(stream.needSeekCTRL());
            spinOpacity.setEnabled(!tglVideo.isSelected());
            jSlSpinO.setEnabled(!tglVideo.isSelected());
            txtWebURL.setEditable(true);
            stream.setRTSP(false);
            stream.setRTMP(false);
            stream.setOnlyAudio(false);
            stream.setOnlyVideo(false);
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
        stream.setWidth((Integer)spinW.getValue());
        jSlSpinW.setValue((Integer)spinW.getValue());
    }//GEN-LAST:event_spinWStateChanged

    private void spinHStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinHStateChanged
        stream.setHeight((Integer)spinH.getValue());
        jSlSpinH.setValue((Integer)spinH.getValue());
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
            v = ((Integer)value).floatValue();
        }
        stream.setVolume(v/100f);
        volume = v/100f;     
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
        stream.setSeek((Integer) spinSeek.getValue());
        jSlSpinSeek.setValue((Integer)spinSeek.getValue());     
    }//GEN-LAST:event_spinSeekStateChanged

    private void txtWebURLFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtWebURLFocusLost
        setToolTipText(txtWebURL.getText());
    }//GEN-LAST:event_txtWebURLFocusLost

    private void jSlSpinXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinXStateChanged
        stream.setX((Integer)jSlSpinX.getValue());
        spinX.setValue(jSlSpinX.getValue());
    }//GEN-LAST:event_jSlSpinXStateChanged

    private void jSlSpinYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinYStateChanged
        stream.setY((Integer)jSlSpinY.getValue());
        spinY.setValue(jSlSpinY.getValue());
    }//GEN-LAST:event_jSlSpinYStateChanged

    private void jSlSpinCWStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinCWStateChanged
        stream.setCaptureWidth((Integer)jSlSpinCW.getValue());
        spinW1.setValue(jSlSpinCW.getValue());
    }//GEN-LAST:event_jSlSpinCWStateChanged

    private void jSlSpinCHStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinCHStateChanged
        stream.setCaptureHeight((Integer)jSlSpinCH.getValue());
        spinH1.setValue(jSlSpinCH.getValue());
    }//GEN-LAST:event_jSlSpinCHStateChanged

    private void jSlSpinWStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinWStateChanged
        stream.setWidth((Integer)jSlSpinW.getValue());
        spinW.setValue(jSlSpinW.getValue());
    }//GEN-LAST:event_jSlSpinWStateChanged

    private void jSlSpinHStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinHStateChanged
        stream.setHeight((Integer)jSlSpinH.getValue());
        spinH.setValue(jSlSpinH.getValue());
    }//GEN-LAST:event_jSlSpinHStateChanged

    private void jSlSpinOStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinOStateChanged
        stream.setOpacity((Integer)jSlSpinO.getValue());
        spinOpacity.setValue(jSlSpinO.getValue());
    }//GEN-LAST:event_jSlSpinOStateChanged

    private void jSlSpinVStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinVStateChanged
        spinVolume.setValue(jSlSpinV.getValue());
        volume = jSlSpinV.getValue();
    }//GEN-LAST:event_jSlSpinVStateChanged

    private void jSlSpinVDStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinVDStateChanged
        stream.setVDelay((Integer)jSlSpinVD.getValue());
        spinVDelay.setValue(jSlSpinVD.getValue());
    }//GEN-LAST:event_jSlSpinVDStateChanged

    private void jSlSpinADStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinADStateChanged
        stream.setADelay((Integer)jSlSpinAD.getValue());
        spinADelay.setValue(jSlSpinAD.getValue());
    }//GEN-LAST:event_jSlSpinADStateChanged

    private void jSlSpinSeekStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinSeekStateChanged
        stream.setSeek((Integer)jSlSpinSeek.getValue());
        spinSeek.setValue(jSlSpinSeek.getValue());
    }//GEN-LAST:event_jSlSpinSeekStateChanged

    private void jSlSpinZOrderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinZOrderStateChanged
        stream.setZOrder((Integer)jSlSpinZOrder.getValue());
        spinZOrder.setValue(jSlSpinZOrder.getValue());
    }//GEN-LAST:event_jSlSpinZOrderStateChanged

    private void tglAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglAudioActionPerformed
        if (tglAudio.isSelected()) {
            tglVideo.setEnabled(false);
        } else {
            tglVideo.setEnabled(true);
        }
    }//GEN-LAST:event_tglAudioActionPerformed

    private void tglVideoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglVideoActionPerformed
        if (tglVideo.isSelected()) {
            tglAudio.setEnabled(false);
        } else {
            tglAudio.setEnabled(true);
        }
    }//GEN-LAST:event_tglVideoActionPerformed

    private void tglPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglPauseActionPerformed
        if (tglPause.isSelected()){
            volume = stream.getVolume();
            stream.setVolume(0);
            stream.pause();
        } else {
            stream.setVolume(volume);
            stream.play();
        }
    }//GEN-LAST:event_tglPauseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel3;
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
    private javax.swing.JSlider jSlSpinSeek;
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
    private javax.swing.JLabel labelSeek;
    private javax.swing.JLabel labelURL;
    private javax.swing.JLabel labelV;
    private javax.swing.JLabel labelVD;
    private javax.swing.JLabel labelW;
    private javax.swing.JLabel labelX;
    private javax.swing.JLabel labelY;
    private javax.swing.JLabel labelZ;
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
    private javax.swing.JToggleButton tglVideo;
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
}