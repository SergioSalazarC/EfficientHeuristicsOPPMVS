package Experimentos;

import GeometryBasics.Point;
import com.gurobi.gurobi.*;
import Estructuras.*;
import AlgoritmosMejora.*;
import BusquedasLocales.*;
import Discretizaciones.*;
import Constructivos.*;
import FuncionesPuntos.*;


public class Main {
    public static void main(String[] args) throws GRBException {

        Point oo = new Point(0,0);
        Point ii = new Point(10,10);

        int[] nvalues = {100,200,500};
        int[] pvalues = {2,3,4,5,10,15,20};

        StringBuilder sb = new StringBuilder();
        StringBuilder scores = new StringBuilder();

        for (int q = 0; q < 3; q++) {
            for (int r = 0; r < 7; r++) {

                int n = nvalues[q];
                int p = pvalues[r];

                Instancia instancia = new Instancia(n,p);

                double a = System.currentTimeMillis();

                ScatterSearch ss = new ScatterSearch(4,4,new MagneticLocalSearch(),instancia);


                //Discretization disc = new VoronoiEdgeDiscretization(instancia,0.1);

                //Discretization disc = new UniformVoronoiDiscretization(instancia,oo,ii,0.1);

                Discretization disc = new VoronoiPointsDiscretization(instancia);

                PointEvaluationFunction f = new kNearestFunctionWithThreshold(n,instancia,instancia.getD()*0.75);

                GRASP_ConstructorLazy constructor = new GRASP_ConstructorLazy(0.67,disc,instancia,f);

                ss.generationStart(constructor,500);

                for (int i = 0; i < 15; i++) {
                    ss.newGeneration();
                }
                ss.getBestSolucion().printKalczinskyFormat();


                //System.out.println(ss.getBestSolucion().eval_nocheck());

                double b = System.currentTimeMillis();
                sb.append((b-a)/1000).append("\n");


            }
        }

        //System.out.println(scores);

        System.out.println(sb);



    }
}
