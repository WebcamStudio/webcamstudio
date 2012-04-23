/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.channels.transitions;

import webcamstudio.streams.Stream;

/**
 *
 * @author patrick
 */
public abstract class Transition implements Runnable{

    Stream source= null;
    protected Transition(Stream source){
        this.source=source;
    }
    protected abstract void execute();
    @Override
    public void run() {
        System.out.println("Executing");
        execute();
    }
    public static Transition getInstance(Stream source,String name){
        Transition t = null;
        if (name.equals("FadeIn")){
            t = new FadeIn(source);
        } else if (name.equals("FadeOut")){
            t = new FadeOut(source);
        }
        return t;
    }
    
    public static String[] getStartTransitions(){
        String[] ts = {"FadeIn"};
        return ts;
    }
    public static String[] getEndTransitions(){
        String[] ts = {"FadeOut"};
        return ts;
    }
}
