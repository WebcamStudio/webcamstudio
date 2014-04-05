/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author patrick
 */
public class Tools {
    final static String userHome = System.getProperty("user.home");
    
    public enum OS {

        WINDOWS,
        LINUX,
        OSX
    }

    public static OS getOS() {
        OS os = OS.LINUX;
        String value = System.getProperty("os.name").toLowerCase().trim();
        if (value.indexOf("linux") != -1) {
            os = OS.LINUX;
        } else if (value.indexOf("windows") != -1) {
            os = OS.WINDOWS;
        } else if (value.indexOf("os x") != -1) {
            os = OS.OSX;
        }
        return os;
    }

    public static String getOSName() {
        String name = "linux";
        OS os = getOS();
        switch (os) {
            case LINUX:
                name = "linux";
                break;
            case WINDOWS:
                name = "windows";
                break;
            case OSX:
                name = "osx";
                break;
        }

        return name;
    }
    
    public static String getUserHome() {
        return userHome;
    }
    
    public static void sleep(long millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException ex) {
            Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void waitUntil(long nanoEndTime) {
        long delta = nanoEndTime - System.nanoTime();
        if (delta > 0) {
            try {
                if (delta > 999999) {
                    long milli = delta / 1000000;
                    long nano = delta % 1000000;
                    Thread.sleep(milli, (int) nano);
                } else {
                    Thread.sleep(0, (int) delta);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
