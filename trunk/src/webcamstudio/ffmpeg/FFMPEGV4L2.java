/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.ffmpeg;

/**
 *
 * @author patrick
 */
public class FFMPEGV4L2 extends FFMPEGAbstract {

    public FFMPEGV4L2() {
        sourceFormat = "video4linux2";
    }
    
    @Override
    public void setSeek(long sec){
        //Seek not supported with this
        seek=0;
    }
}
