package Discretizaciones;

import GeometryBasics.Point;
import Voronoi.Voronoi;
import Estructuras.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MixDiscretization implements Discretization {

    ArrayList<Point> puntos;

    public MixDiscretization(Instancia instancia, Point minizq, Point maxder, double beta, double minsum, double maxsum, double gamma) {
        puntos = new ArrayList<>();
        gradDisc(instancia, minizq, maxder, beta, minsum, maxsum, gamma);
        vorDisc(instancia, beta);

    }

    @Override
    public ArrayList<Point> getPuntos() {
        return puntos;
    }

    @Override
    public Point[] getRandomN(int n) {
        Collections.shuffle(puntos);
        Point[] ret = new Point[n];
        for (int i = 0; i < n; i++) {
            ret[i] = puntos.get(i);
        }
        return ret;
    }

    private void gradDisc(Instancia instancia, Point minizq, Point maxder, double beta, double minsum, double maxsum, double gamma) {
        Point[] comunities = instancia.getComunities();

        double D = instancia.getD();
        double p = instancia.getP();
        double W = instancia.getW();

        double left = minizq.getX();
        double right = maxder.getX();
        double bottom = minizq.getY();
        double top = maxder.getY();

        for (double i = left; i <= right; i += beta) {
            for (int q = 0; q < comunities.length; q++) {
                for (double j = minsum; j <= maxsum; j++) {
                    double qy = comunities[q].getY();
                    double qx = comunities[q].getX();

                    double a = 1;
                    double b = -2 * qy;
                    double c = qy * qy + (i - qx) * (i - qx) - (D * D * p * j / W);

                    if (b * b - 4 * a * c < 0) continue;
                    else if (b * b - 4 * a * c == 0) {
                        double y = -b / (2 * a);
                        Point opcion = new Point(i, y);
                        double dist = opcion.dist(comunities[q]);
                        if (bottom <= y && y <= top && dist >= (D * D * p) / W && dist <= gamma) puntos.add(opcion);
                    } else {
                        double delta = Math.sqrt(b * b - 4 * a * c);
                        double y0 = (-b + delta) / 2 * a;
                        double y1 = (-b - delta) / 2 * a;
                        if (bottom <= y0 && y0 <= top) puntos.add(new Point(i, y0));
                        if (bottom <= y1 && y1 <= top) puntos.add(new Point(i, y1));
                    }

                }
            }
        }
    }

    private void vorDisc(Instancia instancia, double beta) {
        Point[] com = instancia.getComunities();
        ArrayList<Point> sites = new ArrayList<>(List.of(com));

        Voronoi v = new Voronoi(sites, beta);

        puntos.addAll(v.getEdgeAndVPoints());

    }

}
