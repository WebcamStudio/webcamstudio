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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
        ArrayList<String> fwDevice = new ArrayList<>();
        ArrayList<String> fwGDevice = new ArrayList<>();
        ArrayList<String> fwName = new ArrayList<>();
        ArrayList<String> fwGuid = new ArrayList<>();
        System.out.println("FireWire Devices List:");
        String commandFireWire;
        Runtime rt = Runtime.getRuntime();
        commandFireWire = "grep . /sys/bus/firewire/devices/fw*/model_name & grep . /sys/bus/firewire/devices/fw*/guid";
        File fileD = new File(Tools.getUserHome()+"/.webcamstudio/"+"FWFind.sh");
        FileOutputStream fosD;
        DataOutputStream dosD = null;
        try {
            fosD = new FileOutputStream(fileD);
            dosD= new DataOutputStream(fosD);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            if (dosD != null) {
            dosD.writeBytes("#!/bin/bash\n");
            dosD.writeBytes(commandFireWire+"\n");
            dosD.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
        fileD.setExecutable(true);
        String fireWireComm = Tools.getUserHome()+"/.webcamstudio/"+"FWFind.sh";
        
        Process p = rt.exec(fireWireComm);

        Tools.sleep(10);
        try {
            p.waitFor(); //Author spoonybard896
        } catch (InterruptedException ex) {
            Logger.getLogger(FindFires.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedReader buf = new BufferedReader(new InputStreamReader(
                p.getInputStream()));
        String line = "";
        while ((line = buf.readLine()) != null){
            String l = line.trim();
            System.out.println(l);
            if (l.contains("model_name") && !l.contains(".") && !l.contains("fw0")){ //
                
                l = l.replace("/sys/bus/firewire/devices/", "");
                l = l.replace("/model_name", "");
                String[] lA = l.split(":");
                l = l.replaceAll(" ", "");
//                System.out.println(lA);
                fwDevice.add(lA[0]);
                fwName.add(lA[1]);
//                System.out.println("Name: "+lA[1]);
//                line = reader.readLine();
//                l = line.trim().split(":")[1];
                
                
            }
            if(l.contains("guid") && !l.contains(".") && !l.contains("fw0")){ // 
                l = l.replace("/sys/bus/firewire/devices/", "");
                l = l.replace("/guid", "");
                String[] lA = l.split(":");
                l = l.replaceAll(" ", "");
//                System.out.println(lA);
                fwGDevice.add(lA[0]);
                fwGuid.add(lA[1]);
//                System.out.println("Guid: "+lA[1]);
//                line = reader.readLine();
//                l = line.trim().split(":")[1];
            }
//            line = reader.readLine();
        }
        buf.close();
//        isr.close();
//        reader.close();
        p.destroy();
        
        for (int i=0 ; i< fwDevice.size() ; i++){
            FireDevices fw = new FireDevices();
            if (fwDevice.get(i).equals(fwGDevice.get(i))){
                fw.description = fwName.get(i);
                fw.guid = fwGuid.get(i);
//                System.out.println("Name: "+fwName.get(i));
//                System.out.println("Guid: "+fwGuid.get(i));
                list.add(fw);
            }
        }
//        list.add(s);
        return list.toArray(new FireDevices[list.size()]);
    }
    
    public static void main(String[] args){
        try {
            System.out.println(getSources().length);
        } catch (IOException ex) {
            Logger.getLogger(PaCTL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
