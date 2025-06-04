package Discretizaciones;

import GeometryBasics.Point;
import Voronoi.Voronoi;
import Estructuras.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UniformVoronoiDiscretization implements Discretization {

    ArrayList<Point> puntos;


    public UniformVoronoiDiscretization(Instancia instancia, Point minizq, Point maxder, double beta) {

        Point[] com = instancia.getComunities();
        ArrayList<Point> sites = new ArrayList<>(List.of(com));

        Voronoi v = new Voronoi(sites,1000);

        puntos = new ArrayList<>(v.getvPoints());

        double left = minizq.getX();
        double right = maxder.getX();
        double bottom = minizq.getY();
        double top = maxder.getY();

        for (double i = left; i <= right; i += beta) {
            for (double j = bottom; j <= top; j += beta) {
                puntos.add(new Point(i, j));
            }
        }


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
