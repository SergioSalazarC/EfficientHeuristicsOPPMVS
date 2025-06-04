package Cruces;

import Estructuras.Instancia;
import Estructuras.Par;
import Estructuras.Solucion;
import GeometryBasics.Point;
import com.gurobi.gurobi.GRBException;

import java.util.*;

public class VotacionClusterizadaCruce implements Cruce{
    @Override
    public Solucion cross(Solucion s1, Solucion s2, boolean heuristicWeights) throws GRBException {
        Random r = new Random();
        Instancia instancia = s1.getInstancia();
        int p = s1.getInstancia().getP();
        int n = s1.getInstancia().getN();
        Point[] facilities1 = s1.getFacilities();
        Point[] facilities2 = s2.getFacilities();

        Par<Point,Point>[] parejas = emparejamiento(facilities1,facilities2);


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
                double s = votos.get(index1).getE2()+1.0;
                votos.get(index1).setE2(s);
            }
            else{
                //gana2
                double s = votos.get(index2+p).getE2()+1.0;
                votos.get(index2+p).setE2(s);
            }
        }

        Collections.sort(votos, Comparator.comparing(Par::getE2));
        Collections.reverse(votos);

        boolean[] checked = new boolean[p];

        Point[] facilities = new Point[p];
        int indexSol = 0;

        int elementosS1 = 0;
        int elementosS2 = 0;

        int bound = p/2;
        if(p%2==1)bound++;

        for (Par<Integer,Double> par : votos){
            int index = par.getE1();
            Point selec = null;
            int indicePareja = -1;
            if(index>=p){
                index-=p;
                selec = facilities2[index];
                for (int i = 0; i < p; i++) {
                    if(selec == parejas[i].getE2()){
                        indicePareja=i;
                        break;
                    }
                }
                if(!checked[indicePareja] && elementosS2<bound){
                    elementosS2++;
                    checked[indicePareja]=true;
                    facilities[indexSol] = selec;
                    indexSol++;
                }
            }
            else{
                selec = facilities1[index];
                for (int i = 0; i < p; i++) {
                    if(selec == parejas[i].getE1()){
                        indicePareja=i;
                        break;
                    }
                }
                if(!checked[indicePareja] && elementosS1<bound){
                    elementosS1++;
                    checked[indicePareja]=true;
                    facilities[indexSol] = selec;
                    indexSol++;

                }
            }

            if(indexSol>=p){
                break;
            }
        }
        if(indexSol<p){
            //Nunca deberia pasar
            //System.out.println("MIRAR VOATACION CLUSTERIZADA CRUCE NUNCA DEBERIAMOS ENTRAR AQUI");
            while(indexSol<p){
                facilities[indexSol] = new Point(r.nextDouble()*10,r.nextDouble()*10);
                indexSol++;
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
