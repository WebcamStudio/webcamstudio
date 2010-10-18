/*
 * Program ...... SinglePaint
 * File ......... Paint.java
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

import java.awt.Color;

public class Paint {

	public static final char CHAR_BLACK = 'a';

	public static final char CHAR_BLUE = 'b';

	public static final char CHAR_BROWN = 'c';

	public static final char CHAR_CYAN = 'd';

	public static final char CHAR_DARK_GRAY = 'e';

	public static final char CHAR_DARK_GREEN = 'f';

	public static final char CHAR_GRAY = 'g';

	public static final char CHAR_GREEN = 'h';

	public static final char CHAR_LIGHT_GRAY = 'i';

	public static final char CHAR_MAGENTA = 'j';

	public static final char CHAR_ORANGE = 'k';

	public static final char CHAR_PINK = 'l';

	public static final char CHAR_PURPLE = 'm';

	public static final char CHAR_RED = 'n';

	public static final char CHAR_WHITE = 'o';

	public static final char CHAR_YELLOW = 'p';
	
	public static final char CHAR_FIRST = CHAR_BLACK;
	
	public static final char CHAR_LAST = CHAR_YELLOW;

	public static final Paint BLACK = new Paint(Color.BLACK, CHAR_BLACK);

	public static final Paint BLUE = new Paint(Color.BLUE, CHAR_BLUE);

	public static final Paint BROWN = new Paint(new Color(102, 51, 51),
			CHAR_BROWN);

	public static final Paint CYAN = new Paint(Color.CYAN, CHAR_CYAN);

	public static final Paint DARK_GRAY = new Paint(Color.DARK_GRAY,
			CHAR_DARK_GRAY);

	public static final Paint DARK_GREEN = new Paint(new Color(0, 102, 0),
			CHAR_DARK_GREEN);

	public static final Paint GRAY = new Paint(Color.GRAY, CHAR_GRAY);

	public static final Paint GREEN = new Paint(Color.GREEN, CHAR_GREEN);

	public static final Paint LIGHT_GRAY = new Paint(Color.LIGHT_GRAY,
			CHAR_LIGHT_GRAY);

	public static final Paint MAGENTA = new Paint(Color.MAGENTA, CHAR_MAGENTA);

	public static final Paint ORANGE = new Paint(Color.ORANGE, CHAR_ORANGE);

	public static final Paint PINK = new Paint(Color.PINK, CHAR_PINK);

	public static final Paint PURPLE = new Paint(new Color(102, 51, 153),
			CHAR_PURPLE);

	public static final Paint RED = new Paint(Color.RED, CHAR_RED);

	public static final Paint YELLOW = new Paint(Color.YELLOW, CHAR_YELLOW);

	public static final Paint WHITE = new Paint(Color.WHITE, CHAR_WHITE);

	public static final Paint[] PAINTS = { BLACK, BLUE, BROWN, CYAN, DARK_GRAY,
			DARK_GREEN, GRAY, GREEN, LIGHT_GRAY, MAGENTA, ORANGE, PINK, PURPLE,
			RED, WHITE, YELLOW };

	private Color color;

	private char type;

	private Paint(Color c, char type) {
		this.color = c;
		this.type = type;
	}

	public Color getColor() {
		return this.color;
	}

	public char getType() {
		return this.type;
	}

	public static Paint getPaint(char type) {
		if (type < CHAR_FIRST || type > CHAR_LAST) {
			return BLACK;
		}
		return PAINTS[type - CHAR_FIRST];
	}
}
