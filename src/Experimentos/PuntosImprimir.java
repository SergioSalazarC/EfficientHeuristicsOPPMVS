package Experimentos;

import GeometryBasics.Point;

import java.util.ArrayList;
import Estructuras.*;
import Discretizaciones.*;

public class PuntosImprimir {
    public static void main(String[] args) {
        Instancia instance = new Instancia(100,15);

        Discretization disc = new VoronoiPointsDiscretization(instance);

        for(Point p : disc.getPuntos()){
            System.out.println("("+p.getX()+","+p.getY()+"),");
        }

        System.out.println();

        for(Point p : instance.getComunities()){
            System.out.println("("+p.getX()+","+p.getY()+"),");
        }

        ArrayList<Point> al = new ArrayList<>();
        al.add(new Point(9.3556,8.5341));
        al.add(new Point(1.0617,1.0882));
        al.add(new Point(2.844,4.0949));
        al.add(new Point(2.5938,8.775));
        al.add(new Point(8.637,4.8595));
        al.add(new Point(6.6601,3.5467));
        al.add(new Point(6.9725,6.5678));
        al.add(new Point(1.4681,5.6225));
        al.add(new Point(3.3556,1.7256));
        al.add(new Point(5.8466,0.8346));
        al.add(new Point(8.5397,0.9193));
        al.add(new Point(5.1846,4.6399));
        al.add(new Point(6.0049,8.9819));
        al.add(new Point(4.0531,5.9652));
        al.add(new Point(0.9085,7.5506));


        System.out.println();

        for(Point p : al){
            System.out.println("("+p.getX()+","+p.getY()+"),");
        }



    }
}
