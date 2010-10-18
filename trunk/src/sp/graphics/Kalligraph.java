/*
 * Program ...... SinglePaint
 * File ......... Kalligraph.java
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
import java.awt.Polygon;
import java.awt.image.BufferedImage;

public class Kalligraph extends PaintJob {

	// private static final float sing30Deg = 0.5f;

	// private static final float cos30Deg = 0.8660254f;

	private static final float sing15Deg = 0.25881905f;

	private static final float cos15Deg = 0.9659258f;

	private Point p;

	public Kalligraph(BufferedImage image, Graphics2D graphics) {
		super(image, graphics);
	}

	private void drawRect(int x1, int y1, int x2, int y2, int x3, int y3,
			int x4, int y4) {
		Polygon pol = new Polygon();
		// p bottom
		pol.addPoint(x1, y1);
		// p left
		pol.addPoint(x2, y2);
		// p top
		pol.addPoint(x3, y3);
		// p right
		pol.addPoint(x4, y4);
		this.graphics.fillPolygon(pol);
	}

	public void drawPressed(Point p) {
		int x = p.x;
		int y = p.y;
		float size = ((BasicStroke) this.graphics.getStroke()).getLineWidth();
		float size3 = 3 * size;

		float fdx = size3 * Kalligraph.cos15Deg; // size2 * cos(30)
		float fdy = size3 * Kalligraph.sing15Deg; // size2 * sin(30)
		float fddx = size * Kalligraph.sing15Deg; // size * sin(30)
		float fddy = size * Kalligraph.cos15Deg; // size * cos(30)

		int dx = (int) fdx;
		if (fdx - dx >= 0.5) {
			dx += 1;
		}
		int dy = (int) fdy;
		if (fdy - dy >= 0.5) {
			dy += 1;
		}
		int ddx = (int) fddx;
		if (fddx - ddx >= 0.5) {
			ddx += 1;
		}
		int ddy = (int) fddy;
		if (fddy - ddy >= 0.5) {
			ddy += 1;
		}
		int x1 = x + dx - ddx;
		int x2 = x - dx - ddx;
		int x3 = x - dx + ddx;
		int x4 = x + dx + ddx;

		int y1 = y - dy - ddy;
		int y2 = y + dy - ddy;
		int y3 = y + dy + ddy;
		int y4 = y - dy + ddy;

		this.drawRect(x1, y1, x2, y2, x3, y3, x4, y4);
		this.p = p;
	}

	public void drawDragged(Point p) {
		float size = ((BasicStroke) this.graphics.getStroke()).getLineWidth();
		float size3 = 3 * size;

		float fdx = size3 * Kalligraph.cos15Deg; // size3 * cos(30�)
		float fdy = size3 * Kalligraph.sing15Deg; // size3 * sin(30�)
		float fddx = size * Kalligraph.sing15Deg; // size * sin(30�)
		float fddy = size * Kalligraph.cos15Deg; // size * cos(30�)

		int pdx = (int) fdx;
		if (fdx - pdx >= 0.5) {
			pdx += 1;
		}
		int pdy = (int) fdy;
		if (fdy - pdy >= 0.5) {
			pdy += 1;
		}
		int pddx = (int) fddx;
		if (fddx - pddx >= 0.5) {
			pddx += 1;
		}
		int pddy = (int) fddy;
		if (fddy - pddy >= 0.5) {
			pddy += 1;
		}

		int pdx1 = pdx - pddx;
		int pdx2 = -pdx - pddx;
		int pdx3 = -pdx + pddx;
		int pdx4 = pdx + pddx;

		int pdy1 = -pdy - pddy;
		int pdy2 = pdy - pddy;
		int pdy3 = pdy + pddy;
		int pdy4 = -pdy + pddy;

		int p1x = this.p.x;
		int p1y = this.p.y;
		int p2x = p.x;
		int p2y = p.y;
		// in these cases we interchange the coordinates so that
		// the point (x1, y1) does always have the smaller x-coordinate
		if (p1x > p2x) {
			int temp = p1x;
			p1x = p2x;
			p2x = temp;
			temp = p1y;
			p1y = p2y;
			p2y = temp;
		}

		int dx = Math.abs(p2x - p1x);
		int dy = Math.abs(p2y - p1y);

		int x = p1x;
		int y = p1y;

		int D;

		/**
		 * COMMENTS DON'T MATCH ANYMORE
		 */
		if (dx >= dy) {
			// 0 < angle <= 45�
			if (p1y < p2y) {
				D = 2 * dy - dx;

				while (x <= p2x) {
					this.drawRect(x + pdx1, y + pdy1, x + pdx2, y + pdy2, x
							+ pdx3, y + pdy3, x + pdx4, y + pdy4);
					x++;
					if (D > 0) { // SE -> midpoint above line -> increase y
						y++;
						D += 2 * (dy - dx);
					} else { // E -> midpoint below line -> don't increase y
						D += 2 * dy;
					}
				}
			}
			// -45� <= angle <= 0
			else {
				D = 2 * dy - dx;

				while (x <= p2x) {
					this.drawRect(x + pdx1, y + pdy1, x + pdx2, y + pdy2, x
							+ pdx3, y + pdy3, x + pdx4, y + pdy4);
					x++;
					if (D > 0) { // NE -> midpoint above line -> decrease y
						y--;
						D += 2 * (dy - dx);
					} else { // E -> midpoint below line -> don't decrease y
						D += 2 * dy;
					}
				}
			}
		}
		// if angle with y-axis larger than 45 degrees
		else {
			// 45� < angle <= 90�
			if (p1y > p2y) { // CASE 3: if slope is positive

				D = 2 * dx - dy; // like CASE 2, if y-axis is handled as
				// x-axis

				y = p2y;
				x = p2x;
				while (y <= p1y) {
					this.drawRect(x + pdx1, y + pdy1, x + pdx2, y + pdy2, x
							+ pdx3, y + pdy3, x + pdx4, y + pdy4);
					y++;
					if (D > 0) {
						x--;
						D += 2 * (dx - dy);
					} else {
						D += 2 * dx;
					}
				}
			}
			// -90� <= angle < -45�
			else { // CASE 4: if slope is negative
				D = 2 * dx - dy; // like CASE 1, if y-axis is handled as
				// x-axis

				while (y <= p2y) {
					this.drawRect(x + pdx1, y + pdy1, x + pdx2, y + pdy2, x
							+ pdx3, y + pdy3, x + pdx4, y + pdy4);
					y++;
					if (D > 0) {
						x++;
						D += 2 * (dx - dy);
					} else {
						D += 2 * dx;
					}
				}
			}
		}
		this.p = p;
	}

	public void drawReleased(Point p) {
		this.drawDragged(p);
	}
}
