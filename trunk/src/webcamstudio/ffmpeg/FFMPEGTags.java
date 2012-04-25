/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.ffmpeg;

/**
 *
 * @author patrick
 */
public enum FFMPEGTags {
   
        OWIDTH("@OWIDTH"),
        OHEIGHT("@OHEIGHT"),
        CWIDTH("@CWIDTH"),
        CHEIGHT("@CHEIGHT"),
        RATE("@RATE"),
        SEEK("@SEEK"),
        VPORT("@VPORT"),
        APORT("@APORT"),
        FILE("@FILE"),
        FREQ("@FREQ"),
        BITSIZE("@BITSIZE"),
        CHANNELS("@CHANNELS"),
        VCODEC("@VCODEC"),
        ACODEC("@ACODEC"),
        VBITRATE("@VBITRATE"),
        ABITRATE("@ABITRATE"),
        URL("@URL");
        
    private String name = "";
    private FFMPEGTags(String name) {
        this.name = name;
    }
    public String toString() {
    return name;
    }
}    

