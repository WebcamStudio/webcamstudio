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
package webcamstudio.exporter.vloopback;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

/**
 *
 * @author pballeux
 */
public class VideoDevice {

    public VideoDevice(String name) {
        device = new java.io.File(name);
        loadInfo();
    }

    private void loadInfo() {
        openDevice();
        initDevice();
        closeDevice();
    }

    private void openDevice() {
        deviceFD = CLibrary.INSTANCE.open(device.getAbsolutePath(), 4002);
    }

    private void initDevice() {
        video_capability.ByReference vid_caps = new video_capability.ByReference();
        v4l2_capability.ByReference v4l2_caps = new v4l2_capability.ByReference();

        if (CLibrary.INSTANCE.ioctl(deviceFD, VIDIOC_QUERYCAP, v4l2_caps) == 0) {
            version = Version.V4L2;
            if ((v4l2_caps.capabilities & 3) == 3) {
                type = Type.InputOutput;
            } else if ((v4l2_caps.capabilities & 1) == 1) {
                type = Type.Output;
            } else if ((v4l2_caps.capabilities & 2) == 2) {
                type = Type.Input;
            } else {
                type = Type.Unknown;
            }
            name = new String(v4l2_caps.card).trim() +  " ("+device.getName()+")";
        } else if (CLibrary.INSTANCE.ioctl(deviceFD, VIDIOCGCAP, vid_caps) == 0) {
            version = Version.V4L;
            if ((vid_caps.type & 1) == 1) {
                type = Type.Output;
            } else {
                type = Type.Input;
            }

            name = new String(vid_caps.name);
            if (name.indexOf(0) >= 0) {
                name = name.substring(0, name.indexOf(0));
            }

        }
    }

    public Version getVersion() {
        return version;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public java.io.File getFile() {
        return device;
    }

    @Override
    public String toString() {
        return name;
    }

    public static VideoDevice[] getInputDevices() {
        VideoDevice[] d = new VideoDevice[0];
        VideoDevice[] dAll = new VideoDevice[0];
        java.util.Vector<VideoDevice> devices = new java.util.Vector<VideoDevice>();
        dAll = getDevices();
        for (int i = 0; i < dAll.length; i++) {
            if (dAll[i].getType() == Type.Input || dAll[i].getType() == Type.InputOutput) {
                devices.add(dAll[i]);
            }
        }
        d = new VideoDevice[devices.size()];
        for (int i = 0; i < d.length; i++) {
            d[i] = devices.get(i);
        }
        devices.clear();
        devices = null;
        return d;
    }

    public static VideoDevice[] getOutputDevices() {
        VideoDevice[] d = new VideoDevice[0];
        VideoDevice[] dAll = new VideoDevice[0];
        java.util.Vector<VideoDevice> devices = new java.util.Vector<VideoDevice>();
        dAll = getDevices();
        for (int i = 0; i < dAll.length; i++) {
            if (dAll[i].getType() == Type.Output) {
                devices.add(dAll[i]);
            }
        }
        d = new VideoDevice[devices.size()];
        for (int i = 0; i < d.length; i++) {
            d[i] = devices.get(i);
        }
        devices.clear();
        devices = null;
        return d;
    }

    public static VideoDevice[] getDevices() {
        VideoDevice[] d = new VideoDevice[0];
        java.io.File[] fs = new java.io.File("/dev/").listFiles();
        java.util.Vector<VideoDevice> devices = new java.util.Vector<VideoDevice>();
        if (fs != null) {
            for (int i = 0; i < fs.length; i++) {
                if (fs[i].getName().startsWith("video") && !fs[i].isDirectory()) {
                    devices.add(new VideoDevice(fs[i].getAbsolutePath()));
                }
            }
        }
        d = new VideoDevice[devices.size()];
        for (int i = 0; i < d.length; i++) {
            d[i] = devices.get(i);
        }
        devices.clear();
        devices = null;
        return d;
    }

    private void closeDevice() {
        CLibrary.INSTANCE.close(deviceFD);
    }

    public enum Type {

        Input,
        Output,
        InputOutput,
        Unknown
    }

    public enum Version {

        V4L,
        V4L2,
        Unknown
    }
    private Version version = Version.Unknown;
    private Type type = Type.Unknown;
    private String name = "";
    private java.io.File device = null;
    private int deviceFD = 0;
    final static int O_RDWR = 2;
    final static int VIDIOCGCAP = -2143521279;
    final static int VIDIOC_QUERYCAP = -2140645888;

    public interface CLibrary extends Library {

        CLibrary INSTANCE = (CLibrary) Native.loadLibrary((Platform.isWindows() ? "msvcrt" : "c"),
                CLibrary.class);

        void printf(String format, Object... args);

        int open(String device, int mode);

        void close(int device);

        int ioctl(int device, int command, Object struct);

        String strerr(int no);

        int write(int device, byte[] buffer, int count);
    }
}
