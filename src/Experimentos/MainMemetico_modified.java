package Experimentos;



import AlgoritmosMejora.Memetic_Modified;
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


public class MainMemetico_modified {


    public static void main(String[] args) throws GRBException, IOException {

        Point oo = new Point(0,0);
        Point ii = new Point(10,10);

        int[] nvalues = {100};
        int[] pvalues = {2};

        //int[] nvalues = {100,200,500};
        //int[] pvalues = {2,3,4,5,10,15,20};

        //StringBuilder sb = new StringBuilder();
        File filval = new File("Results");
        File filtime = new File("Time");
        FileWriter fwv = new FileWriter(filval);
        FileWriter fwt = new FileWriter(filtime);


        for (int n: nvalues) {
            for (int p : pvalues) {
                for (int iter = 0; iter < 1; iter++) {

                    Instancia instancia = new Instancia(n,p);

                    double a = System.currentTimeMillis();

                    Discretization disc = new VoronoiPointsDiscretization(instancia);

                    Constructor random = new RandomFeasibleConstructor(disc,instancia);

                    PointEvaluationFunction f = new kNearestFunctionWithThreshold(40,instancia,instancia.getD()*0.886);
                    Constructor grasp = new GRASP_ConstructorLazy(0.617,disc,instancia,f);

                    LocalSearch ls = new HookeJeeves8PLocalSearch(1e-6,0.615);

                    int[] sizePoblacion = {14,36};
                    Constructor[] constructivos = {random, grasp};

                    Cruce cruce = new VotacionClusterizadaCruce();

                    Mutacion mutacion = new DiscretizationMutacion();

                    int sizeMut = (int) Math.max(p*0.931,1);

                    Memetic_Modified memetico = new Memetic_Modified(sizePoblacion, constructivos, instancia,disc,ls, cruce, mutacion, sizeMut, false);


                    for (int i = 0; i < 1000; i++) {
                        memetico.newGen(0.792,0.158,0.118, false);
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
