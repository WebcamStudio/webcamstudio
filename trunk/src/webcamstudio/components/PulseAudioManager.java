/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author patrick
 */
public class PulseAudioManager {

    java.util.Vector<String> sources = new java.util.Vector<String>();
    java.util.TreeMap<String, String> apps = new java.util.TreeMap<String, String>();

    public PulseAudioManager() {
    }

    public void update() throws IOException {
        Process p = Runtime.getRuntime().exec("/usr/bin/ws4gl-pulseaudio-getsources.sh");
        java.io.BufferedInputStream in = new java.io.BufferedInputStream(p.getInputStream());
        byte[] buffer = new byte[4096];
        in.read(buffer);
        String[] ssources = new String(buffer).trim().split("\n");
        sources.clear();
        for (String s : ssources) {
            if (s != null && s.length() > 0) {
                sources.add(s);
            }
        }
        in.close();
        p.destroy();
        p = Runtime.getRuntime().exec("/usr/bin/ws4gl-pulseaudio-getapps.sh");
        in = new java.io.BufferedInputStream(p.getInputStream());
        buffer = new byte[4096];
        in.read(buffer);
        ssources = new String(buffer).trim().split("\n");
        apps.clear();
        for (String s : ssources) {
            if (s != null && s.length() > 0) {
                apps.put(s.split(",")[1], s.split(",")[0]);
            }
        }
        in.close();
        in = null;
        p.destroy();
        p = null;
    }

    public String[] getSources() {
        String[] retValues = new String[sources.size()];
        for (int i = 0; i < sources.size(); i++) {
            retValues[i] = sources.get(i);
        }
        return retValues;
    }

    public String[] getApps() {
        String[] retValues = new String[apps.size()+1];
        retValues[0]="";
        for (int i = 1; i < apps.size(); i++) {
            retValues[i] = apps.keySet().toArray()[i].toString();
        }
        return retValues;
    }

    public void setSoundInput(String app, String source) {
        try {
            update();
            String index = apps.get(app);
            Process p = Runtime.getRuntime().exec("/usr/bin/pacmd move-source-output " + index + " " + source);
            try {
                p.waitFor();
            } catch (InterruptedException ex) {
                Logger.getLogger(PulseAudioManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            p.destroy();
            p = null;
        } catch (IOException ex) {
            Logger.getLogger(PulseAudioManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        PulseAudioManager p = new PulseAudioManager();
        try {
            p.update();
        } catch (IOException ex) {
            Logger.getLogger(PulseAudioManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
