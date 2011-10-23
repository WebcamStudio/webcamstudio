/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.studio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import javax.xml.parsers.ParserConfigurationException;
import webcamstudio.components.Mixer;
import webcamstudio.exporter.vloopback.VideoOutput;
import webcamstudio.layout.Layout;
import webcamstudio.layout.LayoutItem;
import webcamstudio.sources.*;

/**
 *
 * @author pballeux
 */
public class Studio {
    
    private int outputWidth = 320;
    private int outputHeight = 240;
    private String device = "/dev/video1";
    private int pixFormat = VideoOutput.RGB24;
    private boolean enabledAudioMixer = false;
    final public static String[] SUPPORTEDEXT = {".studio", ".studioz"};
    final public static String STUDIOPATH = "%STUDIOPATH%";
    private StudioListener listener = null;
    
    public Studio(StudioListener l) {
        listener = l;
    }
    
    public boolean isAudioMixerActive() {
        return enabledAudioMixer;
    }
    
    public void setEnabledAudioMixer(boolean active) {
        enabledAudioMixer = active;
    }
    
    public int getWidth() {
        return outputWidth;
    }
    
    public int getHeight() {
        return outputHeight;
    }
    
    public int getPixFormat() {
        return pixFormat;
    }
    
    public String getDevice() {
        return device;
    }
    
    public void loadStudio(final File studio) throws BackingStoreException, InvalidPreferencesFormatException, IOException {
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    if (listener != null) {
                        listener.startLoading(studio.getName(), 1);
                    }
                    if (studio.getName().endsWith(".studioz")) {
                        loadCompressedStudio(studio);
                    } else if (studio.getName().endsWith(".studio")) {
                        loadUnCompressedStudio(studio);
                    }
                    if (listener != null) {
                        listener.completedLoading(studio.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.completedLoading(e.getMessage());
                    }
                }
            }
        }).start();
        
        
    }
    
    private void loadUnCompressedStudio(File studio) throws BackingStoreException, InvalidPreferencesFormatException, IOException {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(this.getClass());
        prefs.node("Sources").removeNode();
        prefs.node("Layouts").removeNode();
        outputWidth = prefs.getInt("width", outputWidth);
        outputHeight = prefs.getInt("height", outputHeight);
        device = prefs.get("device", device);
        pixFormat = prefs.getInt("pixformat", pixFormat);
        enabledAudioMixer = prefs.getBoolean("enabledaudiomixer", enabledAudioMixer);
        prefs.flush();
        prefs.sync();
        java.util.prefs.Preferences.importPreferences(studio.toURI().toURL().openStream());
        VideoSource source = null;
        String[] layoutsName = prefs.node("Layouts").childrenNames();
        Layout layout = null;
        for (int i = 0; i < layoutsName.length; i++) {
            layout = new Layout();
            layout.loadFromStudioConfig(prefs.node("Layouts").node(layoutsName[i]));
            Layout.addLayout(layout);
        }
    }
    
    public void saveStudio(final File studio, final Mixer mixer) throws ParserConfigurationException, BackingStoreException, FileNotFoundException, IOException, URISyntaxException {
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    if (listener != null) {
                        listener.startSaving(studio.getName(), 1);
                    }
                    
                    if (studio.getName().endsWith(".studioz")) {
                        saveCompressedStudio(studio, mixer, true);
                    } else if (studio.getName().endsWith(".studio")) {
                        saveUnCompressedStudio(studio, mixer);
                    }
                    if (listener != null) {
                        listener.completedSaving(studio.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.completedSaving(e.getMessage());
                    }
                }
            }
        }).start();
    }
    
    private void saveUnCompressedStudio(File studio, Mixer mixer) throws ParserConfigurationException, BackingStoreException, FileNotFoundException, IOException {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(this.getClass());
        java.io.FileOutputStream fout = new java.io.FileOutputStream(studio);
        outputWidth = Mixer.getWidth();
        outputHeight = Mixer.getHeight();
        if (mixer.getDevice() != null) {
            device = mixer.getDevice().getDevice();
            pixFormat = mixer.getDevice().getPixFormat();
        } else {
            device = "";
            pixFormat = VideoOutput.RGB24;
        }
        prefs.putInt("width", outputWidth);
        prefs.putInt("height", outputHeight);
        prefs.put("device", device);
        prefs.putInt("pixformat", pixFormat);
        prefs.putBoolean("enabledaudiomixer", enabledAudioMixer);
        prefs.node("Sources").removeNode();
        prefs.node("Layouts").removeNode();
        prefs.flush();
        prefs.sync();
        int index = 1;
        for (Layout layout : Layout.getLayouts().values()) {
            layout.applyStudioConfig(prefs.node("Layouts"), index++);
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
        } else if (currentClass.equals(VideoSourceQRCode.class.getName()) || currentClass.equals(VideoSourceQRCode.class.getName().replace(".sources", ""))) {
            source = new VideoSourceQRCode("");
        } else if (currentClass.equals(VideoSourceFullDesktop.class.getName()) || currentClass.equals(VideoSourceFullDesktop.class.getName().replace(".sources", ""))) {
            source = new VideoSourceFullDesktop();
        } else if (currentClass.equals(VideoSourceLayout.class.getName()) || currentClass.equals(VideoSourceLayout.class.getName().replace(".sources", ""))) {
            source = new VideoSourceLayout("");
        }
        //webcamstudio.sources.VideoSourcePipeline
        return source;
    }
    
    public static String getKeyIndex(int index) {
        String orderNumber = index + "";
        //Padding order with 0 to ensure that when loaded, it will be in the same order as it was saved...
        while (orderNumber.length() < 10) {
            orderNumber = "0" + orderNumber;
        }
        return orderNumber;
    }
    
    private void saveCompressedStudio(File file, Mixer mixer, boolean includeSource) throws IOException, BackingStoreException, ParserConfigurationException, URISyntaxException {
        java.io.OutputStream out = new java.io.FileOutputStream(file);
        java.util.TreeMap<String,String> originalPath = new java.util.TreeMap<String, String>();
        java.util.jar.JarOutputStream jarFile = new java.util.jar.JarOutputStream(out);
        jarFile.setLevel(9);
        if (listener != null) {
            listener.startSaving(file.getName(), Layout.getLayouts().size());
        }
        int index = 0;
        if (includeSource) {
            for (Layout layout : Layout.getLayouts().values()) {
                for (LayoutItem item : layout.getItems()) {
                    String fileSource = item.getSource().getLocation();
                    if (!fileSource.startsWith("/dev/")) {
                        File location = new File(fileSource);
                        if (fileSource.startsWith("file:")) {
                            URI uri = new URL(fileSource).toURI();
                            location = new File(uri);
                        }
                        if (location.exists() && location.isFile()) {
                            addEntry(jarFile, location, location.getName(), index);
                            originalPath.put(item.getSource().getUUID(), item.getSource().getLocation()+"");
                            item.getSource().setLocation(STUDIOPATH + location.getName());
                        }
                    }
                }
                index++;
            }
        }
        File studioFile = File.createTempFile("WS4GL", ".studio");
        saveUnCompressedStudio(studioFile, mixer);
        addEntry(jarFile, studioFile, "default.studio", index++);
        jarFile.flush();
        jarFile.close();
        studioFile.delete();
        //Resetting original paths
        for (Layout layout : Layout.getLayouts().values()){
            for (LayoutItem item : layout.getItems()){
                if (originalPath.containsKey(item.getSource().getUUID())){
                    item.getSource().setLocation(originalPath.get(item.getSource().getUUID()));
                }
            }
        }
    }
    
    private void addEntry(JarOutputStream jar, File file, String name, int index) throws IOException {
        JarEntry jarAdd = new JarEntry(name);
        jarAdd.setTime(file.lastModified());
        jar.putNextEntry(jarAdd);
        long size = file.length();
        // Write file to archive
        FileInputStream in = new FileInputStream(file);
        byte[] buffer = new byte[65536];
        int count = in.read(buffer, 0, buffer.length);
        long total = 0;
        while (count > 0) {
            if (listener != null) {
                total +=count;
                listener.compressing(name, size, total, index);
            }
            jar.write(buffer, 0, count);
            count = in.read(buffer, 0, buffer.length);
        }
        in.close();
    }
    
    private void loadCompressedStudio(File studio) throws IOException, BackingStoreException, InvalidPreferencesFormatException {
        JarFile jarFile = new JarFile(studio);
        Enumeration<JarEntry> entries = jarFile.entries();
        byte[] buffer = new byte[65536];
        
        File wsDir = new File(System.getProperty("user.home"), ".webcamstudio");
        File tmpDir = new File(wsDir, "tmpstudio");
        if (tmpDir.exists()) {
            tmpDir.delete();
        } else {
            tmpDir.mkdir();
        }
        tmpDir.deleteOnExit();
        //Extracting files, including default.studio
        if (listener != null) {
            listener.startLoading(studio.getName(), jarFile.size());
        }
        int index = 0;
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            File outputFile = new File(tmpDir, entry.getName());
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.deleteOnExit();
            FileOutputStream out = new FileOutputStream(outputFile);
            InputStream in = jarFile.getInputStream(jarFile.getEntry(entry.getName()));
            int count = in.read(buffer, 0, buffer.length);
            long total = 0;
            while (count > 0) {
                if (listener != null) {
                    total += count;
                    listener.compressing(entry.getName(), entry.getSize(), total, index);
                }
                out.write(buffer, 0, count);
                count = in.read(buffer, 0, buffer.length);
            }
            out.close();
            in.close();
            index++;
        }
        File studioFile = new File(tmpDir, "default.studio");
        loadUnCompressedStudio(studioFile);
        for (Layout layout : Layout.getLayouts().values()) {
            for (LayoutItem item : layout.getItems()) {
                String location = item.getSource().getLocation();
                if (location.startsWith(STUDIOPATH)) {
                    item.getSource().setLocation(location.replaceFirst(STUDIOPATH, tmpDir.getAbsolutePath() + "/"));
                }
            }
        }
    }
    
    public static void main(String[] args) {
        Studio studio = new Studio(null);
        try {
            try {
                studio.saveStudio(new File("/home/patrick/test.studioz"), new Mixer());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            } catch (URISyntaxException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                studio.loadStudio(new File("/home/patrick/test.studioz"));
            } catch (InvalidPreferencesFormatException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BackingStoreException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
