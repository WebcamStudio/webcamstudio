/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import webcamstudio.sources.VideoSource;

/**
 *
 * @author pballeux
 */
public class LayerManager {

    private static java.util.TreeMap<Integer, VideoSource> sources = new java.util.TreeMap<Integer, VideoSource>();

    public static void add(VideoSource source) {
        if (sources.size() == 0) {
            source.setLayer(1);
        } else {
            source.setLayer(sources.lastKey() + 1);
        }
        sources.put(source.getLayer(), source);
    }

    public static void remove(VideoSource source) {
        sources.remove(source.getLayer());
    }

    public static void clear() {
        sources.clear();
    }

    public static void updateSourcesLayer(){
        java.util.Vector<VideoSource> list = new java.util.Vector<VideoSource>();
        list.addAll(sources.values());
        sources.clear();
        for (VideoSource source : list){
            sources.put(source.getLayer(), source);
        }
    }
    public static boolean isLoaded(VideoSource source) {
        return sources.containsValue(source);
    }

    public static int size() {
        return sources.size();
    }

    public static void remove(int index) {
        sources.remove(index);
    }

    public static java.util.Collection<VideoSource> getSources() {
        return sources.values();
    }

    public static java.util.Collection<VideoSource> getSourcesReversed() {
        java.util.Vector<VideoSource> retValues = new java.util.Vector<VideoSource>();
        if (sources.size() > 0) {
            Integer key = sources.lastKey();
            while (key != null) {
                retValues.add(sources.get(key));
                key = sources.lowerKey(key);
            }
        }
        return retValues;
    }

    public static VideoSource getByUUID(String uuid) {
        VideoSource source = null;
        for (VideoSource s : sources.values()) {
            if (s.getUUID().equals(uuid)) {
                source = s;
                break;
            }
        }
        return source;
    }

    public static void moveDown(VideoSource source) {
        if (sources.lowerKey(source.getLayer()) != null) {
            int currentLayer = source.getLayer();
            int prevLayer = sources.lowerKey(source.getLayer());
            VideoSource otherSource = sources.get(prevLayer);
            source.setLayer(prevLayer);
            otherSource.setLayer(currentLayer);
            sources.remove(source.getLayer());
            sources.remove(otherSource.getLayer());
            sources.put(source.getLayer(), source);
            sources.put(otherSource.getLayer(), otherSource);
        }
    }

    public static void moveUp(VideoSource source) {
        if (sources.higherKey(source.getLayer()) != null) {
            int currentLayer = source.getLayer();
            int nextLayer = sources.higherKey(source.getLayer());
            VideoSource otherSource = sources.get(nextLayer);
            source.setLayer(nextLayer);
            otherSource.setLayer(currentLayer);
            sources.remove(source.getLayer());
            sources.remove(otherSource.getLayer());
            sources.put(source.getLayer(), source);
            sources.put(otherSource.getLayer(), otherSource);
        }
    }
}
