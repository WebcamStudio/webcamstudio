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
        retValue.put(SlideIn.class.getSimpleName(), new SlideIn());
        retValue.put(SlideOut.class.getSimpleName(), new SlideOut());
        return retValue;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }
}
