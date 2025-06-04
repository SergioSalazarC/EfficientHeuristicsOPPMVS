package Cruces;

import Estructuras.Solucion;
import com.gurobi.gurobi.GRBException;

public interface Cruce {
    public Solucion cross(Solucion Solucion1, Solucion Solucion2, boolean heuristicWeights) throws GRBException;
}
