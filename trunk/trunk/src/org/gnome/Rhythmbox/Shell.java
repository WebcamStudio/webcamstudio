/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gnome.Rhythmbox;

/**
 *
 * @author patrick
 */
public interface Shell extends org.freedesktop.dbus.DBusInterface{
    public java.util.Map<String,org.freedesktop.dbus.Variant> getSongProperties( String uri );
}

