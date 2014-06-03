/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.channels.transitions;

import webcamstudio.streams.SourceChannel;
import webcamstudio.streams.Stream;

/**
 *
 * @author patrick (modified by karl)
 */
public abstract class Transition implements Runnable{

    public static Transition getInstance(Stream source, String name) {
        Transition t = null;
        switch (name) {
            case "FadeIn":
                t = new FadeIn(source);
                break;
            case "FadeOut":
                t = new FadeOut(source);
                break;
            case "TranslateIn":
                t = new TranslateIn(source);
                break;
            case "TranslateOut":
                t = new TranslateOut(source);
                break;
            case "CornerResize":
                t= new CornerResize(source);
                break;
            case "CornerShrink":
                t= new CornerShrink(source);
                break;    
            case "ResizeIn":
                t= new ResizeIn(source);
                break;
            case "RevealLeft":
                t = new RevealLeft(source);
                break;
            case "RevealRight":
                t = new RevealRight(source);
                break;
            case "HideRight":
                t = new HideRight(source);
                break;
            case "HideLeft":
                t = new HideLeft(source);
                break;
            case "ShrinkOut":
                t = new ShrinkOut(source);
                break;
            case "AudioFadeIn":
                t = new AudioFadeIn(source);
                break;
            case "AudioFadeOut":
                t = new AudioFadeOut(source);
                break;
        }
        return t;
    }

    public static String[] getStartTransitions() {
        String[] ts = {"FadeIn","AudioFadeIn","TranslateIn","CornerResize","ResizeIn","RevealLeft","RevealRight"};
        return ts;
    }

    public static String[] getEndTransitions() {
        String[] ts = {"FadeOut","AudioFadeOut","TranslateOut","CornerShrink","ShrinkOut","HideLeft","HideRight"};
        return ts;
    }

    Stream source= null;
    SourceChannel channel = null;
    
    protected Transition(Stream source){
        this.source=source;
    }
    protected abstract void execute();
    
    public Transition run(SourceChannel sc){
        channel=sc;
        return this;
    }
    @Override
    public void run() {
        execute();
    }
}
