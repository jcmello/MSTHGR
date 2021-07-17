package MSTHGR;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Reconstruction;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.strel.DiskStrel;
import java.util.ArrayList;

/**
 *
 * @author Julio César Mello Román
 * @author Facultad Politécnica, Universidad Nacional de Asunción
 * @version 2021
 */
public class MultiscaleMathematicalMorphology {
    
    /**
     *  @article{Rom_n_2021,
            doi = {10.3390/s21093110},
            year = 2021,
            month = {apr},
            publisher = {{MDPI} {AG}},
            volume = {21},
            number = {9},
            pages = {3110},
            author = {Julio C{\'{e}}sar Mello Rom{\'{a}}n and Vicente R. Fretes and Carlos G. Adorno and Ricardo Gariba Silva and Jos{\'{e}} Luis V{\'{a}}zquez Noguera and Horacio Legal-Ayala and Jorge Daniel Mello-Rom{\'{a}}n and Ricardo Daniel Escobar Torres and Jacques Facon},
            title = {Panoramic Dental Radiography Image Enhancement Using Multiscale Mathematical Morphology},
            journal = {Sensors}
        }
     */
    public static ImageProcessor MSTHGR(String path, int radio, int iter){
        ImagePlus imp = IJ.openImage(path);
        IJ.run(imp, "8-bit", "");
        ImagePlus imp2 = imp.duplicate();
        int M = imp2.getWidth();
        int N = imp2.getHeight();
        ImageProcessor ip = imp2.getProcessor();
        
        //Inicio del algoritmo
        int k = 0;
        int r = radio;
        Strel H = DiskStrel.fromRadius(r);
        ImageProcessor wth = Morphology.whiteTopHat(ip, H);
        ImageProcessor bth = Morphology.blackTopHat(ip, H);
        ImageProcessor eRosion = Morphology.erosion(ip, H); 
        ImageProcessor aReconst = Reconstruction.reconstructByDilation(eRosion, ip, 8);  //Apertura por reconstrucción
        ImageProcessor RWTH = resta(ip,aReconst);
        ImageProcessor dIlation = Morphology.dilation(ip, H);
        ImageProcessor cReconst = Reconstruction.reconstructByErosion(dIlation, ip, 8); //Cierre por reconstruccion
        ImageProcessor RBTH = resta(cReconst,ip);
        RWTH = Reconstruction.reconstructByDilation(RWTH, wth, 8);
        RBTH = Reconstruction.reconstructByDilation(RBTH, bth, 8);

        ArrayList<ImageProcessor> matrizWTH = new ArrayList<>();
        ArrayList<ImageProcessor> matrizBTH = new ArrayList<>();
        matrizWTH.add(RWTH);
        matrizBTH.add(RBTH);
        ArrayList<ImageProcessor> matrizDRWTH = new ArrayList<>();
        ArrayList<ImageProcessor> matrizDRBTH = new ArrayList<>();
        ImageProcessor dRWTH = ip.createProcessor(M, N);
        ImageProcessor dRBTH = ip.createProcessor(M, N);
        //
        if(iter>1){
            for(k=1; k<iter; k++){
                r = r + 1;
                H = DiskStrel.fromRadius(r);
                wth = Morphology.whiteTopHat(ip, H);
                bth = Morphology.blackTopHat(ip, H);
                eRosion = Morphology.erosion(ip, H); 
                aReconst = Reconstruction.reconstructByDilation(eRosion, ip, 8);  //Apertura por reconstrucción
                RWTH = resta(ip,aReconst);
                dIlation = Morphology.dilation(ip, H);
                cReconst = Reconstruction.reconstructByErosion(dIlation, ip, 8); //Cierre por reconstruccion
                RBTH = resta(cReconst,ip);
                RWTH = Reconstruction.reconstructByDilation(RWTH, wth, 8);
                RBTH = Reconstruction.reconstructByDilation(RBTH, bth, 8);
                matrizWTH.add(RWTH);
                matrizBTH.add(RBTH);
                dRWTH = resta(matrizWTH.get(k), matrizWTH.get(k-1));
                matrizDRWTH.add(dRWTH);
                dRBTH = resta(matrizBTH.get(k), matrizBTH.get(k-1));
                matrizDRBTH.add(dRBTH);
            }
        }

        //Propuesta inspirada en el algoritmo de BAI
        //Calculo de los maximos
        ImageProcessor m_WTH = matrizWTH.get(0);
        ImageProcessor m_DRWTH = matrizDRWTH.get(0);
        ImageProcessor m_BTH = matrizBTH.get(0);
        ImageProcessor m_DRBTH = matrizDRBTH.get(0);

        for(int w=1;w<iter;w++){
            m_WTH = maxValueMatriz(matrizWTH.get(w), m_WTH);
            m_BTH = maxValueMatriz(matrizBTH.get(w), m_BTH);
        }
        for(int w=1;w<iter-1;w++){
            m_DRWTH = maxValueMatriz(matrizDRWTH.get(w), m_DRWTH);
            m_DRBTH = maxValueMatriz(matrizDRBTH.get(w), m_DRBTH);
        }
        
        ImageProcessor IE = enhancement(ip,m_WTH,m_DRWTH,m_BTH,m_DRBTH);

        return IE;
    
    }
    
    //**********************************************************************//
    //**********************************************************************//
    //**********************************************************************//
    private static ImageProcessor resta(ImageProcessor f1, ImageProcessor f2) {
        int M = f1.getWidth();
        int N = f1.getHeight();
        ImageProcessor res = f1.createProcessor(M, N);
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                int val = f1.getPixel(i, j) - f2.getPixel(i, j);
                if(val<0) val=0;
                res.putPixel(i, j, val);
            }
        }
        return res;
    }
    
    private static ImageProcessor maxValueMatriz(ImageProcessor f1, ImageProcessor f2) {
        int M = f1.getWidth();
        int N = f1.getHeight();
        ImageProcessor res = f1.createProcessor(M, N);
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                int val = Math.max(f1.getPixel(i, j), f2.getPixel(i, j));
                if(val<0) val=0;
                res.putPixel(i, j, val);
            }
        }
        return res;
    }
    
    private static ImageProcessor enhancement(ImageProcessor f1, ImageProcessor f2, ImageProcessor f3, ImageProcessor f4, ImageProcessor f5) {
        int M = f1.getWidth();
        int N = f1.getHeight();
        ImageProcessor res = f1.createProcessor(M, N);
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                int val = f1.getPixel(i, j) + f2.getPixel(i, j) + f3.getPixel(i, j) - f4.getPixel(i, j) - f5.getPixel(i, j);
                if(val<0) val=0;
                if(val>255) val=255;
                res.putPixel(i, j, val);
            }
        }
        return res;
    }
    
}
