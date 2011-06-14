/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.layout.Layout;

/**
 *
 * @author patrick
 */
public class LayoutEventsManager {

    private Collection<Layout> layouts = null;
    private boolean stopMe = false;

    public LayoutEventsManager(Collection<Layout> ls) {
        layouts = ls;
        new Thread(new Runnable() {

            @Override
            public void run() {
                monitor();
            }
        }).start();
    }

    public void stop() {
        stopMe = true;
    }

    private void monitor() {
        Layout currentLayout = null;
        while (!stopMe) {
            for (Layout l : layouts) {
                if (l.isActive()) {
                    currentLayout = l;
                    break;
                }
            }
            if (currentLayout != null) {
                if (currentLayout.getDuration() > 0) {
                    if (currentLayout.timeStamp == 0) {
                        currentLayout.timeStamp = (currentLayout.getDuration() * 1000) + System.currentTimeMillis();
                    } else if (currentLayout.timeStamp <= System.currentTimeMillis()) {
                        currentLayout.timeStamp = 0;
                        for (Layout l : layouts) {
                            if (l.toString().equals(currentLayout.getNextLayout())) {
                                currentLayout = l;
                                currentLayout.enterLayout();
                                while (!currentLayout.isActive()) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(LayoutEventsManager.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LayoutEventsManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
