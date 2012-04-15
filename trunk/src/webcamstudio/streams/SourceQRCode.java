/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author patrick
 */
public class SourceQRCode extends SourceText {

    public SourceQRCode(String content) {
        super(content);
        name="QRCode";
    }

    @Override
    public void updateContent(String content) {
        this.content = content;
        if (content != null && content.length() > 0) {
            MultiFormatWriter w = new MultiFormatWriter();
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            BitMatrix b;
            try {
                b = w.encode(content, BarcodeFormat.QR_CODE, width, height);
                image = com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(b);
            } catch (WriterException ex) {
                Logger.getLogger(SourceQRCode.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (frame != null) {
                frame.setImage(image);
                frame.setOutputFormat(x, y, width, height, opacity, volume);
                frame.setZOrder(zorder);
            }
        }
    }
}
