/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.awt.Component;
import java.util.ArrayList;
import webcamstudio.streams.SourceDVB;
import webcamstudio.streams.SourceDesktop;
import webcamstudio.streams.SourceImage;
import webcamstudio.streams.SourceImageGif;
import webcamstudio.streams.SourceImageU;
import webcamstudio.streams.SourceMovie;
import webcamstudio.streams.SourceMusic;
import webcamstudio.streams.SourceQRCode;
import webcamstudio.streams.SourceText;
import webcamstudio.streams.SourceURL;
import webcamstudio.streams.SourceWebcam;
import webcamstudio.streams.Stream;

/**
 *
 * @author patrick (modified by karl)
 */
public class SourceControls {

    public static ArrayList<Component> getControls(Stream source) {
        ArrayList<Component> comps = new ArrayList<>();
        Component c = null;
        Component d = null;
        c = new SourceControlTransitions(source);
        comps.add(c);
        d = new SourceControlChannels(source);
        d.setName("CH Options");
        comps.add(d);
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
        } else if (source instanceof SourceQRCode) {
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
//            c = new SourceControlEffects(source);
//            c.setName("Effects");
//            comps.add(c);
        }

        return comps;
    }
}
