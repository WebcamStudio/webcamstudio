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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gstreamer.*;

/**
 *
 * @author pballeux
 */
public class VideoExporterPipeline extends VideoExporter {

    private String gstPipeline = "videotestsrc ! video/x-raw-rgb,width=320,height=240 ! ffmpegcolorspace name=tosink";
    public VideoExporterPipeline(File pluginFile,String outputDevice) {
        if (pluginFile.exists()) {
            try {
                java.util.Properties plugin = new java.util.Properties();
                plugin.load(pluginFile.toURI().toURL().openStream());
                gstPipeline = plugin.getProperty("pipeline");
                if (plugin.getProperty("name")!=null){
                    name = plugin.getProperty("name");
                }
                gstPipeline = gstPipeline.replaceAll("FROMDEVICE", outputDevice);
                System.out.println("Plugin Loaded: " + gstPipeline);
            } catch (IOException ex) {
                Logger.getLogger(VideoExporterPipeline.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    @Override
    public void startExport() {
        pipe = org.gstreamer.Pipeline.launch(gstPipeline);
        pipe.getBus().connect(new Bus.ERROR() {

            @Override
            public void errorMessage(GstObject arg0, int arg1, String arg2) {
                error("OGG Export Error: " + arg0 + "," + arg1 + ", " + arg2);
                System.out.println("OGG Export Error: " + arg0 + "," + arg1 + ", " + arg2);
            }
        });
        pipe.getBus().connect(new Bus.INFO() {

            @Override
            public void infoMessage(GstObject arg0, int arg1, String arg2) {
                System.out.println("OGG Export Info: " + arg0 + "," + arg1 + ", " + arg2);
            }
        });

        pipe.play();
    }

}
