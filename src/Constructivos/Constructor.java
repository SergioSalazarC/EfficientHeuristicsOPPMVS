package Constructivos;

import com.gurobi.gurobi.*;

import Estructuras.*;

public interface Constructor {

    public Solucion buildSolution(boolean heuristicWeights) throws GRBException;

}
