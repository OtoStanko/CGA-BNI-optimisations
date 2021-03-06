/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bni;

//import static bni.ARACNE.ARACNE_DIR;
import static bni.InferBN.DIR_BASE;
//import static bni.InferBN.DREAM3_SIZE;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
//import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author colin_PC2
 */
public class DataSumm {
    public static final String DIR_RESULTS = DIR_BASE + "Inference\\results\\";
    
    public static void analyze_TrialFolder(File folder, String match, Metric mtr) {
//        File folder = new File(path);
        File[] subFolders = folder.listFiles();

        assert subFolders != null;
        for (File subFolder : subFolders) {
            if (!subFolder.isDirectory()) continue;

            if (subFolder.getName().contains(match)) {
                analyze_ResultFolder(subFolder, mtr);
            }
        }
    }
    
    public static void analyze_ResultFolder(File folder, Metric mtr) {
        //DREAM3_SIZE_DREAM3_SPECIES folder
        File[] files = folder.listFiles();

        assert files != null;
        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }

            if (file.getName().contains("_net.csv")) {
                String[][] data = Utils.loadTextFile(file.getPath(), ",", false);

                assert data != null;
                mtr.add_Dynamic(Double.parseDouble(data[1][2]), Double.parseDouble(data[1][0]));
                continue;
            }

            if (file.getName().contains("_struct_avg.csv")) {
                String[][] data = Utils.loadTextFile(file.getPath(), ",", false);
                assert data != null;
                int lastLine = data[0].length - 1;

                double prec = Double.parseDouble(data[1][lastLine]);
                double tpr = Double.parseDouble(data[2][lastLine]);

                mtr.add_Structure(prec,
                        tpr,
                        Double.parseDouble(data[3][lastLine]));

                if (prec < 0.5 || tpr < 0.3) {
                    System.out.println("Weak result = " + folder.getName());
                }
            }
        } 
    }
    
    public static void main(String[] args) throws FileNotFoundException {
        String[] methods = {
//            "GA", 
//            "GA_10_19", "GA_20_29", "GA_30_49", 
//            "GENIE3", "ARACNE", "BC3NET", 
            "BTR"
        };
        
        String[] ecoliSize = new String[]{"Ecoli_large", "Size1424_Ecoli"};
        
        int NO_SIZES = 10;
        String[] RBN_sizes = new String[NO_SIZES];
        
        for(int i = 0; i < NO_SIZES; i++) {
            RBN_sizes[i] = "Size" + 10 * (i + 1);
        }

        for (String method : methods) {
            System.out.println(method + " summarizing ...");

            if (InferBN.DATABASE == InferBN.DREAM3) {
                analyze(DIR_RESULTS + "DREAM3\\Parallel_noSign\\", method, ",", InferBN.DREAM3_SIZE);
            } else if (InferBN.DATABASE == InferBN.GNW) {
                analyze(DIR_RESULTS + "GNW\\", method, ",", InferBN.GNW_SIZE);
            } else if (InferBN.DATABASE == InferBN.ECOLI) {
                analyze(DIR_RESULTS + "ECOLI\\", method + "_ECOLI", ",", ecoliSize);
            } else if (InferBN.DATABASE == InferBN.RBN) {
                analyze(DIR_RESULTS + "RBN\\", method + "_RBN", ",", RBN_sizes);
            }
        }
    }
    
    public static void analyze(String dir, String method, String del, String[] sizes) throws FileNotFoundException {        
        String path = dir + method + "\\";
        
        PrintWriter pw = new PrintWriter(new FileOutputStream(path + method + ".csv"),true);
        pw.print("ID" + del + "Dynamic Accuracy" + del + "Precision" + del + "Recall" 
                + del + "Structure Accuracy" + del + "Time" + del);
        pw.println("D_acc std" + del + "Prec std" + del + "Recall std" 
                + del + "S_acc std" + del + "Time std");
        
        File folder = new File(path);
        File[] trialFolders = folder.listFiles();

        for (String size : sizes) {
            String matchName = size + "_";
            Metric mtr = new Metric(matchName);

            assert trialFolders != null;
            for (File trialFolder : trialFolders) {
                if (!trialFolder.isDirectory()) continue;

                analyze_TrialFolder(trialFolder, matchName, mtr);
            }

            mtr.out(pw, del);
        }
        
        pw.close();
    }
}

class Metric {
    ArrayList<Double> Precision;
    ArrayList<Double> TPR;
    ArrayList<Double> S_Accuracy;
    ArrayList<Double> D_Accuracy;
    
    ArrayList<Double> searchTime;
    
    String matchName;
    
    public Metric(String matchName) {
        this.Precision = new ArrayList<>();
        this.TPR = new ArrayList<>();
        this.S_Accuracy = new ArrayList<>();
        
        this.D_Accuracy = new ArrayList<>();
        this.searchTime = new ArrayList<>();
        
        this.matchName = matchName;
    }
    
    public void add_Dynamic(double acc, double time) {
        this.D_Accuracy.add(acc);
        this.searchTime.add(time);
    }
    
    public void add_Structure(double prec, double tpr, double acc) {
        this.S_Accuracy.add(acc);
        this.Precision.add(prec);
        this.TPR.add(tpr);
    }
    
    private double[] summ(List<Double> metric) {
        double[] dvalues = Utils.toArray(metric);
        double mean = Utils.mean(dvalues);
        double std = Utils.std(dvalues, mean);
        
        return new double[] {mean, std};
    }
    
    public void out(PrintWriter pw, String del) {
        System.out.println(this.matchName + " = " + this.D_Accuracy.size());
        
        List<List<Double>> metrics = new ArrayList<>();
        metrics.add(this.D_Accuracy);
        metrics.add(this.Precision);
        metrics.add(this.TPR);
        metrics.add(this.S_Accuracy);
        metrics.add(this.searchTime);
        
        double[][] results = new double[metrics.size()][];
        
        for (int i = 0; i < metrics.size(); i++) {
            results[i] = summ(metrics.get(i));
        }
        
        pw.print(this.matchName);
        
        for (int mt = 0; mt < 2; mt++) {
            for (int i = 0; i < metrics.size(); i++) {
                pw.print(del + results[i][mt]);
            }
        }
        pw.println();
    }
}