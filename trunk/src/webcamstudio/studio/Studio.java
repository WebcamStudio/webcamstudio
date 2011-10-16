/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.studio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import javax.xml.parsers.ParserConfigurationException;
import webcamstudio.components.Mixer;
import webcamstudio.exporter.vloopback.VideoOutput;
import webcamstudio.layout.Layout;
import webcamstudio.sources.*;

/**
 *
 * @author pballeux
 */
public class Studio {

    java.util.TreeMap<String,Layout> layouts = new java.util.TreeMap<String,Layout>();
    private int outputWidth = 320;
    private int outputHeight = 240;
    private String device = "/dev/video1";
    private int pixFormat = VideoOutput.RGB24;
    private boolean enabledAudioMixer = false;
    public Studio() {
    }


    public boolean isAudioMixerActive(){
        return enabledAudioMixer;
    }
    public void setEnabledAudioMixer(boolean active){
        enabledAudioMixer=active;
    }
    public int getWidth(){
        return outputWidth;
    }
    public int getHeight(){
        return outputHeight;
    }
    public int getPixFormat(){
        return pixFormat;
    }
    public String getDevice(){
        return device;
    }
    public void setLayouts(java.util.AbstractMap<String,Layout> list) {
        layouts.clear();
        layouts.putAll(list);
    }


    public java.util.AbstractMap<String,Layout> getLayouts() {
        return layouts;
    }

    public void loadStudio(File studio) throws BackingStoreException, InvalidPreferencesFormatException, IOException {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(this.getClass());
        prefs.node("Sources").removeNode();
        prefs.node("Layouts").removeNode();
        outputWidth=prefs.getInt("width", outputWidth);
        outputHeight = prefs.getInt("height", outputHeight);
        device = prefs.get("device",device);
        pixFormat = prefs.getInt("pixformat",pixFormat);
        enabledAudioMixer=prefs.getBoolean("enabledaudiomixer", enabledAudioMixer);
        prefs.flush();
        prefs.sync();
        java.util.prefs.Preferences.importPreferences(studio.toURI().toURL().openStream());
        VideoSource source = null;
        String[] layoutsName = prefs.node("Layouts").childrenNames();
        Layout layout = null;
        for (int i = 0; i < layoutsName.length; i++) {
            layout = new Layout();
            layout.loadFromStudioConfig(prefs.node("Layouts").node(layoutsName[i]));
            layouts.put(layout.getUUID(),layout);
        }
    }

    public void saveStudio(File studio,Mixer mixer) throws ParserConfigurationException, BackingStoreException, FileNotFoundException, IOException {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(this.getClass());
        java.io.FileOutputStream fout = new java.io.FileOutputStream(studio);
        outputWidth=mixer.getImage().getWidth();
        outputHeight = mixer.getImage().getHeight();
        device = mixer.getDevice().getDevice();
        pixFormat = mixer.getDevice().getPixFormat();
        prefs.putInt("width", outputWidth);
        prefs.putInt("height", outputHeight);
        prefs.put("device",device);
        prefs.putInt("pixformat",pixFormat);
        prefs.putBoolean("enabledaudiomixer", enabledAudioMixer);
        prefs.node("Sources").removeNode();
        prefs.node("Layouts").removeNode();
        prefs.flush();
        prefs.sync();
        Layout layout = null;
        for (int i = 0; i < layouts.size(); i++) {
            layout = layouts.get(i);
            layout.applyStudioConfig(prefs.node("Layouts"), i);
        }
        prefs.exportSubtree(fout);
        prefs.flush();
        fout.close();
    }

    public static VideoSource getSourceFromClassName(String currentClass) {
        VideoSource source = null;
        if (currentClass.equals(VideoSourceMovie.class.getName()) || currentClass.equals(VideoSourceMovie.class.getName().replaceAll(".sources", ""))) {
            source = new VideoSourceMovie(new java.io.File("."));
        } else if (currentClass.equals(VideoSourceImage.class.getName()) || currentClass.equals(VideoSourceImage.class.getName().replace(".sources", ""))) {
            source = new VideoSourceImage((java.io.File) null);
        } else if (currentClass.equals(VideoSourceDesktop.class.getName()) || currentClass.equals(VideoSourceDesktop.class.getName().replace(".sources", ""))) {
            source = new VideoSourceDesktop();
        } else if (currentClass.equals(VideoSourceAnimation.class.getName()) || currentClass.equals(VideoSourceAnimation.class.getName().replace(".sources", ""))) {
            source = new VideoSourceAnimation(new java.io.File("."));
        } else if (currentClass.equals(VideoSourceV4L.class.getName()) || currentClass.equals(VideoSourceV4L.class.getName().replace(".sources", ""))) {
            source = new VideoSourceV4L();
        } else if (currentClass.equals(VideoSourceV4L2.class.getName()) || currentClass.equals(VideoSourceV4L2.class.getName().replace(".sources", ""))) {
            source = new VideoSourceV4L2();
        } else if (currentClass.equals(VideoSourceText.class.getName()) || currentClass.equals(VideoSourceText.class.getName().replace(".sources", ""))) {
            source = new VideoSourceText("");
        } else if (currentClass.equals(VideoSourceDV.class.getName()) || currentClass.equals(VideoSourceDV.class.getName().replace(".sources", ""))) {
            source = new VideoSourceDV();
        } else if (currentClass.equals(VideoSourceIRC.class.getName()) || currentClass.equals(VideoSourceIRC.class.getName().replace(".sources", ""))) {
            source = new VideoSourceIRC("127.0.0.1", 6667, "#webcamstudio", "");
        } else if (currentClass.equals(VideoSourcePlaylist.class.getName()) || currentClass.equals(VideoSourcePlaylist.class.getName().replace(".sources", ""))) {
            source = new VideoSourcePlaylist();
        } else if (currentClass.equals(VideoSourceWidget.class.getName()) || currentClass.equals(VideoSourceWidget.class.getName().replace(".sources", ""))) {
            source = new VideoSourceWidget();
        } else if (currentClass.equals(VideoSourceConsole.class.getName()) || currentClass.equals(VideoSourceConsole.class.getName().replace(".sources", ""))) {
            source = new VideoSourceConsole("");
        } else if (currentClass.equals(VideoSourceMusic.class.getName()) || currentClass.equals(VideoSourceMusic.class.getName().replace(".sources", ""))) {
            source = new VideoSourceMusic("");
        }   else if (currentClass.equals(VideoSourcePipeline.class.getName()) || currentClass.equals(VideoSourcePipeline.class.getName().replace(".sources", ""))) {
            source = new VideoSourcePipeline();
        } else if (currentClass.equals(VideoSourceQRCode.class.getName()) || currentClass.equals(VideoSourceQRCode.class.getName().replace(".sources", ""))) {
            source = new VideoSourceQRCode("");
        } else if (currentClass.equals(VideoSourceFullDesktop.class.getName()) || currentClass.equals(VideoSourceFullDesktop.class.getName().replace(".sources", ""))) {
            source = new VideoSourceFullDesktop();
        }
        //webcamstudio.sources.VideoSourcePipeline
        return source;
    }

    public static String getKeyIndex(int index){
        String orderNumber = index + "";
        //Padding order with 0 to ensure that when loaded, it will be in the same order as it was saved...
        while (orderNumber.length() < 10) {
            orderNumber = "0" + orderNumber;
        }
        return orderNumber;
    }
}
