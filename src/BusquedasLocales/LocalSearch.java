package BusquedasLocales;

import com.gurobi.gurobi.*;
import Estructuras.*;

public interface LocalSearch {
    Solucion improveSolution(Solucion s0) throws GRBException;

    Solucion improveSolution(Solucion s0, int size) throws GRBException;

    Solucion improveSolution(Solucion s0, int size, int iter) throws GRBException;
}
