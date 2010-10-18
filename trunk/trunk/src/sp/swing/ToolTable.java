/*
 * Program ...... SinglePaint
 * File ......... ToolTable.java
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

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import sp.Mediator;
import sp.icons.Icons;

public class ToolTable extends ButtonTable {
	private JLabel toolLabel;

	public static final long serialVersionUID = -123861823213L;

	private static final int buttonsPerRow = 4;

	private static final int nButtons = 8;

	private static final Dimension buttonDim = new Dimension(25, 25);

	public static final byte TOOL_SKETCH = 0;

	public static final byte TOOL_LINE = 1;

	public static final byte TOOL_KALLIGRAPH = 2;

	public static final byte TOOL_FLOOD_FILL = 3;

	public static final byte TOOL_RECT_FRAME = 4;

	public static final byte TOOL_RECT_FILLED = 5;

	public static final byte TOOL_OVAL_FRAME = 6;

	public static final byte TOOL_OVAL_FILLED = 7;

	private static final String[] toolTipTexts = { "Sketch", "Line",
			"Kalligraph", "Flood Fill", "Rectangle Frame", "Filled Rectangle",
			"Oval Frame", "Filled Oval" };

	private static final ImageIcon[] buttonIcons = { Icons.BUTTON_SKETCH,
			Icons.BUTTON_LINE, Icons.BUTTON_KALLIGRAPH,
			Icons.BUTTON_FLOOD_FILL, Icons.BUTTON_RECT_FRAME,
			Icons.BUTTON_RECT_FILLED, Icons.BUTTON_OVAL_FRAME,
			Icons.BUTTON_OVAL_FILLED };
	
	private static final ImageIcon[] labelIcons = { Icons.LABEL_SKETCH,
		Icons.LABEL_LINE, Icons.LABEL_KALLIGRAPH,
		Icons.LABEL_FLOOD_FILL, Icons.LABEL_RECT_FRAME,
		Icons.LABEL_RECT_FILLED, Icons.LABEL_OVAL_FRAME,
		Icons.LABEL_OVAL_FILLED };

	public ToolTable() {
		super(ToolTable.buttonsPerRow, ToolTable.nButtons, buttonDim);
		this.init();
	}

	private void init() {
		for (int i = 0; i < this.buttons.length; i++) {
			this.buttons[i].setToolTipText(ToolTable.toolTipTexts[i]);
			if (ToolTable.buttonIcons[i] != null) {
				this.buttons[i].setIcon(ToolTable.buttonIcons[i]);
			}
		}

		// create label, set it up
		this.toolLabel = new JLabel();
		this.toolLabel.setOpaque(true);
		this.toolLabel.setBorder(new LineBorder(Color.BLACK, 2));
		Dimension size = new Dimension(50, 50);
		this.toolLabel.setMinimumSize(size);
		this.toolLabel.setMaximumSize(size);
		this.toolLabel.setPreferredSize(size);
		this.toolLabel.setToolTipText("Current Tool");
		if (ToolTable.labelIcons[0] != null) {
			this.toolLabel.setIcon(ToolTable.labelIcons[0]);
		}
	}

	public JLabel getToolLabel() {
		return this.toolLabel;
	}

	public void actionPerformed(ActionEvent e) {
		byte tool = Byte.parseByte(e.getActionCommand());
		Mediator.singleton.getPainter().setTool(tool);
		if (ToolTable.labelIcons[tool] != null) {
			this.toolLabel.setIcon(ToolTable.labelIcons[tool]);
		}
	}

}
