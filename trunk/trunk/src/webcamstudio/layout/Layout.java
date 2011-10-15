/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.layout;

import java.awt.Color;
import java.awt.Font;
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
    private String hotKey = "";
    private boolean isActive = false;
    private boolean isEntering = false;
    private boolean isExiting = false;
    private static Layout activeLayout = null;
    public static Layout previousActiveLayout = null;
    private String inputSource = "";
    private String inputSourceApp = "";
    private int duration = 0;
    private String nextLayoutName = "";
    public long timeStamp = 0;
    protected BufferedImage preview = null;
    private LayoutItem itemSelected = null;
    public void setDuration(int sec, String nextLayout) {
        duration = sec;
        nextLayoutName = nextLayout;
    }

    public int getDuration() {
        return duration;
    }

    public void setItemSelected(LayoutItem layoutItem){
        itemSelected = layoutItem;
    }
    public LayoutItem getItemSelected(){
        return itemSelected;
    }
    public String getNextLayout() {
        return nextLayoutName;
    }

    public void setAudioSource(String source) {
        inputSource = source;
    }

    public void setAudioApp(String app) {
        inputSourceApp = app;
    }

    public String getAudioSource() {
        return inputSource;
    }

    public String getAudioApp() {
        return inputSourceApp;
    }

    public static Layout getActiveLayout() {
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
        if (activeLayout!=null) {
            activeLayout.exitLayout();

        } 
        activeLayout = this;
        if (inputSourceApp.length() > 0) {
            PulseAudioManager p = new PulseAudioManager();
            p.setSoundInput(inputSourceApp, inputSource);
        }
        for (LayoutItem item : items.values()) {
            item.getSource().setLayer(item.getLayer());
        }
        if (items.size() > 0) {
            java.util.concurrent.ExecutorService tp = java.util.concurrent.Executors.newFixedThreadPool(items.size());
            for (LayoutItem item : items.values()) {
                item.setTransitionToDo(item.getTransitionIn(),item.getTransitionDurationIn());
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
            tp = null;
        }
        isEntering = false;
        isActive = true;

    }

    protected void exitLayout() {
        isExiting = true;
        isActive = false;
        timeStamp=0;
        previousActiveLayout = this;
        if (items.size() > 0) {
            java.util.concurrent.ExecutorService tp = java.util.concurrent.Executors.newFixedThreadPool(items.size());
            for (LayoutItem item : items.values()) {
                item.setTransitionToDo(item.getTransitionOut(),item.getTransitionDurationOut());
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
            tp = null;
        }
        isExiting = false;

    }

    public Collection<LayoutItem> getItems() {
        return items.values();
    }

    public Image getPreview(int w, int h,boolean isSelected) {
        if (preview == null || preview.getWidth() != w || preview.getHeight() != h) {
            preview = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D buffer = preview.createGraphics();
        buffer.setBackground(Color.GRAY);
        buffer.clearRect(0, 0, w, h);
        buffer.setStroke(new java.awt.BasicStroke(10f*w/320));
        buffer.setColor(Color.BLACK);
        buffer.drawRect(0, 0, w, h);
        for (LayoutItem item : items.values()) {
            Color color = Color.WHITE;
            
            if (item.getSource() instanceof VideoSourceV4L || item.getSource() instanceof VideoSourceDV) {
                color = Color.RED.darker();
            } else if (item.getSource() instanceof VideoSourceText) {
                color = Color.DARK_GRAY.darker();
            } else if (item.getSource() instanceof VideoSourceWidget || item.getSource() instanceof VideoSourceAnimation) {
                color = Color.GREEN.darker();
            } else if (item.getSource() instanceof VideoSourceMovie) {
                color = Color.BLUE.darker();
            } else if (item.getSource() instanceof VideoSourceImage) {
                color = Color.YELLOW.darker();
            } else if (item.getSource() instanceof VideoSourceDesktop) {
                color = Color.ORANGE.darker();
            }
            if (item.getSource().getImage() != null && (isActive||isEntering||isExiting)) {
                buffer.drawImage(item.getSource().getImage(), item.getSource().getShowAtX(), item.getSource().getShowAtY(), item.getSource().getOutputWidth() + item.getSource().getShowAtX(), item.getSource().getOutputHeight() + item.getSource().getShowAtY(), 0, 0, item.getSource().getImage().getWidth(), item.getSource().getImage().getHeight(), null);
            }
            if (item.equals(itemSelected)){
                color=color.brighter();
            }
            buffer.setColor(color);
            buffer.drawRect(item.getX(), item.getY(), item.getWidth(), item.getHeight());
        }
        if (isSelected){
            buffer.setColor(Color.DARK_GRAY);
        } else {
            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5F));
            buffer.setColor(Color.DARK_GRAY);
        }
        buffer.fillRect(0, 0, w, (34*h/240));
        if (isEntering) {
            buffer.setColor(Color.YELLOW);
        } else if (isExiting) {
            buffer.setColor(Color.RED);
        } else if (isActive) {
            buffer.setColor(Color.GREEN);
        } else {
            buffer.setColor(Color.RED);
        }
        buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1F));
        buffer.setFont(new Font(Font.MONOSPACED, Font.BOLD, (30*w/320)));
        buffer.drawString(name, (5*w/320),(30 *h/240) );
        buffer.dispose();

        return preview;
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
        if (items.size() != 0) {
            index = items.lastKey() + 1;
        }
        VideoSource tempSource = source;
        if (VideoSource.loadedSources.containsKey(source.getLocation())) {
            tempSource = VideoSource.loadedSources.get(source.getLocation());
            System.out.println("Same source" + source.getLocation());
        } else {
            VideoSource.loadedSources.put(source.getLocation(), source);
            System.out.println("Not same source" + source.getLocation());
        }
        LayoutItem item = new LayoutItem(tempSource, index);
        items.put(item.getLayer(), item);
        if (isActive) {
            item.getSource().setLayer(item.getLayer());
            item.setActive(true);
            item.setTransitionToDo(item.getTransitionIn(),item.getTransitionDurationIn());
            item.run();

        }
    }

    public void applyStudioConfig(java.util.prefs.Preferences prefs, int order) {
        java.util.prefs.Preferences layout = prefs.node(Studio.getKeyIndex(order));
        layout.put("name", name);
        layout.put("uuid", layoutUUID);
        layout.put("hotkey", hotKey);
        layout.put("inputsource", inputSource);
        layout.put("inputsourceapp", inputSourceApp);
        layout.putInt("duration", duration);
        layout.put("nextlayout", nextLayoutName);
        for (LayoutItem item : items.values()) {
            item.applyStudioConfig(layout.node("Items").node("" + item.getLayer()));
        }
    }

    public void loadFromStudioConfig(java.util.prefs.Preferences prefs) throws BackingStoreException {
        java.util.prefs.Preferences layout = prefs;
        name = layout.get("name", name);
        layoutUUID = layout.get("uuid", layoutUUID);
        hotKey = layout.get("hotkey", hotKey);
        inputSource = layout.get("inputsource", inputSource);
        inputSourceApp = layout.get("inputsourceapp", inputSourceApp);
        duration = layout.getInt("duration", duration);
        nextLayoutName = layout.get("nextlayout", nextLayoutName);
        String[] itemIndexes = layout.node("Items").childrenNames();
        for (String itemIndex : itemIndexes) {
            LayoutItem item = new LayoutItem(null, 0);
            item.loadFromStudioConfig(layout.node("Items").node(itemIndex));
            items.put(item.getLayer(), item);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
