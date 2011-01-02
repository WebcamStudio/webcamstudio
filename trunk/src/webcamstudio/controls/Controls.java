/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webcamstudio.controls;

import webcamstudio.components.SourceListener;

/**
 *
 * @author pballeux
 */
public interface Controls {

    public String getLabel();
    public void removeControl();
    public void setListener(SourceListener l);

}
