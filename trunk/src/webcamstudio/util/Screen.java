/*
 * Copyright (C) 2014 patrick
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package webcamstudio.util;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;

/**
 *
 * @author patrick
 */
public class Screen {

    public static String[] getSources() {
        java.util.ArrayList<String> list = new java.util.ArrayList<>();
        System.out.println("Screen List:");
        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = g.getScreenDevices();

        for (GraphicsDevice d : devices){
            System.out.println(d.getIDstring() + " " + d.getDefaultConfiguration().getBounds().toString().replaceAll("java.awt.Rectangle", ""));
            list.add(d.getIDstring());
        }
        return list.toArray(new String[list.size()]);

    }
    
    public static int getWidth(String id){
        System.out.println("Screen: "+id);
        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = g.getScreenDevices();

        int retValue = 0;
        for (GraphicsDevice d : devices){
           if (d.getIDstring().equals(id)){
               retValue = d.getDisplayMode().getWidth();
               break;
           } 
        }
        return retValue;
    }
    public static int getHeight(String id){
        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = g.getScreenDevices();
        int retValue = 0;
        for (GraphicsDevice d : devices){
           if (d.getIDstring().equals(id)){
               retValue = d.getDisplayMode().getHeight();
               break;
           } 
        }
        return retValue;
    }
    
    public static int getX(String id){
        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = g.getScreenDevices();

        int retValue = 0;
        for (GraphicsDevice d : devices){
           if (d.getIDstring().equals(id)){
               retValue = d.getDefaultConfiguration().getBounds().x;
               break;
           } 
        }
        return retValue;
    }
    
    public static int getY(String id){
        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = g.getScreenDevices();
        int retValue = 0;
        for (GraphicsDevice d : devices){
           if (d.getIDstring().equals(id)){
               retValue = d.getDefaultConfiguration().getBounds().y;
               break;
           } 
        }
        return retValue;
    }
    
    public static boolean avconvDetected(){
        boolean retValue = false;
        Process p = null;
        try {
            
            p = Runtime.getRuntime().exec("avconv");
            p.waitFor();
//            System.out.println(p.exitValue());
            retValue = p.exitValue() == 1;
        } catch (IOException | InterruptedException ex) {
//            System.err.println(ex.getMessage());
        } finally {
            if (p != null){
                p.destroy();
                p=null;
            }
        }
        return retValue;
    }
    
    public static boolean ffmpegDetected(){
        boolean retValue = false;
        Process p = null;
        try {
            
            p = Runtime.getRuntime().exec("ffmpeg");
            p.waitFor();
//            System.out.println(p.exitValue());
            retValue = p.exitValue() == 1;
        } catch (IOException | InterruptedException ex) {
//            System.err.println(ex.getMessage());
        } finally {
            if (p != null){
                p.destroy();
                p=null;
            }
        }
        return retValue;
    }
    
}
