/*
 * Program ...... SinglePaint
 * File ......... PaintJob.java
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

package sp.graphics;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public abstract class PaintJob {

	protected BufferedImage image;

	protected Graphics2D graphics;

	
	public PaintJob(BufferedImage image, Graphics2D graphics) {
		this.image = image;
		this.graphics = graphics;
	}

	public void drawPressed(Point p) {

	}

	public void drawDragged(Point p) {

	}

	public void drawReleased(Point p) {

	}

	public void preview(Graphics2D g) {

	}
	
	public String stringPressed(Point p) {
		return null;
	}
	
	public String stringDragged(Point p) {
		return null;
	}
	
	public String stringReleased(Point p) {
		return null;
	}
}
