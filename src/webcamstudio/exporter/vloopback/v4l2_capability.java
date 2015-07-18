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

import java.util.Arrays;
import java.util.List;

/**
 *
 * 	__u8	driver[16];	
	__u8	card[32];	
	__u8	bus_info[32];	
	__u32   version;        
	__u32	capabilities;	
	__u32	reserved[4];

 * @author pballeux
 */
public class v4l2_capability extends com.sun.jna.Structure {

    public byte[] driver = new byte[16];
    public byte[] card = new byte[32];
    public byte[] bus_info = new byte[32];
    public int version;
    public int capabilities;
    public int[] reserved = new int[4];

    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[] { 
            "bus_info", "capabilities", "card", "driver", "reserved", "version"
        });
    }

    public static class ByValue extends v4l2_capability implements com.sun.jna.Structure.ByValue {
    }

    public static class ByReference extends v4l2_capability implements com.sun.jna.Structure.ByReference {
    }

}
