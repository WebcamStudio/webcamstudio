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

/*
 * A playlist is a sequence of videosource that will
 * play one after the other, at the current index set by
 * the user...
 * To make a playlist work, a text file must be created
 * Keywords are going to be parse to know what to do...
 * 
 * LOAD FILE, [KEY]
 * POSITION X,Y
 * SIZE WxH
 * LOOP [TIMES]
 * START AT HOUR[:MIN][:SEC]
 * START IN HOUR[:MIN][:SEC]
 * PLAY [FILE | KEY],[DURATION]
 * OPACITY PERCENTVALUE
 * ROTATION ANGLEVALUE
 * PAUSE X Sec
 */
package webcamstudio.sources;

import java.io.File;
import webcamstudio.*;
import java.util.Calendar;
import javax.swing.JPanel;

public class VideoSourcePlaylist extends VideoSource implements Runnable {

    public VideoSourcePlaylist() {
        controls.add(new webcamstudio.controls.ControlEffects(this));
        controls.add(new webcamstudio.controls.ControlShapes(this));

    }

    public VideoSourcePlaylist(java.io.File list, InfoListener l) {
        listener = l;
        location = list.getAbsolutePath();
        outputWidth = 320;
        outputHeight = 240;
        controls.add(new webcamstudio.controls.ControlEffects(this));
        controls.add(new webcamstudio.controls.ControlShapes(this));
    }

    private void loadList() {
        String temp = "";
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(new File(location).toURI().toURL().openStream()));
            temp = reader.readLine();
            while (temp != null) {
                playlist.add(temp);
                temp = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        loadList();
        System.out.println("Starting...");
        stopMe = false;
        while (!stopMe) {
            try {
                if (listener != null) {
                    listener.info("Playlist " + location + " is starting...");
                }

                for (int i = 0; i < playlist.size(); i++) {
                    String line = playlist.get(i);
                    parseLine(line);
                    //Check if we have to wait to continue the playlist...
                    while (startingTime > System.currentTimeMillis() && !stopMe) {
                        Thread.sleep(1000);
                    }
                    if (currentSource != null) {
                        while (currentSource.isPlaying() && !stopMe && (endingTimeOfCurrentSource == 0 || endingTimeOfCurrentSource > System.currentTimeMillis())) {
                            currentImage = currentSource.getImage();
                            Thread.sleep(1000 / 15);
                        }
                        endingTimeOfCurrentSource = 0;
                        currentSource.stopSource();
                        currentSource = null;
                    }
                    Thread.sleep(1);
                }

            } catch (Exception e) {
                if (listener != null) {
                    listener.error("Playlist Error: " + location + " : " + e.getMessage());
                }

            }
            if (doLoopPlaylist && loopingTime != -1) {
                loopingTime--;
            }
            if (!doLoopPlaylist || (doLoopPlaylist && loopingTime == 0)) {
                break;
            }
        }
        if (listener != null) {
            listener.info("Playlist " + location + " has stopped...");
        }
        currentImage = null;
        sources.clear();
        stopMe = false;
    }

    private void parseLine(String line) {
        System.out.println("Line " + line);
        String command = "";
        String parameter = "";
        String key = "";
        String[] array = null;

        if (line.indexOf(" ") != -1) {
            command = line.substring(0, line.indexOf(" ", 0)).toUpperCase().trim();
            parameter = line.substring(line.indexOf(" ", 0)).trim();
        } else {
            command = line.toUpperCase().trim();
            parameter = "";
        }
        if (command.equals("LOAD")) {
            array = parameter.split(",");
            if (array.length == 2) {
                key = array[1];
            } else {
                key = array[0];
            }
            if (!sources.containsKey(key)) {
                String suffixImg = "jpg,jpeg,png,gif";
                VideoSource s = null;
                if (suffixImg.indexOf(array[0].toLowerCase().substring(array[0].length() - 3)) >= 0) {
                    System.out.println("Loading " + array[0]);
                    s = new VideoSourceImage(new java.io.File(array[0]));
                } else {
                    //It's a movie...
                    if (array[0].toLowerCase().startsWith("http")) {
                        try {
                            s = new VideoSourceMovie(new java.net.URL(array[0]));
                        } catch (Exception e) {
                            System.out.println("Exception Loading Movie URL in Playlist : " + e.getMessage());
                        }
                    } else {
                        s = new VideoSourceMovie(new java.io.File(array[0]));
                    }

                }
                if (s != null) {
                    s.setLooping(false);
                    sources.put(key, s);
                    s.setCaptureWidth(outputWidth);
                    s.setCaptureHeight(outputHeight);
                    s.setOutputHeight(outputWidth);
                    s.setOutputWidth(outputHeight);
                }
            }
        } else if (command.equals("POSITION")) {
            array = parameter.split(",");
            if (array.length == 2) {
                showAtX = new Integer(array[0]).intValue();
                showAtY = new Integer(array[1]).intValue();
            }
        } else if (command.equals("SIZE")) {
            array = parameter.split("x");
            if (array.length == 2) {
                outputWidth = new Integer(array[0]).intValue();
                outputHeight = new Integer(array[1]).intValue();
            }
        } else if (command.equals("OPACITY")) {
            opacity = new Integer(parameter.trim()).intValue();
        } else if (command.equals("LOOP")) {
            if (!doLoopPlaylist) {
                doLoopPlaylist = true;
                if (parameter.length() > 0) {
                    loopingTime = new Integer(parameter).intValue();
                } else {
                    loopingTime = -1;
                }
            }
        } else if (command.equals("PAUSE")) {
            int sec = new Integer(parameter).intValue();
            long timestamp = System.currentTimeMillis() + (sec * 1000);
            while (System.currentTimeMillis() < timestamp && !stopMe) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        } else if (command.equals("START")) {
            //We can only initialize the start command once...
            if (startingTime == 0) {
                if (parameter.toUpperCase().startsWith("AT")) {
                    java.util.GregorianCalendar calendar = new java.util.GregorianCalendar();
                    array = parameter.toUpperCase().replace("AT", "").trim().split(":");
                    switch (array.length) {
                        case 1:
                            calendar.set(Calendar.HOUR_OF_DAY, new Integer(array[0]).intValue());
                            startingTime = calendar.getTimeInMillis();
                            break;
                        case 2:
                            calendar.set(Calendar.HOUR_OF_DAY, new Integer(array[0]).intValue());
                            calendar.set(Calendar.MINUTE, new Integer(array[1]).intValue());
                            startingTime = calendar.getTimeInMillis();
                            break;
                        case 3:
                            calendar.set(Calendar.HOUR_OF_DAY, new Integer(array[0]).intValue());
                            calendar.set(Calendar.MINUTE, new Integer(array[1]).intValue());
                            calendar.set(Calendar.SECOND, new Integer(array[2]).intValue());
                            startingTime = calendar.getTimeInMillis();
                            break;
                    }
                } else if (parameter.toUpperCase().startsWith("IN")) {
                    array = parameter.toUpperCase().replace("IN", "").trim().split(":");
                    switch (array.length) {
                        case 1:
                            startingTime = (new Integer(array[0]).intValue() * 60 * 60 * 1000) + System.currentTimeMillis();
                            break;
                        case 2:
                            startingTime = (new Integer(array[0]).intValue() * 60 * 60 * 1000) + System.currentTimeMillis();
                            startingTime += (new Integer(array[1]).intValue() * 60 * 1000);
                            break;
                        case 3:
                            startingTime = (new Integer(array[0]).intValue() * 60 * 60 * 1000) + System.currentTimeMillis();
                            startingTime += (new Integer(array[1]).intValue() * 60 * 1000);
                            startingTime += (new Integer(array[2]).intValue() * 1000);
                            break;
                    }
                }
            }
        } else if (command.equals("PLAY")) {
            if (startingTime == 0) {
                startingTime = System.currentTimeMillis();
            }
            array = parameter.split(",");
            if (array.length == 2) {
                key = array[0];
                endingTimeOfCurrentSource = System.currentTimeMillis() + (new Integer(array[1]).intValue() * 1000);
            } else {
                key = array[0];
                endingTimeOfCurrentSource = 0;
            }
            if (currentSource != null) {
                currentSource.stopSource();
                currentSource = null;
            }
            currentSource = sources.get(key);
            System.out.println("size: " + outputWidth + "x" + outputHeight);
            if (currentSource != null) {
                currentSource.setCaptureWidth(outputWidth);
                currentSource.setCaptureHeight(outputHeight);
                currentSource.setOutputHeight(outputWidth);
                currentSource.setOutputWidth(outputHeight);

                currentSource.setShowAtX(0);
                currentSource.setShowAtY(0);
                currentSource.setOpacity(opacity);
                currentSource.startSource();
                if (currentSource.isImage() && endingTimeOfCurrentSource == 0) {
                    endingTimeOfCurrentSource = System.currentTimeMillis() + 5000;
                }
                currentSource.play();
                while (!currentSource.isPlaying() && !stopMe) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                }
            }
        }

    }

    @Override
    public void setOutputWidth(int w) {
        outputWidth = w;
        if (currentSource != null) {
            currentSource.setOutputWidth(w);
        }
    }

    public void setOutputHeight(int h) {
        outputHeight = h;
        if (currentSource != null) {
            currentSource.setOutputHeight(h);
        }
    }

    public void play() {
        new Thread(this).start();
    }

    public void stop() {
        stopMe = true;
    }

    public void setLoop(boolean doLoop) {
        doLoopPlaylist = doLoop;
    }

    public java.awt.image.BufferedImage getImage() {
        if (currentImage != null) {
            captureHeight = currentImage.getHeight();
            captureWidth = currentImage.getWidth();
        }
        applyEffects(currentImage);
        return currentImage;
    }

    public String getName() {
        return location;
    }

    public String getString() {
        return location;
    }
    private java.util.Vector<String> playlist = new java.util.Vector<String>();
    private boolean doLoopPlaylist = false;
    private java.awt.image.BufferedImage currentImage = null;
    private java.util.TreeMap<String, VideoSource> sources = new java.util.TreeMap<String, VideoSource>();
    private VideoSource currentSource = null;
    private long startingTime = 0;
    private long endingTimeOfCurrentSource = 0;
    private int loopingTime = -1;

    @Override
    public void startSource() {
        try {
            play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopSource() {
        this.stop();
    }

    @Override
    public boolean canUpdateSource() {
        return false;
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isPlaying() {
        return !stopMe;

    }

    @Override
    public void pause() {
    }

}
