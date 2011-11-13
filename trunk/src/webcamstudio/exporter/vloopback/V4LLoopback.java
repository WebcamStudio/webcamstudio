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

/*
 * Original code was from:
 * mjpegtools_yuv_to_v4l.c
 * Copyright (c) Jan Panteltje 2008-always
 * This software is distributed under the GNU public license version 2
 * See also the file 'COPYING'.
 *
 * See his site : http://panteltje.com/panteltje/mcamip/
 * 
 * Thanks for your work Jan!
 * 
 * Recoded in java by Patrick Balleux (2008)
 *
 * 
 */
package webcamstudio.exporter.vloopback;

import webcamstudio.*;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import java.awt.image.BufferedImage;
import webcamstudio.media.Image;

/**
 *
 * @author pballeux
 */
public class V4LLoopback extends VideoOutput {

    final static int VIDEO_PALETTE_YUV420P = 15;
    final static int VIDEO_PALETTE_RGB24 = 4;
    final static int VIDEO_PALETTE_RGB32 = 5;
    final static int VIDEO_PALETTE_UYVY = 9;
    final static int O_RDWR = 2;
    final static int VIDIOCGCAP = -2143521279;
    final static int VIDIOCGPICT = -2146535930;
    private static int VIDIOCSPICT = 1074689543;
    //private static int VIDIOCGWIN = -2144831991;        //64bits
    private static int VIDIOCGWIN = -2145356279;          //32bits
    //private static int VIDIOCSWIN = 1076393482;         //64bits
    private static int VIDIOCSWIN = 1075869194;           //32bits
    private int fmt = VIDEO_PALETTE_YUV420P;
    private byte[] buffer = null;

    public V4LLoopback(InfoListener l) {
        //We have to detect if we are in 32bits or 64bits
        listener = l;
        System.out.println("Pointer Size: " + com.sun.jna.Native.POINTER_SIZE);
        switch (com.sun.jna.Native.POINTER_SIZE) {
            case 4:
                VIDIOCGWIN = -2145356279;
                VIDIOCSWIN = 1075869194;
                break;
            case 8:
                VIDIOCGWIN = -2144831991;
                VIDIOCSWIN = 1076393482;
                break;
        }
    }

    @Override
    public void open(String path, int w, int h, int pixFormat) {
        this.w = w;
        this.h = h;
        this.pixFormat = pixFormat;
        switch (pixFormat) {
            case VideoOutput.RGB24:
                fmt = VIDEO_PALETTE_RGB24;
                break;
            case VideoOutput.UYVY:
                fmt = VIDEO_PALETTE_UYVY;
                break;
            default:
                fmt = VIDEO_PALETTE_RGB24;
                break;
        }
        devicePath = path;
        devFD = CLibrary.INSTANCE.open(devicePath, 4002);
        if (devFD <= 0) {
            if (listener != null) {
                listener.error("Unable to open device: " + devicePath);
            }

        } else {
            video_capability.ByReference vid_caps = new video_capability.ByReference();
            video_window.ByReference vid_win = new video_window.ByReference();
            video_picture.ByReference vid_pic = new video_picture.ByReference();

            if (CLibrary.INSTANCE.ioctl(devFD, VIDIOCGCAP, vid_caps) == -1) {
                listener.error("Unable to open device: " + devicePath);
            }
            if (CLibrary.INSTANCE.ioctl(devFD, VIDIOCGWIN, vid_win) == -1) {
                listener.error("Unable to open device: " + devicePath);
            }
            vid_win.x = 0;
            vid_win.y = 0;
            vid_win.flags = 0;
            vid_win.width = w;
            vid_win.height = h;
            if (CLibrary.INSTANCE.ioctl(devFD, VIDIOCSWIN, vid_win) == -1) {
                listener.error("Unable to open device: " + devicePath);
            }

            if (CLibrary.INSTANCE.ioctl(devFD, VIDIOCGPICT, vid_pic) == -1) {
                listener.error("Unable to open device: " + devicePath);
            }
            vid_pic.brightness = 0;
            vid_pic.color = 0;
            vid_pic.contrast = 0;
            vid_pic.depth = 24;
            vid_pic.hue = 0;
            vid_pic.palette = (short) fmt;
            vid_pic.whiteness = 0;

            if (CLibrary.INSTANCE.ioctl(devFD, VIDIOCSPICT, vid_pic) == -1) {
                listener.error("Unable to open device: " + devicePath);
            }

        }

    }

    @Override
    public void close() {
        if (devFD != 0) {
            if (listener != null) {
                listener.info("Closing device : " + devicePath);
            }

            int status = CLibrary.INSTANCE.close(devFD);
            if (status != 0) {
                System.out.println("Error closing device : " + devFD);
                if (listener != null) {
                    listener.error("Error closing device : " + devicePath);
                }
            }
            if (listener != null) {
                listener.info("Virtual Webcam Stopped");
            }
        }
    }
@Override
    public void newImage(Image image) {
        BufferedImage outputImage = image.getImage();
        int [] dataImageOutput = ((java.awt.image.DataBufferInt) outputImage.getRaster().getDataBuffer()).getData();
        write(dataImageOutput);
    }
    @Override
    public void write(int[] data) {
        if (devFD != 0) {
            switch (fmt) {
                case VIDEO_PALETTE_RGB24:
                    buffer = img2rgb24(data);
                    break;
                case VIDEO_PALETTE_UYVY:
                    buffer = img2uyvy(data);
                    break;
            }
            int countWritten = 0;
            countWritten = CLibrary.INSTANCE.write(devFD, buffer, buffer.length);
            if (countWritten != buffer.length) {
                if (listener != null) {
                    listener.error("WebcamStudio: Error while writing image...");
                }
            }
        }
    }

    public interface CLibrary extends Library {

        CLibrary INSTANCE = (CLibrary) Native.loadLibrary((Platform.isWindows() ? "msvcrt" : "c"),
                CLibrary.class);

        void printf(String format, Object... args);

        int open(String device, int mode);

        int close(int device);

        int ioctl(int device, int command, Object struct);

        String strerr(int no);

        int write(int device, byte[] buffer, int count);
    }
}
