/*
 * Program ...... SinglePaint
 * File ......... IconLoader.java
 * Author ....... Harald Hetzner
 * 
 * Copyright (C) 2006  Harald Hetzner
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 * 
 * Harald Hetzner <singlepaint [at] mkultra [dot] dyndns [dot] org>
 */

package sp.icons;

import java.net.URL;
import javax.swing.ImageIcon;

/**
 * This class implements a mechanism for loading an <code>ImageIcon</code>
 * from an image-file with a known relative path.
 * 
 * @author Harald Hetzner
 * 
 */
public class IconLoader {

	/**
	 * Protected Constructor.
	 * 
	 */
	private IconLoader() {
	}

	/**
	 * Creates an <code>ImageIcon</code> from a GIF-, JPG- or PNG-file.
	 * Therefor the relative path from this class to the file has to be
	 * specified.
	 * 
	 * @param path
	 *            the path of the image-file relative to the location of this
	 *            class
	 * @param description
	 *            the description that should be displayed along with the icon
	 * @return an <code>ImageIcon</code> that shows the loaded image or
	 *         <code>null</code>, if the image-file was not found
	 */
	public static ImageIcon createImageIcon(String path, String description) {
		// get the URL for the path that is relative to the location of this
		// class
		URL imgURL = IconLoader.class.getResource(path);
		// if the URL could not be created, return null
		if (imgURL == null) {
			return null;
		}
		// create the icon and return it
		return new ImageIcon(imgURL, description);
	}
}
