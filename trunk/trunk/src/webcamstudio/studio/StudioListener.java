/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.studio;

/**
 *
 * @author patrick
 */
public interface StudioListener {
    public void startSaving(String studio,int itemCount);
    public void startLoading(String studio,int itemCount);
    public void unCompressing(String file,long size, long currentPos,int itemIndex);
    public void compressing(String file,long size,long currentPos,int itemIndex);
    public void completedSaving(String studio);
    public void completedLoading(String studio);
    
}
