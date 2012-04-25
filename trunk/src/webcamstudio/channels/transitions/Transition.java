/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.channels.transitions;

import webcamstudio.streams.SourceChannel;
import webcamstudio.streams.Stream;

/**
 *
 * @author patrick
 */
public abstract class Transition implements Runnable{

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
    public static Transition getInstance(Stream source,String name){
        Transition t = null;
        if (name.equals("FadeIn")){
            t = new FadeIn(source);
        } else if (name.equals("FadeOut")){
            t = new FadeOut(source);
        } else if (name.equals("Translate")){
            t = new Translate(source);
        } else if (name.equals("Resize")){
            t= new Resize(source);
        } else if (name.equals("RevealLeft")){
            t = new RevealLeft(source);
        }
        return t;
    }
    
    public static String[] getStartTransitions(){
        String[] ts = {"FadeIn","Translate","Resize","RevealLeft"};
        return ts;
    }
    public static String[] getEndTransitions(){
        String[] ts = {"FadeOut","Shrink"};
        return ts;
    }
}
