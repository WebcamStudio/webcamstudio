package webcamstudio.visage;

/*Autor: Adel Restom adel_restom@yahoo.com*/

import java.awt.Point;
import java.util.Vector;

/*
  This class makes all connected components' operations from finding labels,unifying them, to
  calculating clusters' centers.
*/

public class ConnectedComponents
{

  private  int labels[],cluster;
  private int label;
  private final int clustersMembers[][];
  public int counters[],indecies[],clusters[];

  public ConnectedComponents(int labels[],int clustersMembers[][])
  {
    label = 0;
    this.labels = labels;
    this.clustersMembers = clustersMembers;
  }

  public int findClustersLabels(int x,int y,int width)
  {
    //The algorithm is the following :
    //If the pixel has no neighbors( i.e in a binary image: all neighbors are 0s ) assign a new label to it
    //If the pixel has one neighbor take it's label
    //If the pixel has more than one neighbor take one of the labels and mark the labels as similar
    int w,nw,n,ne;
    if(y == 0)
    {
      if(x == 0)
      {
        label++;
        clustersMembers[x][y] = label;
        labels[label] = Integer.MIN_VALUE;
      }
      else
      {
        if(clustersMembers[x-1][y] == 0)
        {
          label++;
          clustersMembers[x][y] = label;
          labels[label] = Integer.MIN_VALUE;
         }
         else
           clustersMembers[x][y] = clustersMembers[x-1][y];
      }
    }
    else
    {
      n = clustersMembers[x][y-1] ;
      if( x+1 == width )
        ne = 0;
      else
        ne = clustersMembers[x+1][y-1] ;
      if(x != 0)
      {
        nw = clustersMembers[x-1][y-1] ;
        w = clustersMembers[x-1][y] ;
        if( (w == 0) && (nw == 0) && (n==0) && (ne==0) )
        {
          label++;
          clustersMembers[x][y] = label;
          labels[label] = Integer.MIN_VALUE;
        }
        else
        {
          if (w != 0)
          {
            clustersMembers[x][y] = w;
            if(nw != 0)
            {
              if( nw != w )
                labels[nw] = w;
              if( (n==0) && (ne!=0) && (nw != ne) && (labels[nw] != ne) )
                labels[ne] = nw;
            }
            else
            {
              if(n != 0)
              {
                if( w != n)
                  labels[n] = w;
              }
              else
                if( (ne != 0) && (ne != w) && (labels[w] != ne) )
                    labels[ne] = w;
            }
          }
          else
          {
            if (nw != 0)
            {
              clustersMembers[x][y] = nw;
              if( (n==0) && (ne!=0) && (ne !=nw) && (labels[nw] != ne) )
                  labels[ne] = nw;
            }
            else
              if (n != 0)
                clustersMembers[x][y] = n;
              else
              if (ne != 0)
                clustersMembers[x][y] = ne;
          }
        }
      }
      else
      {
        if( (n==0) && (ne==0) )
        {
          label++;
          clustersMembers[x][y] = label;
          labels[label] = Integer.MIN_VALUE;
        }
        else
        {
          if( n != 0)
            clustersMembers[x][y] = n;
          else
            clustersMembers[x][y] = ne;
        }
      }
    }
    labels[label+1] = Integer.MAX_VALUE; //a flag that shows where the array ends
    return label;
  }

  ////////////////////////////////////////

  public void findRootLabels() //For equivalent labels, find the representitive label
  {
    clusters = new int[label + 2];
    cluster = 0;
    if( label == 1 ) //one label found
    {
      cluster++;
      clusters[cluster] = 1;
      clusters[cluster + 1] = Integer.MAX_VALUE;
    }
    else
    {
      boolean visited[] = new boolean[label+1];
      int j,i;
      //eliminate loops and find roots
      for(i=1; labels[i] != Integer.MAX_VALUE ; i++)
      {
        if( labels[i] != Integer.MIN_VALUE )
        {
          for (int q = 1; q < visited.length; q++)
            visited[q] = false;
          visited[i] = true;
          j = i;
          while (labels[j] != Integer.MIN_VALUE)
          {
            j = labels[j];
            if (visited[j])
            {
              labels[i] = Integer.MIN_VALUE;
              break;
            }
            else
              visited[j] = true;
          }
          labels[i] = j;
        }
        else
        {
          cluster++;
          clusters[cluster] = i;
        }
      }
      for (i = 1; labels[i] != Integer.MAX_VALUE ; i++)
        if (labels[i] == Integer.MIN_VALUE)
          labels[i] = i;
      clusters[cluster + 1] = Integer.MAX_VALUE;
    }
   }

  /////////////////////////////////////////

  private int[][] findClustersCenters(int clusters[],double limit,int width,int height)
  {
    if( cluster == 1 ) //one cluster found
    {
      int centerX = 0;
      int centerY = 0;
      int counter = 0;
      for (int y = 0; y < height; y++)
      {
        for (int x = 0; x < width; x++)
        {
          if (clustersMembers[x][y] != 0)
          {
            centerX += x;
            centerY += y;
            counter++;
          }
        }
      }
      if( counter > limit ) //if cluster size is larger than the threshold
      {
        int centers[][] = new int[cluster+2][2];
        centers[1][0] = (int)centerX / counter;
        centers[1][1] = (int)centerY / counter;
        centers[cluster+1][0] = Integer.MAX_VALUE;
        return centers;
      }
      else
        return null;
    }
    else
    {
      //more than one cluster
      long[] centerX = new long[cluster + 2];
      long[] centerY = new long[cluster + 2];
      counters = new int[cluster + 2];
      indecies = new int[label + 2];
      int index, clusterArea;
      for (int i = 1; clusters[i] != Integer.MAX_VALUE; i++)
        indecies[clusters[i]] = i;
      Vector<Integer> finalClusters = new Vector<>();

      for (int y = 0; y < height; y++)
      {
        for (int x = 0; x < width; x++)
        {
          if (clustersMembers[x][y] != 0)
          {
            index = indecies[labels[clustersMembers[x][y]]];
            centerX[index] += x;
            centerY[index] += y;
            counters[index]++;
          }
        }
      }
      for (int i = 1; clusters[i] != Integer.MAX_VALUE; i++)
      {
        clusterArea = counters[indecies[clusters[i]]];
        if (clusterArea > limit)
          finalClusters.add(new Integer(clusters[i]));
        else
          cluster--;
      }
      if( cluster != 0 )
      {
        for (int i = 0; i < finalClusters.size(); i++)
          clusters[i + 1] = ( (Integer) (finalClusters.get(i))).intValue();
        clusters[cluster + 1] = Integer.MAX_VALUE;
        int centers[][] = new int[cluster + 2][2];

        for (int i = 1; clusters[i] != Integer.MAX_VALUE; i++)
        {
          index = indecies[clusters[i]];
          centers[i][0] = (int) (centerX[index] / counters[index]);
          centers[i][1] = (int) (centerY[index] / counters[index]);
        }
        centers[cluster + 1][0] = Integer.MAX_VALUE;
        return centers;
      }
      else
        return null;
    }
  }

  //////////////////////////////////////////////////

  private Point oneClusterCenter(int centerX,int centerY,int counter,int start
                                 ,int end,int width,double limit)
  {
    for (int y = start; y < end; y++) //look in the specified region
    {
      for (int x = 0; x < width; x++)
      {
        if (clustersMembers[x][y] != 0)
        {
          centerX += x;
          centerY += y;
          counter++;
        }
      }
    }
    if (counter > (double) limit / 2d)
    {
      Point center = new Point();
      center.setLocation( (int) (centerX / counter), (int) (centerY / counter));
      return center;
    }
    else
      return null;
  }

  //////////////////////////////////////////////////

  private Point multipleClustersCenter(int clusters[],int start,int end,int width,double limit
                                       ,int grayPixels[],int x0,int y0,int fWidth)
  {
    long[] centerX = new long[cluster + 2];
    long[] centerY = new long[cluster + 2];
    long[] colors = new long[cluster + 2];
    int[] counter = new int[cluster + 2];
    int[] indecies = new int[label + 2];
    int index = 0, clusterArea, darkestPixel = 256, pixelColor, darkestX = 0, darkestY = 0, ind;
    for (int i = 1; clusters[i] != Integer.MAX_VALUE; i++)
      indecies[clusters[i]] = i;
    for (int y = start; y < end; y++)
    {
      for (int x = 0; x < width; x++)
      {
        if (clustersMembers[x][y] != 0)
        {
          index = indecies[labels[clustersMembers[x][y]]];
          centerX[index] += x;
          centerY[index] += y;
          counter[index]++;
          pixelColor = grayPixels[ (y + y0) * fWidth + (x + x0)];
          colors[index] += pixelColor;
          if (pixelColor < darkestPixel)
          {
            darkestPixel = pixelColor;
            darkestX = x;
            darkestY = y;
          }
        }
      }
    }
    double max = Double.NEGATIVE_INFINITY, ratio, dist = 0;
    int cX, cY;
    index = Integer.MIN_VALUE;
    for (int i = 1; clusters[i] != Integer.MAX_VALUE; i++)
    {
      clusterArea = counter[indecies[clusters[i]]];
      if (clusterArea > (double) limit / 2d)
      {
        ind = indecies[clusters[i]];
        cX = (int) (centerX[ind] / counter[ind]);
        cY = (int) (centerY[ind] / counter[ind]);
        dist = Math.sqrt(Math.pow( (cX - darkestX), 2) +
                         Math.pow( (cY - darkestY), 2));
        ratio = clusterArea / (colors[ind] * dist); //look for the largest,darkest,and closest to the darkest pixel, cluster
        if (ratio > max)
        {
          max = ratio;
          index = ind;
        }
      }
    }
    if (index != Integer.MIN_VALUE)
    {
      Point center = new Point();
      center.setLocation( (int) (centerX[index] / counter[index]),
                         (int) (centerY[index] / counter[index]));
      return center;
    }
    else
      return null;
  }

  //////////////////////////////////////////////////

  public Point findPupilsClustersCenters(int x0,int y0,int clusters[],double limit,int width,int height,int grayPixels[],int fWidth)
  {
    if( cluster == 1 ) //one cluster found
    {
      int centerX = 0;
      int centerY = 0;
      int counter = 0; //look in the lower half
      Point center = oneClusterCenter(centerX,centerY,counter,(int)height/2,height,width,limit);
      if( center != null )
        return center;
      else
      {
        centerX = 0;
        centerY = 0;
        counter = 0; //look in the upper half
        return oneClusterCenter(centerX,centerY,counter,0,(int)height/2,width,limit);
      }
    }
    else
    { //look in the lower half
      Point center = multipleClustersCenter(clusters,(int)height/2,height,width,limit,grayPixels,x0,y0,fWidth);
      if( center != null )
        return center;
      else  //look in the upper half
        return multipleClustersCenter(clusters,0,(int)height/2,width,limit,grayPixels,x0,y0,fWidth);
    }
  }


  //////////////////////////////////////////////////

  public int[][] processClusters(double limit,int width,int height)
  {
    findRootLabels();
    return findClustersCenters(clusters,limit,width,height);
  }

  public int getCluster() {
    return cluster;
  }

  public int getLabel() {
    return label;
  }

  ///////////////////////////////////////////////////




}
