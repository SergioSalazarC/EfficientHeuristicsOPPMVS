package Mutaciones;

import Discretizaciones.Discretization;
import Estructuras.Solucion;
import GeometryBasics.Point;
import com.gurobi.gurobi.GRBException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class NormalRandomMutation implements Mutacion{

    private double SIGMA = 1;
    @Override
    public Solucion mutate(Solucion s, Discretization disc, int sizeMut, boolean heuristicWeights) throws GRBException {
        Random r = new Random();
        int p = s.getInstancia().getP();
        ArrayList<Integer> permutacion = new ArrayList<>(3*p);
        int[] aux =new int[p];
        for (int i = 0; i < p; i++) {
            permutacion.add(i);
            aux[i]=i;
        }
        Collections.shuffle(permutacion);

        Point[] fac = s.getFacilities().clone();

        int[] indexToRepair = new int[sizeMut];

        for (int i = 0; i < sizeMut; i++) {
            int indexToMutate = permutacion.get(i);
            double x = fac[indexToMutate].getX() + r.nextGaussian()*SIGMA;
            double y = fac[indexToMutate].getY() + r.nextGaussian()*SIGMA;
            indexToRepair[i] = indexToMutate;

            fac[indexToMutate] = new Point(x,y);
        }


        Solucion solu  = new Solucion(fac,s.getInstancia());

        for(Point point : solu.getFacilities()){
            if(Double.isNaN(point.getX()) || Double.isNaN(point.getY())){
                System.out.println("BP");
            }
        }
        if(heuristicWeights) solu.adjustHeuristic();
        else solu.adjustWeightToLP();

        while (solu.eval()>10000000){
            solu.repair(aux);
            //System.out.println(solu.getFacilities()[indexToRepair[0]]+",");
        }
        return solu;
    }
}
