package Experimentos;

import AlgoritmosMejora.*;
import BusquedasLocales.*;
import Constructivos.*;
import Cruces.Cruce;
import Cruces.VotacionClusterizadaCruce;
import Discretizaciones.*;
import Estructuras.*;
import FuncionesPuntos.*;
import GeometryBasics.*;
import Mutaciones.DiscretizationMutacion;
import Mutaciones.Mutacion;
import com.gurobi.gurobi.GRBException;

import java.util.Random;


public class ExperimentoIteracionBusquedaLocal {
    public static void main(String[] args) throws GRBException {

        System.out.println("[");
        for (int t = 0; t < 1000; t++) {
            Random r = new Random();
            int n = r.nextInt(80,520);
            int p = r.nextInt(2,21);

            Instancia instancia = new Instancia(n,p);

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
                memetico.newGen(0.6,0.1,0.0625, false);
            }

            int usos= MagneticLocalSearch.cuantosUsos();
            System.out.print("("+n+","+p+","+usos+")");
            if(t!=999) System.out.println(",");
            else System.out.println();

        }

        System.out.println("]");
    }
}
