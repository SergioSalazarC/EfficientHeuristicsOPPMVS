package Regeneraciones;

import Constructivos.Constructor;
import Estructuras.Solucion;
import com.gurobi.gurobi.GRBException;

import java.util.ArrayList;

public interface Regenerador {
    Solucion[] regenerarPoblacion(ArrayList<Double> historico, int[] volumenPoblacion, Constructor[] constructivos, double similitud, int sizeHistorico) throws GRBException;

}
