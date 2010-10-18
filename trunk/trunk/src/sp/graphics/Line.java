/*
 * Program ...... SinglePaint
 * File ......... Line.java
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

public class Line extends PaintJob {
	
	private Point start;
	private Point current;
	
	public Line(BufferedImage image, Graphics2D graphics) {
		super(image, graphics);
	}
	
	public void drawPressed(Point p) {
		this.start = p;
	}
	
	public void drawDragged(Point p) {
		this.current = p;
	}
	
	public void drawReleased(Point p) {	
		this.graphics.drawLine(this.start.x, this.start.y, p.x, p.y);
		//this.start = null;
		this.current = null;
	}
	
	public void preview(Graphics2D g) {
		if (this.start != null && this.current != null) {
			g.drawLine(this.start.x, this.start.y, this.current.x, this.current.y);
		}
	}
}
