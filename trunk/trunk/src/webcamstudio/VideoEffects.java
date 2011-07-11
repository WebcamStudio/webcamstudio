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
package webcamstudio;

import java.awt.image.*;
import java.awt.*;

/**
 *
 * @author pballeux
 */
public class VideoEffects {

    public BufferedImage applyEffect(int effect, BufferedImage input, int low, int high) {

        if (output == null || w != output.getWidth() || h != output.getWidth()) {
            w = input.getWidth();
            h = input.getHeight();
            output = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(w, h, BufferedImage.TYPE_INT_ARGB);
            buffer = output.createGraphics();
            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 1F));
        }
        switch (effect) {
            case NONE:
                backgroundImage = null;
                buffer.drawImage(input, 0, 0, null);
                break;
            case MOSAIC:
                output = getMosaic(input, 3);
                break;
            case MOSAICDELAYED:
                output = getMosaicDelayed(input, 3);
                break;
            case TARGET:
                output = getTarget(input);
                break;
            case DIFFERENCE:
                output = getDifference(input, low);
                break;
            case LCD:
                output = getLCD(input);
                break;
            case PIXELS:
                output = getPixels(input, low + 1);
                break;
            case MIRROR:
                output = getMirror(input);
                break;
            case SCAN:
                output = getScan(input);
                break;
            case CARTOON:
                output = getCartoon(input, low, high);
                break;
            case GREEN:
                output = getChomakeyGreen(input, low, high);
                break;
            case BLUE:
                output = getChomakeyBlue(input, low, high);
                break;
            case CHROME:
                output = getChrome(input, low, high);
                break;
            case AVERAGE:
                output = getAverage(input);
                break;
            case CIRCLE:
                output = getCircle(input, low, high);
                break;
            case KALEIDOSCOPE:
                output = getKaleidoscope(input, low, high);
                break;
            case PERPECTIVE:
                output = getPerpective(input, low, high);
                break;
            case TWIRL:
                output = getTwirl(input, low, high);
                break;
            case NOBACKGROUND:
                output = getNoBackground(input, low, high);
                break;
            case SEETHEPAST:
                output = getSeeThePast(input, low, high);
                break;
            case FILTERRED:
                output = getRed(input);
                break;
            case FILTERGREEN:
                output = getGreen(input);
                break;
            case FILTERBLUE:
                output = getBlue(input);
                break;
            case NIGHTVISION:
                output = getNightVision(input, low, high);
                break;
            case NIGHTVISION2:
                output = getNightVision2(input, low, high);
                break;
        }
        return output;
    }

    public BufferedImage getMosaic(BufferedImage input, int nbSquaresWidthHeight) {

        int smallWidth = w / nbSquaresWidthHeight;
        int smallHeight = h / nbSquaresWidthHeight;
        while (smallWidth * nbSquaresWidthHeight < w) {
            smallWidth++;
        }
        while (smallHeight * nbSquaresWidthHeight < h) {
            smallHeight++;
        }

        for (int y = 0; y < h; y += smallHeight) {
            for (int x = 0; x < w; x += smallWidth) {
                buffer.drawImage(input, x, y, x + smallWidth, y + smallHeight, 0, 0, w, h, null);
            }
        }
        return output;
    }

    public BufferedImage getMosaicDelayed(BufferedImage input, int nbSquaresWidthHeight) {

        int smallWidth = w / nbSquaresWidthHeight;
        int smallHeight = h / nbSquaresWidthHeight;
        while (smallWidth * nbSquaresWidthHeight < w) {
            smallWidth++;
        }
        while (smallHeight * nbSquaresWidthHeight < h) {
            smallHeight++;
        }
        int x = lastX;
        int y = lastY;
        if (lastX == -1 && lastY == -1) {
            lastX = 0;
            lastY = 0;
        }
        if (lastX >= w) {
            x = 0;
            y = lastY + smallHeight;
        }
        if (lastY >= h) {
            y = 0;
            x = 0;
        }
        if (lastImage == null) {
            lastImage = input;
        }
        buffer.drawImage(lastImage, 0, 0, null);
        buffer.drawImage(input, x, y, x + smallWidth, y + smallHeight, 0, 0, w, h, null);
        lastX = x + smallWidth;
        lastY = y;
        lastImage = output;
        return output;
    }

    public void setImage(BufferedImage img) {
        lastImage = img;
    }

    public BufferedImage getTarget(BufferedImage input) {
        if (lastX == -1 && lastY == -1) {
            lastX = w / 3;
            lastY = w / 3;
        }
        int x = lastX;
        int y = lastY;
        buffer.drawImage(input, 0, 0, null);
        buffer.setColor(Color.BLACK);
        buffer.drawOval(x, y, w / 3, h / 3);
        buffer.setColor(Color.RED.darker());
        buffer.drawOval(x + 2, y + 2, (w / 3) - 4, (h / 3) - 4);
        buffer.setColor(Color.RED.brighter());
        buffer.drawOval(x + 4, y + 4, (w / 3) - 8, (h / 3) - 8);
        buffer.setColor(Color.BLACK);
        buffer.drawLine(x + (w / 6), 0, x + (w / 6), h);
        buffer.drawLine(0, y + (h / 6), w, y + (h / 6));
        lastX = x + random.nextInt(11) - 5;  //to have a value from -5 to 5
        lastY = y + random.nextInt(11) - 5;  // to have a value from -5 to 5
        if (lastX < 0) {
            lastX = 0;
        } else if ((lastX + (w / 3)) > w) {
            lastX = w - (w / 3);
        }
        if (lastY < 0) {
            lastY = 0;
        } else if ((lastY + h / 3) > h) {
            lastY = h - (h / 3);
        }
        return output;
    }

    public BufferedImage getDifference(BufferedImage input, int delay) {
        lastImage = graphicConfiguration.createCompatibleImage(w, h, BufferedImage.TYPE_INT_ARGB);
        lastImage.createGraphics().drawImage(input, 0, 0, null);
        lastImages.add(0, lastImage);
        for (int i = delay; i < lastImages.size(); i++) {
            lastImages.remove(lastImages.lastElement());
        }
        buffer.drawImage(input, 0, 0, null);
        buffer.setXORMode(Color.BLACK);
        buffer.drawImage(lastImages.lastElement(), 0, 0, null);
        buffer.setPaintMode();
        buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 1F));
        frameCount++;
        return output;
    }

    public BufferedImage getSeeThePast(BufferedImage input, int delay, int opacity) {
        frameCount++;
        if (frameCount > delay) {
            lastImage = graphicConfiguration.createCompatibleImage(w, h, BufferedImage.TYPE_INT_ARGB);
            lastImage.createGraphics().drawImage(input, 0, 0, null);
            lastImages.add(0, lastImage);
            frameCount = 0;
            if (lastImages.size() > 3) {
                lastImages.removeElement(lastImages.lastElement());
            }
        }
        buffer.drawImage(input, 0, 0, null);
        buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, (float) opacity / 255F));
        for (int i = lastImages.size() - 1; i >= 0; i--) {
            buffer.drawImage(lastImages.get(i), 0, 0, null);
        }
        buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 1F));
        return output;
    }

    public BufferedImage getMirror(BufferedImage input) {
        buffer.drawImage(input, 0, 0, w / 2, h, 0, 0, w / 2, h, null);
        buffer.drawImage(input, w, 0, w / 2, h, 0, 0, w / 2, h, null);
        return output;
    }

    public BufferedImage getLCD(BufferedImage input) {
        int color = 0;
        //Looking at each 5 pixels...
        buffer.setColor(Color.LIGHT_GRAY);
        buffer.fillRect(0, 0, w, h);

        for (int y = 0; y < h; y += 5) {
            for (int x = 0; x < w; x += 5) {
                color = input.getRGB(x, y);
                Color c = new Color(color);
                int avg = (c.getBlue() + c.getRed() + c.getGreen()) / 3;
                if (avg < 150) {
                    buffer.setColor(Color.DARK_GRAY);
                    buffer.fillRect(x, y, 5, 5);
                }
            }
        }
        return output;
    }

    public BufferedImage getPixels(BufferedImage input, int low) {

        blockFilter.setBlockSize(low);
        input = blockFilter.filter(input, null);
        buffer.drawImage(input, 0, 0, null);
        return output;
    }

    public BufferedImage getScan(BufferedImage input) {
        int color = 0;
        //draw only one line per frame...
        if (lastImage == null) {
            lastImage = input;
        }
        buffer.drawImage(lastImage, 0, 0, null);

        if (frameCount >= h) {
            frameCount = 0;
        }
        for (int i = 0; i < w; i++) {
            color = input.getRGB(i, frameCount);
            buffer.setColor(new Color(color));
            buffer.drawLine(i, frameCount, i, frameCount);
        }
        frameCount++;
        if (frameCount >= h) {
            frameCount = 0;
        }

        for (int i = 0; i < w; i++) {
            color = input.getRGB(i, frameCount);
            buffer.setColor(new Color(color));
            buffer.drawLine(i, frameCount, i, frameCount);
        }
        frameCount++;
        if (frameCount >= h) {
            frameCount = 0;
        }
        for (int i = 0; i < w; i++) {
            color = input.getRGB(i, frameCount);
            buffer.setColor(new Color(color));
            buffer.drawLine(i, frameCount, i, frameCount);
        }
        frameCount++;
        if (frameCount >= h) {
            frameCount = 0;
        }
        for (int i = 0; i < w; i++) {
            color = input.getRGB(i, frameCount);
            buffer.setColor(new Color(color));
            buffer.drawLine(i, frameCount, i, frameCount);
        }
        frameCount++;
        if (frameCount >= h) {
            frameCount = 0;
        }
        for (int i = 0; i < w; i++) {
            color = input.getRGB(i, frameCount);
            buffer.setColor(new Color(color));
            buffer.drawLine(i, frameCount, i, frameCount);
        }
        buffer.setColor(Color.RED);
        buffer.drawLine(0, frameCount, w, frameCount);
        lastImage = output;
        return output;
    }

    public BufferedImage getCartoon(BufferedImage input, int low, int high) {
        counterFilter.setScale(0.5F);
        counterFilter.setLevels((float) high / 10F);

        int[] data = ((java.awt.image.DataBufferInt) input.getRaster().getDataBuffer()).getData();
        int r, g, b, a, c;
        int delta1, delta2;
        for (int i = 0; i < data.length; i++) {
            c = data[i];
            r = (c & 0x00FF0000) >> 16;
            g = (c & 0x0000FF00) >> 8;
            b = (c & 0x000000FF) >> 0;
            a = (c & 0xFF000000) >> 24;

            delta1 = r - g;
            delta2 = g - b;
            if (low == 0) {
                low = 1;
            }
            //Downscaling the red value
            r = r / low * low;
            g = r - delta1;
            if (g < 0) {
                g = 0;
            }
            b = g - delta2;
            if (b < 0) {
                b = 0;
            }
            data[i] = ((r << 16) | (g << 8) | (b << 0) | (a << 24));
        }
        input = counterFilter.filter(input, null);
        buffer.drawImage(input, 0, 0, null);
        return output;
    }

    public BufferedImage getChomakeyGreen(BufferedImage input, final int low, final int high) {
        int[] data = ((java.awt.image.DataBufferInt) input.getRaster().getDataBuffer()).getData();
        int r, g, b, c;
        for (int i = 0; i < data.length; i++) {
            c = data[i];
            r = (c & 0x00FF0000) >> 16;
            g = (c & 0x0000FF00) >> 8;
            b = (c & 0x000000FF) >> 0;
            if ((r + b) <= low && g > high) {
                data[i] = data[i] & 0x00FFFFFF;
            }
        }
        buffer.drawImage(input, 0, 0, null);
        return output;
    }

    public BufferedImage getChomakeyBlue(BufferedImage input, final int low, final int high) {
        int[] data = ((java.awt.image.DataBufferInt) input.getRaster().getDataBuffer()).getData();
        int r, g, b, c;

        for (int i = 0; i < data.length; i++) {
            c = data[i];
            r = (c & 0x00FF0000) >> 16;
            g = (c & 0x0000FF00) >> 8;
            b = (c & 0x000000FF) >> 0;
            if ((r + g) <= low && b > high) {
                data[i] = data[i] & 0x00FFFFFF;
            }
        }
        buffer.drawImage(input, 0, 0, null);
        return output;
    }

    public BufferedImage getChrome(BufferedImage input, int low, int high) {
        chromeFilter.setAmount((float) low / 255F);
        chromeFilter.setExposure((float) high / 255);
        input = chromeFilter.filter(input, null);
        buffer.drawImage(input, 0, 0, null);
        return output;
    }

    public BufferedImage getAverage(BufferedImage input) {
        input = averageFilter.filter(input, null);
        buffer.drawImage(input, 0, 0, null);
        return output;
    }

    public BufferedImage getCircle(BufferedImage input, int low, int high) {
        circleFilter.setAngle((float) Math.toRadians(low * 360F / 255F));
        circleFilter.setHeight(high);
        circleFilter.setRadius(high / 4);
        input = circleFilter.filter(input, null);
        buffer.drawImage(input, 0, 0, null);
        return output;
    }

    public BufferedImage getKaleidoscope(BufferedImage input, int low, int high) {

        kaleidoscopeFilter.setAngle((float) Math.toRadians(low * 360F / 255F));
        kaleidoscopeFilter.setAngle2((float) Math.toRadians(high * 360F / 255F));
        input = kaleidoscopeFilter.filter(input, null);

        buffer.drawImage(input, 0, 0, null);
        return output;
    }

    public BufferedImage getPerpective(BufferedImage input, int low, int high) {

        int x1, x2, x3, x4;
        x1 = low * w / 255;
        x2 = high * w / 255;
        x3 = high * w / 255;
        x4 = low * w / 255;
        if (x1 < (w / 2)) {
            x1 = ((w / 2) - x1);
            x4 = 0;
        } else {
            x4 = (x1 - (w / 2));
            x1 = 0;
        }

        if (x2 < (w / 2)) {
            x2 = (w / 2) + x2;
            x3 = w;
        } else {
            x3 = w - (x3 - (w / 2));
            x2 = w;
        }
        perspectiveFilter.setCorners(x1, 0, x2, 0, x3, h, x4, h);
        input = perspectiveFilter.filter(input, null);
        buffer.drawImage(input, 0, 0, null);
        return output;
    }

    public BufferedImage getTwirl(BufferedImage input, int low, int high) {
        twirlFilter.setAngle((float) Math.toRadians(low * 360F / 255F));
        twirlFilter.setRadius(high);
        input = twirlFilter.filter(input, null);
        buffer.drawImage(input, 0, 0, null);
        return output;
    }

    public BufferedImage getNoBackground(BufferedImage input, int low, int high) {
        int[] data = ((java.awt.image.DataBufferInt) input.getRaster().getDataBuffer()).getData();
        int r, g, b, c, a, cb;
        if (high == 0) {
            high = 1;
        }
        if (backgroundImage == null || dataBackgroundImage.length != data.length) {
            backgroundImage = graphicConfiguration.createCompatibleImage(w, h, BufferedImage.TYPE_INT_ARGB);
            backgroundImage.getGraphics().drawImage(input, 0, 0, null);
            dataBackgroundImage = ((java.awt.image.DataBufferInt) backgroundImage.getRaster().getDataBuffer()).getData();
        } else {
            for (int i = 0; i < data.length; i++) {
                c = data[i];
                cb = dataBackgroundImage[i];
                r = ((((c & 0x00FF0000) >> 16))) - (((cb & 0x00FF0000) >> 16));
                g = (((c & 0x0000FF00) >> 8)) - (((cb & 0x0000FF00) >> 8));
                b = (((c & 0x000000FF))) - ((cb & 0x000000FF));
                if (r < 0) {
                    r *= -1;
                }
                if (g < 0) {
                    g *= -1;
                }
                if (b < 0) {
                    b *= -1;
                }
                if (r < low && b < low && g < low) {
                    data[i] = c & 0x00FFFFFF;
                }
            }
            buffer.drawImage(input, 0, 0, null);
        }
        return output;
    }

    public BufferedImage getRed(BufferedImage input) {
        int[] data = ((java.awt.image.DataBufferInt) input.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < data.length; i++) {
            data[i] = (data[i] & 0xFFFF0000);
        }
        buffer.drawImage(input, 0, 0, null);
        return output;
    }

    public BufferedImage getGreen(BufferedImage input) {
        int[] data = ((java.awt.image.DataBufferInt) input.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < data.length; i++) {
            data[i] = (data[i] & 0xFF00FF00);
        }
        buffer.drawImage(input, 0, 0, null);
        return output;
    }

    public BufferedImage getBlue(BufferedImage input) {
        int[] data = ((java.awt.image.DataBufferInt) input.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < data.length; i++) {
            data[i] = (data[i] & 0xFF0000FF);
        }
        buffer.drawImage(input, 0, 0, null);
        return output;
    }

    public BufferedImage getNightVision(BufferedImage input, int low, int high) {
        int[] data = ((java.awt.image.DataBufferInt) input.getRaster().getDataBuffer()).getData();
        int r, g, b, c, dr, dg, db;
        for (int i = 0; i < data.length; i++) {
            c = data[i];
            r = (c & 0x00FF0000) >> 16;
            g = (c & 0x0000FF00) >> 8;
            b = (c & 0x000000FF) >> 0;
            if (r < low && g < low && b < low) {
                r = g = b = 0;
            } else {
                if (r < high && g < high && b < high) {
                    dr = high - r;
                    dg = high - g;
                    db = high - b;
                    if (dr < db && dr < dg) {
                        r += dr;
                        g += dr;
                        b += dr;
                    } else if (dg < dr && dg < db) {
                        r += dg;
                        g += dg;
                        b += dg;
                    } else {
                        r += db;
                        g += db;
                        b += db;
                    }
                }
            }
            data[i] = 0xFF000000 + (r << 16) + (g << 8) + b;
        }
        buffer.drawImage(input, 0, 0, null);
        return output;
    }

    // Created by Mark Dammer
    public BufferedImage getNightVision2(BufferedImage input, int stacksize, int divider) {
        int r, g, b, a, c;
        int lr, lg, lb, lc;
        int or, og, ob;
        ++stacksize;
        ++divider;
        lastImage = graphicConfiguration.createCompatibleImage(w, h, BufferedImage.TYPE_INT_ARGB);
        if ((lastWidth == w) && (lastHeight == h) && (lastStacksize == stacksize)) {
            firstImage = graphicConfiguration.createCompatibleImage(w, h, BufferedImage.TYPE_INT_ARGB);
            firstImage.createGraphics().drawImage(input, 0, 0, null);
            lastImages.add(0, firstImage);
            while (lastImages.size() > stacksize) {
                lastImage.createGraphics().drawImage(lastImages.lastElement(), 0, 0, null);
                lastImages.removeElement(lastImages.lastElement());
            }
            int[] data = ((java.awt.image.DataBufferInt) input.getRaster().getDataBuffer()).getData();
            int[] lastdata = ((java.awt.image.DataBufferInt) lastImage.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < data.length; i++) {
                // separate the color channels of the current image
                c = data[i];
                r = (c & 0x00FF0000) >> 16;
                g = (c & 0x0000FF00) >> 8;
                b = (c & 0x000000FF) >> 0;
                a = (c & 0xFF000000) >> 24;
                redBuffer[i] = redBuffer[i] + r;
                greenBuffer[i] = greenBuffer[i] + g;
                blueBuffer[i] = blueBuffer[i] + b;
                if (lastImages.size() >= stacksize) {
                    // separate the color channels of the last image on the stack
                    lc = lastdata[i];
                    lr = (lc & 0x00FF0000) >> 16;
                    lg = (lc & 0x0000FF00) >> 8;
                    lb = (lc & 0x000000FF) >> 0;
                    redBuffer[i] = redBuffer[i] - lr;
                    greenBuffer[i] = greenBuffer[i] - lg;
                    blueBuffer[i] = blueBuffer[i] - lb;
                }
                or = Math.round(redBuffer[i] / divider);
                og = Math.round(greenBuffer[i] / divider);
                ob = Math.round(blueBuffer[i] / divider);
                if (or > 255) {
                    or = 255;
                }
                if (og > 255) {
                    og = 255;
                }
                if (ob > 255) {
                    ob = 255;
                }
                if (or < 0) {
                    or = 0;
                }
                if (og < 0) {
                    og = 0;
                }
                if (ob < 0) {
                    ob = 0;
                }
                data[i] = ((or << 16) | (og << 8) | (ob << 0) | (a << 24));
            }
            buffer.drawImage(input, 0, 0, null);
        } else {
            redBuffer = new int[((java.awt.image.DataBufferInt) lastImage.getRaster().getDataBuffer()).getData().length];
            greenBuffer = new int[((java.awt.image.DataBufferInt) lastImage.getRaster().getDataBuffer()).getData().length];
            blueBuffer = new int[((java.awt.image.DataBufferInt) lastImage.getRaster().getDataBuffer()).getData().length];
            lastWidth = w;
            lastHeight = h;
            lastStacksize = stacksize;
            lastImages.clear();
        }
        return output;
    }
    private int lastX = -1;
    private int lastY = -1;
    private BufferedImage lastImage = null;
    private BufferedImage backgroundImage = null;
    private int[] dataBackgroundImage = null;
    private int frameCount = 0;
    private java.util.Random random = new java.util.Random();
    private static java.awt.GraphicsConfiguration graphicConfiguration = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private int w = 320;
    private int h = 240;
    java.util.Vector<BufferedImage> lastImages = new java.util.Vector<BufferedImage>();
    public static final int NONE = 0;
    public static final int MOSAIC = 1;
    public static final int MOSAICDELAYED = 2;
    public static final int TARGET = 3;
    public static final int DIFFERENCE = 4;
    public static final int LCD = 5;
    public static final int PIXELS = 6;
    public static final int MIRROR = 7;
    public static final int SCAN = 8;
    public static final int CARTOON = 9;
    public static final int GREEN = 10;
    public static final int BLUE = 11;
    public static final int CHROME = 12;
    public static final int AVERAGE = 13;
    public static final int CIRCLE = 14;
    public static final int KALEIDOSCOPE = 15;
    public static final int PERPECTIVE = 16;
    public static final int TWIRL = 17;
    public static final int NOBACKGROUND = 18;
    public static final int SEETHEPAST = 19;
    public static final int FILTERRED = 20;
    public static final int FILTERGREEN = 21;
    public static final int FILTERBLUE = 22;
    public static final int NIGHTVISION = 23;
    public static final int NIGHTVISION2 = 24;
    private BufferedImage output = graphicConfiguration.createCompatibleImage(w, h, BufferedImage.TYPE_INT_ARGB);
    private java.awt.Graphics2D buffer = null;
    private com.jhlabs.image.ContourFilter counterFilter = new com.jhlabs.image.ContourFilter();
    private com.jhlabs.image.ChromeFilter chromeFilter = new com.jhlabs.image.ChromeFilter();
    private com.jhlabs.image.AverageFilter averageFilter = new com.jhlabs.image.AverageFilter();
    private com.jhlabs.image.TwirlFilter twirlFilter = new com.jhlabs.image.TwirlFilter();
    private com.jhlabs.image.PerspectiveFilter perspectiveFilter = new com.jhlabs.image.PerspectiveFilter();
    private com.jhlabs.image.KaleidoscopeFilter kaleidoscopeFilter = new com.jhlabs.image.KaleidoscopeFilter();
    private com.jhlabs.image.CircleFilter circleFilter = new com.jhlabs.image.CircleFilter();
    private com.jhlabs.image.BlockFilter blockFilter = new com.jhlabs.image.BlockFilter();
    private BufferedImage firstImage = null;
    private BufferedImage imageStack = graphicConfiguration.createCompatibleImage(w, h, BufferedImage.TYPE_INT_ARGB);
    private int[] redBuffer;
    private int[] greenBuffer;
    private int[] blueBuffer;
    private int lastHeight = 0;
    private int lastWidth = 0;
    private int lastStacksize = 1;
}
