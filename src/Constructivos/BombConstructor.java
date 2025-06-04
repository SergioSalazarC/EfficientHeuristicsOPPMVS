package Constructivos;

import Estructuras.*;
import Discretizaciones.*;
import GeometryBasics.Point;
import com.gurobi.gurobi.GRBException;

import java.util.ArrayList;

public class BombConstructor implements Constructor {

    int[] index;
    ArrayList<Point> puntos;
    int p;

    Instancia instancia;

    int sizeCand;



    public BombConstructor(Discretization disc, Instancia instancia) {
        this.instancia = instancia;
        p = instancia.getP();
        puntos = disc.getPuntos();
        index  = new int[p];
        sizeCand = puntos.size();
        for (int i = 0; i < p; i++) {
            index[i] = i;
        }

    }

    @Override
    public Solucion buildSolution(boolean heuristicWeights) throws GRBException {
        Point[] facilities = new Point[p];
        for (int i = 0; i < p; i++) {
            facilities[i] = puntos.get(index[i]);
        }


        for (int i = p-1; i >= 0 ; i--) {
            int tras = p - i ;
            if(index[i]+tras >= sizeCand){
                continue;
            }
            else{
                index[i]++;
                for (int j = i+1; j < p; j++) {
                    index[j] = index[j-1]+1;
                }
                break;
            }
        }

        Solucion s = new Solucion(facilities,instancia);
        if(heuristicWeights) s.adjustHeuristic();
        else s.adjustWeightToLP();
        return s;
    }
}
