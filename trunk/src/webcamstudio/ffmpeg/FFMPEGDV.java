/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.ffmpeg;

/**
 *
 * @author patrick
 */
public class FFMPEGDV extends FFMPEGAbstract {

    public FFMPEGDV() {
        sourceFormat = "";
        command = "webcamstudiodv.sh";
        
    }
    

    @Override
    public void setSeek(long sec){
        //Seek not supported with this
        seek=0;
    }
}
