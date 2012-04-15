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
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.SpinnerNumberModel;
import webcamstudio.streams.Stream;

/**
 *
 * @author patrick
 */
public class StreamPanel extends javax.swing.JPanel {

    Stream stream = null;
    Viewer viewer = new Viewer();
    Timer timer = new Timer();

    /** Creates new form StreamPanel */
    public StreamPanel(Stream stream) {

        initComponents();
//        this.setPreferredSize(MasterPanel.PANEL_SIZE);
//        this.setMaximumSize(MasterPanel.PANEL_SIZE);
        spinOpacity.setModel(new SpinnerNumberModel(50, 0, 100, 1));
        viewer.setOpaque(true);
        viewer.setVisible(true);
        viewer.setBackground(Color.red);
        panPreview.add(viewer, BorderLayout.CENTER);
        this.stream = stream;
        spinX.setValue(stream.getX());
        spinY.setValue(stream.getY());
        spinW.setValue(stream.getWidth());
        spinH.setValue(stream.getHeight());
        spinOpacity.setValue(stream.getOpacity());
        spinVolume.setValue(stream.getVolume() * 100);
        spinZOrder.setValue(stream.getZOrder());
        timer.scheduleAtFixedRate(new RefreshPanel(this), 0, 200);
    }
    public Viewer detachViewer(){
        panPreview.remove(viewer);
        panPreview.revalidate();
        return viewer;
    }
    public Viewer attachViewer(){
        panPreview.add(viewer, BorderLayout.CENTER);
        panPreview.revalidate();
        return viewer;
    }
    public ImageIcon getIcon(){
        ImageIcon icon = null;
        if (stream.getPreview()!=null){
            icon = new ImageIcon(stream.getPreview().getScaledInstance(32, 32, BufferedImage.SCALE_FAST));
        }
        
        return icon;
    }
    public void remove() {
        timer.cancel();
        timer = null;
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

        panPreview = new javax.swing.JPanel();
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

        setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        panPreview.setBackground(new java.awt.Color(113, 113, 113));
        panPreview.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        panPreview.setName("panPreview"); // NOI18N
        panPreview.setLayout(new java.awt.BorderLayout());

        spinX.setName("spinX"); // NOI18N
        spinX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinXStateChanged(evt);
            }
        });

        spinY.setName("spinY"); // NOI18N
        spinY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinYStateChanged(evt);
            }
        });

        spinW.setName("spinW"); // NOI18N
        spinW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinWStateChanged(evt);
            }
        });

        spinH.setName("spinH"); // NOI18N
        spinH.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinHStateChanged(evt);
            }
        });

        spinOpacity.setName("spinOpacity"); // NOI18N
        spinOpacity.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinOpacityStateChanged(evt);
            }
        });

        spinVolume.setName("spinVolume"); // NOI18N
        spinVolume.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinVolumeStateChanged(evt);
            }
        });

        tglActiveStream.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-start.png"))); // NOI18N
        tglActiveStream.setName("tglActiveStream"); // NOI18N
        tglActiveStream.setRolloverEnabled(false);
        tglActiveStream.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglActiveStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglActiveStreamActionPerformed(evt);
            }
        });

        spinZOrder.setName("spinZOrder"); // NOI18N
        spinZOrder.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinZOrderStateChanged(evt);
            }
        });

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        labelX.setText(bundle.getString("X")); // NOI18N
        labelX.setName("labelX"); // NOI18N

        labelY.setText(bundle.getString("Y")); // NOI18N
        labelY.setName("labelY"); // NOI18N

        labelW.setText(bundle.getString("WIDTH")); // NOI18N
        labelW.setName("labelW"); // NOI18N

        labelH.setText(bundle.getString("HEIGHT")); // NOI18N
        labelH.setName("labelH"); // NOI18N

        labelO.setText(bundle.getString("OPACITY")); // NOI18N
        labelO.setName("labelO"); // NOI18N

        labelV.setText(bundle.getString("VOLUME")); // NOI18N
        labelV.setName("labelV"); // NOI18N

        labelZ.setText(bundle.getString("LAYER")); // NOI18N
        labelZ.setName("labelZ"); // NOI18N

        labelCW.setText(bundle.getString("CAPTUREWIDTH")); // NOI18N
        labelCW.setName("labelCW"); // NOI18N

        spinW1.setName("spinW1"); // NOI18N
        spinW1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinW1StateChanged(evt);
            }
        });

        labelCH.setText(bundle.getString("CAPTUREHEIGHT")); // NOI18N
        labelCH.setName("labelCH"); // NOI18N

        spinH1.setName("spinH1"); // NOI18N
        spinH1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinH1StateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panPreview, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelH, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(labelCH, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelCW, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelY, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelX, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE))
                    .addComponent(labelW, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                    .addComponent(labelO, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                    .addComponent(labelV, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                    .addComponent(labelZ, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(spinZOrder, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(spinVolume, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(spinOpacity, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(spinH, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(spinH1, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                            .addComponent(spinW1, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                            .addComponent(spinY, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                            .addComponent(spinX, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                            .addComponent(spinW, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE))
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tglActiveStream, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelX)
                    .addComponent(spinX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelY)
                    .addComponent(spinY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCW)
                    .addComponent(spinW1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCH)
                    .addComponent(spinH1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelW)
                    .addComponent(spinW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelH)
                    .addComponent(spinH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelO)
                    .addComponent(spinOpacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelV)
                    .addComponent(spinVolume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelZ)
                    .addComponent(spinZOrder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tglActiveStream)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tglActiveStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglActiveStreamActionPerformed
        if (tglActiveStream.isSelected()) {
            stream.read();
        } else {
            stream.stop();
        }
    }//GEN-LAST:event_tglActiveStreamActionPerformed

    private void spinOpacityStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinOpacityStateChanged
        stream.setOpacity((Integer) spinOpacity.getValue());
    }//GEN-LAST:event_spinOpacityStateChanged

    private void spinZOrderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinZOrderStateChanged
        stream.setZOrder((Integer) spinZOrder.getValue());
    }//GEN-LAST:event_spinZOrderStateChanged

    private void spinWStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinWStateChanged
        stream.setWidth((Integer)spinW.getValue());
    }//GEN-LAST:event_spinWStateChanged

    private void spinHStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinHStateChanged
        stream.setHeight((Integer)spinH.getValue());
    }//GEN-LAST:event_spinHStateChanged

    private void spinXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinXStateChanged
        stream.setX((Integer)spinX.getValue());
    }//GEN-LAST:event_spinXStateChanged

    private void spinYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinYStateChanged
        stream.setY((Integer)spinY.getValue());
    }//GEN-LAST:event_spinYStateChanged

    private void spinVolumeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinVolumeStateChanged
        Object value = spinVolume.getValue();
        float v = 0;
        if (value instanceof Float){
            v = (Float)value;
        } else if (value instanceof Integer){
            v = ((Integer)value).floatValue();
        }
        stream.setVolume(v/100f);
        
    }//GEN-LAST:event_spinVolumeStateChanged

    private void spinW1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinW1StateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_spinW1StateChanged

    private void spinH1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinH1StateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_spinH1StateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel labelCH;
    private javax.swing.JLabel labelCW;
    private javax.swing.JLabel labelH;
    private javax.swing.JLabel labelO;
    private javax.swing.JLabel labelV;
    private javax.swing.JLabel labelW;
    private javax.swing.JLabel labelX;
    private javax.swing.JLabel labelY;
    private javax.swing.JLabel labelZ;
    private javax.swing.JPanel panPreview;
    private javax.swing.JSpinner spinH;
    private javax.swing.JSpinner spinH1;
    private javax.swing.JSpinner spinOpacity;
    private javax.swing.JSpinner spinVolume;
    private javax.swing.JSpinner spinW;
    private javax.swing.JSpinner spinW1;
    private javax.swing.JSpinner spinX;
    private javax.swing.JSpinner spinY;
    private javax.swing.JSpinner spinZOrder;
    private javax.swing.JToggleButton tglActiveStream;
    // End of variables declaration//GEN-END:variables
}

class RefreshPanel extends TimerTask {

    StreamPanel panel = null;

    public RefreshPanel(StreamPanel p) {
        panel = p;
    }

    @Override
    public void run() {
        if (panel != null) {
            panel.viewer.setImage(panel.stream.getPreview());
            panel.viewer.repaint();
        }
    }
}