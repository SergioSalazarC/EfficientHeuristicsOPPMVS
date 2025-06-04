package Discretizaciones;

import GeometryBasics.Point;

import java.util.ArrayList;
import java.util.Collections;

public class DummyDiscretization implements Discretization {

    ArrayList<Point> puntos = new ArrayList<>();

    public DummyDiscretization(ArrayList<Point> puntos) {
        this.puntos = puntos;
    }

    @Override
    public ArrayList<Point> getPuntos() {
        return puntos;
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
}
