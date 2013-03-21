/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webcamstudio;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pballeux
 */
public class Version {
    public final static String version = "0.61";
    private String build = "";
    public Version(){
        java.io.DataInput di = new java.io.DataInputStream(getClass().getResourceAsStream("build.txt"));
        try {
            build = di.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Version.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public String getBuild(){
        return build;
    }
}
