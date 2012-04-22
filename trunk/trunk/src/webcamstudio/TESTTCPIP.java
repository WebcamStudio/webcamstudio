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
        File file = new File("/home/patrick/Videos/AceVenturaenAfrique.mp4");
        SourceMovie movie = new SourceMovie(file);
        movie.setRate(15);
        
        FFMPEGRenderer ffmpeg = new FFMPEGRenderer(movie, FFMPEGRenderer.ACTION.CAPTURE,"movie");
        ffmpeg.read();
    }
}
