/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import webcamstudio.media.Image;

/**
 *
 * @author patrick
 */
public interface VideoListener {
    public void newImage(Image image);
}
