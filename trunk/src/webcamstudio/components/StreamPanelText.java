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

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import webcamstudio.WebcamStudio;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.streams.SourceText;
import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;


/**
 *
 * @author patrick (modified by karl)
 */
public class StreamPanelText extends javax.swing.JPanel implements Stream.Listener, StreamDesktop.Listener{

    Stream stream = null;
    SourceText sTx = null;
    boolean stopClock = false;
    boolean stopCDown = false;
    private Timer time = new Timer();
    private Timer countDown = new Timer();
    private TimerTask clockIn = new clock();
    private TimerTask cDownIn = new cDown();
    boolean lockRatio = false;
    int oldW = 1;
    int oldH = 1;

    /** Creates new form StreamPanel
     * @param stream */
    @SuppressWarnings("unchecked") 
    public StreamPanelText(Stream stream) {

        initComponents();
        if (WebcamStudio.theme.toLowerCase().equals("classic")){
            lblTxtMode.setForeground(new java.awt.Color(0, 0, 0));
        }
        oldW = stream.getWidth();
        oldH = stream.getHeight();
        
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = e.getAllFonts(); // Get the fonts
        for (Font f : fonts){
            model.addElement(f.getName());
        }
        cboFonts.setModel(model);
        
        this.stream = stream;
        sTx = (SourceText) stream;
        spinX.setValue(stream.getX());
        spinY.setValue(stream.getY());
        spinW.setValue(stream.getWidth());
        spinH.setValue(stream.getHeight());
        cboFonts.setSelectedItem(stream.getFont());
        txtHexColor.setText(Integer.toHexString(stream.getColor()));
        spinZOrder.setValue(stream.getZOrder());
        spinDuration.setEnabled(!stream.getPlayList());
        spinDuration.setValue(stream.getDuration());
        if (stream.getIsATimer()){
            if (stream.getIsQRCode()) {
                lblTxtMode.setText("QR Clock Mode.");
            } else {
                lblTxtMode.setText("Text Clock Mode.");
                tglQRCode.setEnabled(false);
                tglCDown.setEnabled(false);
            }
        } else if (stream.getIsACDown()) {
            if (stream.getIsQRCode()) {
                lblTxtMode.setText("QR Timer Mode.");
            } else {
                lblTxtMode.setText("Timer Mode.");
                tglQRCode.setEnabled(false);
                tglClock.setEnabled(false);
            }
        } else {
            txtArea.setText(stream.getContent());
            lblTxtMode.setText("Text Mode.");
        }
        jcbPlayList.setSelected(stream.getPlayList());
        setToolTipText(lblTxtMode.getText());
        cboFonts.setEnabled(!(stream.getIsQRCode()));
        txtHexColor.setEnabled(!(stream.getIsQRCode()));
        btnSelectColor.setEnabled(!(stream.getIsQRCode()));
        tglClock.setSelected(stream.getIsATimer());
        tglQRCode.setSelected(stream.getIsQRCode());
        tglCDown.setSelected(stream.getIsACDown());
        stream.setListener(this);
    }

    @Override
    public void closeSource() {
        // nothing here.
    }
    
    class clock extends TimerTask{
        
        @Override
        public void run() {
            while (!stopClock){
                long milliSeconds = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
                Date resultdate = new Date(milliSeconds);
                String time = sdf.format(resultdate);
                stream.updateLineContent(time);
                Tools.sleep(1000);
            }
            StreamPanelText.clock.this.stop();
        }
        public void stop() {
            time.cancel();
            time.purge();
            clockIn.cancel();
            stopClock = true;
            stream.stop();
//            System.out.println("Stopping Clock ...");
        }
        
    }
    
    public String getHHMMSS (long seconds) {
        long hr = seconds/3600;
        long rem = seconds%3600;
        long mn = rem/60;
        long sec = rem%60;
        String hrStr = (hr<10 ? "0" : "")+hr;
        String mnStr = (mn<10 ? "0" : "")+mn;
        String secStr = (sec<10 ? "0" : "")+sec;
        return hrStr+":"+mnStr+":"+secStr;
    }
    
    class cDown extends TimerTask{
        
        @Override
        public void run() {
            
            if (stream.getPlayList()) {
                while (!stopCDown){
                    int chTimeTo = ChannelPanel.totalToTimer;
                    final String chTotalTime = getHHMMSS(chTimeTo);
                    int dur = chTimeTo - ChannelPanel.timeToTimer;
                    String duration = getHHMMSS(dur);
                    stream.updateLineContent(duration + " / " + chTotalTime);
                    Tools.sleep(1000);
                }
            } else {
                int timeTo = stream.getDuration();
                final String totalTime = getHHMMSS(timeTo);
                int count = 0;
                String duration = "";
                while (!stopCDown && count < timeTo){
                    duration = getHHMMSS(count);
                    stream.updateLineContent(duration + " / " + totalTime);
                    Tools.sleep(1000);
                    count ++;
                }
            }
            StreamPanelText.cDown.this.stop();
        }
        public void stop() {
            countDown.cancel();
            countDown.purge();
            cDownIn.cancel();
            stopCDown = true;
            stream.stop();
            stream.updateStatus();
//            System.out.println("Stopping Timer ...");
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        spinX = new javax.swing.JSpinner();
        spinY = new javax.swing.JSpinner();
        spinW = new javax.swing.JSpinner();
        spinH = new javax.swing.JSpinner();
        labelFont = new javax.swing.JLabel();
        lblColor = new javax.swing.JLabel();
        cboFonts = new javax.swing.JComboBox();
        txtHexColor = new javax.swing.JFormattedTextField();
        btnSelectColor = new javax.swing.JButton();
        tglActiveStream = new javax.swing.JToggleButton();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator7 = new javax.swing.JSeparator();
        labelText = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        labelX1 = new javax.swing.JLabel();
        labelY1 = new javax.swing.JLabel();
        labelW1 = new javax.swing.JLabel();
        labelH1 = new javax.swing.JLabel();
        jSlSpinX = new javax.swing.JSlider();
        jSlSpinY = new javax.swing.JSlider();
        jSlSpinW = new javax.swing.JSlider();
        jSlSpinH = new javax.swing.JSlider();
        jSeparator5 = new javax.swing.JSeparator();
        spinZOrder = new javax.swing.JSpinner();
        labelZ1 = new javax.swing.JLabel();
        jSlSpinZOrder = new javax.swing.JSlider();
        jSeparator3 = new javax.swing.JSeparator();
        tglClock = new javax.swing.JToggleButton();
        tglQRCode = new javax.swing.JToggleButton();
        jcbLockAR = new javax.swing.JCheckBox();
        jSeparator8 = new javax.swing.JSeparator();
        tglPreview = new javax.swing.JToggleButton();
        scrAreaTxt = new javax.swing.JScrollPane();
        txtArea = new javax.swing.JTextArea();
        lblTxtMode = new javax.swing.JLabel();
        tglCDown = new javax.swing.JToggleButton();
        spinDuration = new javax.swing.JSpinner();
        jcbPlayList = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        setFocusTraversalPolicyProvider(true);
        setMaximumSize(new java.awt.Dimension(286, 370));
        setPreferredSize(new java.awt.Dimension(286, 370));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        spinX.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinX.setName("spinX"); // NOI18N
        spinX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinXStateChanged(evt);
            }
        });
        add(spinX, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 160, 50, -1));

        spinY.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinY.setName("spinY"); // NOI18N
        spinY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinYStateChanged(evt);
            }
        });
        add(spinY, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 180, 50, -1));

        spinW.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinW.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        spinW.setName("spinW"); // NOI18N
        spinW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinWStateChanged(evt);
            }
        });
        add(spinW, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 200, 50, -1));

        spinH.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinH.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        spinH.setName("spinH"); // NOI18N
        spinH.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinHStateChanged(evt);
            }
        });
        add(spinH, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 220, 50, -1));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        labelFont.setText(bundle.getString("FONT")); // NOI18N
        labelFont.setName("labelFont"); // NOI18N
        add(labelFont, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 308, 72, -1));

        lblColor.setText(bundle.getString("COLOR")); // NOI18N
        lblColor.setName("lblColor"); // NOI18N
        add(lblColor, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 338, 72, -1));

        cboFonts.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboFonts.setName("cboFonts"); // NOI18N
        cboFonts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboFontsActionPerformed(evt);
            }
        });
        add(cboFonts, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 300, 210, -1));

        try {
            txtHexColor.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("HHHHHH")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        txtHexColor.setName("txtHexColor"); // NOI18N
        txtHexColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtHexColorActionPerformed(evt);
            }
        });
        txtHexColor.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtHexColorFocusLost(evt);
            }
        });
        add(txtHexColor, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 330, 178, -1));

        btnSelectColor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/applications-graphics.png"))); // NOI18N
        btnSelectColor.setToolTipText(bundle.getString("COLOR")); // NOI18N
        btnSelectColor.setName("btnSelectColor"); // NOI18N
        btnSelectColor.setPreferredSize(new java.awt.Dimension(32, 20));
        btnSelectColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectColorActionPerformed(evt);
            }
        });
        add(btnSelectColor, new org.netbeans.lib.awtextra.AbsoluteConstraints(248, 333, -1, -1));

        tglActiveStream.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-start.png"))); // NOI18N
        tglActiveStream.setName("tglActiveStream"); // NOI18N
        tglActiveStream.setPreferredSize(new java.awt.Dimension(32, 20));
        tglActiveStream.setRolloverEnabled(false);
        tglActiveStream.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglActiveStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglActiveStreamActionPerformed(evt);
            }
        });
        add(tglActiveStream, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 84, 110, 20));

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator4.setName("jSeparator4"); // NOI18N
        add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 7, 10, 142));

        jSeparator7.setName("jSeparator7"); // NOI18N
        jSeparator7.setPreferredSize(new java.awt.Dimension(48, 10));
        add(jSeparator7, new org.netbeans.lib.awtextra.AbsoluteConstraints(126, 152, 150, 10));

        labelText.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelText.setText(bundle.getString("ENTER_TEXT")); // NOI18N
        labelText.setToolTipText("");
        labelText.setName("labelText"); // NOI18N
        add(labelText, new org.netbeans.lib.awtextra.AbsoluteConstraints(177, 137, 70, -1));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/FontCC.png"))); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 7, -1, 50));

        labelX1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelX1.setText(bundle.getString("X")); // NOI18N
        labelX1.setName("labelX1"); // NOI18N
        add(labelX1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, 10, 10));

        labelY1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelY1.setText(bundle.getString("Y")); // NOI18N
        labelY1.setName("labelY1"); // NOI18N
        add(labelY1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 10, -1));

        labelW1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelW1.setText(bundle.getString("WIDTH")); // NOI18N
        labelW1.setName("labelW1"); // NOI18N
        add(labelW1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 52, -1));

        labelH1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelH1.setText(bundle.getString("HEIGHT")); // NOI18N
        labelH1.setName("labelH1"); // NOI18N
        add(labelH1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 40, -1));

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
        add(jSlSpinX, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 160, 150, 20));

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
        add(jSlSpinY, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 180, 150, 20));

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
        add(jSlSpinW, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 200, 150, 20));

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
        add(jSlSpinH, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 220, 150, 20));

        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator5.setName("jSeparator5"); // NOI18N
        add(jSeparator5, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 160, 10, 129));

        spinZOrder.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinZOrder.setName("spinZOrder"); // NOI18N
        spinZOrder.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinZOrderStateChanged(evt);
            }
        });
        add(spinZOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 260, 50, -1));

        labelZ1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelZ1.setText(bundle.getString("LAYER")); // NOI18N
        labelZ1.setMaximumSize(new java.awt.Dimension(30, 10));
        labelZ1.setMinimumSize(new java.awt.Dimension(30, 10));
        labelZ1.setName("labelZ1"); // NOI18N
        labelZ1.setPreferredSize(new java.awt.Dimension(30, 10));
        add(labelZ1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 270, 40, 9));

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

        jSeparator3.setName("jSeparator3"); // NOI18N
        jSeparator3.setPreferredSize(new java.awt.Dimension(48, 10));
        add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 290, 268, 10));

        tglClock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/clock-add.png"))); // NOI18N
        tglClock.setToolTipText("Switch to Date/Clock Mode.");
        tglClock.setName("tglClock"); // NOI18N
        tglClock.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/clock-add.png"))); // NOI18N
        tglClock.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/clock-add-selected.png"))); // NOI18N
        tglClock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglClockActionPerformed(evt);
            }
        });
        add(tglClock, new org.netbeans.lib.awtextra.AbsoluteConstraints(62, 107, 55, 20));

        tglQRCode.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/qrcode.png"))); // NOI18N
        tglQRCode.setToolTipText("Switch to QRCode mode.");
        tglQRCode.setName("tglQRCode"); // NOI18N
        tglQRCode.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/qrcode.png"))); // NOI18N
        tglQRCode.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/qrcode-selected.png"))); // NOI18N
        tglQRCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglQRCodeActionPerformed(evt);
            }
        });
        add(tglQRCode, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 107, 55, 20));

        jcbLockAR.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        jcbLockAR.setText("Lock A/R");
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
        add(jcbLockAR, new org.netbeans.lib.awtextra.AbsoluteConstraints(64, 241, -1, -1));

        jSeparator8.setName("jSeparator8"); // NOI18N
        jSeparator8.setPreferredSize(new java.awt.Dimension(48, 10));
        add(jSeparator8, new org.netbeans.lib.awtextra.AbsoluteConstraints(126, 249, 150, 10));

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
        add(tglPreview, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 245, 40, 20));

        scrAreaTxt.setAutoscrolls(true);
        scrAreaTxt.setName("scrAreaTxt"); // NOI18N

        txtArea.setName("txtArea"); // NOI18N
        txtArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtAreaKeyReleased(evt);
            }
        });
        scrAreaTxt.setViewportView(txtArea);

        add(scrAreaTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(129, 5, 149, 127));

        lblTxtMode.setForeground(new java.awt.Color(255, 220, 0));
        lblTxtMode.setName("lblTxtMode"); // NOI18N
        add(lblTxtMode, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 133, 105, 20));

        tglCDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/Chrono.png"))); // NOI18N
        tglCDown.setToolTipText("Switch to Timer Mode.");
        tglCDown.setName("tglCDown"); // NOI18N
        tglCDown.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/Chrono.png"))); // NOI18N
        tglCDown.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/Chrono_selected.png"))); // NOI18N
        tglCDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglCDownActionPerformed(evt);
            }
        });
        add(tglCDown, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 61, 34, 20));

        spinDuration.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinDuration.setToolTipText("Set duration in seconds.");
        spinDuration.setName("spinDuration"); // NOI18N
        spinDuration.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinDurationStateChanged(evt);
            }
        });
        add(spinDuration, new org.netbeans.lib.awtextra.AbsoluteConstraints(57, 60, 61, -1));

        jcbPlayList.setToolTipText("PlayList Mode Switch.");
        jcbPlayList.setName("jcbPlayList"); // NOI18N
        jcbPlayList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbPlayListActionPerformed(evt);
            }
        });
        add(jcbPlayList, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 62, 20, -1));

        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents

    private void cboFontsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboFontsActionPerformed
        stream.setFont(cboFonts.getSelectedItem().toString());
    }//GEN-LAST:event_cboFontsActionPerformed

    private void txtHexColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtHexColorActionPerformed
        stream.setColor(Integer.parseInt(txtHexColor.getText().trim(),16));
    }//GEN-LAST:event_txtHexColorActionPerformed

    private void txtHexColorFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtHexColorFocusLost
        stream.setColor(Integer.parseInt(txtHexColor.getText().trim(),16));
    }//GEN-LAST:event_txtHexColorFocusLost

    private void btnSelectColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectColorActionPerformed
        ColorChooser c = new ColorChooser(null,true);
        c.setLocationRelativeTo(this);
        c.setVisible(true);
        Color color = c.getColor();
        if (color!=null){
            txtHexColor.setText(Integer.toHexString(color.getRGB()).substring(2));
            stream.setColor(color.getRGB());
        }    
    }//GEN-LAST:event_btnSelectColorActionPerformed

    private void tglActiveStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglActiveStreamActionPerformed
        if (tglClock.isSelected()){
            time = new Timer();
            clockIn = new clock();
            if (tglActiveStream.isSelected()) {
                this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.green));
                tglClock.setEnabled(false);
                tglQRCode.setEnabled(false);
                tglCDown.setEnabled(false);
                tglPreview.setEnabled(false);
                if (stream.getIsQRCode()) {
                    lblTxtMode.setText("QR Clock Mode.");
                } else {
                    lblTxtMode.setText("Text Clock Mode.");
                }
                stopClock=false;
                time.schedule(clockIn, 0);
                stream.read();
                System.out.println("Starting Clock ...");
            } else {
                this.setBorder(BorderFactory.createEmptyBorder());
                tglPreview.setEnabled(true);
                tglClock.setEnabled(true);
                tglQRCode.setEnabled(!tglClock.isSelected());
                tglCDown.setEnabled(!tglClock.isSelected());
                time.cancel();
                time.purge();
                clockIn.cancel();
                stopClock=true;
                stream.stop();
//                System.out.println("Stopping Clock ...");
            }
        } else if (tglCDown.isSelected()) {
            if (stream.getDuration() == 0 && !stream.getPlayList()) {
                tglActiveStream.setSelected(false);
            } else {
                countDown = new Timer();
                cDownIn = new cDown();
                if (tglActiveStream.isSelected()) {
                    stopCDown = false;
                    countDown.schedule(cDownIn, 0);
                    stream.read();
                    this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.green));
                    tglClock.setEnabled(false);
                    tglQRCode.setEnabled(false);
                    tglCDown.setEnabled(false);
                    tglPreview.setEnabled(false);
                    if (stream.getIsQRCode()) {
                        lblTxtMode.setText("QR Timer Mode.");
                    } else {
                        lblTxtMode.setText("Timer Mode.");
                    }
                } else {
                    countDown.cancel();
                    countDown.purge();
                    cDownIn.cancel();
                    stopCDown = true;
                    stream.stop();
                    this.setBorder(BorderFactory.createEmptyBorder());
                    tglPreview.setEnabled(true);
                    tglClock.setEnabled(!tglCDown.isSelected());
                    tglQRCode.setEnabled(!tglCDown.isSelected());
                    tglCDown.setEnabled(true);
                }
            }
        } else {
            if (tglActiveStream.isSelected()) {
                tglPreview.setEnabled(false);
                tglClock.setEnabled(false);
                tglQRCode.setEnabled(false);
                tglCDown.setEnabled(false);
                this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.green));
                stream.read();
            } else {
                tglPreview.setEnabled(true);
                tglClock.setEnabled(true);
                tglQRCode.setEnabled(true);
                tglCDown.setEnabled(true);
                this.setBorder(BorderFactory.createEmptyBorder());
                stream.stop();
            }
        }
    }//GEN-LAST:event_tglActiveStreamActionPerformed

    private void spinXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinXStateChanged
        stream.setX((Integer)spinX.getValue());
        jSlSpinX.setValue((Integer)spinX.getValue());
    }//GEN-LAST:event_spinXStateChanged

    private void spinYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinYStateChanged
        stream.setY((Integer)spinY.getValue());
        jSlSpinY.setValue((Integer)spinY.getValue());
    }//GEN-LAST:event_spinYStateChanged

    private void spinWStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinWStateChanged
        int w = (Integer) spinW.getValue();
        jSlSpinW.setValue(w);
        int h = oldH;
        if (lockRatio){
            h = (oldH * w) / oldW; 
            if (h >= 1) {
                spinH.setValue(h);
            } else {
                h = 1;
            }
        }
        stream.setWidth(w);
    }//GEN-LAST:event_spinWStateChanged

    private void spinHStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinHStateChanged
        int h = (Integer) spinH.getValue();
        jSlSpinH.setValue(h);
        if (!lockRatio){
            oldH = stream.getHeight();
        }
        stream.setHeight(h);
    }//GEN-LAST:event_spinHStateChanged

    private void jSlSpinXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinXStateChanged
        spinX.setValue(jSlSpinX.getValue());
    }//GEN-LAST:event_jSlSpinXStateChanged

    private void jSlSpinYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinYStateChanged
        spinY.setValue(jSlSpinY.getValue());
    }//GEN-LAST:event_jSlSpinYStateChanged

    private void jSlSpinWStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinWStateChanged
        int w = (Integer) jSlSpinW.getValue();
        spinW.setValue(w);
    }//GEN-LAST:event_jSlSpinWStateChanged

    private void jSlSpinHStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinHStateChanged
        spinH.setValue(jSlSpinH.getValue());
    }//GEN-LAST:event_jSlSpinHStateChanged

    private void spinZOrderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinZOrderStateChanged
        stream.setZOrder((Integer) spinZOrder.getValue());
        jSlSpinZOrder.setValue((Integer) spinZOrder.getValue());
    }//GEN-LAST:event_spinZOrderStateChanged

    private void jSlSpinZOrderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinZOrderStateChanged
        spinZOrder.setValue(jSlSpinZOrder.getValue());
    }//GEN-LAST:event_jSlSpinZOrderStateChanged

    private void tglClockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglClockActionPerformed
        if (tglClock.isSelected()){
            tglQRCode.setEnabled(false);
            tglCDown.setEnabled(false);
            stream.setIsATimer(true);
            if (stream.getIsQRCode()) {
                lblTxtMode.setText("QR Clock Mode.");
            } else {
                lblTxtMode.setText("Text Clock Mode.");
            }   
            stopClock=false;
        } else {
            tglQRCode.setEnabled(true);
            tglCDown.setEnabled(true);
            stream.setIsATimer(false);
            if (stream.getIsQRCode()) {
                lblTxtMode.setText("QR Code Mode.");
            } else {
                lblTxtMode.setText("Text Mode.");
                stream.updateContent(txtArea.getText());
            }
            stopClock=true;
        }
    }//GEN-LAST:event_tglClockActionPerformed

    private void tglQRCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglQRCodeActionPerformed
        if (tglQRCode.isSelected()){
            stream.setIsQRCode(true);
            lblTxtMode.setText("QR Code Mode.");
            cboFonts.setEnabled(!(stream.getIsQRCode()));
            txtHexColor.setEnabled(!(stream.getIsQRCode()));
            btnSelectColor.setEnabled(!(stream.getIsQRCode()));
        } else {
            stream.setIsQRCode(false);
            lblTxtMode.setText("Text Mode.");
            stream.updateContent(txtArea.getText());
            cboFonts.setEnabled(!(stream.getIsQRCode()));
            txtHexColor.setEnabled(!(stream.getIsQRCode()));
            btnSelectColor.setEnabled(!(stream.getIsQRCode()));
        }
    }//GEN-LAST:event_tglQRCodeActionPerformed

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

    private void tglPreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglPreviewActionPerformed
        if (tglPreview.isSelected()) {
            stream.setPreView(true);
        } else {
            stream.setPreView(false);
        }
    }//GEN-LAST:event_tglPreviewActionPerformed

    private void txtAreaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAreaKeyReleased
        if (!stream.getIsATimer() && !stream.getIsACDown()) {
            stream.updateContent(txtArea.getText());
        }
    }//GEN-LAST:event_txtAreaKeyReleased

    private void tglCDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglCDownActionPerformed
        if (tglCDown.isSelected()){
            tglQRCode.setEnabled(false);
            tglClock.setEnabled(false);
            stream.setIsACDown(true);
            if (stream.getIsQRCode()) {
                lblTxtMode.setText("QR Timer Mode.");
            } else {
                lblTxtMode.setText("Timer Mode.");
            }   
            stopCDown = false;
        } else {
            tglQRCode.setEnabled(true);
            tglClock.setEnabled(true);
            stream.setIsACDown(false);
            if (stream.getIsQRCode()) {
                lblTxtMode.setText("QR Code Mode.");
            } else {
                lblTxtMode.setText("Text Mode.");
            }
            stopCDown = true;
        }
    }//GEN-LAST:event_tglCDownActionPerformed

    private void spinDurationStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinDurationStateChanged
        stream.setDuration((Integer) spinDuration.getValue());
    }//GEN-LAST:event_spinDurationStateChanged

    private void jcbPlayListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbPlayListActionPerformed
        if (jcbPlayList.isSelected()){
            stream.setPlayList(true);
            spinDuration.setEnabled(false);
        } else {
            stream.setPlayList(false);
            spinDuration.setEnabled(true);
        }
    }//GEN-LAST:event_jcbPlayListActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSelectColor;
    private javax.swing.JComboBox cboFonts;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSlider jSlSpinH;
    private javax.swing.JSlider jSlSpinW;
    private javax.swing.JSlider jSlSpinX;
    private javax.swing.JSlider jSlSpinY;
    private javax.swing.JSlider jSlSpinZOrder;
    private javax.swing.JCheckBox jcbLockAR;
    private javax.swing.JCheckBox jcbPlayList;
    private javax.swing.JLabel labelFont;
    private javax.swing.JLabel labelH1;
    private javax.swing.JLabel labelText;
    private javax.swing.JLabel labelW1;
    private javax.swing.JLabel labelX1;
    private javax.swing.JLabel labelY1;
    private javax.swing.JLabel labelZ1;
    private javax.swing.JLabel lblColor;
    private javax.swing.JLabel lblTxtMode;
    private javax.swing.JScrollPane scrAreaTxt;
    private javax.swing.JSpinner spinDuration;
    private javax.swing.JSpinner spinH;
    private javax.swing.JSpinner spinW;
    private javax.swing.JSpinner spinX;
    private javax.swing.JSpinner spinY;
    private javax.swing.JSpinner spinZOrder;
    private javax.swing.JToggleButton tglActiveStream;
    private javax.swing.JToggleButton tglCDown;
    private javax.swing.JToggleButton tglClock;
    private javax.swing.JToggleButton tglPreview;
    private javax.swing.JToggleButton tglQRCode;
    private javax.swing.JTextArea txtArea;
    private javax.swing.JFormattedTextField txtHexColor;
    // End of variables declaration//GEN-END:variables

    @Override
    public void sourceUpdated(Stream stream) {
        if (stream.isPlaying()){
            this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.green));
            tglPreview.setEnabled(false);
            if (stream.getIsATimer()){
                stopClock=false;
                time = new Timer();
                clockIn = new clock();
                time.schedule(clockIn, 0);
                tglClock.setSelected(true);
                tglCDown.setSelected(false);
                System.out.println("Source Updated Starting Clock ...");
            } else if (stream.getIsACDown()) {
                stopCDown = false;
                countDown = new Timer();
                cDownIn = new cDown();
                countDown.schedule(cDownIn, 0);
                tglCDown.setSelected(true);
                tglClock.setSelected(false);
                System.out.println("Source Updated Starting Timer ...");
            } else {
                stopCDown = true;
                countDown.cancel();
                countDown.purge();
                cDownIn.cancel();
                stopClock=true;
                time.cancel();
                time.purge();
                clockIn.cancel();
                tglClock.setSelected(false);
                tglCDown.setSelected(false);
            }
        } else {
            this.setBorder(BorderFactory.createEmptyBorder());
            tglPreview.setEnabled(true);
            if (stream.getIsATimer()){
                stopClock = true;
                time.cancel();
                time.purge();
                clockIn.cancel();
                tglClock.setSelected(true);
//                System.out.println("Source Updated Stopping Clock ...");
            } else if (stream.getIsACDown()) {
                stopCDown = true;
                countDown.cancel();
                countDown.purge();
                cDownIn.cancel();
                tglCDown.setSelected(true);
//                System.out.println("Source Updated Stopping Timer ...");
            } else {
                tglClock.setSelected(false);
                tglCDown.setSelected(false);
            }
        }
        if (stream.getIsQRCode()){
            tglQRCode.setSelected(true);
            cboFonts.setEnabled(!(stream.getIsQRCode()));
            txtHexColor.setEnabled(!(stream.getIsQRCode()));
            btnSelectColor.setEnabled(!(stream.getIsQRCode()));
        } else {
            tglQRCode.setSelected(false);
            cboFonts.setEnabled(!(stream.getIsQRCode()));
            txtHexColor.setEnabled(!(stream.getIsQRCode()));
            btnSelectColor.setEnabled(!(stream.getIsQRCode()));
        }
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
        spinDuration.setValue(stream.getDuration());
        cboFonts.setSelectedItem(this.stream.getFont());
        txtHexColor.setText(Integer.toHexString(this.stream.getColor()));
        spinZOrder.setValue(stream.getZOrder());
        if (!stream.getIsATimer() && !stream.getIsACDown()) {
            txtArea.setText(this.stream.getContent());
        }
        tglActiveStream.setSelected(stream.isPlaying());
        if (stream.getIsATimer() || stream.getIsACDown() || stream.isPlaying()) {
            tglQRCode.setEnabled(false);
        } else {
            tglQRCode.setEnabled(true);
        }
        if (stream.getIsATimer() || stream.isPlaying()) {
            tglCDown.setEnabled(false);
        } else {
            tglCDown.setEnabled(true);
        }
        if (stream.getIsACDown() || stream.isPlaying()) {
            tglClock.setEnabled(false);
        } else {
            tglClock.setEnabled(true);
        }
        jcbPlayList.setSelected(stream.getPlayList());
    }

    @Override
    public void updatePreview(BufferedImage image) {
        // nothing here.
    }

    @Override
    public void selectedSource(Stream source) {
        // nothing here.
    }
}