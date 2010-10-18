/*
 * Program ...... SinglePaint
 * File ......... SideBar.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sp.Mediator;
import sp.Settings;

public class SideBar extends JPanel implements ActionListener, ChangeListener {

    public static final long serialVersionUID = 823462384L;
    private JSlider slider;
    private JTextField tfToolSize;
    private ToolTable toolTable;
    private ColorTable colorTable;
    private JButton buttonClear;
    public static final SideBar singleton = new SideBar();

    private SideBar() {
        this.init();
    }

    public void init() {
        this.slider = new JSlider(Settings.MIN_TOOL_SIZE,
                Settings.MAX_TOOL_SIZE, Mediator.singleton.getPainter().getToolSize());
        this.slider.addChangeListener(this);
        this.toolTable = new ToolTable();
        this.colorTable = new ColorTable();

//        int cols = 2;
//        this.tfToolSize = new JTextField(cols);
//        this.tfToolSize.setHorizontalAlignment(JTextField.RIGHT);
//        //this.tfToolSize.setEditable(false);
//        this.tfToolSize.setEnabled(false);
//        this.tfToolSize.setDisabledTextColor(Color.BLACK);
//        this.tfToolSize.setText(Integer.toString(Mediator.singleton.getPainter().getToolSize()));
//        this.tfToolSize.setMinimumSize(this.tfToolSize.getPreferredSize());
//        this.tfToolSize.setMaximumSize(this.tfToolSize.getPreferredSize());

        this.buttonClear = new JButton(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("CLEAR"));
        this.buttonClear.addActionListener(this);

        this.setLayout(new BorderLayout());
        slider.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("SIZE")));
        this.add(this.slider,BorderLayout.NORTH);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(this.toolTable,BorderLayout.NORTH);
        panel.add(this.colorTable,BorderLayout.CENTER);
        JPanel panelLabel = new JPanel();
        //panel.add(panelLabel,BorderLayout.SOUTH);
        panelLabel.setLayout(new BoxLayout(panelLabel,BoxLayout.X_AXIS));
        panelLabel.add(this.toolTable.getToolLabel());
        panelLabel.add(this.colorTable.getColorLabel());
        this.add(panel,BorderLayout.CENTER);
        this.add(this.buttonClear,BorderLayout.SOUTH);
        this.setMaximumSize(new java.awt.Dimension(100,200));
        this.setMinimumSize(new java.awt.Dimension(100,200));
        this.setPreferredSize(new java.awt.Dimension(100,200));

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.buttonClear)) {
            Mediator.singleton.getPainter().clear();
        }
    }

    public void stateChanged(ChangeEvent e) {
        int value = this.slider.getValue();
        Mediator.singleton.getPainter().setToolSize(value);
    }
}
