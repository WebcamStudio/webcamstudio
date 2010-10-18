/*
 * Program ...... SinglePaint
 * File ......... FloodFill.java
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
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

//import java.util.Date;

import sp.swing.PaintArea;
import sp.util.RingListBuffer;

public class FloodFill extends PaintJob {

	public FloodFill(BufferedImage image, Graphics2D graphics) {
		super(image, graphics);
	}

	public void drawReleased(Point p) {
		int width = this.image.getWidth();
		int height = this.image.getHeight();
		
		//int width = this.image.getWidth(PaintArea.singleton);
		//int height = this.image.getHeight(PaintArea.singleton);

		// if the coordinates are out of bounds of the image
		if (p.x < 0 || p.x >= width || p.y < 0 || p.y >= height) {
			return;
		}
		
		int newColorValue = this.graphics.getColor().getRGB();
		
		//Date date = new Date();
		
		int[] pixel = new int[1];
		
		PixelGrabber pg_prev = new PixelGrabber(this.image, p.x, p.y, 1, 1,
				pixel, 0, width);
		
		try {
			if (!pg_prev.grabPixels()) {
				return;
			}
		} catch (InterruptedException e) {
			return;
		}
		
		/*System.out.println("Preview took "
				+ ((new Date().getTime() - date.getTime()) / 1000.0)
				+ " seconds");*/
		
		if (pixel[0] == newColorValue) {
			return;
		}
		
		
				
		//date = new Date();
		
		int[] pixels = new int[width * height];

		PixelGrabber pg = new PixelGrabber(this.image, 0, 0, width, height,
				pixels, 0, width);
		
		try {
			if (!pg.grabPixels()) {
				return;
			}
		} catch (InterruptedException e) {
			return;
		}

		

		MemoryImageSource mis = new MemoryImageSource(width, height, pixels, 0,
				width);
		
		/*System.out.println("Grabber took "
				+ ((new Date().getTime() - date.getTime()) / 1000.0)
				+ " seconds");*/

		this.floodFill(pixels, width, height, p.x, p.y, newColorValue);

		this.graphics.drawImage(PaintArea.singleton.createImage(mis), 0, 0,
				PaintArea.singleton);
	}

	private void floodFill(int[] pixels, int width, int height, int x, int y,
			int newColor) {

		//Date date = new Date();

		int oldColor = pixels[x + y * width];
		/*if (oldColor == newColor) {
			return false;
		}*/

		int index = x + y * width;

		pixels[index] = newColor;

		RingListBuffer pixelBuffer = new RingListBuffer();

		byte flag = Pixel.ALL;

		if (x + 1 >= width) {
			flag |= Pixel.NO_EAST;
		}
		if (x - 1 < 0) {
			flag |= Pixel.NO_WEST;
		}
		if (y + 1 >= height) {
			flag |= Pixel.NO_SOUTH;
		}
		if (y - 1 < 0) {
			flag |= Pixel.NO_NORTH;
		}

		// pixel_coord = x + y * width

		Pixel p = new Pixel(x, y, flag);

		int px, py;

		while (p != null) {

			// EAST of the current pixel
			if ((p.flag & Pixel.NO_EAST) == 0) {
				px = p.x + 1;
				if (px < width) {
					py = p.y;
					index = px + py * width;
					if (pixels[index] == oldColor) {
						pixels[index] = newColor;
						pixelBuffer.put(new Pixel(px, py, Pixel.NO_WEST));
					}
				}
			}

			// WEST

			if ((p.flag & Pixel.NO_WEST) == 0) {
				px = p.x - 1;
				if (px >= 0) {
					py = p.y;
					index = px + py * width;
					if (pixels[index] == oldColor) {
						pixels[index] = newColor;
						pixelBuffer.put(new Pixel(px, py, Pixel.NO_EAST));
					}
				}
			}

			// SOUTH

			if ((p.flag & Pixel.NO_SOUTH) == 0) {
				py = p.y + 1;
				if (py < height) {
					px = p.x;
					index = px + py * width;
					if (pixels[index] == oldColor) {
						pixels[index] = newColor;
						pixelBuffer.put(new Pixel(px, py, Pixel.NO_NORTH));
					}
				}
			}

			// NORTH
			if ((p.flag & Pixel.NO_NORTH) == 0) {
				py = p.y - 1;
				if (py >= 0) {
					px = p.x;
					index = px + py * width;
					if (pixels[index] == oldColor) {
						pixels[index] = newColor;
						pixelBuffer.put(new Pixel(px, py, Pixel.NO_SOUTH));
					}
				}
			}

			p = (Pixel) pixelBuffer.get();
		}

		/*System.out.println("Fill took "
				+ ((new Date().getTime() - date.getTime()) / 1000.0)
				+ " seconds");*/
	}

	private class Pixel {

		public static final byte ALL = 0x00;
		public static final byte NO_SOUTH = 0x01;
		public static final byte NO_NORTH = 0x02;
		public static final byte NO_EAST = 0x04;
		public static final byte NO_WEST = 0x08;

		public int x, y;

		public byte flag;

		public Pixel(int x, int y, byte flag) {
			this.x = x;
			this.y = y;
			this.flag = flag;
		}
	}
}
