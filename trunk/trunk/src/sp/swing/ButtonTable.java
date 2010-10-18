/*
 * Program ...... SinglePaint
 * File ......... ButtonTable.java
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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * 
 * @author Harald Hetzner
 * 
 */
public abstract class ButtonTable extends JPanel implements ActionListener {

	protected JButton[] buttons;

	public ButtonTable(int buttonsPerRow, int nButtons, Dimension buttonDim) {
		this.init(buttonsPerRow, nButtons, buttonDim);
	}

	private void init(int buttonsPerRow, int nButtons, Dimension buttonDim) {
		this.setLayout(new GridBagLayout());

		this.buttons = new JButton[nButtons];

		GridBagConstraints cButton = new GridBagConstraints();
		cButton.fill = GridBagConstraints.NONE;

		int nRows = nButtons / buttonsPerRow;
		int xLastRow = nButtons - nRows * buttonsPerRow - 1;
		int buttonCount = 0;

		for (int y = 0; y < nRows; y++) {
			for (int x = 0; x < buttonsPerRow; x++) {
				// create the button
				this.buttons[buttonCount] = new JButton();
				// set it up
				this.buttons[buttonCount].setActionCommand(Integer
						.toString(buttonCount));
				this.buttons[buttonCount].addActionListener(this);
				this.buttons[buttonCount].setMinimumSize(buttonDim);
				this.buttons[buttonCount].setMaximumSize(buttonDim);
				this.buttons[buttonCount].setPreferredSize(buttonDim);
				// last button in row
				if (x == buttonsPerRow - 1 || (y == nRows - 1 && x == xLastRow)) {
					cButton.gridwidth = GridBagConstraints.REMAINDER;

				}
				// not last button in row
				else {
					cButton.gridwidth = 1; // GridBagConstraints.RELATIVE;
				}
                                buttons[buttonCount].setOpaque(true);
				// add it to this component
				this.add(this.buttons[buttonCount], cButton);
				buttonCount++;
				if (y == nRows - 1 && x == xLastRow) {
					break;
				}
                                
			}
		}
	}

	public abstract void actionPerformed(ActionEvent e);

}
