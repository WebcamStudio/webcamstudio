/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio;

import webcamstudio.media.renderer.Capturer;
import webcamstudio.streams.SourceWebcam;

/**
 *
 * @author patrick
 */
public class TESTTCPIP {
    public static void main(String[] args){
        Capturer capture = new Capturer(new SourceWebcam("default"));
        
    }
}
