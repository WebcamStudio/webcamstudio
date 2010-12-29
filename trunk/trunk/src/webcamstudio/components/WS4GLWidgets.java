/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import webcamstudio.sources.VideoSourceWidget;

/**
 *
 * @author patrick
 */
public class WS4GLWidgets {

    private java.util.TreeMap<String, VideoSourceWidget> sources = new java.util.TreeMap<String, VideoSourceWidget>();
    private final String strPath = "/usr/share/webcamstudio/widgets";

    public WS4GLWidgets() {
    }

    public void updateSourceList() throws MalformedURLException, IOException {
        sources.clear();
        File dir = new File(strPath);
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                VideoSourceWidget source = new VideoSourceWidget(file.toURI().toURL());
                if (sources.containsKey(source.getName())) {
                    sources.put(source.getLocation(), source);
                } else {
                    sources.put(source.getName(), source);
                }
            }
        }
    }

    public java.util.Collection<VideoSourceWidget> getSources() {
        return sources.values();
    }

    //For testing purpose
    public static void main(String[] args) {
        WS4GLWidgets wid = new WS4GLWidgets();
        try {
            wid.updateSourceList();
        } catch (MalformedURLException ex) {
            System.out.println(ex.getMessage());

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
