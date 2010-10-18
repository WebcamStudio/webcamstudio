/**
 *  WebcamStudio for GNU/Linux
 *  Copyright (C) 2008  Patrick Balleux
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * 
 */
package webcamstudio.exporter.vloopback;

/**
 *
 * @author pballeux
 */
public class video_clip extends com.sun.jna.Structure {

    public static class ByValue extends video_clip implements com.sun.jna.Structure.ByValue {
    }
    public static class ByReference extends video_clip implements com.sun.jna.Structure.ByReference {
    }
    public int x,y,width,height;
    public com.sun.jna.Pointer next;
}
