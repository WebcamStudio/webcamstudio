/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import webcamstudio.layout.Layout;

/**
 *
 * @author patrick
 */
public class LayoutEventsManager extends TimerTask {

    private Collection<Layout> layouts = null;
    private boolean stopMe = false;
    private Timer timer = null;

    public LayoutEventsManager(Collection<Layout> ls) {
        layouts = ls;
        timer = new Timer(true);
        timer.scheduleAtFixedRate(this, 0, 1000);
    }

    public void stop() {
        timer.cancel();
        timer=null;
    }


    @Override
    public void run() {
        Layout currentLayout = null;
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
                            break;
                        }
                    }
                }
            }
        }

    }
}
