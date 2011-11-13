/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.media;

import java.awt.image.BufferedImage;

/**
 *
 * @author patrick
 */
public class Image  {
    private BufferedImage image = null;
    private long  timecode = 0;
    private int opacity;
    private int zorder;
    private int x = 0;
    private int y = 0;
    private int w = 320;
    private int h = 240;
    public Image(BufferedImage img, long timecode,int opacity,int zorder){
        image=img;
        this.timecode=timecode;
        this.opacity = opacity;
        this.zorder = zorder;

    }
    public int getWidth(){
        return w;
    }
    public int getHeight(){
        return h;
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public void setFormat(int x, int y,int w, int h){
        this.x=x;
        this.y=y;
        this.w=w;
        this.h=h;
    }
    public void setZOrder(int zorder){
        this.zorder=zorder;
    }
    public void setOpacity(int opacity){
        this.opacity=opacity;
    }
    public int getZOrder(){
        return zorder;
    }
    public int getOpacity(){
        return opacity;
    }
    public void updateImage(BufferedImage img){
        image=img;
    }
    public long getTimeCode(){
        return timecode;
    }
    public BufferedImage getImage(){
        return image;
    }
}
