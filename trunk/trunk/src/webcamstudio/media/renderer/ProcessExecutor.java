/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick
 */
public class ProcessExecutor {

    private Process process;
    private boolean processRunning = false;
    private String name = "";

    public ProcessExecutor(String name) {
        this.name = name;
    }

    public void execute(String[] params) throws IOException, InterruptedException {
        processRunning = true;
        process = Runtime.getRuntime().exec(params);
        int retValue = process.waitFor();
        if (retValue != 0) {
            try {
                byte[] buffer = new byte[1024];
                int count = 0;
                InputStream err = process.getErrorStream();

                try {
                    count = err.read(buffer);
                    if (count > 0) {
                        System.out.println(name + ": " + new String(buffer, 0, count));
                    }
                } catch (IOException ioe) {
                    processRunning = false;
                }
                Tools.sleep(10);

                err.close();

            } catch (IOException ex) {
                Logger.getLogger(ProcessExecutor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        process.destroy();
        process = null;
        System.out.println(name + " ended...");
        processRunning = false;
    }

    public void destroy() {
        if (process!=null){
            process.destroy();
        }
    }
}
