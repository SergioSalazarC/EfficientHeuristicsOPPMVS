package Experimentos;

import AlgoritmosMejora.Memetic;
import BusquedasLocales.LocalSearch;
import BusquedasLocales.MagneticLocalSearch;
import Constructivos.Constructor;
import Constructivos.GRASP_ConstructorLazy;
import Constructivos.RandomFeasibleConstructor;
import Cruces.Cruce;
import Cruces.VotacionClusterizadaCruce;
import Discretizaciones.Discretization;
import Discretizaciones.VoronoiPointsDiscretization;
import Estructuras.Instancia;
import FuncionesPuntos.PointEvaluationFunction;
import FuncionesPuntos.kNearestFunctionWithThreshold;
import GeometryBasics.Point;
import Mutaciones.DiscretizationMutacion;
import Mutaciones.Mutacion;
import com.gurobi.gurobi.GRBException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class MemeticoLogsGraficas {

    public static int iter(long n, long p){
        double a= 1101.2975685168217;
        double b= 3339023.895065711;
        double d= -14534.74829259977;
        double e= 127.88103165371754;
        double f= -47138.636859983344;

        double x = a*p*p + b*p/n + d*p + e*n +f;
        return (int) Math.round(x);
    }
    public static void main(String[] args) throws GRBException, IOException {

        Point oo = new Point(0,0);
        Point ii = new Point(10,10);

        int[] nvalues = {100,200,500};
        int[] pvalues = {2,3,4,5,10,15,20};

        StringBuilder sb = new StringBuilder();



        for (int n: nvalues) {
            for (int p : pvalues) {

                File fil = new File("CambioTorneo/n_"+n+"_p_"+p);
                FileWriter fw = new FileWriter(fil);

                StringBuilder best = new StringBuilder();
                StringBuilder avg = new StringBuilder();


                Instancia instancia = new Instancia(n,p);

                double a = System.currentTimeMillis();

                Discretization disc = new VoronoiPointsDiscretization(instancia);

                Constructor random = new RandomFeasibleConstructor(disc,instancia);

                PointEvaluationFunction f = new kNearestFunctionWithThreshold(1,instancia,instancia.getD()*0.75);
                Constructor grasp = new GRASP_ConstructorLazy(0.25,disc,instancia,f);

                LocalSearch ls = new MagneticLocalSearch();

                int[] sizePoblacion = {25,25};
                Constructor[] constructivos = {random, grasp};

                Cruce cruce = new VotacionClusterizadaCruce();

                Mutacion mutacion = new DiscretizationMutacion();

                int sizeMut = Math.max(p/4,1);

                Memetic memetico = new Memetic(sizePoblacion, constructivos, instancia,disc,ls, cruce, mutacion, sizeMut, false );




                for (int i = 0; i < 200; i++) {
                    memetico.newGen(0.6,0.1,1.0/16.0, false);
                    //System.out.println(i);
                    best.append(memetico.bestSol()).append("\n");
                    avg.append(memetico.avgSol()).append("\n");
                }
                System.out.println(memetico.getBestSolucion().eval());

                double b = System.currentTimeMillis();
                sb.append((b-a)/1000).append("\n");
                fw.write(best.toString());
                fw.write("\n");
                fw.write(avg.toString());
                fw.flush();
                fw.close();


            }
        }


        System.out.println(sb);



    }
}
