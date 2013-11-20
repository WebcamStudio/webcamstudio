/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JInternalFrame.JDesktopIcon;
import webcamstudio.streams.SourceText;
import webcamstudio.streams.SourceDVB;
import webcamstudio.streams.Stream;
import webcamstudio.streams.SourceURL;

/**
 *
 * @author patrick
 */
public class DesktopIcon extends JDesktopIcon {

    Stream stream = null;
    Viewer viewer = null;
    JInternalFrame frameDesktop = null;
    public DesktopIcon(JInternalFrame f, Stream s) {
        super(f);
        frameDesktop = f;
        stream = s;
        this.removeAll();
        this.setLayout(new BorderLayout());
        viewer = new Viewer();
        add(viewer, BorderLayout.CENTER);
        this.setToolTipText(s.getName());
        this.setVisible(true);
        this.setSize(64, 64);
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!frameDesktop.isClosed()) {
                    try {
                        if (frameDesktop.isIcon()) {
                            BufferedImage img = stream.getPreview();                            
                            if (stream.isPlaying()) {
                                if (img != null){
                                    int ImgHeight = img.getHeight();
                                    int ImgWidth = img.getWidth();
                                    BufferedImage newImg = new BufferedImage(ImgWidth,ImgHeight, BufferedImage.TYPE_INT_RGB);
                                    Graphics2D gr = newImg.createGraphics();
                                    gr.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                                                        java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                                    gr.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                                                        java.awt.RenderingHints.VALUE_RENDER_SPEED);
                                    gr.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                                                        java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
                                    gr.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                                                        java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                                    gr.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS,
                                                        java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
                                    gr.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING,
                                                        java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED);
                                    gr.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING,
                                                        java.awt.RenderingHints.VALUE_DITHER_DISABLE);
                                    gr.setColor(Color.green);
                                    gr.fillRect(0,0,newImg.getWidth(),newImg.getHeight());
                                    gr.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 70 / 100F));
                                    gr.drawImage(img,0,0,null);
                                    img = newImg;
                                }
                            } else {
                                if (img != null){
                                    BufferedImage stopImg = stream.getPreview();
                                    img=stopImg;
                                }
                            } 
                            viewer.setImage(img);
                            viewer.setAudioLevel(stream.getAudioLevelLeft(), stream.getAudioLevelRight());
                            viewer.repaint();
                            if (stream instanceof SourceText){
                                SourceText sc = (SourceText)stream;
                                setToolTipText(sc.getContent());
                            }
                            if (stream instanceof SourceDVB){
                                SourceDVB sD = (SourceDVB)stream;
                                setToolTipText(sD.getChName());
                            }
                            if (stream instanceof SourceURL){
                                SourceURL sU = (SourceURL)stream;
                                setToolTipText(sU.getWebURL());
                            } 
                        }
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DesktopIcon.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
        
    }
}
