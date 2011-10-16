/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.util.AbstractMap;
import java.util.Timer;
import java.util.TimerTask;
import webcamstudio.layout.Layout;

/**
 *
 * @author patrick
 */
public class LayoutEventsManager extends TimerTask {

    private AbstractMap<String,Layout> layouts = null;
    private boolean stopMe = false;
    private Timer timer = null;

    public LayoutEventsManager(AbstractMap<String,Layout> ls) {
        layouts = ls;
        timer = new Timer(this.getClass().getSimpleName(), true);
        timer.scheduleAtFixedRate(this, 0, 1000);
    }

    public void stop() {
        timer.cancel();
        timer = null;
    }

    @Override
    public void run() {
        try {
            Layout currentLayout = null;
            for (Layout l : layouts.values()) {
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
                        if (currentLayout.getNextLayout().equals("PREVIOUS_LAYOUT")) {
                            System.out.println("Returning to previous layout");
                            currentLayout = Layout.previousActiveLayout;
                            currentLayout.enterLayout(false);
                        } else {
                            for (Layout l : layouts.values()) {
                                if (l.toString().equals(currentLayout.getNextLayout())) {
                                    currentLayout = l;
                                    currentLayout.enterLayout(false);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }
}
