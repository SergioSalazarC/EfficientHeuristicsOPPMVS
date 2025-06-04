package Cruces;

import Estructuras.Instancia;
import Estructuras.Par;
import Estructuras.Solucion;
import GeometryBasics.Point;
import com.gurobi.gurobi.GRBException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class RandomClusterizadaCruce implements Cruce{
    @Override
    public Solucion cross(Solucion s1, Solucion s2, boolean heuristicWeights) throws GRBException {
        Random r = new Random();
        Instancia instancia = s1.getInstancia();
        int p  =s1.getInstancia().getP();
        Point[] facilities1 = s1.getFacilities();
        Point[] facilities2 = s2.getFacilities();

        Par<Point,Point>[] parejas = emparejamiento(facilities1,facilities2);

        Point[] facilities = new Point[p];


        for (int i = 0; i < p; i++) {
            int s = r.nextInt(0,2);
            if(s%2==0){
                facilities[i]=parejas[i].getE1();
            }
            else{
                facilities[i]=parejas[i].getE2();
            }
        }

        Solucion cross = new Solucion(facilities,instancia);
        if(heuristicWeights) cross.adjustHeuristic();
        else cross.adjustWeightToLP();
        if(cross.eval() >= 10000000){
            return s1;
        }

        return cross;
    }


    private Point closer(Point punto, ArrayList<Point> candidatos){
        double dist = Double.MAX_VALUE;
        int sol = -1;
        for (int i = 0; i < candidatos.size(); i++) {
            double distancia = punto.dist(candidatos.get(i));
            if(distancia < dist){
                sol = i;
                dist = distancia;
            }
        }

        return candidatos.get(sol);
    }

    private Par<Point,Point>[] emparejamiento(Point[] facilities1, Point[] facilities2){
        int p = facilities1.length;
        Par<Point,Point>[] parejas = new Par[p];
        int index = 0;

        ArrayList<Point> f1 = new ArrayList<>(List.of(facilities1));
        ArrayList<Point> f2 = new ArrayList<>(List.of(facilities2));
        while(!f1.isEmpty()){
            Iterator<Point> it = f1.iterator();
            while(it.hasNext()){
                Point aux = it.next();
                Point closer = closer(aux, f2);
                if(closer(closer, f1) == aux){
                    parejas[index] = new Par<>(aux,closer);
                    it.remove();
                    f2.remove(closer);
                    index++;
                }
            }
        }

        return parejas;
    }
}
