package Mutaciones;

import Discretizaciones.Discretization;
import Estructuras.Solucion;
import com.gurobi.gurobi.GRBException;

public interface Mutacion {
    public Solucion mutate(Solucion s, Discretization disc, int sizeMut, boolean heuristicWeights) throws GRBException;
}
