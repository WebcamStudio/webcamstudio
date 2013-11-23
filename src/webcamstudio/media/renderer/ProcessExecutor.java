/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media.renderer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.externals.ProcessRenderer;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class ProcessExecutor {

    private Process process;
    private boolean processRunning = false;
    private String name = "";
    private static String childPids = "";

    public ProcessExecutor(String gName) {
        this.name = gName;
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
                            System.out.println("Process Err: " + new String(buffer,0,count));
                        }
//                        count = in2.read(buffer);
//                        if (count > 0){
//                            System.out.println("FFMPEG Out: " + new String(buffer,0,count));
//                        }
                        Tools.sleep(100);
                        
                    } catch (IOException ex) {
                        Logger.getLogger(ProcessExecutor.class.getName()).log(Level.SEVERE, null, ex);
                        break;
                    }
                    
                }
                System.out.println("Process Closing streams");
                
            }
        }).start();
    }
    public void execute(String[] params) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        process = rt.exec(params);
        processRunning = true;
//      readOutput(process);      
    }
    public void executeString(String params) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        process = rt.exec(params);
        processRunning = true;
//      readOutput(process);
    }
    public void destroy() {
        processRunning=false;
        try {
            Tools.sleep(50);
            killUnixProcess(process);
        } catch (Exception ex) {
            Logger.getLogger(ProcessExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static int getUnixPID(Process process) throws Exception //Author Martijn Courteaux Code
{
    if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
        Class cl = process.getClass();
        Field field = cl.getDeclaredField("pid");
        field.setAccessible(true);
        Object pidObject = field.get(process);
        return (Integer) pidObject;
    } else {
        throw new IllegalArgumentException("Needs to be a UNIXProcess");
    }
}

public static void killUnixProcess(Process process) throws Exception //Modified from Martijn Courteaux Code
{
    int pid = getUnixPID(process);
    Runtime rt = Runtime.getRuntime();
    String commandPids = "ps -ef | awk '{if ($3 == "+pid+") print $2;}'";
    File fileP=new File(System.getProperty("user.home")+"/.webcamstudio/"+"WSPidsBuster.sh"); 
    FileOutputStream fosV;
    DataOutputStream dosV = null;
    try {
        fosV = new FileOutputStream(fileP);
        dosV= new DataOutputStream(fosV);
    } catch (FileNotFoundException ex) {
            Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
    }
    try {
                    dosV.writeBytes("#!/bin/bash\n");
                    dosV.writeBytes(commandPids+"\n");
    } catch (IOException ex) {
        Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
    }
    String batchPidCommand = "sh "+System.getProperty("user.home")+"/.webcamstudio/"+"WSPidsBuster.sh";
    try {
    Process getChildPids = rt.exec(batchPidCommand);
    Tools.sleep(10);
    getChildPids.waitFor(); //Author spoonybard896
    BufferedReader buf = new BufferedReader(new InputStreamReader(
    getChildPids.getInputStream()));
    String line = "";
    childPids = "";
    while ((line = buf.readLine()) != null) {
       childPids += line + "\n";
    } //Author spoonybard896
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
    }
    Runtime.getRuntime().exec("kill -9 " + pid).waitFor();
    Runtime.getRuntime().exec("kill -9 " + childPids).waitFor();
    childPids = null;
}
}