package Experimentos;

import AlgoritmosMejora.Memetic;
import BusquedasLocales.HookeJeeves8PLocalSearch;
import BusquedasLocales.HookeJeevesBasicoLocalSearch;
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


public class MainMemetico {

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

        //int[] nvalues = {100};
        //int[] pvalues = {2};

        int[] nvalues = {100};
        int[] pvalues = {2};

        //StringBuilder sb = new StringBuilder();
        File filval = new File("Results");
        File filtime = new File("Time");
        FileWriter fwv = new FileWriter(filval);
        FileWriter fwt = new FileWriter(filtime);


        for (int n: nvalues) {
            for (int p : pvalues) {
                for (int iter = 0; iter < 10; iter++) {

                    Instancia instancia = new Instancia(n,p);

                    double a = System.currentTimeMillis();

                    Discretization disc = new VoronoiPointsDiscretization(instancia);

                    Constructor random = new RandomFeasibleConstructor(disc,instancia);

                    PointEvaluationFunction f = new kNearestFunctionWithThreshold(66,instancia,instancia.getD()*0.60);
                    Constructor grasp = new GRASP_ConstructorLazy(0.34,disc,instancia,f);

                    LocalSearch ls = new HookeJeeves8PLocalSearch(1e-6,0.62);

                    int[] sizePoblacion = {30,20};
                    Constructor[] constructivos = {random, grasp};

                    Cruce cruce = new VotacionClusterizadaCruce();

                    Mutacion mutacion = new DiscretizationMutacion();

                    int sizeMut = (int) Math.max(p*0.14,1);

                    Memetic memetico = new Memetic(sizePoblacion, constructivos, instancia,disc,ls, cruce, mutacion, sizeMut, true );


                    for (int i = 0; i < 1000; i++) {
                        memetico.newGen(0.51,0.2,0.188, true);
                    }

                    memetico.hardAdjustToLP();

                    double b = System.currentTimeMillis();
                    double time = (b-a)/1000;

                    double sol = memetico.getBestSolucion().eval();
                    //memetico.getBestSolucion().printKalczinskyFormat();
                    System.out.println();

                    System.out.print(sol+" ");
                    System.out.println(time);

                    fwv.write(sol+",");

                    fwt.write(time+",");
                }
                System.out.println();
                fwv.write("\n");
                fwv.flush();
                fwt.write("\n");
                fwt.flush();
            }
        }
        fwv.close();
        fwt.close();

    }
}
