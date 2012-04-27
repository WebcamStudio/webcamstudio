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
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import webcamstudio.channels.MasterChannels;
import webcamstudio.components.*;
import webcamstudio.exporter.vloopback.VideoDevice;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.mixers.SystemPlayer;
import webcamstudio.streams.*;
import webcamstudio.util.Tools;
import webcamstudio.util.Tools.OS;

/**
 *
 * @author patrick
 */
public class WebcamStudio extends javax.swing.JFrame implements StreamDesktop.Listener {

    Preferences prefs = null;
    Properties animations = new Properties();
    OutputRecorder recorder = new OutputRecorder();

    /**
     * Creates new form WebcamStudio
     */
    public WebcamStudio() {
        initComponents();
        String build = new Version().getBuild();
        setTitle("WebcamStudio " + Version.version + " (" + build + ")");
        ImageIcon icon = new ImageIcon(this.getClass().getResource("/webcamstudio/resources/icon.png"));
        this.setIconImage(icon.getImage());

        if (Tools.getOS() == OS.LINUX) {
            for (VideoDevice d : VideoDevice.getOutputDevices()) {
                Stream webcam = new SourceWebcam(d.getFile());
                webcam.setName(d.getName());
                StreamDesktop frame = new StreamDesktop(webcam, this);
                desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
                try {
                    frame.setSelected(true);
                } catch (PropertyVetoException ex) {
                    Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (Tools.getOS() == OS.WINDOWS) {
            Stream webcam = new SourceWebcam("Default");
            StreamDesktop frame = new StreamDesktop(webcam, this);
            desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        }
        desktop.setDropTarget(new DropTarget() {

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
                    System.out.println(data.getClass().getCanonicalName());
                    if (data instanceof Reader) {
                        Reader reader = (Reader) data;
                        char[] text = new char[65536];
                        int count = reader.read(text);
                        files = new String(text).trim();
                    } else if (data instanceof InputStream) {
                        InputStream list = (InputStream) data;
                        java.io.InputStreamReader reader = new java.io.InputStreamReader(list);
                        char[] text = new char[65536];
                        int count = reader.read(text);
                        files = new String(text).trim();
                    } else {
                        List list = (List)data;
                        for (Object o : list){
                            files += new File(o.toString()).toURI().toURL().toString() + "\n";
                        }
                    }
                    if (files.length() > 0) {
                        String[] lines = files.split("\n");
                        for (String line : lines) {
                            System.out.println(line);
                            File file = new File(new URL(line.trim()).toURI());
                            if (file.exists()) {
                                fileName = file.getName();
                                Stream stream = Stream.getInstance(file);
                                if (stream != null) {
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
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        this.add(ResourceMonitor.getInstance(), BorderLayout.SOUTH);
        prefs = Preferences.userNodeForPackage(this.getClass());
        panControls.add(recorder, BorderLayout.NORTH);

        loadPrefs();
        MasterMixer.getInstance().start();
        panSources.add(new MasterPanel(), BorderLayout.WEST);
        initAnimations();
    }

    private StreamDesktop getNewStreamDesktop(Stream s) {
        return new StreamDesktop(s, this);
    }

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

    private void loadPrefs() {
        int x = prefs.getInt("main-x", 100);
        int y = prefs.getInt("main-y", 100);
        int w = prefs.getInt("main-w", 800);
        int h = prefs.getInt("main-h", 400);
        MasterMixer.getInstance().setWidth(prefs.getInt("mastermixer-w", 320));
        MasterMixer.getInstance().setHeight(prefs.getInt("mastermixer-h", 240));
        MasterMixer.getInstance().setRate(prefs.getInt("mastermixer-r", 15));
        mainSplit.setDividerLocation(prefs.getInt("split-x", 800));
        mainSplit.setDividerLocation(prefs.getInt("split-last-x", 800));
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

        toolbar = new javax.swing.JToolBar();
        btnAddFile = new javax.swing.JButton();
        btnAddDesktop = new javax.swing.JButton();
        btnAddText = new javax.swing.JButton();
        btnAddQRCode = new javax.swing.JButton();
        cboAnimations = new javax.swing.JComboBox();
        btnAddAnimation = new javax.swing.JButton();
        mainSplit = new javax.swing.JSplitPane();
        panSources = new javax.swing.JPanel();
        desktop = new javax.swing.JDesktopPane();
        panControls = new javax.swing.JPanel();
        tabControls = new javax.swing.JTabbedPane();
        lblSourceSelected = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("WebcamStudio");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        toolbar.setRollover(true);
        toolbar.setName("toolbar"); // NOI18N

        btnAddFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/list-add.png"))); // NOI18N
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

        cboAnimations.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboAnimations.setToolTipText(bundle.getString("ANIMATIONS")); // NOI18N
        cboAnimations.setName("cboAnimations"); // NOI18N
        toolbar.add(cboAnimations);

        btnAddAnimation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/emotes/face-cool.png"))); // NOI18N
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

        getContentPane().add(toolbar, java.awt.BorderLayout.NORTH);

        mainSplit.setDividerLocation(400);
        mainSplit.setName("mainSplit"); // NOI18N
        mainSplit.setOneTouchExpandable(true);

        panSources.setMinimumSize(new java.awt.Dimension(400, 400));
        panSources.setName("panSources"); // NOI18N
        panSources.setLayout(new java.awt.BorderLayout());

        desktop.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("SOURCES"))); // NOI18N
        desktop.setToolTipText(bundle.getString("DROP_SOURCSE")); // NOI18N
        desktop.setAutoscrolls(true);
        desktop.setMinimumSize(new java.awt.Dimension(400, 400));
        desktop.setName("desktop"); // NOI18N
        panSources.add(desktop, java.awt.BorderLayout.CENTER);

        mainSplit.setLeftComponent(panSources);

        panControls.setName("panControls"); // NOI18N
        panControls.setLayout(new java.awt.BorderLayout());

        tabControls.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("PROPERTIES"))); // NOI18N
        tabControls.setName("tabControls"); // NOI18N
        panControls.add(tabControls, java.awt.BorderLayout.CENTER);

        lblSourceSelected.setName("lblSourceSelected"); // NOI18N
        panControls.add(lblSourceSelected, java.awt.BorderLayout.SOUTH);

        mainSplit.setRightComponent(panControls);

        getContentPane().add(mainSplit, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        savePrefs();
        SystemPlayer.getInstance(null).stop();
        MasterChannels.getInstance().stopAllStream();
        MasterMixer.getInstance().stop();
    }//GEN-LAST:event_formWindowClosing

    private void btnAddDesktopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddDesktopActionPerformed
        SourceDesktop stream = new SourceDesktop();
        StreamDesktop frame = new StreamDesktop(stream, this);
        desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddDesktopActionPerformed

    private void btnAddTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTextActionPerformed
        SourceText stream = new SourceText("");
        StreamDesktop frame = new StreamDesktop(stream, this);
        desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddTextActionPerformed

    private void btnAddQRCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddQRCodeActionPerformed
        SourceQRCode stream = new SourceQRCode("webcamstudio");
        StreamDesktop frame = new StreamDesktop(stream, this);
        desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddQRCodeActionPerformed

    private void btnAddFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFileActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("WebcamStudio");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.showOpenDialog(this);
        File file = chooser.getSelectedFile();
        if (file != null) {
            Stream s = Stream.getInstance(file);
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

    }//GEN-LAST:event_btnAddFileActionPerformed

    private void btnAddAnimationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAnimationActionPerformed
        String key = cboAnimations.getSelectedItem().toString();
        String res = animations.getProperty(key);
        URL url = getClass().getResource("/webcamstudio/resources/animations/" + res);
        Stream stream = new SourceImageGif(key, url);
        StreamDesktop frame = new StreamDesktop(stream, this);
        desktop.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddAnimationActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(WebcamStudio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(WebcamStudio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(WebcamStudio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(WebcamStudio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new WebcamStudio().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddAnimation;
    private javax.swing.JButton btnAddDesktop;
    private javax.swing.JButton btnAddFile;
    private javax.swing.JButton btnAddQRCode;
    private javax.swing.JButton btnAddText;
    private javax.swing.JComboBox cboAnimations;
    private javax.swing.JDesktopPane desktop;
    private javax.swing.JLabel lblSourceSelected;
    private javax.swing.JSplitPane mainSplit;
    private javax.swing.JPanel panControls;
    private javax.swing.JPanel panSources;
    private javax.swing.JTabbedPane tabControls;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables

    @Override
    public void selectedSource(Stream source) {
        lblSourceSelected.setText(source.getName());
        lblSourceSelected.setToolTipText(source.getName());
        tabControls.removeAll();
        ArrayList<Component> comps = SourceControls.getControls(source);
        for (Component c : comps) {
            tabControls.add(c.getName(), c);
        }
    }
}
