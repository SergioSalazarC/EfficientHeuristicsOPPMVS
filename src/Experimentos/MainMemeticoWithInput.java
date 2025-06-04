package Experimentos;

import AlgoritmosMejora.Memetic;
import BusquedasLocales.HookeJeeves8PLocalSearch;
import BusquedasLocales.LocalSearch;
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


public class MainMemeticoWithInput {

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

        int[] nvalues = {Integer.parseInt(args[0])};
        int[] pvalues = {Integer.parseInt(args[1])};

        //StringBuilder sb = new StringBuilder();
        File filval = new File("Results");
        File filtime = new File("Time");
        FileWriter fwv = new FileWriter(filval);
        FileWriter fwt = new FileWriter(filtime);


        for (int n: nvalues) {
            for (int p : pvalues) {
                for (int iter = 0; iter < 20; iter++) {

                    Instancia instancia = new Instancia(n,p);

                    double a = System.currentTimeMillis();

                    Discretization disc = new VoronoiPointsDiscretization(instancia);

                    Constructor random = new RandomFeasibleConstructor(disc,instancia);

                    PointEvaluationFunction f = new kNearestFunctionWithThreshold(Integer.parseInt(args[6]),instancia,instancia.getD()*Double.parseDouble(args[5]));
                    Constructor grasp = new GRASP_ConstructorLazy(Double.parseDouble(args[4]),disc,instancia,f);

                    LocalSearch ls = new HookeJeeves8PLocalSearch(1e-6,Double.parseDouble(args[11]));

                    int[] sizePoblacion = {Integer.parseInt(args[3]),50-Integer.parseInt(args[3])};
                    Constructor[] constructivos = {random, grasp};


                    Cruce cruce = new VotacionClusterizadaCruce();

                    Mutacion mutacion = new DiscretizationMutacion();


                    double aux = p*Double.parseDouble(args[9])/100;
                    int sizeMut = (int) Math.max(aux,1);

                    Memetic memetico = new Memetic(sizePoblacion, constructivos, instancia,disc,ls, cruce, mutacion, sizeMut, false );


                    for (int i = 0; i < Integer.parseInt(args[2]); i++) {
                        memetico.newGen(Double.parseDouble(args[7]),Double.parseDouble(args[8]),Double.parseDouble(args[10]), false);
                    }


                    memetico.hardAdjustToLP();

                    double b = System.currentTimeMillis();
                    double time = (b-a)/1000;

                    double sol = memetico.getBestSolucion().eval();

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
