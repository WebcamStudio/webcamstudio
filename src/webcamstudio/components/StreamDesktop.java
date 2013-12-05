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
import javax.swing.Box;
import webcamstudio.streams.SourceDVB;
import webcamstudio.streams.SourceImageGif;
import webcamstudio.streams.SourceImageU;
import webcamstudio.streams.SourceMicrophone;
import webcamstudio.streams.SourceURL;
import webcamstudio.streams.SourceText;
import webcamstudio.streams.SourceWebcam;
import webcamstudio.streams.Stream;

/**
 *
 * @author patrick (modified by karl)
 */
public class StreamDesktop extends javax.swing.JInternalFrame {

    StreamPanel panel = null;
    StreamPanelDVB panelDVB = null;
    StreamPanelURL panelURL = null;
    Stream stream = null;
    Listener listener = null;

/*    StreamDesktop(Stream webcam, ActionListener aThis) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/
    public interface Listener{
        public void selectedSource(Stream source);
    }
    /** Creates new form StreamDesktop
     * @param s
     * @param l */
    public StreamDesktop(Stream s,Listener l) {
        listener = l;
        stream = s;
        initComponents();
        if (s instanceof SourceText) {
            StreamPanelText p = new StreamPanelText((SourceText)s);
            this.setLayout(new BorderLayout());
            this.add(p, BorderLayout.CENTER);
            this.setTitle(s.getName());
            this.setVisible(true);
            jMControls.setEnabled(false);
            JMBackEnd.setEnabled(false);
            s.setPanelType("PanelText");
        } else if (s instanceof SourceDVB) {
            StreamPanelDVB p = new StreamPanelDVB(s);
            this.setLayout(new BorderLayout());
            this.add(p, BorderLayout.CENTER);
            this.setTitle(s.getName());
            this.setVisible(true);
            JMBackEnd.setEnabled(false);
            stream.setComm("GS");
            panelDVB = p;
            s.setPanelType("PanelDVB");
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
                        break;
                    case "GS":
                        jCBGStreamer.setSelected(true);
                        stream.setComm("GS");
                        jCBAVConv.setSelected(false);
                        break;
                }
            } else {
                    jCBAVConv.setSelected(true);
                    stream.setComm("AV");
                    jCBGStreamer.setSelected(false);
            }
            JMBackEnd.setEnabled(true);
            panelURL = p;
            s.setPanelType("PanelURL");
            jCBShowSliders.setEnabled(false);
        } else {
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
                    default:
                        if (stream instanceof SourceWebcam || stream instanceof SourceMicrophone ||stream instanceof SourceImageU) {
                        jCBGStreamer.setSelected(true);
                        stream.setComm("GS");
                        jCBAVConv.setSelected(false);
                        } else {
                        jCBAVConv.setSelected(true);
                        stream.setComm("AV");
                        jCBGStreamer.setSelected(false);
                        }
                        break;
                }
            } else {
                if (stream instanceof SourceWebcam || stream instanceof SourceMicrophone ||stream instanceof SourceImageU) {
                    jCBGStreamer.setSelected(true);
                    stream.setComm("GS");
                    jCBAVConv.setSelected(false);
                } else {
                    jCBAVConv.setSelected(true);
                    stream.setComm("AV");
                    jCBGStreamer.setSelected(false);
                }
            }
            if (stream instanceof SourceImageGif){
                JMBackEnd.setEnabled(false);
            }
            panel = p;
            s.setPanelType("Panel");
            jCBMoreOptions.setEnabled(true);
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
        jCBMoreOptions = new javax.swing.JCheckBoxMenuItem();
        jCBShowSliders = new javax.swing.JCheckBoxMenuItem();
        JMBackEnd = new javax.swing.JMenu();
        jCBGStreamer = new javax.swing.JCheckBoxMenuItem();
        jCBAVConv = new javax.swing.JCheckBoxMenuItem();

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
                formInternalFrameDeiconified(evt);
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
        jMBOptions.setPreferredSize(new java.awt.Dimension(74, 13));

        jMControls.setForeground(new java.awt.Color(74, 7, 1));
        jMControls.setText("Controls");
        jMControls.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jMControls.setName("jMControls"); // NOI18N

        jCBMoreOptions.setText("Show more Options");
        jCBMoreOptions.setName("jCBMoreOptions"); // NOI18N
        jCBMoreOptions.setPreferredSize(new java.awt.Dimension(169, 15));
        jCBMoreOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBMoreOptionsActionPerformed(evt);
            }
        });
        jMControls.add(jCBMoreOptions);
        jCBMoreOptions.getAccessibleContext().setAccessibleParent(jMControls);

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

        jMBOptions.add(jMControls);

        JMBackEnd.setForeground(new java.awt.Color(74, 7, 1));
        JMBackEnd.setText("BkEnd");
        JMBackEnd.setBorderPainted(true);
        JMBackEnd.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        JMBackEnd.setName("JMBackEnd"); // NOI18N

        jCBGStreamer.setText("GStreamer");
        jCBGStreamer.setName("jCBGStreamer"); // NOI18N
        jCBGStreamer.setPreferredSize(new java.awt.Dimension(107, 15));
        jCBGStreamer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBGStreamerActionPerformed(evt);
            }
        });
        JMBackEnd.add(jCBGStreamer);

        jCBAVConv.setText("AVConv");
        jCBAVConv.setName("jCBAVConv"); // NOI18N
        jCBAVConv.setPreferredSize(new java.awt.Dimension(107, 15));
        jCBAVConv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBAVConvActionPerformed(evt);
            }
        });
        JMBackEnd.add(jCBAVConv);

        jMBOptions.add(Box.createHorizontalGlue());

        jMBOptions.add(JMBackEnd);

        setJMenuBar(jMBOptions);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formInternalFrameIconified(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameIconified
       if (panel!=null){
        this.setFrameIcon(panel.getIcon());
       }
    }//GEN-LAST:event_formInternalFrameIconified

    private void formInternalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameDeiconified
    }//GEN-LAST:event_formInternalFrameDeiconified

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        stream.destroy();
        stream = null;
        panel = null;
        webcamstudio.WebcamStudio.tabControls.removeAll();
        webcamstudio.WebcamStudio.tabControls.repaint();        
//        System.gc();
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
                    this.setSize(new Dimension(this.getWidth(),440));
                    this.revalidate();
                    this.repaint();
                } else {
                    this.setSize(new Dimension(this.getWidth(),334));
                    this.revalidate();
                    this.repaint();
                }   break;
            case "PanelDVB":
                if (jCBMoreOptions.isSelected()){
                    this.setSize(new Dimension(this.getWidth(),550));
                    this.revalidate();
                    this.repaint();
                } else {
                    this.setSize(new Dimension(this.getWidth(),444));
                    this.revalidate();
                    this.repaint();
                }   break;
            case "PanelURL":
                if (jCBMoreOptions.isSelected()){
                    this.setSize(new Dimension(this.getWidth(),480));
                    this.revalidate();
                    this.repaint();
                } else {
                    this.setSize(new Dimension(this.getWidth(),374));
                    this.revalidate();
                    this.repaint();
                }   break;
        }
    }//GEN-LAST:event_jCBMoreOptionsActionPerformed

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

    private void jCBGStreamerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBGStreamerActionPerformed
        if (jCBGStreamer.isSelected()){
            stream.setComm("GS");
            jCBAVConv.setSelected(false);
        } else {
            jCBAVConv.setSelected(true);
            jCBGStreamer.setSelected(false);
            stream.setComm("AV");
        }
    }//GEN-LAST:event_jCBGStreamerActionPerformed

    private void jCBAVConvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBAVConvActionPerformed
        if (jCBAVConv.isSelected()){
            stream.setComm("AV");
            jCBGStreamer.setSelected(false);
        } else {
            jCBGStreamer.setSelected(true);
            jCBAVConv.setSelected(false);
            stream.setComm("GS");
        }
    }//GEN-LAST:event_jCBAVConvActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu JMBackEnd;
    private javax.swing.JCheckBoxMenuItem jCBAVConv;
    private javax.swing.JCheckBoxMenuItem jCBGStreamer;
    private javax.swing.JCheckBoxMenuItem jCBMoreOptions;
    private javax.swing.JCheckBoxMenuItem jCBShowSliders;
    private javax.swing.JMenuBar jMBOptions;
    private javax.swing.JMenu jMControls;
    // End of variables declaration//GEN-END:variables
}
