/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webcamstudio.components;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author patrick
 */
public class PreciseTimer {

    static public void sleep(long startingTimestamp,long millis){
        long endingTime = startingTimestamp + millis;
        while (System.currentTimeMillis() <= endingTime){
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(PreciseTimer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
