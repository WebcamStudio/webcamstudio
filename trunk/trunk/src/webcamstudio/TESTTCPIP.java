/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio;

import java.io.File;
import webcamstudio.ffmpeg.FFMPEGRenderer;
import webcamstudio.streams.SourceMovie;

/**
 *
 * @author patrick
 */
public class TESTTCPIP {
    public static void main(String[] args){
        File file = new File("C:\\Users\\Patrick Balleux\\Downloads\\big_buck_bunny_480p_stereo.ogg");
        SourceMovie movie = new SourceMovie(file);
        movie.setRate(30);
        
        FFMPEGRenderer ffmpeg = new FFMPEGRenderer(movie, FFMPEGRenderer.ACTION.CAPTURE,"movie");
        ffmpeg.read();
    }
}
