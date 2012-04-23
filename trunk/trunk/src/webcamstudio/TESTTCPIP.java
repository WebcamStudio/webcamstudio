/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.ffmpeg.FFMPEGRenderer;
import webcamstudio.streams.SourceMovie;
import webcamstudio.streams.SourceWebcam;

/**
 *
 * @author patrick
 */
public class TESTTCPIP {
    public static void main(String[] args){
        File file = new File("/dev/video0");
        SourceWebcam movie = new SourceWebcam(file);
        movie.setRate(10);
        
        movie.read();
        while(movie.isPlaying()){
            movie.getFrame();
            try {
                Thread.sleep(1000/10);
            } catch (InterruptedException ex) {
                Logger.getLogger(TESTTCPIP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
