/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webcamstudio.sources;

/**
 *
 * @author pballeux
 */
public interface VideoSourceIRCListener {

    public void newLine(VideoSourceIRC irc, String from, String text);
}
