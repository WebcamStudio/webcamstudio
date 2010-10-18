/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webcamstudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import webcamstudio.sources.effects.controls.BlockControl;

/**
 *
 * @author pballeux
 */
public class Block extends Effect{
    private com.jhlabs.image.BlockFilter filter = new com.jhlabs.image.BlockFilter();
    private int blockSize = 3;
    @Override
    public void applyEffect(BufferedImage img) {
        filter.setBlockSize(blockSize);
        Graphics2D buffer = img.createGraphics();
        BufferedImage temp = filter.filter(img, null);
        buffer.setBackground(new java.awt.Color(0,0,0,0));
        buffer.clearRect(0,0,img.getWidth(),img.getHeight());
        buffer.drawImage(temp, 0, 0,null);
        buffer.dispose();
    }

   public void setSize(int value){
       blockSize=value;
   }
   public int getSize(){
       return blockSize;
   }
    @Override
    public JPanel getControl() {
        return new BlockControl(this);
    }

    @Override
    public void applyStudioConfig(Preferences prefs) {
        prefs.putInt("blocksize", blockSize);
    }

    @Override
    public void loadFromStudioConfig(Preferences prefs) {
        blockSize = prefs.getInt("blocksize", blockSize);
    }

}
