/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.util.Date;
import javax.swing.JPanel;
import webcamstudio.*;
import webcamstudio.dbus.Rhythmbox;

/**
 *
 * @author pballeux
 */
public class VideoSourceText extends VideoSource {

    private Rhythmbox rhythmbox = null;

    protected VideoSourceText() {
        frameRate = 1;
    }

    public VideoSourceText(String txt) {
        frameRate = 1;
        location = "";
        name = "Text";
        captureWidth = 320;
        captureHeight = 240;
    }

    public void updateText(String txt) {
        customText = txt;
        loadText();
    }

    public void updateFile(java.io.File f) {
        try {
            customText = "";
            location = f.toURI().toURL().toString();
            loadText();
            name = f.getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public VideoSourceText(java.io.File loc) {
        frameRate = 1;
        try {
            location = loc.toURI().toURL().toString();
        } catch (Exception e) {
            error("Text Error  :" + e.getMessage());
        }
        name = loc.getName();

    }

    public VideoSourceText(java.net.URL loc) {
        frameRate = 1;
        location = loc.toString();
        name = location;
    }

    protected void loadText() {
        String data = "";
        if (location.length() > 0) {
            lines.clear();
            try {
                java.net.URL url = new java.net.URL(location);
                java.io.DataInput din = new java.io.DataInputStream(url.openStream());
                data = din.readLine();
                while (data != null) {
                    lines.add(data);
                    newTextLine(data);
                    data = din.readLine();
                }
                din = null;
                url = null;
            } catch (Exception e) {
                error("Text Error  :" + e.getMessage());
            }
        } else {
            String[] datas = customText.split("\n");
            lines.clear();
            for (int i = 0; i < datas.length; i++) {
                lines.add(datas[i]);
            }
        }
    }

    @Override
    public void startSource() {
        isPlaying = true;
        loadText();

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                if (rhythmbox!=null){
                    rhythmbox.disconnect();
                    rhythmbox=null;
                }
                rhythmbox=new Rhythmbox();
                long lastTimeStamp = System.currentTimeMillis();
                if (outputWidth == 0 || outputHeight == 0) {
                    outputWidth = 320;
                    outputHeight = 240;
                }
                int index = 0;
                int y = fontSize;
                int x = 0;
                int lineIndex = index;
                stopMe = false;
                String text = "";
                int interLine = 0;
                while (!stopMe) {
                    tempimage = graphicConfiguration.createCompatibleImage(captureWidth, captureHeight, java.awt.image.BufferedImage.TRANSLUCENT);
                    java.awt.Graphics2D buffer = tempimage.createGraphics();

                    if (updateTimeLaspe > 0 && System.currentTimeMillis() - lastTimeStamp > updateTimeLaspe) {
                        loadText();
                        lastTimeStamp = System.currentTimeMillis();
                    }
                    try {
                        buffer.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        buffer.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                        buffer.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                        buffer.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
                        buffer.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                        buffer.setFont(new java.awt.Font(fontName, java.awt.Font.PLAIN, fontSize));

                        buffer.setBackground(backgroundColor);
                        buffer.clearRect(0, 0, tempimage.getWidth(), tempimage.getHeight());
                        buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, backgroundOpacity));
                        buffer.setColor(backgroundColor);
                        buffer.fillRect(0, 0, tempimage.getWidth(), tempimage.getHeight());
                        buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 1F));
                        //draw Line

                        switch (scrollDirection) {
                            case SCROLL_NONE:
                                interLine = 0;
                                index = 0;
                                x = 0;
                                y = fontSize;
                                lineIndex = index;
                                while (y < (tempimage.getHeight() + fontSize) && lines.size() > 0 && lineIndex < lines.size()) {
                                    //buffer.setColor(backgroundColor);
                                    //buffer.fillRect(0, 0, tempimage.getWidth(), tempimage.getHeight());
                                    //buffer.drawString(lines.get(lineIndex), x + 1, y + 1);
                                    buffer.setColor(foregroundColor);
                                    buffer.drawString(translateTags(lines.get(lineIndex)), x, y);
                                    lineIndex++;
                                    y += fontSize;
                                }
                                break;
                            case SCROLL_BOTTOMTOTOP:
                                if (interLine <= 0) {
                                    interLine = fontSize;
                                    index += 3;
                                }
                                if (index >= lines.size()) {
                                    index = 0;
                                }
                                x = 0;
                                y = interLine -= 1;
                                lineIndex = index;
                                while (y < (tempimage.getHeight() + fontSize) && lines.size() > 0) {
                                    if (lineIndex >= lines.size()) {
                                        lineIndex = 0;
                                    }
                                    //buffer.setColor(backgroundColor);
                                    //buffer.fillRect(0, 0, tempimage.getWidth(), tempimage.getHeight());
                                    //buffer.drawString(lines.get(lineIndex), x + 1, y + 1);
                                    buffer.setColor(foregroundColor);
                                    buffer.drawString(translateTags(lines.get(lineIndex)), x, y);
                                    lineIndex++;
                                    y += fontSize;
                                }
                                break;
                            case SCROLL_TOPTOBOTTOM:
                                if (interLine >= fontSize) {
                                    interLine = 0;
                                    index -= 3;
                                }
                                if (index < 0) {
                                    index = lines.size() - 1;
                                }
                                x = 0;
                                y = interLine += 1;
                                lineIndex = index;
                                while (y < (tempimage.getHeight() + fontSize) && lines.size() > 0) {
                                    if (lineIndex >= lines.size()) {
                                        lineIndex = 0;
                                    }
                                    //buffer.setColor(backgroundColor);
                                    //buffer.fillRect(0, 0, tempimage.getWidth(), tempimage.getHeight());
                                    //buffer.drawString(lines.get(lineIndex), x + 1, y + 1);
                                    buffer.setColor(foregroundColor);
                                    buffer.drawString(translateTags(lines.get(lineIndex)), x, y);
                                    lineIndex++;
                                    y += fontSize;
                                }
                                break;
                            case SCROLL_LEFTTORIGHT:
                                if (x >= 0) {
                                    index -= 1;
                                    if (index < 0) {
                                        index = lines.size() - 1;
                                    }
                                    x = 0 - buffer.getFontMetrics().stringWidth(translateTags(lines.get(lineIndex)));
                                }
                                x += 3;
                                y = fontSize + 10;
                                lineIndex = index;
                                text = "";
                                while ((buffer.getFontMetrics().stringWidth(text) + x) <= tempimage.getWidth()) {
                                    text += translateTags(lines.get(lineIndex++));
                                    if (lineIndex >= lines.size()) {
                                        lineIndex = 0;
                                    }
                                }
                                //buffer.setColor(backgroundColor);
                                //buffer.fillRect(0, 0, tempimage.getWidth(), tempimage.getHeight());
                                //buffer.drawString(text, x + 1, y + 1);
                                buffer.setColor(foregroundColor);
                                buffer.drawString(text, x, y);
                                break;
                            case SCROLL_RIGHTTOLEFT:
                                if ((buffer.getFontMetrics().stringWidth(translateTags(lines.get(index))) + x) <= 0) {
                                    x = 0;
                                    index += 1;
                                }
                                if (index >= lines.size()) {
                                    index = 0;
                                }
                                x -= 3;
                                y = fontSize + 10;
                                lineIndex = index;
                                text = "";
                                while ((buffer.getFontMetrics().stringWidth(text) + x) <= tempimage.getWidth()) {
                                    text += translateTags(lines.get(lineIndex++));
                                    if (lineIndex >= lines.size()) {
                                        lineIndex = 0;
                                    }
                                }
                                //buffer.setColor(backgroundColor);
                                //buffer.fillRect(0, 0, tempimage.getWidth(), tempimage.getHeight());
                                //buffer.drawString(text, x + 1, y + 1);
                                buffer.setColor(foregroundColor);
                                buffer.drawString(text, x, y);
                                break;
                        }
                        buffer.dispose();
                        applyEffects(tempimage);
                        applyShape(tempimage);
                        image = tempimage;
                        Thread.sleep(1000 / frameRate);
                    } catch (Exception e) {
                        error("Text Error  :" + e.getMessage());
                    }
                }
                image = null;
            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    @Override
    public boolean canUpdateSource() {
        return true;
    }

    @Override
    public void stopSource() {
        if (rhythmbox != null) {
            rhythmbox.disconnect();
            rhythmbox = null;
        }
        isPlaying = false;
        stopMe = true;
        image = null;
    }

    @Override
    public boolean hasText() {
        return true;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void pause() {
        //
    }

    @Override
    public void play() {
        //
    }

    @Override
    public String toString() {
        return "Text: " + name;
    }

    @Override
    public java.util.Collection<JPanel> getControls() {
        java.util.Vector<JPanel> list = new java.util.Vector<JPanel>();
        list.add(new webcamstudio.controls.ControlPosition(this));
        list.add(new webcamstudio.controls.ControlText(this));
        list.add(new webcamstudio.controls.ControlEffects(this));
        list.add(new webcamstudio.controls.ControlShapes(this));
        list.add(new webcamstudio.controls.ControlLayout(this));
        list.add(new webcamstudio.controls.ControlReload(this));
        return list;
    }
    protected java.util.Vector<String> lines = new java.util.Vector<String>();
    protected VideoEffects objVideoEffects = new VideoEffects();

    private String translateTags(String text) {
        // Rhythmbox keys
//        album
//        album-folded
//        album-sort-key
//        album-sortname
//        album-sortname-folded
//        album-sortname-sort-key
//        artist
//        artist-folded
//        artist-sort-key
//        artist-sortname-folded
//        artist-sortname-sort-key
//        bitrate
//        copyright
//        date
//        description
//        disc-number
//        duration
//        entry-id
//        file-size
//        first-seen
//        first-seen-str
//        genre
//        genre-folded
//        genre-sort-key
//        hidden
//        image
//        keyword
//        lang
//        last-played
//        last-played-str
//        last-seen
//        last-seen-str
//        location
//        mb-albumartistid
//        mb-albumid
//        mb-artistid
//        mb-artistsortname
//        mb-trackid
//        mimetype
//        mountpoint
//        mtime
//        play-count
//        playback-error
//        post-time
//        rating
//        search-match
//        status
//        subtitle
//        summary
//        title
//        title-folded
//        title-sort-key
//        track-number
//        year
        String retValue = text + "";

        if (text.indexOf("#RHYTHMBOX:TITLE") != -1) {
            retValue = retValue.replaceFirst("#RHYTHMBOX:TITLE", rhythmbox.getCurrentSongTitle());
        }
        if (text.indexOf("#RHYTHMBOX:ALBUM") != -1) {
            retValue = retValue.replaceFirst("#RHYTHMBOX:ALBUM", rhythmbox.getCurrentSongProperties("album"));
        }
        if (text.indexOf("#RHYTHMBOX:ARTIST") != -1) {
            retValue = retValue.replaceFirst("#RHYTHMBOX:ARTIST", rhythmbox.getCurrentSongProperties("artist"));
        }
        if (text.indexOf("#RHYTHMBOX:DURATION") != -1) {
            retValue = retValue.replaceFirst("#RHYTHMBOX:DURATION", rhythmbox.getCurrentSongProperties("duration"));
        }
        if (text.indexOf("#RHYTHMBOX:GENRE") != -1) {
            retValue = retValue.replaceFirst("#RHYTHMBOX:GENRE", rhythmbox.getCurrentSongProperties("genre"));
        }
        if (text.indexOf("#RHYTHMBOX:YEAR") != -1) {
            retValue = retValue.replaceFirst("#RHYTHMBOX:YEAR", rhythmbox.getCurrentSongProperties("year"));
        }
        if (text.indexOf("#NOW") != -1) {
            retValue = retValue.replaceFirst("#NOW", new Date().toString());
        }

        return retValue;
    }
}
