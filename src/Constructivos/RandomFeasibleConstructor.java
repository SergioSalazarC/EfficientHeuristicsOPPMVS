package Constructivos;

import Discretizaciones.Discretization;
import Estructuras.Instancia;
import Estructuras.Solucion;
import GeometryBasics.Point;
import com.gurobi.gurobi.GRBException;

import java.util.*;

public class RandomFeasibleConstructor implements Constructor{
    ArrayList<Point> puntos;
    int p;

    Instancia instancia;

    int sizeCand;

    public RandomFeasibleConstructor(Discretization disc, Instancia instancia) {
        this.instancia = instancia;
        p = instancia.getP();
        puntos = disc.getPuntos();
        sizeCand = puntos.size();

    }

    @Override
    public Solucion buildSolution(boolean heuristicWeights) throws GRBException {
        Point[] facilities = new Point[p];
        LinkedList<Point> posibles = new LinkedList<>(puntos);
        Collections.shuffle(posibles);

        double distAcumulado = 0;

        double D = instancia.getD();


        for (int i = 0; i < p; i++) {
            if(i==p-1){
                //Añadir el último punto debe ser factible
                boolean flag = false;
                for(Point q:posibles){
                    if(distAcumulado+dist2Near(q)>= D*D*p ){
                        flag = true;
                        facilities[i] = q;
                        break;
                    }
                }

                if(!flag){
                    i=0;
                    facilities = new Point[p];
                    posibles = new LinkedList<>(puntos);
                    Collections.shuffle(posibles);
                    distAcumulado = 0;
                }

            }
            else{
                //Añadir punto
                Point add = posibles.removeFirst();
                distAcumulado += dist2Near(add);
                facilities[i] = add;
            }
        }

        Solucion ret = new Solucion(facilities,instancia);
        if(heuristicWeights) ret.adjustHeuristic();
        else ret.adjustWeightToLP();

        return ret;

    }

    public double dist2Near(Point q){
        double min = Double.MAX_VALUE;
        for(Point site : instancia.getComunities()){
            min = Math.min(min,site.dist2(q));
        }
        return min;
    }



}
