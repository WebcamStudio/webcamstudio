/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.io.File;
import java.io.IOException;
import webcamstudio.sources.VideoSourceAnimation;

/**
 *
 * @author patrick
 */
public class WS4GLAnimations {

    private java.util.Vector<VideoSourceAnimation> sources = new java.util.Vector<VideoSourceAnimation>();
    private final String strPath = "/usr/share/webcamstudio/animations";

    public WS4GLAnimations() {
    }

    public void updateSourceList() throws IOException {
        sources.clear();
        File dir = new File(strPath);
        if (dir.isDirectory()){
        for (File file :  dir.listFiles()){
            VideoSourceAnimation anm = new VideoSourceAnimation(file);
            sources.add(anm);
        }
        }
    }

    public java.util.Vector<VideoSourceAnimation> getSources() {
        return sources;
    }

    //For testing purpose
    public static void main(String[] args){
        WS4GLAnimations anm = new WS4GLAnimations();
        try {
            anm.updateSourceList();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
