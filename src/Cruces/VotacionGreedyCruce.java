package Cruces;

import Estructuras.Instancia;
import Estructuras.Par;
import Estructuras.Solucion;
import GeometryBasics.Point;
import com.gurobi.gurobi.GRBException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class VotacionGreedyCruce implements Cruce{


    @Override
    public Solucion cross(Solucion s1, Solucion s2, boolean heuristicWeights) throws GRBException {

        Instancia instancia = s1.getInstancia();

        double DIST = instancia.getDhat();

        int p = instancia.getP();
        int n = instancia.getN();

        ArrayList<Par<Integer,Double>> votos = new ArrayList<>(2*p);
        for (int i = 0; i < 2 * p; i++) {
            votos.add(new Par(i,0.0));
        }
        double[][] pesos1 = s1.getWeights();
        double[][] pesos2 = s2.getWeights();
        for (int i = 0; i < n; i++) {
            int index1 = 0;
            int index2 = 0;
            for (int j = 0; j < p; j++) {
                if(pesos1[i][j] > pesos1[i][index1]) index1 = j;
                if(pesos2[i][j] > pesos2[i][index2]) index2 = j;
            }
            if(s1.getFacilities()[index1].dist(instancia.getComunities()[i]) < s2.getFacilities()[index2].dist(instancia.getComunities()[i])){
                //gana1
                votos.get(index1).setE2(votos.get(index1).getE2()+1);
            }
            else{
                //gana2
                votos.get(index2+p).setE2(votos.get(index2+p).getE2()+1);
            }
        }

        Collections.sort(votos, Comparator.comparing(Par::getE2));
        Collections.reverse(votos);

        ArrayList<Point> facilitiesList = new ArrayList<>();

        for (int i = 0; i < 2*p && facilitiesList.size()<p ; i++) {
            Par<Integer,Double> x = votos.get(i);
            Point posible = null;

            if(x.getE1()<p){
                posible = s1.getFacilities()[x.getE1()];
            }
            else{
                posible = s2.getFacilities()[x.getE1()-p];
            }

            boolean flag_nocerca = false;

            for(Point resto : facilitiesList){
                if(posible.dist(resto) < DIST){
                    flag_nocerca = true;
                    break;
                }
            }

            if(!flag_nocerca) facilitiesList.add(posible);
            //else System.out.println("*");
        }

        Random r = new Random();

        Point[] facilities = new Point[p];
        for (int i = 0; i < p; i++) {
            if(i<facilitiesList.size()){
                facilities[i] = facilitiesList.get(i);
            }
            else{
                facilities[i] = new Point(r.nextDouble()*10,r.nextDouble()*10);
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


}
