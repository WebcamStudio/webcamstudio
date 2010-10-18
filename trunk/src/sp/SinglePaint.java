/*
 * Program ...... SinglePaint
 * File ......... SinglePaint.java
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JApplet;
import javax.swing.JButton;

import sp.Settings;
import sp.icons.Icons;
import sp.swing.SinglePaintFrame;

public class SinglePaint extends JApplet implements ActionListener {

	public static final long serialVersionUID = 98234782374235100L;

	public void init() {
		String codeBase = this.getCodeBase().toString();
		String postPath = this.getParameter(Settings.PARAM_RELATIVE_POST_URL);
		if (postPath != null && !postPath.equals("")) {
			Settings.postURL = codeBase + postPath;
		}
		
		JButton button = new JButton();
		button.addActionListener(this);
		if (Icons.SINGLE_PAINT != null) {
			button.setIcon(Icons.SINGLE_PAINT);
		}
		this.getContentPane().add(button);
	}

	public void start() {
		SinglePaintFrame.singleton.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		SinglePaintFrame.singleton.setVisible(true);
	}

}
