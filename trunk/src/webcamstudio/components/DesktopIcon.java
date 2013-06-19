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
import webcamstudio.streams.SourceMicrophone;
import webcamstudio.streams.SourceText;
import webcamstudio.streams.Stream;

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
                        }
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DesktopIcon.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
        
    }
    private BufferedImage clImage(BufferedImage src){
        BufferedImage tempimage = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(src.getWidth(), src.getHeight(), java.awt.image.BufferedImage.TRANSLUCENT);
        Graphics2D tempbuffer = tempimage.createGraphics();
        tempbuffer.drawImage(src, 0, 0, null);
        tempbuffer.dispose();
        return tempimage;
}
}
