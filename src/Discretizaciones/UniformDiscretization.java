package Discretizaciones;

import GeometryBasics.Point;

import java.util.ArrayList;
import java.util.Collections;

public class UniformDiscretization implements Discretization {

    ArrayList<Point> puntos;


    //Uniform
    public UniformDiscretization(Point minizq, Point maxder, double beta) {

        puntos = new ArrayList<>(10000);

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
