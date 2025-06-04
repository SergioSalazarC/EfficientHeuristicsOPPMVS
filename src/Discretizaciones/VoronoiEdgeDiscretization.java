package Discretizaciones;

import GeometryBasics.Point;
import Voronoi.Voronoi;
import Estructuras.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VoronoiEdgeDiscretization implements Discretization {

    ArrayList<Point> puntos;


    public VoronoiEdgeDiscretization(Instancia instancia, double beta) {

        Point[] com = instancia.getComunities();
        ArrayList<Point> sites = new ArrayList<>(List.of(com));

        Voronoi v = new Voronoi(sites,beta);

        puntos = new ArrayList<>(v.getEdgeAndVPoints());


    }


    @Override
    public Point[] getRandomN(int n){
        Collections.shuffle(puntos);
        Point[] ret = new Point[n];
        for (int i = 0; i < n; i++) {
            ret[i]=puntos.get(i);
        }
        return ret;
    }

    @Override
    public ArrayList<Point> getPuntos() {
        return puntos;
    }
}
