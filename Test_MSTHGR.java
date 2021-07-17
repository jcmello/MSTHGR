package MSTHGR;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.io.File;

/**
 *
 * @author Julio César Mello Román
 * @author Facultad Politécnica, Universidad Nacional de Asunción
 * @version 2021
 */
public class Test_MSTHGR {
    
    public static void main(String[] args){
        String pathBase=System.getProperty("user.dir")+File.separator+"src"+File.separator+"MSTHGR"+File.separator;
        String path = pathBase+"67.jpg";
        ImagePlus imp = IJ.openImage(path);
        imp.show();
        ImagePlus imp2 = imp.duplicate();
        int M = imp2.getWidth();
        int N = imp2.getHeight();
        ImageProcessor ip = imp2.getProcessor();
        int[][] imageO = new int[M][N];

        for (int j = 0; j <N; j++) {
            for (int i = 0; i <M; i++) {
                int p = ip.getPixel(i, j);
                imageO[i][j] = p;

            }
        }

        //MSTHGR
        int[][] image = new int[M][N];
        int rad = 1;
        int iter = 7;
        ImageProcessor ip2 = MultiscaleMathematicalMorphology.MSTHGR(path, rad, iter);
        for (int j = 0; j <N; j++) {
            for (int i = 0; i <M; i++) {
                int p = ip2.getPixel(i, j);
                image[i][j] = p;

            }
        }
        for (int i = 0; i <M; i++) {
            for (int j = 0; j <N; j++) {
                int p = image[i][j];
                ip.putPixel(i, j, p);
            }
        }
        imp2.show();   
    }
}
