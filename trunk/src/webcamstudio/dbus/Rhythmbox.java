/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.dbus;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.gnome.Rhythmbox.Player;
import org.gnome.Rhythmbox.Shell;

/**
 *
 * @author patrick
 */
public class Rhythmbox {

    private org.freedesktop.dbus.DBusConnection conn = null;

    public Rhythmbox() {
        try {
            conn = org.freedesktop.dbus.DBusConnection.getConnection(DBusConnection.SESSION);
        } catch (DBusException ex) {
            Logger.getLogger(Rhythmbox.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getCurrentSongTitle() {
        return getCurrentSongProperties("title");
    }

    public String getCurrentSongProperties(String name) {
        String retValue = "";

        try {
            org.freedesktop.DBus dbus = (org.freedesktop.DBus) conn.getRemoteObject("org.freedesktop.DBus", "/org/freedesktop/DBus");
            java.util.Vector<String> list = new java.util.Vector<String>();
            for (String s : dbus.ListNames()) {
                list.add(s);
            }
            if (list.contains("org.gnome.Rhythmbox")) {
                Player player = (Player) conn.getRemoteObject("org.gnome.Rhythmbox", "/org/gnome/Rhythmbox/Player");
                Shell shell = (Shell) conn.getRemoteObject("org.gnome.Rhythmbox", "/org/gnome/Rhythmbox/Shell");
                if (player.getPlayingUri().length() > 0) {
                    java.util.Map<String, org.freedesktop.dbus.Variant> maps = shell.getSongProperties(player.getPlayingUri());
//                    for (String s : maps.keySet()){
//                        System.out.println(s);
//                    }
                    retValue = maps.get(name).getValue().toString();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Rhythmbox.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retValue;
    }

    public void disconnect() {
        conn.disconnect();
        conn=null;
    }
}
