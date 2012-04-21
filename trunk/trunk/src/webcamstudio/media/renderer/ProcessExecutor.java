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

    private void readOutput(final Process p){
        new Thread(new Runnable() {

            @Override
            public void run() {
                InputStream in1 = p.getErrorStream();
                InputStream in2 = p.getInputStream();
                byte[] buffer = new byte[65536];
                int count = 0;
                while (count != -1 && processRunning){
                    try {
                        count = in1.read(buffer);
                        if (count > 0){
                            System.out.println("FFMPEG Err: " + new String(buffer,0,count));
                        }
                        count = in2.read(buffer);
                        if (count > 0){
                            System.out.println("FFMPEG Out: " + new String(buffer,0,count));
                        }
                        Tools.sleep(1);
                        
                    } catch (IOException ex) {
                        Logger.getLogger(ProcessExecutor.class.getName()).log(Level.SEVERE, null, ex);
                        break;
                    }
                    
                }
                System.out.println("FFMPEG Closing streams");
                
            }
        }).start();
    }
    public void execute(String[] params) throws IOException, InterruptedException {
        process = Runtime.getRuntime().exec(params);
        processRunning = true;
        //readOutput(process);
        
    }

    public void destroy() {
        processRunning=false;
        if (process!=null){
            process.destroy();
        }
    }
}
