/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JProgressBar;

/**
 *
 * @author patrick
 */
public class SystemMonitor implements Runnable{

    private boolean stopMe = false;
    private JProgressBar bar = null;
    private double cpuperc = 0;
    public SystemMonitor(JProgressBar b) {
        bar =b;
        new Thread(this).start();
    }

    public double getCPUUsage() {
        return cpuperc;
    }

    public void stopMe(){
        stopMe=true;
    }
    public void run() {
        java.lang.management.OperatingSystemMXBean bean = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        while (!stopMe) {
            try {
                bar.setValue((int) (bean.getSystemLoadAverage()*100));
                //System.out.println(bean.getSystemLoadAverage());
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SystemMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
