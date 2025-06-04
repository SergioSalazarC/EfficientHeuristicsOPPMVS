package Reemplazo;

import Estructuras.Solucion;

import java.util.LinkedList;

public interface Reemplazo {

    Solucion[] reemplazo(LinkedList<Solucion> candidatos, int volumenTotalPoblacion);

}
