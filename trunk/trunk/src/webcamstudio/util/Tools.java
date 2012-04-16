/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.util;

/**
 *
 * @author patrick
 */
public class Tools {
    public enum OS{
        WINDOWS,
        LINUX,
        OSX
    }
    public static OS getOS(){
        OS os =OS.LINUX;
        String value = System.getProperty("os.name").toLowerCase().trim();
        if (value.indexOf("linux")!=-1){
            os=OS.LINUX;
        } else if (value.indexOf("windows") != -1){
            os=OS.WINDOWS;
        } else if (value.indexOf("osx") !=-1){
            os=OS.OSX;
        }
        return os;
    }
    public static String getOSName(){
        String name = "linux";
        OS os = getOS();
        switch(os){
            case LINUX:
                name="linux";
                break;
            case WINDOWS:
                name="windows";
                break;
            case OSX:
                name="osx";
                break;
        }
        
        return name;
    }
}
