/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MosaicControl.java
 *
 * Created on 2010-01-15, 01:51:51
 */

package webcamstudio.sources.effects.controls;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import webcamstudio.WebcamStudio;
import static webcamstudio.WebcamStudio.animations;
import static webcamstudio.WebcamStudio.cboAnimations;
import webcamstudio.sources.effects.FaceDetectorAlpha;

/**
 *
 * @author pballeux
 */
public class FaceDetectorControl extends javax.swing.JPanel {

    FaceDetectorAlpha effect = null;
    public static Properties faces = new Properties();
    /** Creates new form MosaicControl
     * @param effect */
    public FaceDetectorControl(FaceDetectorAlpha effect) {
        initComponents();
        this.effect=effect;
        initFaces();        
    }
    @SuppressWarnings("unchecked") 
    private void initFaces() {
        try {
            faces.load(getClass().getResourceAsStream("/webcamstudio/resources/faces/Faces.properties"));
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (Object o : faces.keySet()) {
                model.addElement(o); 
            }
            cboFaces.setModel(model);
        } catch (IOException ex) {
            Logger.getLogger(WebcamStudio.class.getName()).log(Level.SEVERE, null, ex);
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

        cboFaces = new javax.swing.JComboBox();
        lblfaces = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(396, 107));

        cboFaces.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboFaces.setName("cboFaces"); // NOI18N
        cboFaces.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboFacesActionPerformed(evt);
            }
        });

        lblfaces.setText("Overlays:");
        lblfaces.setName("lblfaces"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboFaces, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblfaces, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(228, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblfaces)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cboFaces, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(45, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cboFacesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboFacesActionPerformed
        effect.setFace(cboFaces.getSelectedItem().toString());
    }//GEN-LAST:event_cboFacesActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboFaces;
    private javax.swing.JLabel lblfaces;
    // End of variables declaration//GEN-END:variables

}