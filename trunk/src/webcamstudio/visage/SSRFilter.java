package webcamstudio.visage;

/*Autor: Adel Restom adel_restom@yahoo.com*/

/*
  This class represents the Six Segmented Rectangular filter
*/

public class SSRFilter
{
  private int width,height,sectorWidth,sectorHeight;
  private int ii[][];
  private double area;

  public SSRFilter(int width,int height,int ii[][])
  {
    this.width = width;
    this.height = height;
    this.sectorWidth = (int)(width/3);
    this.sectorHeight = (int)(height/2);
    this.ii = ii; //Integral image array
    area = width*height;
  }

  /////////////////////////////////////////////

  public boolean foundFaceCandidate(int x,int y)
  {
    int s1,s2,s3,s4,s6;

    //Calculate sectors
    s1 = ii[x+sectorWidth][y+sectorHeight] - ii[x+sectorWidth][y] - ii[x][y+sectorHeight] + ii[x][y];
    s2 = ii[x+2*sectorWidth][y+sectorHeight] - ii[x+2*sectorWidth][y] - ii[x+sectorWidth][y+sectorHeight] + ii[x+sectorWidth][y];
    s3 = ii[x+3*sectorWidth][y+sectorHeight] - ii[x+3*sectorWidth][y] - ii[x+2*sectorWidth][y+sectorHeight] + ii[x+2*sectorWidth][y];
    s4 = ii[x+sectorWidth][y+2*sectorHeight] - ii[x+sectorWidth][y+sectorHeight] - ii[x][y+2*sectorHeight] + ii[x][y+sectorHeight];
    s6 = ii[x+3*sectorWidth][y+2*sectorHeight] - ii[x+3*sectorWidth][y+sectorHeight] - ii[x+2*sectorWidth][y+2*sectorHeight] + ii[x+2*sectorWidth][y+sectorHeight];

    //Face candidate conditions
    if( (s1<s2) && (s1<s4) && (s3<s2) && (s3<s6) )
      return true;
    else
      return false;
  }

  ////////////////////////////////////////

  public int findNoseBridgeCandidate(int x,int y)
  {
    int s1,s2,s3;

    //calculate sectors
    s1 = ii[x+sectorWidth][y] - ii[x][y];
    s2 = ii[x+2*sectorWidth][y] - ii[x+sectorWidth][y];
    s3 = ii[x+3*sectorWidth][y] - ii[x+2*sectorWidth][y];

    //Nose bridge candidate conditions
    if( (s1<s2) && (s3<s2) )
      return s2;
    else
      return Integer.MIN_VALUE;
  }

  /////////////////////////////////////////

  public int getHeight() {
    return height;
  }
  public int getWidth() {
    return width;
  }
  public double getArea() {
    return area;
  }

  /////////////////////////////////////////////

}
