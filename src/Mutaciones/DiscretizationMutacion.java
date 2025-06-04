package Mutaciones;

import Discretizaciones.Discretization;
import Estructuras.Instancia;
import Estructuras.Solucion;
import GeometryBasics.Point;
import com.gurobi.gurobi.GRBException;

import java.util.Random;

public class DiscretizationMutacion implements Mutacion{

    @Override
    public Solucion mutate(Solucion s, Discretization disc, int sizeMut, boolean heuristicWeights) throws GRBException {
        int p = s.getInstancia().getP();
        Instancia instancia = s.getInstancia();
        Point[] fac = s.getFacilities().clone();


        //int sizeMut = 1;


        boolean feasible =false;

        while(!feasible){
            Point[] dis = disc.getRandomN(sizeMut);
            Random r = new Random();
            for (int i = 0; i < sizeMut; i++) {
                int indexSol = r.nextInt(p);
                fac[indexSol] = dis[i];
            }
            Solucion ret = new Solucion(fac,instancia);
            if(heuristicWeights) ret.adjustHeuristic();
            else ret.adjustWeightToLP();


            feasible = ret.isFeasible();
            if(feasible){
                return ret;
            }
            //System.out.println("*");
        }
        return null;
    }
}
