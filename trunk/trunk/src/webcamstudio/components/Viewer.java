/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

/**
 *
 * @author pballeux
 */
public class Viewer extends javax.swing.JPanel {

    public java.awt.image.BufferedImage img = null;
    private java.awt.Component comp = null;

    public Viewer() {
        this.setOpaque(false);

    }

    public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (img != null) {
            g.drawImage(img, 0, 0, getWidth(), getHeight(), 0, 0, img.getWidth(), img.getHeight(), null);
            g.dispose();
        }
    }
}
