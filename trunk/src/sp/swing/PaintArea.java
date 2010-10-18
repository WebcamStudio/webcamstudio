/*
 * Program ...... SinglePaint
 * File ......... PaintArea.java
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Date;

import javax.swing.JPanel;

import sp.Mediator;
import sp.Settings;

public class PaintArea extends JPanel implements MouseListener,
		MouseMotionListener {

	private boolean mousePressed = false;

	/**
	 * The last time the paint area was repainted.
	 */
	private long lastRepainted = 0;

	public static final long serialVersionUID = 289374823423488234L;

	public static final PaintArea singleton = new PaintArea();
        private java.awt.image.BufferedImage backgroundImg = null;

	private PaintArea() {
		this.init();
	}

	private void init() {
		this.setOpaque(true);
		this.setDoubleBuffered(true);
		this.setBackground(Color.WHITE);
		Dimension size = new Dimension(Settings.GUI_PA_WIDTH,
				Settings.GUI_PA_HEIGHT);
		this.setMinimumSize(size);
		this.setPreferredSize(size);
		this.setMaximumSize(size);
		this.setSize(size);

		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		Graphics2D g2D = (Graphics2D) Mediator.singleton.getPainter().getImage().getGraphics();

		g2D.addRenderingHints(new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF));
		g2D.setColor(Color.WHITE);
                g2D.setBackground(new Color(0,0,0,0));
		g2D.clearRect(0, 0, this.getWidth(), this.getHeight());
                
	}

        public void setBackgroundImage(java.awt.image.BufferedImage img){
            backgroundImg=img;
            repaint();
        }
	public void repaint() {
		// 1000 / 40 = 25 frames per second is fast enough repainting
		if (new Date().getTime() - this.lastRepainted > 40) {
			super.repaint();
			this.lastRepainted = new Date().getTime();
		}
	}

	public void forcedRepaint() {
		super.repaint();
		this.lastRepainted = new Date().getTime();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
                if (backgroundImg !=null){
                    g.drawImage(backgroundImg, 0, 0, getWidth(),getHeight(),0,0,backgroundImg.getWidth(),backgroundImg.getHeight(),null);
                }
		g.drawImage(Mediator.singleton.getPainter().getImage(), 0, 0, this);
		Mediator.singleton.getPainter().preview((Graphics2D) g);
	}

	public void paintImage(Image image) {
		this.paintComponent(image.getGraphics());
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
		this.requestFocusInWindow();
	}

	public void mouseExited(MouseEvent e) {
		this.getParent().requestFocusInWindow();
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) {
			return;
		}

		Mediator.singleton.getPainter().mousePressed(e.getPoint());
		this.mousePressed = true;
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) {
			return;
		}
		Mediator.singleton.getPainter().mouseReleased(e.getPoint());
		this.mousePressed = false;
	}

	public void mouseDragged(MouseEvent e) {
		if (this.mousePressed) {
			Mediator.singleton.getPainter().mouseDragged(e.getPoint());
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

}
