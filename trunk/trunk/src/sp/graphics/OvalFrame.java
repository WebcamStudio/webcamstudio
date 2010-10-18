/*
 * Program ...... SinglePaint
 * File ......... OvalFrame.java
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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class OvalFrame extends PaintJob {

	private Point start;
	private Point current;
	
	public OvalFrame(BufferedImage image, Graphics2D graphics) {
		super(image, graphics);
	}
	
	public void drawPressed(Point p) {
		this.start = p;
	}
	
	public void drawDragged(Point p) {
		this.current = p;
	}
	
	public void drawReleased(Point p) {
		int size = (int)((BasicStroke)this.graphics.getStroke()).getLineWidth();
		int x, y, width, height;
		width = Math.abs(p.x - this.start.x);
		height = Math.abs(p.y - this.start.y);
		x = Math.min(this.start.x, p.x);
		y = Math.min(this.start.y, p.y);
		int size2 = size + size;
		if (width <= size2 || height <= size2) {
			this.graphics.fillOval(x, y, width, height);
		}
		
		width -= size;
		height -= size;
		size /= 2;
		x += size;
		y += size;
		this.graphics.drawOval(x, y, width, height);
		//this.start = null;
		this.current = null;
	}
	
	public void preview(Graphics2D g) {
		if (this.start != null && this.current != null) {
			int size = (int)((BasicStroke)this.graphics.getStroke()).getLineWidth();
			int x, y, width, height;
			width = Math.abs(this.current.x - this.start.x);
			height = Math.abs(this.current.y - this.start.y);
			x = Math.min(this.start.x, this.current.x);
			y = Math.min(this.start.y, this.current.y);
			int size2 = size + size;
			if (width <= size2 || height <= size2) {
				g.fillOval(x, y, width, height);
			}
			width -= size;
			height -= size;
			size /= 2;
			x += size;
			y += size;
			g.drawOval(x, y, width, height);
		}
	}
}
