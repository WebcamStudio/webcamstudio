/**
 *  WebcamStudio for GNU/Linux
 *  Copyright (C) 2008  Patrick Balleux
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * 
 */
package webcamstudio.sources;

import java.awt.Image;
import webcamstudio.*;
import java.util.jar.*;

public class Animator  {

    public Animator(InfoListener list) {
        listener = list;

    }

    public void setLoop(int nb) {
        loadedNbLoop = nb;
        nbLoop = loadedNbLoop;
    }

    public static Image getThumbnail(final java.io.File animationFile) throws Exception {

        java.util.jar.JarFile jarFile = new java.util.jar.JarFile(animationFile);
        java.awt.image.BufferedImage img = null;
        java.util.Enumeration<java.util.jar.JarEntry> list = jarFile.entries();
        String nameList = "";
        while (list.hasMoreElements()) {
            JarEntry entry = list.nextElement();
            if (entry.getName().toLowerCase().endsWith(".png")) {
                img = javax.imageio.ImageIO.read(jarFile.getInputStream(jarFile.getEntry(entry.getName())));
            }
        }
        jarFile.close();
        return img.getScaledInstance(128, 128, Image.SCALE_FAST);
    }

    public void loadAnimation(final java.io.File animationFile) {

        try {
            java.util.jar.JarFile jarFile = new java.util.jar.JarFile(animationFile);
            java.awt.image.BufferedImage tempimage = null;
            java.awt.image.BufferedImage img = null;
            java.util.Enumeration<java.util.jar.JarEntry> list = jarFile.entries();
            String nameList = "";
            while (list.hasMoreElements()) {
                JarEntry entry = list.nextElement();
                if (entry.getName().toLowerCase().endsWith(".png")) {
                    tempimage = javax.imageio.ImageIO.read(jarFile.getInputStream(jarFile.getEntry(entry.getName())));
                    img = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(tempimage.getWidth(), tempimage.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    width = img.getWidth();
                    height = img.getHeight();
                    java.awt.Graphics2D g = img.createGraphics();
                    g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 1));
                    g.setClip(0, 0, tempimage.getWidth(), tempimage.getHeight());
                    g.drawImage(tempimage, 0, 0, null);
                    //Store this image to be found by their name...  To support playlist...
                    nameList += entry.getName().toLowerCase().trim() + ",";
                    mapSprites.put(entry.getName().toLowerCase().trim(), img);
                    g.dispose();
                }
            }
            playlist.add(nameList);
            String speed = jarFile.getManifest().getMainAttributes().getValue("Speed");
            timelapse = new Integer(speed);
            creator = jarFile.getManifest().getMainAttributes().getValue("Created-By");
            comment = jarFile.getManifest().getMainAttributes().getValue("Comment");
            if (jarFile.getManifest().getMainAttributes().getValue("UseAudio") != null) {
                useAudioLevel = jarFile.getManifest().getMainAttributes().getValue("UseAudio").toString().equals("TRUE");
            }

            if (jarFile.getManifest().getMainAttributes().containsKey(new java.util.jar.Attributes.Name("Loop"))) {
                loadedNbLoop = new Integer(jarFile.getManifest().getMainAttributes().getValue("Loop"));
            } else {
                loadedNbLoop = -1;
            }
            //Loading custom playlist...
            int index = 1;
            while (jarFile.getManifest().getMainAttributes().containsKey(new java.util.jar.Attributes.Name("Playlist" + index))) {
                playlist.add(jarFile.getManifest().getMainAttributes().getValue("Playlist" + index));
                index++;
            }
            if (playlist.size() > 1) {
                playRandom = true;
            }
            if (listener != null) {
                listener.info("Animation " + animationFile.getName() + " loaded.  Created by " + creator + " (" + comment + ")");
            }
            jarFile.close();
            ready = true;
        } catch (Exception e) {
            if (listener != null) {
                listener.info("Animation " + animationFile.getName() + " Error:  " + e.getMessage());
            }
            stopMe = true;
        }
    }

   
    public boolean isStopped() {
        return stopped;
    }

    public java.awt.image.BufferedImage getCurrentImage() {
        String itemName = "";
        if (playlist.size() > 0 && ready) {

            //Play the chosen list...
            currentIndex++;
            if (list == null || currentIndex >= list.length) {
                currentIndex = 0;
                if (playRandom) {
                    // When random mode, do not play the first playlist as it will have everything in it...
                    if (useAudioLevel) {
                        int r = ((playlist.size() - 1) * audioLevel / 128) + 1;
                        list = playlist.get(r).split(",");
                    } else {
                        int r = new java.util.Random().nextInt(playlist.size() - 1) + 1;
                        list = playlist.get(r).split(",");
                    }
                } else {
                    list = playlist.get(0).split(",");
                }
            }
            itemName = list[currentIndex].trim();
            if (itemName.toLowerCase().endsWith(".png")) {
                currentImage = mapSprites.get(itemName);
                if (currentImage == null) {
                    System.out.println("Image is null: " + itemName);
                }

            }
        }
        return currentImage;
    }

    public void setSpeed(int milli) {
        if (milli == 0) {
            milli = 100;
        }
        timelapse = milli;
    }

    public int getTimeLapse() {
        return timelapse;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setAudioLevel(int l) {
        audioLevel = l;
    }
    private java.util.TreeMap<String, java.awt.image.BufferedImage> mapSprites = new java.util.TreeMap<String, java.awt.image.BufferedImage>();
    private java.util.Vector<String> playlist = new java.util.Vector<String>();
    private java.awt.image.BufferedImage currentImage = null;
    private boolean stopMe = false;
    private int timelapse = 1000;
    private String comment = "";
    private String creator = "";
    private boolean playRandom = false;
    private boolean ready = false;
    private InfoListener listener = null;
    private int nbLoop = -1;
    private int loadedNbLoop = -1;
    private boolean stopped = false;
    private int width = 320;
    private int height = 240;
    private int audioLevel = 0;
    private boolean useAudioLevel = false;
    private int currentIndex = 0;
    private String[] list = null;
}
