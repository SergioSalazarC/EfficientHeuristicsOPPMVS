package Constructivos;

import Estructuras.*;
import Discretizaciones.*;
import GeometryBasics.Point;
import com.gurobi.gurobi.GRBException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class RandomConstructor implements Constructor {

    ArrayList<Point> puntos;
    int p;

    Instancia instancia;

    int sizeCand;

    public int[] gen(int max) {

        int[] array = new int[p];
        HashSet<Integer> numerosGenerados = new HashSet<>();
        Random random = new Random();

        for (int i = 0; i < p; i++) {
            int numeroGenerado;
            do {
                numeroGenerado = random.nextInt(max);
            } while (numerosGenerados.contains(numeroGenerado));

            array[i] = numeroGenerado;
            numerosGenerados.add(numeroGenerado);
        }

        return array;
    }



    public RandomConstructor(Discretization disc, Instancia instancia) {
        this.instancia = instancia;
        p = instancia.getP();
        puntos = disc.getPuntos();
        sizeCand = puntos.size();

    }

    @Override
    public Solucion buildSolution(boolean heuristicWeights) throws GRBException {
        Point[] facilities = new Point[p];
        int[] index = gen(sizeCand);
        for (int i = 0; i < p; i++) {
            facilities[i] = puntos.get(index[i]);
        }

        Solucion s = new Solucion(facilities,instancia);
        if(heuristicWeights) s.adjustHeuristic();
        else s.adjustWeightToLP();
        return s;
    }
}
