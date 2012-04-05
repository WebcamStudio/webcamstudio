/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import webcamstudio.*;

/**
 *
 * @author pballeux
 */
public class VideoSourceText extends VideoSource {

    protected VideoSourceText() {
        frameRate = 1;

    }

    public VideoSourceText(String txt) {
        frameRate = 1;

        name = "Text";
        location = name;
        captureWidth = 320;
        captureHeight = 240;
    }

    public void updateText(String txt) {
        customText = txt;
        loadText();
    }

    protected void loadText() {
        String data = "";
        String[] datas = customText.split("\n");
        lines.clear();
        for (int i = 0; i < datas.length; i++) {
            lines.add(datas[i]);
        }
    }

    @Override
    public void setFrameRate(int r) {
        frameRate = r;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer(name, true);
        timer.scheduleAtFixedRate(new imageText(this), 0, 1000 / frameRate);

    }

    @Override
    public void startSource() {
        stopMe = false;
        isPlaying = true;
        loadText();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer(name, true);
        timer.scheduleAtFixedRate(new imageText(this), 0, 1000 / frameRate);
    }

    @Override
    public boolean canUpdateSource() {
        return true;
    }

    @Override
    public void stopSource() {
        if (timer != null) {
            timer.cancel();
            timer = null;
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
    protected java.util.Vector<String> lines = new java.util.Vector<String>();

    protected String translateTags(String text) {
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
        String tmp = "";
        if (text.indexOf("#NOW") != -1) {
            retValue = retValue.replaceAll("#NOW", new Date().toString());
        }
        if (text.indexOf("#HOUR") != -1) {
            tmp = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "";
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            retValue = retValue.replaceAll("#HOUR", tmp);
        }
        if (text.indexOf("#MINUTE") != -1) {
            tmp = Calendar.getInstance().get(Calendar.MINUTE) + "";
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            retValue = retValue.replaceAll("#MINUTE", tmp);
        }
        if (text.indexOf("#SECOND") != -1) {
            tmp = Calendar.getInstance().get(Calendar.SECOND) + "";
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            retValue = retValue.replaceAll("#SECOND", tmp);
        }
        if (text.indexOf("#MONTHNAME") != -1) {
            retValue = retValue.replaceAll("#MONTHNAME", Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()));
        }
        if (text.indexOf("#DAY") != -1) {
            retValue = retValue.replaceAll("#DAY", Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()));
        }
        if (text.indexOf("#MONTH") != -1) {
            tmp = (Calendar.getInstance().get(Calendar.MONTH) + 1) + "";
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            retValue = retValue.replaceAll("#MONTH", tmp);
        }
        if (text.indexOf("#YEAR") != -1) {
            retValue = retValue.replaceAll("#YEAR", Calendar.getInstance().get(Calendar.YEAR) + "");
        }
        if (text.indexOf("#DATE") != -1) {
            tmp = Calendar.getInstance().get(Calendar.DATE) + "";
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            retValue = retValue.replaceAll("#DATE", tmp);
        }

        return retValue;
    }
}

class imageText extends TimerTask {

    VideoSourceText text = null;
    int index = 0;
    int y = 0;
    int x = 0;
    int lineIndex = index;
    String txt = "";
    int interLine = 0;
    private long lastTimeStamp = System.currentTimeMillis();
    private boolean isRendering = false;

    public imageText(VideoSourceText t) {
        text = t;
        y = text.fontSize;
    }

    @Override
    public void run() {

        if (!isRendering) {
            isRendering = true;
            //lineIndex = index;
            if (text.outputWidth == 0 || text.outputHeight == 0) {
                text.outputWidth = 320;
                text.outputHeight = 240;
            }
            text.tempimage = new BufferedImage(text.captureWidth, text.captureHeight, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D buffer = text.tempimage.createGraphics();

            if (text.updateTimeLaspe > 0 && System.currentTimeMillis() - lastTimeStamp > text.updateTimeLaspe) {
                text.loadText();
                lastTimeStamp = System.currentTimeMillis();
            }
            buffer.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            buffer.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            buffer.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            buffer.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
            buffer.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            buffer.setFont(new java.awt.Font(text.fontName, java.awt.Font.PLAIN, text.fontSize));

            buffer.setBackground(text.backgroundColor);
            buffer.clearRect(0, 0, text.tempimage.getWidth(), text.tempimage.getHeight());
            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, text.backgroundOpacity));
            buffer.setColor(text.backgroundColor);
            buffer.fillRect(0, 0, text.tempimage.getWidth(), text.tempimage.getHeight());
            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 1F));
            //draw Line
            switch (text.scrollDirection) {
                case VideoSource.SCROLL_NONE:
                    interLine = 0;
                    index = 0;
                    x = 0;
                    y = text.fontSize;
                    lineIndex = index;
                    while (y < (text.tempimage.getHeight() + text.fontSize) && text.lines.size() > 0 && lineIndex < text.lines.size()) {
                        //buffer.setColor(backgroundColor);
                        //buffer.fillRect(0, 0, tempimage.getWidth(), tempimage.getHeight());
                        //buffer.drawString(lines.get(lineIndex), x + 1, y + 1);
                        buffer.setColor(text.foregroundColor);
                        buffer.drawString(text.translateTags(text.lines.get(lineIndex)), x, y);
                        lineIndex++;
                        y += text.fontSize;
                    }
                    break;
                case VideoSource.SCROLL_BOTTOMTOTOP:

                    if (interLine <= 0) {
                        interLine = text.fontSize;
                        index += 1;
                    }
                    if (index >= text.lines.size()) {
                        index = 0;
                    }
                    x = 0;
                    y = interLine -= 1;
                    lineIndex = index;
                    while (y < (text.tempimage.getHeight() + text.fontSize) && text.lines.size() > 0) {
                        if (lineIndex >= text.lines.size()) {
                            lineIndex = 0;
                        }
                        buffer.setColor(text.foregroundColor);
                        buffer.drawString(text.translateTags(text.lines.get(lineIndex)), x, y);
                        lineIndex++;
                        y += text.fontSize;
                    }
                    break;
                case VideoSource.SCROLL_TOPTOBOTTOM:
                    if (interLine >= text.fontSize) {
                        interLine = 0;
                        index -= 1;
                    }
                    if (index < 0) {
                        index = text.lines.size() - 1;
                    }
                    x = 0;
                    y = interLine += 1;
                    lineIndex = index;
                    while (y < (text.tempimage.getHeight() + text.fontSize) && text.lines.size() > 0) {
                        if (lineIndex >= text.lines.size()) {
                            lineIndex = 0;
                        }
                        buffer.setColor(text.foregroundColor);
                        buffer.drawString(text.translateTags(text.lines.get(lineIndex)), x, y);
                        lineIndex++;
                        y += text.fontSize;
                    }
                    break;
                case VideoSource.SCROLL_LEFTTORIGHT:
                    if (x >= 0) {
                        index -= 1;
                        if (index < 0) {
                            index = text.lines.size() - 1;
                        }
                        x = 0 - buffer.getFontMetrics().stringWidth(text.translateTags(text.lines.get(index)));
                    }
                    x += 3;
                    y = text.fontSize + 10;
                    lineIndex = index;
                    txt = "";
                    while ((buffer.getFontMetrics().stringWidth(txt) + x) <= text.tempimage.getWidth()) {
                        txt += text.translateTags(text.lines.get(lineIndex++));
                        if (lineIndex >= text.lines.size()) {
                            lineIndex = 0;
                        }
                    }
                    buffer.setColor(text.foregroundColor);
                    buffer.drawString(txt, x, y);
                    break;
                case VideoSource.SCROLL_RIGHTTOLEFT:
                    if ((buffer.getFontMetrics().stringWidth(text.translateTags(text.lines.get(index))) + x) <= 0) {
                        x = 0;
                        index += 1;
                    }
                    if (index >= text.lines.size()) {
                        index = 0;
                    }
                    x -= 3;
                    y = text.fontSize + 10;
                    lineIndex = index;
                    txt = "";
                    while ((buffer.getFontMetrics().stringWidth(txt) + x) <= text.tempimage.getWidth()) {
                        txt += text.translateTags(text.lines.get(lineIndex++));
                        if (lineIndex >= text.lines.size()) {
                            lineIndex = 0;
                        }
                    }
                    buffer.setColor(text.foregroundColor);
                    buffer.drawString(txt, x, y);
                    break;
            }
            buffer.dispose();
            text.applyEffects(text.tempimage);
            text.applyShape(text.tempimage);
            text.image = text.tempimage;
            isRendering = false;

        }
    }
}
