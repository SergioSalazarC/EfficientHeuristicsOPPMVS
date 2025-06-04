package Discretizaciones;

import GeometryBasics.Point;

import java.util.ArrayList;

public interface Discretization {

    public ArrayList<Point> getPuntos();
    public Point[] getRandomN(int n);
}
