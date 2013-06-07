/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.externals;

/**
 *
 * @author patrick (modified by karl)
 */
public enum Tags {
   
        OWIDTH("@OWIDTH"),
        OHEIGHT("@OHEIGHT"),
        CWIDTH("@CWIDTH"),
        CHEIGHT("@CHEIGHT"),
        RATE("@RATE"),
        SEEK("@SEEK"),
        VPORT("@VPORT"),
        FVPORT("@FVPORT"),
        APORT("@APORT"),
        FILE("@FILE"),
        FREQ("@FREQ"),
        BITSIZE("@BITSIZE"),
        CHANNELS("@CHANNELS"),
        VCODEC("@VCODEC"),
        ACODEC("@ACODEC"),
        VBITRATE("@VBITRATE"),
        ABITRATE("@ABITRATE"),
        DESKTOPX("@DESKTOPX"),
        DESKTOPY("@DESKTOPY"),
        DESKTOPW("@DESKTOPW"),
        DESKTOPH("@DESKTOPH"),
        DESKTOPENDX("@DESKTOPENDX"),
        DESKTOPENDY("@DESKTOPENDY"),
        DESKTOPN("@DESKTOPN"),
        URL("@URL"),
        BW("@BW"),
        DVBFREQ("@DVBFREQ"),
        DVBCH("@DVBCH"),
        WEBURL("@WEBURL");
        
    private String name = "";
    private Tags(String name) {
        this.name = name;
    }
    public String toString() {
    return name;
    }
}    

