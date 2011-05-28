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
 *  You sshould have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * 
 */
package webcamstudio.sources;

import java.net.MalformedURLException;
import java.util.prefs.BackingStoreException;
import webcamstudio.components.Shapes;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import webcamstudio.*;
import webcamstudio.sources.effects.Effect;
import webcamstudio.studio.Studio;

public abstract class VideoSource implements InfoListener {

    public static java.util.TreeMap<String,VideoSource> loadedSources = new TreeMap<String,VideoSource>();
    public abstract void startSource();

    public abstract void stopSource();

    public void setName(String n){
        name=n;
    }
    public void setLocation(String l){
        location=l;
    }
    protected javax.swing.ImageIcon getCachedThumbnail() {
        ImageIcon icon = null;
        File img = new File(System.getenv("HOME") + "/.webcamstudio/thumbs/" + location.replaceAll("/", "_").replaceAll("file:","") + ".png");
        if (img.exists()){
            try {
                icon = new ImageIcon(new ImageIcon(img.toURI().toURL()).getImage().getScaledInstance(32, 32, BufferedImage.SCALE_FAST));
            } catch (MalformedURLException ex) {
                Logger.getLogger(VideoSource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return icon;
    }
    public javax.swing.ImageIcon getThumbnail() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TRANSLUCENT);
        Graphics2D g = img.createGraphics();
        String n = name;
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, 32, 32);
        g.setColor(Color.BLACK);
        int l = g.getFontMetrics().stringWidth(n);
        g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,8));
        g.drawString(n, 1, 7);
        g.drawString(n, -32, 15);
        g.drawString(n, -64, 23);
        g.drawString(n, -96, 31);
        g.dispose();
        return new ImageIcon(img);
    }

    public void saveThumbnail(ImageIcon icon) throws IOException{

        File dir = new File(System.getenv("HOME"), ".webcamstudio");
        if (!dir.exists()){
            dir.mkdir();
        }
        File thumbs = new File(dir,"thumbs");
        if (!thumbs.exists()){
            thumbs.mkdir();
        }
        File img = new File(thumbs,location.replaceAll("/","_").replaceAll("file:","")+".png");
        if (img.exists()){
            img.delete();
        }

        BufferedImage i = new BufferedImage(icon.getIconWidth(),icon.getIconHeight(),BufferedImage.TRANSLUCENT);
        i.getGraphics().drawImage(icon.getImage(), 0, 0, null);
        javax.imageio.ImageIO.write((RenderedImage)i, "png", img);
        System.out.println("Saving to " + img.getAbsolutePath());
    }
    public abstract java.util.Collection<JPanel> getControls();

    public boolean hasSound() {
        return hasSound;
    }

    public boolean isImage() {
        return this instanceof VideoSourceImage;
    }

    public boolean isAnimation() {
        return this instanceof VideoSourceAnimation;
    }

    public void setLoadSound(boolean doLoad) {
        loadSound = doLoad;
    }

    public void setFontSize(int s) {
        fontSize = s;
    }

    public int getFontSize() {
        return fontSize;
    }

    public Color getForeground() {
        return foregroundColor;
    }

    public Color getBackground() {
        return backgroundColor;
    }

    public void applyStudioConfig(java.util.prefs.Preferences prefs, int order) {

        prefs.put("class", this.getClass().getName());
        prefs.put("location", location);
        prefs.putInt("outputWidth", outputWidth);
        prefs.putInt("outputHeight", outputHeight);
        prefs.putInt("showAtX", showAtX);
        prefs.putInt("showAtY", showAtY);
        prefs.putInt("captureAtX", captureAtX);
        prefs.putInt("captureAtY", captureAtY);
        prefs.putInt("captureWidth", captureWidth);
        prefs.putInt("captureHeight", captureHeight);
        prefs.put("activeEffect", activeEffect);
        prefs.put("name", name);
        prefs.putInt("videoeffect", videoEffect);
        prefs.putInt("effectsSensitivityLow", effectsSensitivityLow);
        prefs.putInt("effectsSensitivityHigh", effectsSensitivityHigh);
        prefs.putInt("fontsize", fontSize);
        prefs.put("username", username);
        prefs.putBoolean("followmousecursor", followMouseCursor);
        prefs.putBoolean("visibleOnlyWhenSelected", visibleOnlyWhenSelected);
        switch (colorFormat) {
            case RGB:
                prefs.put("colorformat", "rgb");
                break;
            case YUV:
                prefs.put("colorformat", "yuv");
                break;
        }
        prefs.putLong("updatetimelapse", updateTimeLaspe);
        prefs.put("shapename", shapeName);
        prefs.put("audiosink", audioSink);
        prefs.putInt("activitythreshold", activityThreshold);
        prefs.putInt("scrollDirection", scrollDirection);
        prefs.putInt("frameRate", frameRate);
        prefs.put("fontname", fontName);
        prefs.put("nick", nick);
        prefs.put("customtext", customText);
        prefs.putBoolean("forvieweronly", forViewerOnly);
        prefs.put("virtualhostkeywords", virtualHostKeywords);
        prefs.put("customshapefilename", customShapeFileName);
        prefs.putBoolean("showmousecursor", isShowMouseCursor());
        prefs.putInt("foreground", foregroundColor.getRGB());
        prefs.putInt("background", backgroundColor.getRGB());
        prefs.putInt("volume", volume);
        prefs.put("uuid", uuId);
        prefs.putBoolean("rescale", doRescale);
        prefs.putBoolean("reverseshapemask", doReverseShapeMask);
        prefs.putBoolean("ignorelayouttransition", ignoreLayoutTransition);
        int index = 0;
        for (Effect effect : effects) {
            String key = Studio.getKeyIndex(index++);
            prefs.node("Effects").node(key).put("name", effect.getName());
            effect.applyStudioConfig(prefs.node("Effects").node(key));
        }
        prefs.putFloat("bgopacity", backgroundOpacity);
        prefs.putInt("layer", layer);
    }

    public abstract boolean canUpdateSource();

    public void loadFromStudioConfig(java.util.prefs.Preferences prefs) {
        location = prefs.get("location", ""); //source.put("location", location);
        outputWidth = prefs.getInt("outputWidth", 320); //source.putInt("outputWidth", outputWidth);
        outputHeight = prefs.getInt("outputHeight", 240); //source.putInt("outputHeight",outputHeight);
        setOutputWidth(outputWidth);
        setOutputHeight(outputHeight);

        showAtX = prefs.getInt("showAtX", 0);  //source.putInt("showAtX",showAtX);
        showAtY = prefs.getInt("showAtY", 0);  //source.putInt("showAtY",showAtY);
        setShowAtX(showAtX);
        setShowAtY(showAtY);

        captureAtX = prefs.getInt("captureAtX", 0); //source.putInt("captureAtX",captureAtX);
        captureAtY = prefs.getInt("captureAtY", 0); //source.putInt("captureAtY",captureAtY);
        setCaptureAtX(captureAtX);
        setCaptureAtY(captureAtY);

        captureWidth = prefs.getInt("captureWidth", 320); //source.putInt("captureWidth",captureWidth);
        captureHeight = prefs.getInt("captureHeight", 240); //source.putInt("captureHeight",captureHeight);
        setCaptureWidth(captureWidth);
        setCaptureHeight(captureHeight);

        activeEffect = prefs.get("activeEffect", "");
        setEffect(activeEffect);

        name = prefs.get("name", "");
        videoEffect = prefs.getInt("videoeffect", VideoEffects.NONE);
        effectsSensitivityHigh = prefs.getInt("effectsSensitivityHigh", 100);
        effectsSensitivityLow = prefs.getInt("effectsSensitivityLow", 0);
        username = prefs.get("username", username);
        fontSize = prefs.getInt("fontsize", fontSize);
        followMouseCursor = prefs.getBoolean("followmousecursor", followMouseCursor);
        visibleOnlyWhenSelected = prefs.getBoolean("visibleOnlyWhenSelected", visibleOnlyWhenSelected);
        String temp = prefs.get("colorformat", "yuv");
        if (temp.equals("yuv")) {
            colorFormat = ColorFormat.YUV;
        } else if (temp.equals("rgb")) {
            colorFormat = ColorFormat.RGB;
        }
        updateTimeLaspe = prefs.getLong("updatetimelapse", updateTimeLaspe);
        shapeName = prefs.get("shapename", shapeName);
        customShapeFileName = prefs.get("customshapefilename", customShapeFileName);
        if (shapeName != null && shapeName.length() > 0) {
            if (shapeName.equals("custom")) {
                try {
                    if (new File(customShapeFileName).exists()) {
                        shape = javax.imageio.ImageIO.read(new File(customShapeFileName));
                    }
                } catch (IOException ex) {
                    Logger.getLogger(VideoSource.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                shape = new Shapes().getImage(shapeName);
            }
        }
        audioSink = prefs.get("audiosink", audioSink);
        activityThreshold = prefs.getInt("activitythreshold", activityThreshold);
        scrollDirection = prefs.getInt("scrollDirection", scrollDirection);
        frameRate = prefs.getInt("frameRate", frameRate);
        fontName = prefs.get("fontname", fontName);
        nick = prefs.get("nick", nick);
        customText = prefs.get("customtext", customText);
        forViewerOnly = prefs.getBoolean("forvieweronly", forViewerOnly);
        virtualHostKeywords = prefs.get("virtualhostkeywords", virtualHostKeywords);
        foregroundColor = new Color(prefs.getInt("foreground", foregroundColor.getRGB()));
        backgroundColor = new Color(prefs.getInt("background", backgroundColor.getRGB()));
        volume = prefs.getInt("volume", volume);
        uuId = prefs.get("uuid", uuId);
        doRescale = prefs.getBoolean("rescale", doRescale);
        doReverseShapeMask = prefs.getBoolean("reverseshapemask", doReverseShapeMask);
        ignoreLayoutTransition = prefs.getBoolean("ignorelayouttransition", ignoreLayoutTransition);
        String[] effectIndexes;
        try {
            effectIndexes = prefs.node("Effects").childrenNames();
            java.util.TreeMap<String, Effect> list = Effect.getEffects();
            for (String effectIndex : effectIndexes) {
                Effect e = list.get(prefs.node("Effects").node(effectIndex).get("name", ""));
                if (e != null) {
                    e.loadFromStudioConfig(prefs.node("Effects").node(effectIndex));
                    effects.add(e);
                }
            }

        } catch (BackingStoreException ex) {
            Logger.getLogger(VideoSource.class.getName()).log(Level.SEVERE, null, ex);
        }
        backgroundOpacity = prefs.getFloat("bgopacity", backgroundOpacity);
        layer = prefs.getInt("layer", layer);
    }

    public String getNick() {
        return nick;
    }

    public String getCustomText() {
        return customText;
    }

    public void setKeywords(String keywords) {
        virtualHostKeywords = keywords;
    }

    public boolean hasNewKeywords() {
        return !virtualHostKeywords.equals(oldvirtualHostKeywords);
    }

    public void updateOldKeywords() {
        oldvirtualHostKeywords = virtualHostKeywords.toString();
    }

    public String getKeywords() {
        return virtualHostKeywords;
    }

    public void setFrameRate(int rate) {
        if (rate == 0) {
            frameRate = 1;
        } else {
            frameRate = rate;
        }
    }

    public int getFrameRate() {
        return frameRate;
    }

    public abstract boolean hasText();

    public void setCaptureAtX(int x) {
        captureAtX = x;
    }

    public abstract boolean isPaused();

    public void setCaptureAtY(int y) {
        captureAtY = y;
    }

    public void setCaptureWidth(int w) {
        captureWidth = w;
    }

    public void setCaptureHeight(int h) {
        captureHeight = h;

    }

    public String getName() {
        return name;
    }

    public abstract boolean isPlaying();

    public abstract void pause();

    public abstract void play();

    @Override
    public String toString() {

        return location;
    }

    public String getLocation() {
        return location;
    }

    public int getOutputWidth() {
        return outputWidth;
    }

    public void setOutputWidth(int outputWidth) {
        this.outputWidth = outputWidth;
    }

    public int getOutputHeight() {
        return outputHeight;
    }

    public void setOutputHeight(int outputHeight) {
        this.outputHeight = outputHeight;
    }

    public int getShowAtX() {
        return showAtX;
    }

    public void setShowAtX(int showAtX) {
        this.showAtX = showAtX;
    }

    public int getShowAtY() {
        return showAtY;
    }

    public void setShowAtY(int showAtY) {
        this.showAtY = showAtY;
    }

    public int getCaptureAtX() {
        return captureAtX;
    }

    public int getCaptureAtY() {
        return captureAtY;
    }

    public int getCaptureWidth() {
        return captureWidth;
    }

    public int getCaptureHeight() {
        return captureHeight;
    }

    public String getEffect() {
        return activeEffect;
    }

    public void setEffect(String effect) {
        if (isPlaying()) {
            stopSource();
            activeEffect = effect;
            try {
                startSource();
            } catch (Exception e) {
                activeEffect = "";
                stopSource();
                startSource();
            }
        } else {
            activeEffect = effect;
        }
    }

    public String getUUID() {
        return uuId;
    }

    public void setVideoEffect(int effect) {
        videoEffect = effect;
    }

    public int getVideoEffect() {
        return videoEffect;
    }

    public void setVolume(int v) {
        volume = v;

    }

    public int getVolume() {
        return volume;
    }

    public void info(String info) {
        if (listener != null) {
            listener.info(info);
        }

    }

    public void error(String message) {
        if (listener != null) {
            listener.error(message);
        }

    }

    public void newTextLine(String line) {
        if (listener != null) {
            listener.newTextLine(line);
        }

    }

    public java.awt.image.BufferedImage getImage() {
        return image;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int o) {
        opacity = o;
    }

    public void setShapeName(String n) {
        shapeName = n;
    }

    public String getShapeName() {
        return shapeName;
    }

    public void setLooping(boolean doIt) {
        doLoop = doIt;
    }

    public ColorFormat getColorFormat() {
        return colorFormat;
    }

    public void setColorFormat(ColorFormat color) {
        colorFormat = color;
    }

    public void setForeground(java.awt.Color c) {
        foregroundColor = c;
    }

    public void setBackground(java.awt.Color c) {
        backgroundColor = c;
    }

    public void setEffectsSensitivity(int low, int high) {
        effectsSensitivityLow = low;
        effectsSensitivityHigh =
                high;
    }

    public void setEffectsLowSensitivity(int v) {
        effectsSensitivityLow = v;
    }

    public void setEffectsHighSensitivity(int v) {
        effectsSensitivityHigh = v;
    }

    public int getEffectsLowSensitivity() {
        return effectsSensitivityLow;
    }

    public int getEffectsHighSensitivity() {
        return effectsSensitivityHigh;
    }

    public boolean isFollowingMouse() {
        return followMouseCursor;
    }

    public void setFollowMouse(boolean v) {
        followMouseCursor = v;
    }

    public void setListener(InfoListener l) {
        listener = l;
    }

    public void setVisibleWhenSelected(boolean v) {
        visibleOnlyWhenSelected = v;
    }

    public boolean isVisibleWhenSelected() {
        return visibleOnlyWhenSelected;
    }

    public void setSelected(boolean v) {
        isSelected = v;
    }

    public boolean isSelected() {
        return isSelected;
    }

    protected void applyShape(java.awt.image.BufferedImage img) {
        if (shape != null) {
            java.awt.Graphics2D buffer = img.createGraphics();
            if (doReverseShapeMask) {
                buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.DST_OUT, 1.0F));
            } else {
                buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.DST_IN, 1.0F));
            }
            buffer.setColor(java.awt.Color.BLACK);
            buffer.drawImage(shape, 0, 0, img.getWidth(), img.getHeight(), 0, 0, shape.getWidth(null), shape.getHeight(null), null);
            buffer.dispose();
        }

    }

    public void setShape(java.awt.Image img) {
        shape = img;
    }

    public java.awt.Image getShape() {
        return shape;
    }

    public void setUpdateTimeLapse(long millis) {
        updateTimeLaspe = millis;
    }

    public long getUpdateTimeLapse() {
        return updateTimeLaspe;
    }

    public boolean isShowMouseCursor() {
        return showMouseCursor;
    }

    public void setShowMouseCursor(boolean showMouseCursor) {
        this.showMouseCursor = showMouseCursor;
    }

    public enum ColorFormat {

        YUV,
        RGB
    }

    public void setAudioSink(String gstSink) {
        audioSink = gstSink;
    }

    public String getAudioSink() {
        return audioSink;
    }

    public void setActivityDetection(int threshold) {
        activityThreshold = threshold;
        activityDetected =
                false;
    }

    public int getActivityDetection() {
        return activityThreshold;
    }

    public boolean activityDetected() {
        return activityDetected;
    }

    public void setScrollingMode(int mode) {
        scrollDirection = mode;
    }

    public int getScrollingMode() {
        return scrollDirection;
    }

    protected void detectActivity(java.awt.image.BufferedImage input) {
        if (activityDetected && System.currentTimeMillis() - lastTimeStamp > 5000) {
            lastTimeStamp = System.currentTimeMillis();
        } else if (activityThreshold > 0 && System.currentTimeMillis() - lastTimeStamp > 1000) {
            int w = input.getWidth();
            int h = input.getHeight();
            activityDetected =
                    true;
            if (lastInputImage == null || w != lastInputImage.getWidth() || h != lastInputImage.getHeight()) {
                lastInputImage = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(w, h, java.awt.image.BufferedImage.TRANSLUCENT);
                lastInputImage.getGraphics().drawImage(input, 0, 0, null);
            }

            int[] data1 = ((java.awt.image.DataBufferInt) input.getRaster().getDataBuffer()).getData();
            int[] data2 = ((java.awt.image.DataBufferInt) lastInputImage.getRaster().getDataBuffer()).getData();
            int nbDiffPixels = 0;
            int r1, r2, g1, g2, b1, b2, c1, c2;
            for (int i = 0; i
                    < data1.length; i++) {
                c1 = data1[i];
                c2 =
                        data2[i];
                r1 =
                        ((c1 & 0x00FF0000) >> 16) / 16;
                g1 =
                        ((c1 & 0x0000FF00) >> 8) / 16;
                b1 =
                        ((c1 & 0x000000FF) >> 0) / 16;

                r2 =
                        ((c2 & 0x00FF0000) >> 16) / 16;
                g2 =
                        ((c2 & 0x0000FF00) >> 8) / 16;
                b2 =
                        ((c2 & 0x000000FF) >> 0) / 16;

                if (r1 != r2 || b1 != b2 || g1 != g2) {
                    nbDiffPixels++;
                }

            }

            if (nbDiffPixels * 100 / data1.length > activityThreshold) {
                activityDetected = true;
            } else {
                activityDetected = false;
            }

            lastInputImage.getGraphics().drawImage(input, 0, 0, null);
            lastTimeStamp =
                    System.currentTimeMillis();
        }

    }

    public void setFont(String name) {
        fontName = name;
    }

    public String getFont() {
        return fontName;
    }

    public void setViewOnly(boolean value) {
        forViewerOnly = value;
    }

    public boolean isViewOnly() {
        return forViewerOnly;
    }

    public void setListener(VideoSourceListener l) {
        vlistener = l;
    }

    public boolean hasVideoSourceListener() {
        return (vlistener != null);
    }

    public void fireSourceUpdated() {
        if (vlistener != null) {
            new Thread(new Runnable() {

                public void run() {
                    if (vlistener != null) {
                        vlistener.sourceUpdated();
                    }

                }
            }).start();

        }

    }

    public void setCustomShapeFileName(String f) {
        customShapeFileName = f;
    }

    public String getCustomShapeFileName() {
        return customShapeFileName;
    }

    public void setLightMode(boolean mode) {
        lightMode = mode;
    }

    public boolean isLightMode() {
        return lightMode;
    }

    protected synchronized void applyEffects(BufferedImage img) {
        for (Effect e : effects) {
            e.applyEffect(img);
        }
    }

    public synchronized void setEffects(java.util.Vector<Effect> list) {
        effects = list;
    }

    public synchronized void addEffect(Effect e) {
        effects.add(e);
    }

    public synchronized void removeEffect(Effect e) {
        effects.remove(e);
    }

    public void doRescale(boolean v) {
        doRescale = v;
    }

    public boolean isRescaled() {
        return doRescale;
    }

    public void doReverseShapeMask(boolean v) {
        doReverseShapeMask = v;
    }

    public boolean isReverseShapeMask() {
        return doReverseShapeMask;
    }

    public java.util.Vector<Effect> getEffects() {
        return effects;
    }

    public BufferedImage getRawImage() {
        return rawImage;
    }

    protected void applyFaceDetection(BufferedImage source) {
        rawImage = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.OPAQUE);
        rawImage.createGraphics().drawImage(source, 0, 0, null);

        if (faceDetection != null) {
            Graphics2D buffer = source.createGraphics();
            buffer.drawImage(faceDetection, 0, 0, null);
            buffer.dispose();
        }
    }

    public void ignoreLayoutTransition(boolean value) {
        ignoreLayoutTransition = value;
    }

    public boolean isIgnoringLayoutTransition() {
        return ignoreLayoutTransition;
    }

    public void setFaceDetection(BufferedImage face) {
        if (face != null) {
            faceDetection = face.getScaledInstance(captureWidth, captureHeight, Image.SCALE_FAST);
        } else {
            faceDetection = null;
        }
    }

    /**
     * @return the backgroundOpacity
     */
    public float getBackgroundOpacity() {
        return backgroundOpacity;
    }

    /**
     * @param backgroundOpacity the backgroundOpacity to set
     */
    public void setBackgroundOpacity(float backgroundOpacity) {
        this.backgroundOpacity = backgroundOpacity;
    }

    public void setLayer(int l) {
        layer = l;
    }

    public int getLayer() {
        return layer;
    }
    protected String location = null;
    protected int outputWidth = 0;
    protected int outputHeight = 0;
    protected int showAtX = 0;
    protected int showAtY = 0;
    protected int captureAtX = 0;
    protected int captureAtY = 0;
    protected int captureWidth = 320;
    protected int captureHeight = 240;
    protected int opacity = 100;
    protected String activeEffect = "";
    protected String uuId = java.util.UUID.randomUUID().toString();
    protected int videoEffect = 0;
    protected java.awt.image.BufferedImage image = null;
    protected int frameRate = 15;
    protected String name = "";
    protected int volume = 15;
    protected boolean loadSound = true;
    protected boolean hasSound = false;
    protected boolean doLoop = true;
    protected int effectsSensitivityLow = 10;
    protected int effectsSensitivityHigh = 100;
    protected boolean isRendering = false;
    protected int[] pixels = null;
    protected java.awt.image.BufferedImage tempimage = null;
    protected boolean stopMe = true;
    protected boolean isPlaying = false;
    protected java.awt.Color foregroundColor = java.awt.Color.WHITE;
    protected java.awt.Color backgroundColor = java.awt.Color.BLACK;
    protected boolean followMouseCursor = true;
    protected int fontSize = 10;
    protected InfoListener listener = null;
    protected String username = "";
    protected boolean visibleOnlyWhenSelected = false;
    protected boolean isSelected = false;
    protected ColorFormat colorFormat = ColorFormat.YUV;
    protected long updateTimeLaspe = 0;
    protected java.awt.Image shape = null;
    protected String shapeName = "";
    protected String audioSink = "gconfaudiosink";
    protected boolean activityDetected = true;
    protected int activityThreshold = 0;
    private java.awt.image.BufferedImage lastInputImage = null;
    private long lastTimeStamp = 0;    //Scrolling method
    public static final int SCROLL_NONE = 0;
    public static final int SCROLL_TOPTOBOTTOM = 1;
    public static final int SCROLL_BOTTOMTOTOP = 2;
    public static final int SCROLL_LEFTTORIGHT = 3;
    public static final int SCROLL_RIGHTTOLEFT = 4;
    protected int scrollDirection = 0;
    protected String fontName = "Monospaced";
    protected String nick = "";
    protected boolean forViewerOnly = false;
    protected String virtualHostKeywords = "";
    protected String oldvirtualHostKeywords = "";
    private VideoSourceListener vlistener = null;
    private String customShapeFileName = "";
    protected boolean showMouseCursor = false;
    protected boolean lightMode = false;
    protected String customText = "";
    protected java.util.Vector<Effect> effects = new java.util.Vector<Effect>();
    protected GraphicsConfiguration graphicConfiguration = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    protected boolean doRescale = false;
    protected boolean doReverseShapeMask = false;
    protected Image faceDetection = null;
    protected BufferedImage rawImage = null;
    protected boolean ignoreLayoutTransition = false;
    protected float backgroundOpacity = 0;
    protected int layer = -1;
}
