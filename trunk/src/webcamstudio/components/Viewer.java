/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.awt.image.BufferedImage;

/**
 *
 * @author pballeux
 */
public class Viewer extends javax.swing.JPanel {

    private BufferedImage image = null;

    public Viewer() {
        this.setOpaque(true);

    }

    public void updateImage(BufferedImage img) {
        if (image == null) {
            image = img;
            repaint();
        }
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
            g.dispose();
            image = null;
        } 
    }
}
