/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webcamstudio.components;

import webcamstudio.layout.transitions.Transition;
import webcamstudio.sources.VideoSource;

/**
 *
 * @author patrick
 */
public interface SourceListener {
    public void sourceUpdate(VideoSource source);
    public void sourceSetX(VideoSource source,int x);
    public void sourceSetY(VideoSource source,int y);
    public void sourceSetWidth(VideoSource source,int w);
    public void sourceSetHeight(VideoSource source,int h);
    public void sourceMoveUp(VideoSource source);
    public void sourceMoveDown(VideoSource source);
    public void sourceSetTransIn(VideoSource source,Transition in);
    public void sourceSetTransOut(VideoSource source,Transition out);
    
}
