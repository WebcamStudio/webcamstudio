/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JProgressBar;

/**
 *
 * @author patrick
 */
public class SystemMonitor extends TimerTask {

    private boolean stopMe = false;
    private JProgressBar bar = null;
    private double cpuperc = 0;
    private Timer timer = null;
    private java.lang.management.OperatingSystemMXBean bean = java.lang.management.ManagementFactory.getOperatingSystemMXBean();

    public SystemMonitor(JProgressBar b) {
        bar = b;
        timer = new Timer(true);
        timer.scheduleAtFixedRate(this, 0, 1000);
    }

    public double getCPUUsage() {
        return cpuperc;
    }

    public void stopMe() {
        timer.cancel();
    }

    public void run() {
        
        bar.setValue((int) (bean.getSystemLoadAverage() * 100));
    }
}
