/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gnome.Rhythmbox;

/**
 *
 * @author patrick
 */
public interface Player extends org.freedesktop.dbus.DBusInterface {
    public String getPlayingUri();
}
