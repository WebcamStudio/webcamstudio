/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.awt.Component;
import java.util.ArrayList;
import webcamstudio.streams.*;

/**
 *
 * @author patrick (modified by karl)
 */
public class SourceControls {

    public static ArrayList<Component> getControls(Stream source) {
        ArrayList<Component> comps = new ArrayList<Component>();
        Component c = null;
        c = new SourceControlTransitions(source);
        comps.add(c);
        if (source instanceof SourceDesktop) {
            c = new SourceControlDesktop((SourceDesktop) source);
            comps.add(c);
            c = new SourceControlEffects(source);
            c.setName("Effects");
            comps.add(c);
        } else if (source instanceof SourceMovie) {
            c = new SourceControlEffects(source);
            c.setName("Effects");
            comps.add(c);
        } else if (source instanceof SourceImageU) {
            c = new SourceControlEffects(source);
            c.setName("Effects");
            comps.add(c);
        } else if (source instanceof SourceDVB) {
            c = new SourceControlEffects(source);
            c.setName("Effects");
            comps.add(c);
        } else if (source instanceof SourceURL) {
            c = new SourceControlEffects(source);
            c.setName("Effects");
            comps.add(c);
        } else if (source instanceof SourceWebcam) {
            c = new SourceControlEffects(source);
            c.setName("Effects");
            comps.add(c);
        } else if (source instanceof SourceText) {
            c = new SourceControlsText((SourceText) source);
            comps.add(c);
            c = new SourceControlEffects(source);
            c.setName("Effects");
            comps.add(c);
        } else if (source instanceof SourceMusic) {
        } else if (source instanceof SourceImage) {
            c = new SourceControlEffects(source);
            c.setName("Effects");
            comps.add(c);
        } else if (source instanceof SourceImageGif) {
            c = new SourceControlEffects(source);
            c.setName("Effects");
            comps.add(c);
        } else if (source instanceof SourceQRCode) {
        }

        return comps;
    }
}
