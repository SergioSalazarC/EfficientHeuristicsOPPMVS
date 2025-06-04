package Discretizaciones;

import GeometryBasics.Point;
import Voronoi.Voronoi;
import Estructuras.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VoronoiPointsDiscretization implements Discretization {

    ArrayList<Point> puntos;


    public VoronoiPointsDiscretization(Instancia instancia) {

        Point[] com = instancia.getComunities();
        ArrayList<Point> sites = new ArrayList<>(List.of(com));

        Voronoi v = new Voronoi(sites,1000);

        puntos = new ArrayList<>(v.getvPoints());


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

    public void printPuntos(){
        System.out.print("[");
        for(Point p : puntos){
            System.out.println("("+p.getX()+","+p.getY()+"),");
        }
        System.out.println("]");
    }
}
