/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.io.IOException;
import java.net.MalformedURLException;
import webcamstudio.sources.VideoSourceAnimation;
import java.net.URL;

/**
 *
 * @author patrick
 */
public class WS4GLAnimations {

    private java.util.Vector<VideoSourceAnimation> sources = new java.util.Vector<VideoSourceAnimation>();
    private final String strURL = "http://www.ws4gl.org/animations/AnimationsList.txt";

    public WS4GLAnimations() {
    }

    public void updateSourceList() throws MalformedURLException, IOException {
        sources.clear();
        URL website = new URL(strURL);
        StringBuffer buffer = new StringBuffer();
        java.io.DataInputStream din = new java.io.DataInputStream(website.openStream());
        byte[] data = new byte[256];
        int count = din.read(data);
        while (count != -1) {
            buffer.append(new String(data, 0, count));
            count = din.read(data);
        }
        din.close();
        data = null;
        String anmURL = "";
        String[] lines = buffer.toString().split("\n");
        VideoSourceAnimation source = null;
        URL url = null;
        for (int i = 0;i<lines.length;i++){
            anmURL = lines[i];
            if (anmURL.length()>0 && !anmURL.trim().startsWith("//")){
                url = new URL(anmURL);
                source = new VideoSourceAnimation(url);
                sources.add(source);
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
        } catch (MalformedURLException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
