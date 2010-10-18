/*
 * Program ...... SinglePaint
 * File ......... Settings.java
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

package sp;

public class Settings {
	private Settings() {

	}

	public static String postURL = null;

	public static final String PARAM_RELATIVE_POST_URL = "relativePathToPostURL";

	/**
	 * The name of the variable that tells the POST-script what is the image.
	 */
	public static final String POST_VARIABLE_IMAGE = "image";

	/**
	 * The minimum size of the paint tool.
	 */
	public static final int MIN_TOOL_SIZE = 1;

	/**
	 * The maximum size of the paint tool.
	 */
	public static final int MAX_TOOL_SIZE = 32;

	public static final int GUI_PA_WIDTH = 320;

	public static final int GUI_PA_HEIGHT = 240;

	public static final int GUI_SB_WIDTH = 100;

}
