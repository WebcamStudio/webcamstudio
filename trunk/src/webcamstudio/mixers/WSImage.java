/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 * @author patrick
 */
public class WSImage extends BufferedImage {

    int[] data = null;
    byte[] byteData = null;
    int counter = 0;

    public WSImage(int w, int h, int type) {
        super(w, h, type);
        data = ((DataBufferInt) getRaster().getDataBuffer()).getData();
        switch (super.getType()) {
            case BufferedImage.TYPE_INT_RGB:
                byteData = new byte[data.length * 3];
                break;
            case BufferedImage.TYPE_INT_ARGB:

                break;
        }
    }

    public void setData(int[] srcData) {
        System.arraycopy(srcData, 0, data, 0, srcData.length);
    }

    public void readFully(DataInputStream din) throws IOException {
        if (din.available() > 0) {
            din.readFully(byteData);
            counter = 0;
            for (int i = 0; i < data.length; i++) {
                data[i] = 0xff000000 + (byteData[counter++] << 16) + (byteData[counter++] << 8) + (byteData[counter++]);
            }
        }
    }

    public byte[] getBytes() {
        counter = 0;
        switch (super.getType()) {
            case BufferedImage.TYPE_INT_RGB:
                for (int i = 0; i < byteData.length; i += 3) {
                    byteData[i] = (byte) ((data[counter] >> 16) & 0xFF);
                    byteData[i + 1] = (byte) ((data[counter] >> 8) & 0xFF);
                    byteData[i + 2] = (byte) ((data[counter]) & 0xFF);
                    counter++;
                }
                break;
            case BufferedImage.TYPE_INT_ARGB:
                for (int i = 0; i < byteData.length; i += 4) {
                    byteData[i] = (byte) ((data[counter] >> 16) & 0xFF);
                    byteData[i + 1] = (byte) ((data[counter] >> 8) & 0xFF);
                    byteData[i + 2] = (byte) ((data[counter]) & 0xFF);
                    counter++;
                }
                break;
        }

        return byteData;
    }
}
