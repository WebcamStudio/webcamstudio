/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.util.ArrayList;
import webcamstudio.sources.effects.Effect;

/**
 *
 * @author patrick
 */
public class SourceChannel {

    int x = 0;
    int y = 0;
    int capWidth = 0;
    int capHeight = 0;
    int width = 0;
    int height = 0;
    int opacity = 0;
    float volume = 0;
    int zorder = 0;
    String text = "";
    String font = "";
    int color = 0;
    String name = "";
    boolean isPlaying = false;
    ArrayList<Effect> effects = new ArrayList<Effect>();
    boolean followMouse = false;
    int captureX = 0;
    int captureY = 0;
    private SourceChannel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public static SourceChannel getChannel(String channelName, Stream stream) {
        SourceChannel s = new SourceChannel();
        s.x = stream.x;
        s.y = stream.y;
        s.width = stream.width;
        s.height = stream.height;
        s.opacity = stream.opacity;
        s.effects.addAll(stream.effects);
        s.volume = stream.volume;
        s.zorder = stream.zorder;
        s.name = channelName;
        s.isPlaying = stream.isPlaying();
        s.capHeight = stream.captureHeight;
        s.capWidth = stream.captureWidth;
        if (stream instanceof SourceText) {
            SourceText st = (SourceText)stream;
            s.text=st.content;
            s.font=st.fontName;
            s.color=st.color;
            
        } else if (stream instanceof SourceDesktop){
            SourceDesktop sd = (SourceDesktop)stream;
            s.followMouse=sd.followMouse;
            s.captureX=sd.captureX;
            s.captureY=sd.captureY;
        }
        return s;
    }

    public void apply(Stream s) {
        s.x = x;
        s.y = y;
        s.width = width;
        s.height = height;
        s.opacity = opacity;
        s.effects.clear();
        s.effects.addAll(effects);
        s.volume = volume;
        s.zorder = zorder;
        s.captureHeight = capHeight;
        s.captureWidth = capWidth;
        if (s instanceof SourceText) {
            SourceText st = (SourceText)s;
            st.content=text;
            st.fontName=font;
            st.color=color;
            st.updateContent(text);
        } else if (s instanceof SourceDesktop){
            SourceDesktop sd = (SourceDesktop)s;
            sd.followMouse=followMouse;
            sd.captureX=captureX;
            sd.captureY=captureY;
        }
        if (isPlaying) {
            if (!s.isPlaying()) {
                System.out.println("Starting source");
                s.read();
            }
        } else {
            if (s.isPlaying()) {
                System.out.println("Stopping source");
                s.stop();
            }
        }
    }
}
