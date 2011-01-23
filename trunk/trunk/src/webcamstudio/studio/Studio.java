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
import webcamstudio.layout.Layout;
import webcamstudio.sources.*;

/**
 *
 * @author pballeux
 */
public class Studio {

    java.util.Vector<Layout> layouts = new java.util.Vector<Layout>();

    public Studio() {
    }


    public void setLayouts(java.util.Vector<Layout> list) {
        layouts.clear();
        layouts.addAll(list);
    }


    public java.util.Vector<Layout> getLayouts() {
        return layouts;
    }

    public void loadStudio(File studio) throws BackingStoreException, InvalidPreferencesFormatException, IOException {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(this.getClass());
        prefs.node("Sources").removeNode();
        prefs.node("Layouts").removeNode();
        prefs.flush();
        prefs.sync();
        java.util.prefs.Preferences.importPreferences(studio.toURI().toURL().openStream());
        VideoSource source = null;
        String[] layoutsName = prefs.node("Layouts").childrenNames();
        Layout layout = null;
        for (int i = 0; i < layoutsName.length; i++) {
            layout = new Layout();
            layout.loadFromStudioConfig(prefs.node("Layouts").node(layoutsName[i]));
            layouts.add(layout);
        }
    }

    public void saveStudio(File studio) throws ParserConfigurationException, BackingStoreException, FileNotFoundException, IOException {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(this.getClass());
        java.io.FileOutputStream fout = new java.io.FileOutputStream(studio);
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
            source = new VideoSourceV4L("");
        } else if (currentClass.equals(VideoSourceV4L2.class.getName()) || currentClass.equals(VideoSourceV4L2.class.getName().replace(".sources", ""))) {
            source = new VideoSourceV4L2("");
        } else if (currentClass.equals(VideoSourceText.class.getName()) || currentClass.equals(VideoSourceText.class.getName().replace(".sources", ""))) {
            source = new VideoSourceText(new java.io.File("."));
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
        }

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
