/*
 * Program ...... SinglePaint
 * File ......... SinglePaintFrame.java
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

package sp.swing;

//import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

//import sp.Settings;
import sp.icons.Icons;

public class SinglePaintFrame extends JFrame implements WindowListener {

	public static final long serialVersionUID = 761238213487235467L;

	public static final SinglePaintFrame singleton = new SinglePaintFrame();

	private SinglePaintFrame() {
		super("SinglePaint");
		this.init();
	}

	private void init() {
		this.addWindowListener(this);
		ContentPane.init(this.getContentPane());
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		/*Dimension size = new Dimension(Settings.GUI_PA_WIDTH
				+ Settings.GUI_SB_WIDTH, Settings.GUI_PA_HEIGHT + 4);
		this.setSize(size);*/
		this.pack();
		// frame.setResizable(false);
		
		if (Icons.LOGO != null) {
			this.setIconImage(Icons.LOGO.getImage());
		}
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		this.setVisible(false);
		this.dispose();
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
}
