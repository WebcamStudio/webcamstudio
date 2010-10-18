/*
 * Program ...... SinglePaint
 * File ......... Painter.java
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import sp.Settings;
import sp.swing.PaintArea;
import sp.swing.ToolTable;

public class Painter {

    /**
     * The tool this painter is currently using.
     */
    private byte tool = ToolTable.TOOL_SKETCH;
    /**
     * The size of this painter's tool.
     */
    private int toolSize = 5;
    protected BasicStroke stroke;
    /**
     * The current paint color of this painter.
     */
    protected Paint paint = Paint.BLACK;
    /**
     * The current paint job of this painter.
     */
    protected PaintJob paintJob;
    protected BufferedImage image = new BufferedImage(
            Settings.GUI_PA_WIDTH, Settings.GUI_PA_HEIGHT,
            BufferedImage.TYPE_INT_ARGB);
    private Graphics2D graphics;

    public BufferedImage getImage() {
        return this.image;
    }

    public Painter() {
        this.stroke = new BasicStroke(this.toolSize, BasicStroke.JOIN_ROUND,
                BasicStroke.CAP_ROUND);

        this.graphics = (Graphics2D) this.image.getGraphics();
        this.graphics.addRenderingHints(new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF));
        this.graphics.setColor(this.paint.getColor());
        this.graphics.setBackground(new Color(0, 0, 0, 0));
        this.graphics.clearRect(0, 0, this.image.getWidth(), this.image.getHeight());
        this.graphics.setStroke(this.stroke);
        this.paintJob = new Sketch(this.image, this.graphics);

    }

    public int getToolSize() {
        return this.toolSize;
    }

    public void setToolSize(int size) {
        this.toolSize = size;
        this.stroke = new BasicStroke(this.toolSize, this.stroke.getLineJoin(),
                this.stroke.getEndCap());
        this.graphics.setStroke(this.stroke);
    }

    public int getTool() {
        return this.tool;
    }

    public void setTool(byte tool) {
        this.tool = tool;

        switch (tool) {

            case ToolTable.TOOL_SKETCH:
                this.stroke = new BasicStroke(this.toolSize,
                        BasicStroke.JOIN_ROUND, BasicStroke.CAP_ROUND);
                this.paintJob = new Sketch(this.image, this.graphics);
                break;

            case ToolTable.TOOL_KALLIGRAPH:

                this.paintJob = new Kalligraph(this.image, this.graphics);
                break;

            case ToolTable.TOOL_LINE:
                this.stroke = new BasicStroke(this.toolSize,
                        BasicStroke.JOIN_BEVEL, BasicStroke.CAP_BUTT);
                this.paintJob = new Line(this.image, this.graphics);
                break;

            case ToolTable.TOOL_RECT_FRAME:
                this.stroke = new BasicStroke(this.toolSize,
                        BasicStroke.JOIN_MITER, BasicStroke.CAP_BUTT);
                this.paintJob = new RectFrame(this.image, this.graphics);

                break;

            case ToolTable.TOOL_RECT_FILLED:
                this.paintJob = new RectFilled(this.image, this.graphics);
                break;

            case ToolTable.TOOL_OVAL_FRAME:
                this.stroke = new BasicStroke(this.toolSize,
                        BasicStroke.JOIN_ROUND, BasicStroke.CAP_ROUND);
                this.paintJob = new OvalFrame(this.image, this.graphics);
                break;

            case ToolTable.TOOL_OVAL_FILLED:
                this.paintJob = new OvalFilled(this.image, this.graphics);
                break;

            case ToolTable.TOOL_FLOOD_FILL:
                this.paintJob = new FloodFill(this.image, this.graphics);
                break;

            default:
                this.stroke = new BasicStroke(this.toolSize,
                        BasicStroke.JOIN_ROUND, BasicStroke.CAP_ROUND);
                this.paintJob = new Sketch(this.image, this.graphics);
                break;
        }

        this.graphics.setStroke(this.stroke);
    }

    public Paint getPaint() {
        return this.paint;
    }

    public void setPaint(Paint p) {
        this.paint = p;
        this.graphics.setColor(p.getColor());
    }

    public void mousePressed(Point p) {
        this.paintJob.drawPressed(p);
        PaintArea.singleton.repaint();
    }

    public void mouseDragged(Point p) {
        this.paintJob.drawDragged(p);
        PaintArea.singleton.repaint();
    }

    public void mouseReleased(Point p) {
        this.paintJob.drawReleased(p);
        PaintArea.singleton.forcedRepaint();
    }

    public void preview(Graphics2D g) {
        g.setColor(this.paint.getColor());
        g.setStroke(this.stroke);
        this.paintJob.preview(g);
    }

    public void clear() {
        this.graphics.clearRect(0, 0, this.image.getWidth(), this.image.getHeight());
        this.graphics.setColor(this.paint.getColor());
        PaintArea.singleton.forcedRepaint();
    }
}
