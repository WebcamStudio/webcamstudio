/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.layout;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import webcamstudio.components.PulseAudioManager;
import webcamstudio.layout.transitions.Transition;
import webcamstudio.sources.*;
import webcamstudio.studio.Studio;

/**
 *
 * @author pballeux
 */
public class Layout {

    private java.util.TreeMap<Integer, LayoutItem> items = new java.util.TreeMap<Integer, LayoutItem>();
    private String name = "";
    private String layoutUUID = java.util.UUID.randomUUID().toString();
    private String hotKey = "F1";
    private boolean isActive = false;
    private boolean isEntering = false;
    private boolean isExiting = false;
    private static Layout activeLayout = null;
    private String inputSource ="";
    private String inputSourceApp = "";
    private int duration = 0;
    private String nextLayoutName = "";
    public long timeStamp = 0;
    public void setDuration(int sec,String nextLayout){
        duration = sec;
        nextLayoutName=nextLayout;
    }
    public int getDuration(){
        return duration;
    }
    public String getNextLayout(){
        return nextLayoutName;
    }
    public void setAudioSource(String source){
        inputSource = source;
    }
    public void setAudioApp(String app){
        inputSourceApp = app;
    }
    public String getAudioSource(){
        return inputSource;
    }
    public String getAudioApp(){
        return inputSourceApp;
    }
    public static Layout getActiveLayout(){
        return activeLayout;
    }

    public Layout(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return isActive;
    }

    public Layout() {
    }

    public String getHotKey() {
        return hotKey;
    }

    public void setHotKey(String key) {
        hotKey = key;
    }

    public void moveUpItem(LayoutItem item) {
        if (items.higherKey(item.getLayer()) != null) {
            int highKey = items.higherKey(item.getLayer());
            LayoutItem hiItem = items.get(highKey);
            hiItem.setLayer(item.getLayer());
            item.setLayer(highKey);
            items.remove(item.getLayer());
            items.remove(hiItem.getLayer());
            items.put(item.getLayer(), item);
            items.put(hiItem.getLayer(), hiItem);
        }
    }

    public void moveDownItem(LayoutItem item) {
        if (items.lowerKey(item.getLayer()) != null) {
            int loKey = items.lowerKey(item.getLayer());
            LayoutItem loItem = items.get(loKey);
            loItem.setLayer(item.getLayer());
            item.setLayer(loKey);
            items.remove(item.getLayer());
            items.remove(loItem.getLayer());
            items.put(item.getLayer(), item);
            items.put(loItem.getLayer(), loItem);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void enterLayout() {
        isEntering = true;
        activeLayout=this;
        if (inputSourceApp.length()>0){
            PulseAudioManager p = new PulseAudioManager();
            p.setSoundInput(inputSourceApp, inputSource);
        }
        for (LayoutItem item : items.values()) {
            item.getSource().setLayer(item.getLayer());
        }
        java.util.concurrent.ExecutorService tp = java.util.concurrent.Executors.newCachedThreadPool();
        for (LayoutItem item : items.values()) {
            item.setTransitionToDo(item.getTransitionIn());
            item.setActive(true);
            tp.submit(item);
        }
        tp.shutdown();

        try {
            while (!tp.isTerminated()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Layout.class.getName()).log(Level.SEVERE, null, ex);
        }
        isEntering = false;
        isActive = true;
    }

    public void exitLayout() {
        isExiting = true;
        java.util.concurrent.ExecutorService tp = java.util.concurrent.Executors.newCachedThreadPool();
        for (LayoutItem item : items.values()) {
            item.setTransitionToDo(item.getTransitionOut());
            item.setActive(false);
            tp.submit(item);
        }
        tp.shutdown();
        try {
            while (!tp.isTerminated()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Layout.class.getName()).log(Level.SEVERE, null, ex);
        }
        isExiting = false;
        isActive = false;
    }
    public Collection<LayoutItem> getItems() {
        java.util.Vector<LayoutItem> retValues = new java.util.Vector<LayoutItem>();
        if (items.size() > 0) {
            Integer key = items.firstKey();
            while (key != null) {
                retValues.add(items.get(key));
                key = items.higherKey(key);
            }
        }
        return retValues;
    }
    public Collection<LayoutItem> getReversedItems() {
        java.util.Vector<LayoutItem> retValues = new java.util.Vector<LayoutItem>();
        if (items.size() > 0) {
            Integer key = items.lastKey();
            while (key != null) {
                retValues.add(items.get(key));
                key = items.lowerKey(key);
            }
        }
        return retValues;
    }

    public Image getPreview() {
        BufferedImage image = new java.awt.image.BufferedImage(320, 240, java.awt.image.BufferedImage.TRANSLUCENT);
        Graphics2D buffer = image.createGraphics();
        buffer.setBackground(Color.DARK_GRAY);
        buffer.setStroke(new java.awt.BasicStroke(10f));
        buffer.setColor(Color.BLACK);
        buffer.drawRect(0, 0, image.getWidth(), image.getHeight());
        for (LayoutItem item : items.values()) {
            buffer.setColor(Color.WHITE);
            if (item.getSource() instanceof VideoSourceV4L || item.getSource() instanceof VideoSourceDV) {
                buffer.setColor(Color.RED);
            } else if (item.getSource() instanceof VideoSourceText) {
                buffer.setColor(Color.DARK_GRAY);
            } else if (item.getSource() instanceof VideoSourceWidget || item.getSource() instanceof VideoSourceAnimation) {
                buffer.setColor(Color.GREEN);
            } else if (item.getSource() instanceof VideoSourceMovie) {
                buffer.setColor(Color.BLUE);
            } else if (item.getSource() instanceof VideoSourceImage) {
                buffer.setColor(Color.YELLOW);
            } else if (item.getSource() instanceof VideoSourceDesktop) {
                buffer.setColor(Color.ORANGE);
            }
            if (item.getSource().getImage() != null) {
                buffer.drawImage(item.getSource().getImage(), item.getX(), item.getY(), item.getWidth() + item.getX(), item.getHeight() + item.getY(), 0, 0, item.getSource().getImage().getWidth(), item.getSource().getImage().getHeight(), null);
            }
            buffer.drawRect(item.getX(), item.getY(), item.getWidth(), item.getHeight());
        }
        if (isEntering) {
            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5F));
            buffer.setColor(Color.YELLOW);
            buffer.fillRect(0, 0, image.getWidth(), image.getHeight());
        } else if (isExiting) {
            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5F));
            buffer.setColor(Color.RED);
            buffer.fillRect(0, 0, image.getWidth(), image.getHeight());
        } else if (isActive) {
            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5F));
            buffer.setColor(Color.GREEN);
            buffer.fillRect(0, 0, image.getWidth(), image.getHeight());
        }
        buffer.dispose();

        return image;
    }

    public void removeSource(VideoSource source) {
        LayoutItem foundItem = null;
        for (LayoutItem item : items.values()) {
            if (item.getSource().getUUID().equals(source.getUUID())) {
                foundItem = item;
            }
        }
        if (foundItem != null) {
            items.remove(foundItem.getLayer());
        }
    }

    public void updateSourceTransition(VideoSource source, Transition transIn, Transition transOut) {
        for (LayoutItem item : items.values()) {
            if (item.getSource().getUUID().equals(source.getUUID())) {
                item.setTransitionIn(transIn);
                item.setTransitionOut(transOut);
            }
        }
    }

    public void addSource(VideoSource source) {
        int index = 0;
        if (items.size()!=0){
            index = items.lastKey()+1;
        }
        VideoSource tempSource = source;
        if (VideoSource.loadedSources.containsKey(source.getLocation())){
            tempSource = VideoSource.loadedSources.get(source.getLocation());
            System.out.println("Same source" + source.getLocation());
        } else {
            VideoSource.loadedSources.put(source.getLocation(), source);
            System.out.println("Not same source" + source.getLocation());
        }
        LayoutItem item = new LayoutItem(tempSource,  index);
        items.put(item.getLayer(), item);
    }

    public void applyStudioConfig(java.util.prefs.Preferences prefs, int order) {
        java.util.prefs.Preferences layout = prefs.node(Studio.getKeyIndex(order));
        layout.put("name", name);
        layout.put("uuid", layoutUUID);
        layout.put("hotkey", hotKey);
        layout.put("inputsource",inputSource);
        layout.put("inputsourceapp",inputSourceApp);
        layout.putInt("duration",duration);
        layout.put("nextlayout",nextLayoutName);
        for (LayoutItem item : items.values()) {
            item.applyStudioConfig(layout.node("Items").node("" + item.getLayer()));
        }
    }

    public void loadFromStudioConfig(java.util.prefs.Preferences prefs) throws BackingStoreException {
        java.util.prefs.Preferences layout = prefs;
        name = layout.get("name", name);
        layoutUUID = layout.get("uuid", layoutUUID);
        hotKey = layout.get("hotkey", hotKey);
        inputSource=layout.get("inputsource",inputSource);
        inputSourceApp=layout.get("inputsourceapp",inputSourceApp);
        duration = layout.getInt("duration",duration);
        nextLayoutName = layout.get("nextlayout",nextLayoutName);
        String[] itemIndexes = layout.node("Items").childrenNames();
        for (String itemIndex : itemIndexes) {
            LayoutItem item = new LayoutItem(null,0);
            item.loadFromStudioConfig(layout.node("Items").node(itemIndex));
            items.put(item.getLayer(), item);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}