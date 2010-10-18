/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webcamstudio.components;

import webcamstudio.sources.VideoSource;

/**
 *
 * @author pballeux
 */
public interface SourceListListener {
    public void sourceRemoved(VideoSource source);
}
