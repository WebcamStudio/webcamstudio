/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.DataInputStream;
import java.io.IOException;
import static java.lang.System.arraycopy;

/**
 *
 * @author patrick (modified by karl)
 */
public class WSImage extends BufferedImage {

    private int[] data = null;
    private byte[] byteData = null;
    private int counter = 0;

    public WSImage(int w, int h, int type) {
        super(w, h, type);
        data = ((DataBufferInt) getRaster().getDataBuffer()).getData();
        switch (super.getType()) {
            case TYPE_INT_RGB:
                byteData = new byte[data.length * 3];
                break;
            case TYPE_INT_ARGB:

                break;
        }
    }

    public void setData(int[] srcData) {
        arraycopy(srcData, 0, data, 0, srcData.length);
    }
  
    public void convertByte(byte[] barr) {
        counter = 0;
        for (int i = 0; i < data.length; i++) {
//           data[i] = 0xff000000 | ((byteData[counter++]) << 16) | ((byteData[counter++]& 0xff) << 8) | ((byteData[counter++])& 0xff); // Correct CyanGreen Issue !!!               
            int int32 = 0xff000000;
            int int16 = barr[counter++] << 16;
            int int8 = (barr[counter++] & 0xff) << 8;
            int int0 = (barr[counter++]) & 0xff;
            data[i] = int32 + int16 + int8 + int0;
        }
    }
   
    public void readFully(DataInputStream din) throws IOException {
        if (din.available() > 0) {
            din.readFully(byteData);
            convertByte(byteData);
        }
    }

    public byte[] getBytes() {
        counter = 0;
        switch (super.getType()) {
            case TYPE_INT_RGB:
                for (int i = 0; i < byteData.length; i += 3) {
                    byteData[i] = (byte) ((data[counter] >> 16) & 0xFF);
                    byteData[i + 1] = (byte) ((data[counter] >> 8) & 0xFF);
                    byteData[i + 2] = (byte) ((data[counter]) & 0xFF);
                    counter++;
                }
                break;
            case TYPE_INT_ARGB:
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
