/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gnome.Rhythmbox;

/**
 *
 * @author patrick
 */
public interface Properties extends org.freedesktop.DBus.Properties {
    public String get(String name);

}
