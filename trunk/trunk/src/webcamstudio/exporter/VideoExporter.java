/**
 *  WebcamStudio for GNU/Linux
 *  Copyright (C) 2008  Patrick Balleux
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * 
 */
package webcamstudio.exporter;

import webcamstudio.*;


/**
 *
 * @author pballeux
 */
public abstract class VideoExporter {
    protected boolean stopMe = false;
    protected String name = "Export";

    protected VideoExporter() {
    }

    public abstract void startExport();

    public String getName(){
        return name;
    }
    public void stopExport() {
        stopMe=true;
    }

    public String toString(){
        return name;
    }
    public void info(String info) {
        if (listener != null) {
            listener.info(info);
        }
    }

    public void error(String message) {
        if (listener != null) {
            listener.error(message);
        }
    }

    public void setListener(InfoListener l) {
        listener = l;
    }

    public void setQuality(int q) {
        quality = q;
    }

    public void setCaptureWidth(int w) {
        captureWidth = w;
    }

    public void setCaptureHeight(int h) {
        captureHeight = h;
    }

    public void setWidth(int w) {
        width = w;
    }

    public void setHeight(int h) {
        height = h;
    }

    public void setRate(int r) {
        rate = r;
    }

  

    public int getVideoBitrate() {
        return vbitrate;
    }

    public void setAudioBitrate(int r) {
        abitrate = r;
    }

    public void setVideoBitrate(int r) {
        vbitrate = r;
    }
    protected java.io.File output = null;
    protected int captureWidth = 320;
    protected int captureHeight = 240;
    protected int width = 320;
    protected int height = 240;
    protected int rate = 15;
    protected InfoListener listener = null;
    protected int quality = 85;
    protected boolean stopMixer = true;
    protected int[] data = null;
    protected byte[] bdata = null;
    protected int vbitrate = 150;
    protected int abitrate = 128000;
}
