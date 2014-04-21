package webcamstudio.visage;

/*Autor: Adel Restom adel_restom@yahoo.com*/

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

/*
  This class contains utility functions for image processing.
*/

public class ImageProcessing
{
  static final double sqrt3 = Math.sqrt(3);

  public static int[] extractPixels(BufferedImage img, int pixels[]) {
      return img.getRaster().getPixel(0, 0, pixels);
  }

  public static int[] extractPixels(Image image, int sX, int sY, int width, int height, int pixels[]){
      PixelGrabber pg = new PixelGrabber(image, sX, sY, width, height, pixels, 0, width);
      try
      {
          pg.grabPixels(); //the PixelGrabber class grabs the pixels from the image and puts them in the desired array.
      }
      catch (InterruptedException ex){}
      return pixels;
  }
  /////////////////////////////////////
    public static int[] toGrayscale(BufferedImage img, int[] grayPixels)
    {
        BufferedImage gray = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D buffer = gray.createGraphics();
        buffer.drawImage(img, 0, 0,null);
        buffer.dispose();
        return gray.getRaster().getPixel(0, 0, grayPixels);
    }

  public static int[] toGrayscale(int pixels[], int grayPixels[]){
      float[] hsb = new float[3]; //array that will contain the hew,saturation,and brightness values of
      float brightness;           //the image pixels.
      int rgb,red,green,blue;
      for (int i = 0; i < pixels.length; i++) //extract the red,green,blue bands of the image
      {                                       //and convert them to the hsb color model.
          red = (pixels[i]>>16) & 0xFF;
          green = (pixels[i]>>8) & 0xFF;
          blue = pixels[i] & 0xFF;
          Color.RGBtoHSB(red, green, blue, hsb);
          brightness = hsb[2];
          rgb = Color.HSBtoRGB(0, 0, brightness); //set the hew and the saturation to zero to get the representitive gray color.
          grayPixels[i] = rgb & 0xFF; //the red = green = blue after the conversion so we're storing one of the bands.
      }
      return grayPixels;
  }
  /////////////////////////////////////
    public static int[][] calculateIntegralImage(int width, int height, int grayPixels[], int s[][], int ii[][])
    {
        //Calculate cumulative sums
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
            {
                s[x][y] = ( y-1 == -1 ? grayPixels[y*width+x] : grayPixels[y*width+x]+s[x][y-1] );
                ii[x][y] = ( x-1 == -1 ? s[x][y] : ii[x-1][y]+s[x][y] );
            }
        }
        return ii;
    }

  //////////////////////////////////////////////

  public static boolean isSkinPixel(int color)
  {
      int R, G, B;
      double r, g, a, b, aValue, bValue;
      R = (color >> 16) & 0xFF;
      G = (color >> 8) & 0xFF;
      B = color & 0xFF;
      r = R / (R + G + B);
      g = G / (R + G + B);
      a = r + (g / 2);
      b = (sqrt3 / 2d) * g;
      aValue = Math.round(a / 0.01d);
      bValue = Math.round(b / 0.01d);
      return (aValue >= 49) && (aValue <= 59) && (bValue >= 24) && (bValue <= 29);
  }

  /////////////////////////////////////////////

  public static int[] toBinary(BufferedImage img, int binaryPixels[])
  {
      BufferedImage bImage = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
      Graphics2D buffer = bImage.createGraphics();
      buffer.drawImage(img,0,0,null);
      buffer.dispose();
      return bImage.getRaster().getPixel(0, 0, binaryPixels);

  }
  public static int[] toBinary(int grayPixels[], int binaryPixels[], int threshold){
      for (int i = 0; i < grayPixels.length; i++) {
          if( grayPixels[i] < threshold ) {
              binaryPixels[i] = 1;
          } else {
              binaryPixels[i] = 0;
          }
      }
      return binaryPixels;
  }
  ///////////////////////////////////////////////
    public static BufferedImage toInvertedBinary(int grayPixels[], int threshold, int width, int height)
    {
        int counter = 0;
        BufferedImage bImage = new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);
        for(int y=0 ; y<height ; y++) {
            for (int x = 0; x<width; x++) {
                if( grayPixels[y*width+x] < threshold )
                {
                    bImage.setRGB(x, y, Color.white.getRGB());
                    counter++;
                }
            }
        }
        if( counter < 10 ) {
            return null;
        }
        return bImage;
    }

  private ImageProcessing()
  {
  }


}
