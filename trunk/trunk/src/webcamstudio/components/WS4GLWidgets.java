/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.io.IOException;
import java.net.MalformedURLException;
import webcamstudio.sources.VideoSourceWidget;
import java.net.URL;

/**
 *
 * @author patrick
 */
public class WS4GLWidgets {

    private java.util.TreeMap<String, VideoSourceWidget> sources = new java.util.TreeMap<String, VideoSourceWidget>();
    private final String strURL = "http://www.ws4gl.org/widgets/widgets.txt";
    private boolean doneLoading = false;

    public WS4GLWidgets() {
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
        final String[] lines = buffer.toString().split("\n");
        for (String sURL : lines) {
            if (sURL.length() > 0 && !sURL.trim().startsWith("//")) {
                final URL url = new URL(sURL);
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        VideoSourceWidget source = new VideoSourceWidget(url);
                        if (sources.containsKey(source.getName())) {
                            sources.put(source.getLocation(), source);
                        } else {
                            sources.put(source.getName(), source);
                        }
                        if (sources.size() == lines.length) {
                            doneLoading = true;
                        }
                    }
                }).start();

            }
        }
        while (!doneLoading) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
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
