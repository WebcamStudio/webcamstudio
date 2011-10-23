/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.ffmpeg;

/**
 *
 * @author patrick
 */
public class FFMPEGFullDesktop extends FFMPEGAbstract {

    public FFMPEGFullDesktop() {
        sourceFormat = "x11grab";
        sourceInput = ":0.0";
        captureWidth=1024;
        captureHeight=768;
        rate=5;
    }
    
    @Override
    public void setOpaque(int[] pixels){
        for (int i = 0;i<pixels.length;i++){
            pixels[i] = pixels[i] | 0xff000000;
        }
    }
    @Override
    public void setSeek(long sec){
        //Seek not supported with this
        seek=0;
    }
}
