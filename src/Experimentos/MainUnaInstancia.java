package Experimentos;

import Constructivos.*;
import Estructuras.*;
import AlgoritmosMejora.*;
import Discretizaciones.*;
import BusquedasLocales.*;
import FuncionesPuntos.*;
import GeometryBasics.Point;
import com.gurobi.gurobi.*;


public class MainUnaInstancia {
    public static void main(String[] args) throws GRBException {

        Point oo = new Point(0,0);
        Point ii = new Point(10,10);

        StringBuilder sb = new StringBuilder();



        int n = 100;
        int p = 15;

        Instancia instancia = new Instancia(n,p);

        double a = System.currentTimeMillis();

        ScatterSearch ss = new ScatterSearch(4,4,new MagneticLocalSearch(),instancia);


        //Discretization disc = new VoronoiEdgeDiscretization(instancia,0.1);

        //Discretization disc = new UniformVoronoiDiscretization(instancia,oo,ii,0.1);

        Discretization disc = new VoronoiPointsDiscretization(instancia);

        PointEvaluationFunction f = new kNearestFunctionWithThreshold(n,instancia,instancia.getD()*0.75);

        GRASP_ConstructorLazy constructor = new GRASP_ConstructorLazy(0.67,disc,instancia,f);

        ss.generationStart(constructor,500);

        for (int i = 0; i < 30; i++) {
            ss.newGeneration();
        }
        ss.getBestSolucion().printKalczinskyFormat();


        System.out.println(ss.getBestSolucion().eval());

        double b = System.currentTimeMillis();
        sb.append((b-a)/1000).append("\n");




        System.out.println(sb);



    }
}
