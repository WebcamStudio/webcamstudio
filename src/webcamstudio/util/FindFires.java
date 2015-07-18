/*
 * Copyright (C) 2014 patrick
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package webcamstudio.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.externals.ProcessRenderer;

/**
 *
 * @author patrick
 */
public class FindFires {


    public static FireDevices[] getSources() throws IOException {
        ArrayList<FireDevices> list = new ArrayList<>();

        // To identify the Firewire devices on Linux we use the following process:
        // 1: list /sys/bus/firewire/devices/ to get the set of currently-known firewire devices (fw0, fw1, etc.)
        // 2: For each (d) in that list of devices, within the directory /sys/bus/firewire/devices/$d/ :
        //   2a: If is_local contains "1", we skip it.
        //   2b: Otherwise, read vendor_name and model_name (if available) to come up with a name for the device.
        //   2c: Also read guid to get an identifier for the device
        //   2d: Add the device to the result list. If we couldn't come up with a name for the device, use its device identifier (d) as the name.
        File devicesDir = new File("/sys/bus/firewire/devices/");
        File[] fwFiles = devicesDir.listFiles();
        for (File f : fwFiles)
        {
            String devName = f.getName();

            // Check "is_local", which distinguishes devices connected to the computer from the bus from the bus itself,
            // which for some reason has the same sort of device file.
            // If the "is_local" file isn't present, we leave "isLocal" false and the device will appear in the devices list.
            // /sys/bus/firewire/devices/fw*/is_local was added in Linux 3.6, in July. 2012
            boolean isLocal = false;
            File isLocalFile = new File(f, "is_local");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(isLocalFile), "US-ASCII"));
                String line = reader.readLine();

                if (line.equals("1"))
                {
                    // File "is local", meaning it refers to our firewire bus and not a device on the bus
                    isLocal = true;
                }
            }
            catch (Throwable t)
            {
                // Something went wrong, but we don't really care what. In this case we just assume it's not a local device unless it's "fw0"
                if (devName.equals("fw0"))
                {
                    isLocal = true;
                }
            }

            // Check vendor name
            String vendorName = null;
            File vendorNameFile = new File(f, "vendor_name");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(vendorNameFile), "US-ASCII"));
                vendorName = reader.readLine();
            }
            catch (Throwable t)
            {
                // Couldn't read it, so just leave it null
            }

            // Check model name
            String modelName = null;
            File modelNameFile = new File(f, "model_name");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(modelNameFile), "US-ASCII"));
                modelName = reader.readLine();
            }
            catch (Throwable t)
            {
                // Couldn't read it, so just leave it null
            }

            // Check device GUID
            String guid = null;
            File guidFile = new File(f, "guid");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(guidFile), "US-ASCII"));
                guid = reader.readLine();
            }
            catch (Throwable t)
            {
                // Couldn't read it, so just leave it null
            }

            // Now we need to form a device name for the UI. This is what will appear in the drop-down list for the user to choose a device.
            // If we have a vendor name and/or model name, we'll use those.
            // Otherwise, we'll use the device name
            StringBuilder devNameBuilder = new StringBuilder();
            if (vendorName != null)
            {
                devNameBuilder.append(vendorName);
            }

            if (modelName != null)
            {
                if (devNameBuilder.length() > 0)
                {
                    devNameBuilder.append(" ");
                }
                devNameBuilder.append(modelName);
            }

            // Append the device path, in case it is either all we have, or somehow helps the user distinguish between two otherwise identical devices.
            if (devNameBuilder.length() > 0)
            {
                devNameBuilder.append(" ");
            }
            devNameBuilder.append("(/dev/");
            devNameBuilder.append(devName);
            devNameBuilder.append(")");

            if (!isLocal && (guid != null))
            {
                FireDevices fw = new FireDevices();
                fw.description = devNameBuilder.toString();
                fw.guid = guid;

                list.add(fw);
            }
        }

        return list.toArray(new FireDevices[list.size()]);
    }

    public static void main(String[] args) {
        try {
            System.out.println(getSources().length);
        } catch (IOException ex) {
            Logger.getLogger(PaCTL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
