/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.layout;

import java.util.AbstractMap;
import java.util.prefs.BackingStoreException;
import webcamstudio.layout.transitions.Start;
import webcamstudio.layout.transitions.Stop;
import webcamstudio.layout.transitions.Transition;
import webcamstudio.sources.VideoSource;
import webcamstudio.sources.VideoSourceMovie;
import webcamstudio.sources.VideoSourceMusic;
import webcamstudio.studio.Studio;

/**
 *
 * @author pballeux
 */
public class LayoutItem implements Runnable{

    VideoSource source = null;
    private int x = 0;
    private int y = 0;
    private int width = 320;
    private int height = 240;
    private Transition transIn = new Start();
    private Transition transOut = new Stop();
    private Transition transToDo = transIn;
    private int layer = 0;
    private int volume = 10;
    private boolean isInActiveLayout = false;
    private boolean keepRatio = true;
    private long position = 0;
    private int transitionDurationIn = 1;
    private int transitionDurationOut = 1;
    private int transDurationToDo = transitionDurationIn;


    public void setTransitionDurationIn(int sec){
        transitionDurationIn=sec;
    }
    public void setTransitionDurationOut(int sec){
        transitionDurationOut=sec;
    }
    public int getTransitionDurationIn(){
        return transitionDurationIn;
    }
    public int getTransitionDurationOut(){
        return transitionDurationOut;
    }
    public void setPosition(long seek){
        position=seek;
    }
    public long getPosition(){
        return position;
    }
    public void setKeepRatio(boolean keep){
        keepRatio=keep;
    }
    public boolean isKeepingRatio(){
        return keepRatio;
    }

    public void setActive(boolean status){
        isInActiveLayout = status;
    }
    public boolean isActive(){
        return isInActiveLayout;
    }
    public void setVolume(int v){
        volume=v;
    }
    public int getVolume(){
        return volume;
    }
    public void setTransitionIn(Transition in) {
        transIn = in;
    }

    public void setLayer(int l){
        layer=l;
    }
    public void setTransitionOut(Transition out) {
        transOut = out;
    }

    public void setX(int x){
        this.x=x;
    }
    public void setY(int y){
        this.y=y;
    }
    public void setWidth(int w){
        width=w;
    }
    public void setHeight(int h){
        height=h;
    }
    public Transition getTransitionIn() {
        return transIn;
    }

    public Transition getTransitionOut() {
        return transOut;
    }

    public int getLayer(){
        return layer;
    }
    public LayoutItem(VideoSource source,int layer) {
        this.source = source;
        this.layer = layer;
    }

    private Transition getTransitionByName(String tName) {
        Transition retValue = null;
        for (Transition t : Transition.getTransitions().values()) {
            if (t.getName().equals(tName)) {
                retValue = t;
                break;
            }
        }
        return retValue;
    }

    public void setTransitionToDo(Transition t,int sec){
        transToDo=t;
        transDurationToDo=sec;
    }

    public VideoSource getSource() {
        return source;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void applyStudioConfig(java.util.prefs.Preferences prefs) {
        prefs.putInt("X", x);
        prefs.putInt("Y", y);
        prefs.putInt("width", width);
        prefs.putInt("height", height);
        prefs.put("uuid", source.getUUID());
        prefs.putBoolean("keepratio",keepRatio);
        if (transIn != null) {
            prefs.put("transitionin", transIn.getName());
        }
        if (transOut != null) {
            prefs.put("transitionout", transOut.getName());
        }
        prefs.putInt("layer", layer);
        prefs.putInt("volume", volume);
        prefs.putInt("transitiondurationin",transitionDurationOut);
        prefs.putInt("transitiondurationout",transitionDurationOut);
        source.applyStudioConfig(prefs.node("source"),layer);
    }

    public void loadFromStudioConfig(java.util.prefs.Preferences prefs) throws BackingStoreException {
        x = prefs.getInt("X", x);
        y = prefs.getInt("Y", y);
        width = prefs.getInt("width", width);
        height = prefs.getInt("height", height);
        transIn = getTransitionByName(prefs.get("transitionin", "Start"));
        transOut = getTransitionByName(prefs.get("transitionout", "Stop"));
        layer = prefs.getInt("layer", layer);
        source = Studio.getSourceFromClassName(prefs.node("source").get("class",null));
        source.loadFromStudioConfig(prefs.node("source"));
        System.out.println("Source Location is " + source.getLocation());
        if (VideoSource.loadedSources.containsKey(source.getLocation())){
            source = VideoSource.loadedSources.get(source.getLocation());
            System.out.println("Replacing source");
        } else {
            VideoSource.loadedSources.put(source.getLocation(), source);
        }
        keepRatio = prefs.getBoolean("keepratio",keepRatio);
        volume = prefs.getInt("volume", volume);
        transitionDurationIn=prefs.getInt("transitiondurationin",1);
        transitionDurationOut=prefs.getInt("transitiondurationout",1);
    }
    @Override
    public String toString(){
        return source.getName();
    }

    @Override
    public void run() {
        source.setLayer(layer);
        if (source instanceof VideoSourceMovie){
            VideoSourceMovie movie = (VideoSourceMovie)source;
            movie.seek(position);
        }
        else if(source instanceof VideoSourceMusic)
        {
            VideoSourceMusic music = (VideoSourceMusic)source;
            music.seek(position);
        }
        if (transToDo == null || source.isIgnoringLayoutTransition()) {
            source.setShowAtX(x);
            source.setShowAtY(y);
            source.setOutputWidth(width);
            source.setOutputHeight(height);
            source.setVolume(volume);
            source.fireSourceUpdated();
        } else {
            transToDo.doTransition(this,transDurationToDo);
        }
    }
}
