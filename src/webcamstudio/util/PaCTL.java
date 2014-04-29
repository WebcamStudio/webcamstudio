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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author patrick
 */
public class PaCTL {
    
    
    public static String[] getSources() throws IOException {
        java.util.ArrayList<String> list = new java.util.ArrayList<>();
        System.out.println("Source Audio List:");
        Process p = Runtime.getRuntime().exec("pactl list sources");
        InputStream in = p.getInputStream();
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(isr);
        String line = reader.readLine();
        while (line != null){
            if (line.trim().toUpperCase().matches("^.* #[1234567890]$")){
                reader.readLine();
                line = reader.readLine();
                String l = line.trim().split(":")[1];
                l = l.replaceAll(" ", "");
                System.out.println(l);
                list.add(l);
            }
            line = reader.readLine();
        }
        in.close();
        isr.close();
        reader.close();
        p.destroy();
        
        return list.toArray(new String[list.size()]);
    }
    
    public static void main(String[] args){
        try {
            System.out.println(getSources().length);
        } catch (IOException ex) {
            Logger.getLogger(PaCTL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
