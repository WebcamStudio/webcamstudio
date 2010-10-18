/*
 * Program ...... SinglePaint
 * File ......... ColorTable.java
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import sp.Mediator;
import sp.graphics.Paint;



public class ColorTable extends ButtonTable {

	public static final long serialVersionUID = 9823492349234234L;

	private static final int buttonsPerRow = 4;
	private static final int nButtons = Paint.PAINTS.length;
	private static final Dimension buttonDim = new Dimension(20, 20);
	
	private JLabel colorLabel;

	public ColorTable() {
		super(ColorTable.buttonsPerRow, ColorTable.nButtons, buttonDim);
		this.init();
	}

	private void init() {
		
		for (int i = 0; i < ColorTable.nButtons; i++) {
			this.buttons[i].setBackground(Paint.PAINTS[i].getColor());
		}
		
		// create label, set it up
		this.colorLabel = new JLabel();
		this.colorLabel.setOpaque(true);
		this.colorLabel.setBorder(new LineBorder(Color.BLACK, 2));
		this.colorLabel.setBackground(Mediator.singleton.getPainter().getPaint().getColor());
		Dimension size = new Dimension(32, 32);
		this.colorLabel.setMinimumSize(size);
		this.colorLabel.setMaximumSize(size);
		this.colorLabel.setPreferredSize(size);
		this.colorLabel.setToolTipText("Current Color");
	}

	public JLabel getColorLabel() {
		return this.colorLabel;
	}

	public void actionPerformed(ActionEvent e) {
		// find out which color the button is associated with
		int colorNum = Integer.parseInt(e.getActionCommand());
		// set the color as selected and set it on the label
		Mediator.singleton.getPainter().setPaint(Paint.PAINTS[colorNum]);
		this.colorLabel.setBackground(Paint.PAINTS[colorNum].getColor());
	}
}
