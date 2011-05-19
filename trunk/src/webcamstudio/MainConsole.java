/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainConsole.java
 *
 * Created on 2011-05-16, 16:02:26
 */
package webcamstudio;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import org.gstreamer.Gst;
import webcamstudio.components.Preview;
import webcamstudio.exporter.vloopback.V4L2Loopback;
import webcamstudio.exporter.vloopback.VideoOutput;
import webcamstudio.layout.Layout;
import webcamstudio.sound.AudioMixer;
import webcamstudio.studio.Studio;

/**
 *
 * @author patrick
 */
public class MainConsole extends javax.swing.JFrame implements InfoListener {

    private Studio studio = null;
    private VideoOutput output = null;
    private webcamstudio.components.Mixer mixer = null;
    private int width = 320;
    private int height = 240;
    private int pixFormat = VideoOutput.RGB24;
    private String device = "/dev/video1";
    private Layout currentLayout = null;
    private Preview preview = null;
    private AudioMixer audioMixer = null;

    /** Creates new form MainConsole */
    public MainConsole() {
        Gst.init(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("WEBCAMSTUDIO"), new String[0]);
        initComponents();
        javax.swing.DefaultComboBoxModel model = new javax.swing.DefaultComboBoxModel();
        cboLayouts.setModel(model);
        javax.swing.DefaultListCellRenderer renderer = new javax.swing.DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(javax.swing.JList list, Object value, int index, boolean sel, boolean hasFocus) {
                Component retValue = super.getListCellRendererComponent(list, value, index, sel, hasFocus);
                if (retValue instanceof JLabel) {
                    JLabel label = (JLabel) retValue;
                    label.setForeground(Color.BLACK);
                    if ((value) instanceof Layout) {
                        Layout l = (Layout) value;
                        label.setText(l.toString());
                        label.setToolTipText(l.toString());
                        label.setIcon(new ImageIcon(l.getPreview().getScaledInstance(32, 32, BufferedImage.SCALE_FAST)));
                        label.setDisabledIcon(label.getIcon());
                    }
                }
                return retValue;
            }
        };
        cboLayouts.setRenderer(renderer);
        java.awt.Image img = getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/icon.png"));
        setIconImage(img);

        output = null;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    repaint();
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainConsole.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }).start();
        audioMixer = new AudioMixer();

    }

    private void selectOutputDevice(String dev) {

        if (output != null) {
            output.close();
        }
        output = new V4L2Loopback(this);
        output.open(dev, width, height, pixFormat);
    }

    private void loadStudioFromFile(java.io.File providedFile) {
        java.io.File currentStudioFile = null;

        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser(providedFile);
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
                studio = new Studio();
                studio.loadStudio(currentStudioFile);
                width = studio.getWidth();
                height = studio.getHeight();
                pixFormat = studio.getPixFormat();
                device = studio.getDevice();
                selectOutputDevice(device);
                mixer = new webcamstudio.components.Mixer();
                mixer.setSize(width, height);
                mixer.setOutput(output);
                mixer.setFramerate(30);
                audioMixer.stop();
                if (studio.isAudioMixerActive()) {
                    audioMixer.start();
                }
                for (Layout l : studio.getLayouts()) {
                    l.enterLayout();
                    currentLayout = l;
                    break;
                }
                javax.swing.DefaultComboBoxModel model = new javax.swing.DefaultComboBoxModel(studio.getLayouts());
                cboLayouts.setModel(model);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cboLayouts = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        mnuStudiosLoad = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        setTitle(bundle.getString("WEBCAMSTUDIO_FOR_GNU/LINUX")); // NOI18N
        setAlwaysOnTop(true);
        setResizable(false);

        cboLayouts.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboLayouts.setName("cboLayouts"); // NOI18N
        cboLayouts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboLayoutsActionPerformed(evt);
            }
        });

        jButton1.setText(bundle.getString("PREVIEW")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jMenuBar1.setName("jMenuBar1"); // NOI18N

        jMenu1.setText(bundle.getString("STUDIOS")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        mnuStudiosLoad.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        mnuStudiosLoad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/document-open.png"))); // NOI18N
        mnuStudiosLoad.setText(bundle.getString("LOAD")); // NOI18N
        mnuStudiosLoad.setName("mnuStudiosLoad"); // NOI18N
        mnuStudiosLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuStudiosLoadActionPerformed(evt);
            }
        });
        jMenu1.add(mnuStudiosLoad);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addComponent(cboLayouts, javax.swing.GroupLayout.Alignment.LEADING, 0, 376, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cboLayouts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mnuStudiosLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuStudiosLoadActionPerformed
        loadStudioFromFile(null);
}//GEN-LAST:event_mnuStudiosLoadActionPerformed

    private void cboLayoutsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboLayoutsActionPerformed

        cboLayouts.setEnabled(false);
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (currentLayout != null) {
                    currentLayout.exitLayout();
                }
                currentLayout = (Layout) cboLayouts.getSelectedItem();
                audioMixer.setMicVolume(currentLayout.getMicVolume());
                audioMixer.setSysVolume(currentLayout.getSysVolume());
                audioMixer.setLowFilter(currentLayout.getMicLow());
                audioMixer.setMiddleFilter(currentLayout.getMicMiddle());
                audioMixer.setHighFilter(currentLayout.getMicHigh());

                currentLayout.enterLayout();
                cboLayouts.setEnabled(true);
            }
        }).start();

    }//GEN-LAST:event_cboLayoutsActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (preview != null) {
            preview.dispose();
            preview = null;
        }
        preview = new Preview(this, false, mixer);
        preview.addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent e) {
                preview.stopMe();
            }
        });
        preview.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new MainConsole().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboLayouts;
    private javax.swing.JButton jButton1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem mnuStudiosLoad;
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
}
