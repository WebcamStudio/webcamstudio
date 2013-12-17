package webcamstudio.visage;

/*Autor: Adel Restom adel_restom@yahoo.com*/
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import svm.svm;
import svm.svm_model;
import svm.svm_node;

/*
This class detects the eyes and nose locations in a given frame
 */
public class FaceDetector {

    final int fWidth = 320, fHeight = 240;
    int[] labels, start, binaryPixels;
    public int[] grayPixels, pixels;
    private int[] lastPoints = null;
    int[][] s, ii, clustersMembers, eyes;
    int faces, cluster;
    private static svm_model model;
    svm_node nodes[];

    //////////////////////////////
    public FaceDetector(svm_model model) {
        pixels = new int[fWidth * fHeight];
        grayPixels = new int[fWidth * fHeight];
        binaryPixels = new int[fWidth * fHeight];
        s = new int[fWidth][fHeight];
        ii = new int[fWidth][fHeight];
        clustersMembers = new int[fWidth][fHeight];
        labels = new int[fWidth * fHeight];
        nodes = new svm_node[735];
        FaceDetector.model = model;
        start = new int[model.nr_class];
        start[0] = 0;
        for (int i = 1; i < model.nr_class; i++) {
            start[i] = start[i - 1] + model.nSV[i - 1];
        }
    }

    public FaceDetector() {
        pixels = new int[fWidth * fHeight];
        grayPixels = new int[fWidth * fHeight];
        binaryPixels = new int[fWidth * fHeight];
        s = new int[fWidth][fHeight];
        ii = new int[fWidth][fHeight];
        clustersMembers = new int[fWidth][fHeight];
        labels = new int[fWidth * fHeight];
        nodes = new svm_node[735];

    }

    ////////////////////////////////////////
    public int[] detectAllFaces(BufferedImage image) {
        //Initializing
        if (model == null) {
            try {
                model = svm.svm_load_model(getClass().getResourceAsStream("/webcamstudio/visage/Model.txt"));
            } catch (IOException ex) {
                Logger.getLogger(FaceDetector.class.getName()).log(Level.SEVERE, null, ex);
            }
            start = new int[model.nr_class];
            start[0] = 0;
            for (int i = 1; i < model.nr_class; i++) {
                start[i] = start[i - 1] + model.nSV[i - 1];
            }
        }
        BufferedImage img = new BufferedImage(fWidth, fHeight, BufferedImage.OPAQUE);
        Graphics2D buffer = img.createGraphics();
        buffer.drawImage(image, 0, 0, fWidth, fHeight, 0, 0, image.getWidth(), image.getHeight(), null);
        buffer.setBackground(Color.BLACK);
        buffer.clearRect(0, 0, fWidth, fHeight / 3);
        buffer.clearRect(0, fHeight * 2 / 3, fWidth, fHeight / 3);
        buffer.dispose();
        pixels = ImageProcessing.extractPixels(img, 0, 0, fWidth, fHeight, pixels);
        grayPixels = ImageProcessing.toGrayscale(pixels, grayPixels);
        binaryPixels = ImageProcessing.toBinary(grayPixels, binaryPixels, 128);
        ii = ImageProcessing.calculateIntegralImage(fWidth, fHeight, grayPixels, s, ii);
        faces = 0;
        int coords[];

        {
            SSRFilter filter2 = new SSRFilter(84, 54, ii);
            coords = detectFaces(filter2);
        }
        if (faces == 0) {
            SSRFilter filter3 = new SSRFilter(60, 36, ii);
            coords = detectFaces(filter3);
        }
        if (faces == 0) {
            SSRFilter filter3 = new SSRFilter(30, 18, ii);
            coords = detectFaces(filter3);
        }
        lastPoints = coords;
        return coords;
    }

    ////////////////////////////////////////
    private int[] detectFaces(SSRFilter filter) {
        //Detect faces with the given filter
        //If the pixel is a face candidate find it's cluster
        int xCor, yCor, label = 0;
        for (int i = 0; i < fWidth; i++) {
            for (int j = 0; j < fHeight; j++) {
                clustersMembers[i][j] = 0;
            }
        }

        ConnectedComponents CC = new ConnectedComponents(labels, clustersMembers);

        for (int y = 0; y < fHeight - filter.getHeight(); y++) {
            for (int x = 0; x < fWidth - filter.getWidth(); x++) {
                xCor = x + 1 + (filter.getWidth() / 2); //+1 : transition error in sectors calculation
                yCor = y + 1 + (filter.getHeight() / 2);
                if (filter.foundFaceCandidate(x, y) &&
                        ImageProcessing.isSkinPixel(pixels[yCor * fWidth + xCor])) {
                    label = CC.findClustersLabels(xCor, yCor, fWidth);
                }
            }
        }

        //If there are face candidates
        if (label != 0) {
            //Find root labels,and their centers
            int centers[][] = CC.processClusters(filter.getArea() / 48d, fWidth, fHeight);

            //If there are valid clusters
            if (centers != null) {
                //Find left/right pupils for each of the candidates
                cluster = CC.getCluster();
                int counter = findPupilsCandidates(filter, centers);

                //if found some eyes
                if (counter != -1) {
                    //Load templates to the array in order to classify them
                    Vector templates = loadEyesTemplates(counter);

                    //Classiffying templates
                    double cResults[] = classify(templates);

                    //Multiply classification results by clusters' areas
                    if (cluster > 1) {
                        cResults = multiplyByArea(CC.indecies, CC.clusters, CC.counters, cResults);
                    }

                    //Find the template with the highest score
                    int face[] = findBestEyesTemplate(cResults);

                    //Find nose tip
                    if (face != null) {
//                        Point noseTip = findNoseTip(face);
//                        if (noseTip != null) {
                        int coordinates[] = new int[6];
                        coordinates[0] = face[0];
                        coordinates[1] = face[1];
                        coordinates[2] = face[2];
                        coordinates[3] = face[3];
//                            coordinates[4] = (int) noseTip.getX();
//                            coordinates[5] = (int) noseTip.getY();
                        return coordinates;
//                        }
                    }
                }
            }
        }
        return null;
    }

    //////////////////////////////////////////////
    private Point findPupil(int x0, int y0, int width, int height, int labels[], int pupilsMembers[][], int limit) {
        int label = 0; //binarize the sector and look for the appropriate cluster
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pupilsMembers[x][y] = 0;
            }
        }
        ConnectedComponents CC = new ConnectedComponents(labels, pupilsMembers);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (binaryPixels[(y + y0) * fWidth + (x + x0)] == 1) {
                    label = CC.findClustersLabels(x, y, width);
                }
            }
        }
        if (label != 0) {
            CC.findRootLabels();
            return CC.findPupilsClustersCenters(x0, y0, CC.clusters, limit, width, height, grayPixels, fWidth);
        } else {
            return null;
        }
    }

    /////////////////////////////////////////////
    private int findPupilsCandidates(SSRFilter filter, int centers[][]) {
        eyes = new int[cluster][5];
        int width = filter.getWidth() / 3;
        int height = filter.getHeight() / 2;
        int labels[] = new int[width * height];
        int pupilsMembers[][] = new int[width][height];
        int limit = (int) filter.getArea() / (144 * 6);
        int halfW, halfH, sixthW, counter = -1;
        halfW = filter.getWidth() / 2;
        halfH = filter.getHeight() / 2;
        sixthW = filter.getWidth() / 6;
        Point lp1, rp1;
        int x, y;
        for (int i = 1; centers[i][0] != Integer.MAX_VALUE; i++)//pass all clusters
        {
            x = centers[i][0];
            y = centers[i][1];
            //find left pupil in sector s1
            lp1 = findPupil(x - halfW, y - halfH, width, height, labels, pupilsMembers, limit);
            if (lp1 == null) {
                continue;
            }

            //find right pupil in sector s3
            rp1 = findPupil(x + sixthW, y - halfH, width, height, labels, pupilsMembers, limit);
            if (rp1 == null) {
                continue;
            }

            counter++;
            eyes[counter][0] = (int) lp1.getX() + x - halfW;
            eyes[counter][1] = (int) lp1.getY() + y - halfH;
            eyes[counter][2] = (int) rp1.getX() + x + sixthW;
            eyes[counter][3] = (int) rp1.getY() + y - halfH;
            eyes[counter][4] = i;
        }
        return counter;
    }

    //////////////////////////////////////////
    private svm_node[] extractEyesTemplate(int x0, int y0, int x1, int y1) {
        int xLen, yLen, sX, sY, cX, cY, oX, oY;
        double xInc, yInc, nY, scale;
        svm_node node;
        svm_node template[] = new svm_node[735];
        xLen = x1 - x0;
        yLen = y1 - y0;
        xInc = (double) xLen / 23d;
        yInc = (double) yLen / 23d;
        scale = Math.sqrt(Math.pow(xLen, 2) + Math.pow(yLen, 2)) / 23d;
        oX = x0 - (int) (6 * xInc) + (int) (8 * yInc);
        nY = (yLen != 0 ? (8 * yInc * xLen) / yLen : 8 * scale);
        oY = y0 - (int) nY;
        sX = oX;
        sY = oY; //derotate the template to a horizontal position
        for (int y = 0; y < 21; y++) {
            cX = sX;
            cY = sY;
            for (int x = 0; x < 35; x++) {
                node = new svm_node();
                node.index = y * 35 + x + 1;
                if ((cX >= 0) && (cX < fWidth) && (cY < fHeight) && (cY >= 0)) {
                    node.value = grayPixels[cY * fWidth + cX] / 255d;
                } else if (y == 0) {
                    node.value = 128d / 255d;
                } else {
                    node.value = template[(y - 1) * 35 + x].value;
                }
                template[y * 35 + x] = node;
                cX = sX + (int) ((x + 1) * xInc);
                cY = sY + (int) ((x + 1) * yInc);
            }
            sX = oX - (int) ((y + 1) * yInc);
            sY = oY + (int) ((y + 1) * xInc);
        }
        return template;
    }

    //////////////////////////////////////////
    private Vector loadEyesTemplates(int counter) {
        svm_node[] template;
        Vector<svm_node[]> templates = new Vector<>();

        //Extract the templates and write them in the 'LibSVM' file format
        for (int i = 0; i <= counter; i++) {
            template = extractEyesTemplate(eyes[i][0], eyes[i][1],
                    eyes[i][2], eyes[i][3]);
            templates.add(template);
        }
        return templates;
    }

    /////////////////////////////////////////////
    private double[] classify(Vector templates) {
        double cResults[] = new double[templates.size()];
        double[] decValues = new double[1];

        for (int i = 0; i < templates.size(); i++) {
            svm.svm_predict_values(model, (svm_node[]) templates.get(i), decValues, start);
            cResults[i] = decValues[0] * model.label[0];
        }
        return cResults;
    }

    //////////////////////////////////////////////////
    private double[] multiplyByArea(int indecies[], int clusters[], int counters[], double cResults[]) {
        for (int i = 0; i < cResults.length; i++) {
            if (cResults[i] > 0) {
                cResults[i] *= counters[indecies[clusters[eyes[i][4]]]];
            }
        }
        return cResults;
    }

    //////////////////////////////////////////////////
    private int[] findBestEyesTemplate(double cResults[]) {
        double max = 0d;
        int index = 0;

        for (int i = 0; i < cResults.length; i++) {
            if (cResults[i] > max) {
                max = cResults[i];
                index = i;
            }
        }

        if (max > 0) {
            faces++;
            int face[] = new int[4];
            face[0] = eyes[index][0];
            face[1] = eyes[index][1];
            face[2] = eyes[index][2];
            face[3] = eyes[index][3];
            return face;
        } else {
            return null;
        }
    }

    //////////////////////////////////////////////////
    private Point[] findNoseBridge(int length, int ii[][]) {
        int val, max, nbcx = 0, counter = 0;
        SSRFilter filter = new SSRFilter(length / 2, 1, ii);
        Point candidates[] = new Point[length];

        for (int y = 0; y < length; y++) {
            max = Integer.MIN_VALUE;
            for (int x = 0; x < length - filter.getWidth(); x++) {
                val = filter.findNoseBridgeCandidate(x, y);
                if (val > max) {
                    max = val;
                    nbcx = x + 1 + (filter.getWidth() / 2);
                }
            }
            if (max != Integer.MIN_VALUE) {
                counter++;
                candidates[y] = new Point(nbcx, max);
            }
        }
        if (counter > (length / 5)) {
            return candidates;
        } else {
            return null;
        }
    }

    ////////////////////////////////////////////////
    private int findNextCandidate(int i, Point candidates[]) {
        while ((candidates[i] == null) && (i < candidates.length - 1)) {
            i++;
        }
        return i;
    }

    ////////////////////////////////////////////////
    private int[] calculateGradiants(Point candidates[], int length) {
        int gradiants[] = new int[length], i = 0, ind1 = 0, ind2 = 0;
        for (int q = 0; q < gradiants.length; q++) {
            gradiants[q] = Integer.MAX_VALUE;
        }
        Point c1, c2;
        while (true) {
            ind1 = findNextCandidate(ind2, candidates);
            c1 = candidates[ind1];
            if (c1 != null) {
                if (ind1 + 1 < length) {
                    ind2 = findNextCandidate(ind1 + 1, candidates);
                    c2 = candidates[ind2];
                    if (c2 != null) {
                        gradiants[ind2] = (int) (c2.getY() - c1.getY());
                        if (ind2 == length - 1) {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return gradiants;
    }

    //////////////////////////////////////////////////
    private Point findNoseTip(int face[]) {
        int x1, y1, x2, y2, xLen, yLen, length, step, sX, sY, cX, cY;
        double slope;
        x1 = face[0];
        y1 = face[1];
        x2 = face[2];
        y2 = face[3];
        xLen = x2 - x1;
        yLen = y2 - y1;
        length = (int) Math.sqrt(Math.pow(xLen, 2) + Math.pow(yLen, 2));
        step = Math.abs(yLen != 0 ? xLen / yLen : xLen);
        step = (step < 3 ? 3 : step);
        slope = (yLen < 0 ? -1 : 1);
        int ROI[] = new int[length * length];
        sX = x1;
        sY = y1;
        for (int y = 0; y < length; y++) //extract face 'Rigion Of Interest'
        {
            cX = sX;
            cY = sY;
            for (int x = 0; x < length; x++) {
                if ((cX >= 0) && (cX < fWidth) && (cY < fHeight)) {
                    ROI[y * length + x] = grayPixels[cY * fWidth + cX];
                } else {
                    ROI[y * length + x] = ROI[(y - 1) * length + x];
                }
                cX++;
                if ((x + 1) % step == 0) {
                    cY = (int) (cY + slope);
                }
            }
            if ((y + 1) % step == 0) {
                sX = (int) (sX - slope);
            }
            sY++;
        }
        int ii[][] = new int[length][length];
        int s[][] = new int[length][length];
        ii = ImageProcessing.calculateIntegralImage(length, length, ROI, s, ii);
        Point candidates[] = findNoseBridge(length, ii); //find nose bridge candidates
        if (candidates != null) {
            int gradiants[] = calculateGradiants(candidates, length);
            int uMin1 = Integer.MAX_VALUE, uMin2 = Integer.MAX_VALUE,
                    lMin1 = Integer.MAX_VALUE, lMin2 = Integer.MAX_VALUE;
            int lMinGrad, lMinIndex, uMinGrad, uMinIndex, minGrad, minIndex;
            int uInd1 = 0, uInd2 = 0, lInd1 = 0, lInd2 = 0, gLength;
            gLength = gradiants.length;
            for (int i = 0; i < gLength / 4; i++) {
                if (gradiants[i] < uMin1) {
                    uMin1 = gradiants[i];
                    uInd1 = i;
                }
            }
            for (int i = gLength / 4; i < gLength / 2; i++) {
                if (gradiants[i] < uMin2) {
                    uMin2 = gradiants[i];
                    uInd2 = i;
                }
            }
            for (int i = gLength / 2; i < 3 * gLength / 4; i++) {
                if (gradiants[i] < lMin1) {
                    lMin1 = gradiants[i];
                    lInd1 = i;
                }
            }
            for (int i = 3 * gLength / 4; i < gLength; i++) {
                if (gradiants[i] < lMin2) {
                    lMin2 = gradiants[i];
                    lInd2 = i;
                }
            }
            if ((double) lMin2 / (double) lMin1 >= 0.5) {
                lMinGrad = lMin1;
                lMinIndex = lInd1;
            } else {
                lMinGrad = lMin2;
                lMinIndex = lInd2;
            }
            if ((double) uMin1 / (double) uMin2 >= 0.5) {
                uMinGrad = uMin2;
                uMinIndex = uInd2;
            } else {
                uMinGrad = uMin1;
                uMinIndex = uInd1;
            }
            if ((double) uMinGrad / (double) lMinGrad >= 0.5) {
                minGrad = lMinGrad;
                minIndex = lMinIndex;
            } else {
                minGrad = uMinGrad;
                minIndex = uMinIndex;
            }
            int start;
            if (minIndex >= gLength / 2) {
                if (minIndex < 3 * gLength / 4) {
                    start = gLength / 2;
                } else {
                    start = 3 * gLength / 4;
                }
            } else {
                start = 0;
            }
            int max = 0, index = 0;
            for (int i = start; i <= minIndex; i++) //after finding the smallest gradiant ( nose trills ) find the largest
            {
                if ((gradiants[i] > max) && (gradiants[i] != Integer.MAX_VALUE)) //gradiant above it ( nose tip )
                {
                    max = gradiants[i];
                    index = i;
                }
            }
            if (candidates[index] == null) {
                return null;
            }
            Point noseTip = new Point((int) candidates[index].getX(), index);
            slope = (double) yLen / (double) xLen;
            double angle = Math.atan(slope); //rotate ROI to it's original state
            double x = Math.cos(angle) * noseTip.getX() - Math.sin(angle) * noseTip.getY();
            double y = Math.sin(angle) * noseTip.getX() + Math.cos(angle) * noseTip.getY();
            x += face[0];
            y += face[1];
            noseTip.setLocation(x, y);
            return noseTip;
        }
        return null;
    }
}

