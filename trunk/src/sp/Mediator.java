/*
 * Program ...... SinglePaint
 * File ......... Mediator.java
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

import java.awt.image.BufferedImage;

import sp.client.Post;
import sp.graphics.Painter;

public class Mediator {

	/**
	 * The painter of the local client.
	 */
	private Painter painter;

	/**
	 * The only instance of the mediator.
	 */
	public static final Mediator singleton = new Mediator();

	/**
	 * Constructs the mediator.
	 * 
	 */
	private Mediator() {
		this.painter = new Painter();
	}

	public Painter getPainter() {
		return this.painter;
	}

	public void log(String message) {
		//ChatBar.singleton.log(message);
	}

	public void postImage() {
		BufferedImage image = this.painter.getImage();
		try {
			Post.postImage(image);
		} catch (Exception e) {
			System.err.println("Mediator post test: " + e.toString());
			return;
		}
		System.out.println("Posting done");
		this.painter.clear();
	}
        public void getImage(){
        }
}
