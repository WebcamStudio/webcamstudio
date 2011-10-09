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
package webcamstudio.sources;

import java.io.File;
import webcamstudio.controls.ControlRescale;

/**
 *
 * @author pballeux
 */
public class VideoSourceV4L2 extends VideoSourceV4L {

    public VideoSourceV4L2() {
        outputWidth = 320;
        outputHeight = 240;
        frameRate = 15;
        captureWidth = 320;
        captureHeight = 240;
        doRescale = true;
        source = "v4l2src";
        controls.add(new ControlRescale(this));
        controls.add(new webcamstudio.controls.ControlShapes(this));
        controls.add(new webcamstudio.controls.ControlEffects(this));
        controls.add(new webcamstudio.controls.ControlGSTEffects(this));
        controls.add(new webcamstudio.controls.ControlActivity(this));
        controls.add(new webcamstudio.controls.ControlFaceDetection(this));

    }

    public VideoSourceV4L2(String deviceName, File fallbackDevice) {
        outputWidth = 320;
        outputHeight = 240;
        source = "v4l2src";
        location = getDeviceForName(deviceName, fallbackDevice).getAbsolutePath();
        name = deviceName;
        if (deviceName.length() == 0) {
            name = deviceName;
        }
        frameRate = 15;
        captureWidth = 320;
        captureHeight = 240;
        doRescale = true;
        if (controls.isEmpty()) {
            controls.add(new ControlRescale(this));
            controls.add(new webcamstudio.controls.ControlShapes(this));
            controls.add(new webcamstudio.controls.ControlEffects(this));
            controls.add(new webcamstudio.controls.ControlGSTEffects(this));
            controls.add(new webcamstudio.controls.ControlActivity(this));
            controls.add(new webcamstudio.controls.ControlFaceDetection(this));
        }

    }

    @Override
    public String toString() {
        return name + " (" + captureAtX + "," + captureAtY + ":" + captureWidth + "x" + captureHeight + ")";
    }
}
