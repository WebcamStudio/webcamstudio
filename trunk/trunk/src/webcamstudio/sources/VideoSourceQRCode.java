/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class VideoSourceQRCode extends VideoSource {

    public VideoSourceQRCode(String text) {
        location = "";
        name = "QRCode";
        this.customText = text;

    }

    public void updateText(String text){
        customText = text;
    }
    @Override
    public void startSource() {
        isPlaying = true;

        Thread t = new Thread(new Runnable() {

            public void run() {
                stopMe = false;
                while (!stopMe) {
                    com.google.zxing.MultiFormatWriter w = new MultiFormatWriter();
                    try {
                        BitMatrix b = w.encode(customText, BarcodeFormat.QR_CODE, captureWidth, captureHeight);
                        tempimage = com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(b);
                        applyEffects(tempimage);
                        applyShape(tempimage);
                        image = tempimage;
                    } catch (WriterException ex) {
                        Logger.getLogger(VideoSourceQRCode.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(VideoSourceQRCode.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

    }

    @Override
    public boolean canUpdateSource() {
        return false;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void pause() {
        //Do nothing...
    }

    @Override
    public void play() {
        //Do nothing;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public void stopSource() {
        isPlaying = false;
        stopMe = true;
        image = null;
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public String toString() {
        return "Image: " + name;
    }

    public Image getThumnail() {
        return thumbnail;
    }
    private Image thumbnail = null;

    @Override
    public java.util.Collection<JPanel> getControls() {
        java.util.Vector<JPanel> list = new java.util.Vector<JPanel>();
        list.add(new webcamstudio.controls.ControlQRCode(this));
        list.add(new webcamstudio.controls.ControlEffects(this));
        list.add(new webcamstudio.controls.ControlShapes(this));
        return list;
    }

    @Override
    public ImageIcon getThumbnail() {
        ImageIcon icon = getCachedThumbnail();
        if (icon == null) {
            try {
                tempimage = javax.imageio.ImageIO.read(new URL(location));
                if (tempimage != null) {
                    icon = new ImageIcon(tempimage.getScaledInstance(32, 32, BufferedImage.SCALE_FAST));
                } else {
                    icon = super.getThumbnail();
                }
            } catch (IOException ex) {
                Logger.getLogger(VideoSourceQRCode.class.getName()).log(Level.SEVERE, null, ex);
                icon = super.getThumbnail();
            }
            try {
                saveThumbnail(new ImageIcon(tempimage.getScaledInstance(128, 128, BufferedImage.SCALE_FAST)));
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
                Logger.getLogger(VideoSourceQRCode.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return icon;
    }
}
