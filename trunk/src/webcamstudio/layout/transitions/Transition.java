/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.layout.transitions;

import webcamstudio.layout.LayoutItem;

/**
 *
 * @author pballeux
 */
public abstract class Transition {

    public abstract void doTransition(LayoutItem item);

    public static java.util.TreeMap<String, Transition> getTransitions() {
        java.util.TreeMap<String, Transition> retValue = new java.util.TreeMap<String, Transition>();
        retValue.put(Slide.class.getSimpleName(), new Slide());
        retValue.put(None.class.getSimpleName(), new None());
        retValue.put(FadeIn.class.getSimpleName(), new FadeIn());
        retValue.put(FadeOut.class.getSimpleName(), new FadeOut());
        retValue.put(Start.class.getSimpleName(), new Start());
        retValue.put(Stop.class.getSimpleName(), new Stop());
        retValue.put(ShrinkIn.class.getSimpleName(), new ShrinkIn());
        retValue.put(ShrinkOut.class.getSimpleName(), new ShrinkOut());
        retValue.put(SlideDown.class.getSimpleName(), new SlideDown());
        retValue.put(SlideUp.class.getSimpleName(), new SlideUp());
        retValue.put(SlideLeft.class.getSimpleName(), new SlideLeft());
        retValue.put(SlideRight.class.getSimpleName(), new SlideRight());
        return retValue;
    }
    public static java.util.TreeMap<String, Transition> getAudioTransitions() {
        java.util.TreeMap<String, Transition> retValue = new java.util.TreeMap<String, Transition>();
        retValue.put(None.class.getSimpleName(), new None());
        retValue.put(AudioFadeIn.class.getSimpleName(), new AudioFadeIn());
        retValue.put(AudioFadeOut.class.getSimpleName(), new AudioFadeOut());
        retValue.put(Start.class.getSimpleName(), new Start());
        retValue.put(Stop.class.getSimpleName(), new Stop());
        return retValue;
    }

    public static java.util.TreeMap<String, Transition> getTransitionIns() {
        java.util.TreeMap<String, Transition> retValue = new java.util.TreeMap<String, Transition>();
        retValue.put(Slide.class.getSimpleName(), new Slide());
        retValue.put(None.class.getSimpleName(), new None());
        retValue.put(FadeIn.class.getSimpleName(), new FadeIn());
        retValue.put(Start.class.getSimpleName(), new Start());
        retValue.put(ShrinkIn.class.getSimpleName(), new ShrinkIn());
        retValue.put(SlideDown.class.getSimpleName(), new SlideDown());
        retValue.put(SlideUp.class.getSimpleName(), new SlideUp());
        retValue.put(SlideLeft.class.getSimpleName(), new SlideLeft());
        retValue.put(SlideRight.class.getSimpleName(), new SlideRight());
        return retValue;
    }
    public static java.util.TreeMap<String, Transition> getTransitionOuts() {
        java.util.TreeMap<String, Transition> retValue = new java.util.TreeMap<String, Transition>();
        retValue.put(None.class.getSimpleName(), new None());
        retValue.put(FadeOut.class.getSimpleName(), new FadeOut());
        retValue.put(Stop.class.getSimpleName(), new Stop());
        retValue.put(ShrinkOut.class.getSimpleName(), new ShrinkOut());
        return retValue;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }
}
