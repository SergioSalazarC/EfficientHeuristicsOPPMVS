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


public class MainMemetico_ReducedLS {
    public static void main(String[] args) throws GRBException {

        Point oo = new Point(0,0);
        Point ii = new Point(10,10);

        int[] nvalues = {100,200,500};
        int[] pvalues = {2,3,4,5,10,15,20};

        StringBuilder sb = new StringBuilder();

        for (int q = 0; q < 3; q++) {
            for (int r = 0; r < 7; r++) {

                int n =nvalues[q];
                int p = pvalues[r];


                Instancia instancia = new Instancia(n,p);

                double a = System.currentTimeMillis();

                Discretization disc = new VoronoiPointsDiscretization(instancia);

                Constructor random = new RandomFeasibleConstructor(disc,instancia);

                PointEvaluationFunction f = new kNearestFunctionWithThreshold(n/p,instancia,instancia.getD()*0.75);
                Constructor grasp = new GRASP_ConstructorLazy(0.67,disc,instancia,f);

                LocalSearch ls = new MagneticLocalSearch();

                int[] sizePoblacion = {25,25};
                Constructor[] constructivos = {random, grasp};

                Cruce cruce = new VotacionClusterizadaCruce();

                Mutacion mutacion = new DiscretizationMutacion();

                int sizeMut = Math.max(p/4,1);

                Memetic memetico = new Memetic(sizePoblacion, constructivos, instancia,disc,ls, cruce, mutacion, sizeMut, false );




                for (int i = 0; i < 200; i++) {
                    memetico.newGenReducingLS(0.6,0.1,2.0/16.0,p/2,(p*1000)/n, false);
                    //System.out.println(memetico.getBestSolucion().eval_nocheck());
                    //System.out.println();
                    //memetico.showSolutions();
                }
                System.out.println(memetico.getBestSolucion().eval());
                //int usos= MagneticLocalSearch.cuantosUsos();
                //System.out.println("Para n:"+n+" y p:"+p+"se han realizado "+usos+" iteraciones de la busqueda local");
                //System.out.println();

                double b = System.currentTimeMillis();
                sb.append((b-a)/1000).append("\n");


            }
        }




        System.out.println(sb);

        System.out.println();

        MagneticLocalSearch.cuantosUsos();




    }
}
