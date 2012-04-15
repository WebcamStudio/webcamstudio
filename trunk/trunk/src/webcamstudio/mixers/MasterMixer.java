/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.mixers;

import java.awt.image.BufferedImage;

/**
 *
 * @author patrick
 */
public class MasterMixer {

    static protected int frameRate = 15;
    static protected int width = 720;
    static protected int height = 480;
    
    static private MasterFrameBuilder builder = new MasterFrameBuilder();
    static Frame currentFrame = null;

    public static void setWidth(int w) {
        width = w;
    }

    public static void setHeight(int h) {
        height = h;
    }

    public static void setRate(int rate) {
        frameRate = rate;
    }

    public static int getRate() {
        return frameRate;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    static public void start() {
        new Thread(builder).start();
    }

    static public void stop() {
        builder.stop();
    }
    public static void setCurrentFrame(Frame f ){
        currentFrame=f;
    }
    public static void setCurrentFrame(BufferedImage img,byte[] audio){
        Frame f = new Frame("",img, audio);
        currentFrame=f;
    }
    public static Frame getCurrentFrame() {
        return currentFrame;
    }
}


