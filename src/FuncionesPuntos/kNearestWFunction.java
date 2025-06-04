package FuncionesPuntos;

import GeometryBasics.Point;

import java.util.HashSet;

import Estructuras.*;

public class kNearestWFunction implements PointEvaluationFunction{

    private int k;
    private Instancia instancia;

    public kNearestWFunction(int k, Instancia instancia) {
        this.k = k;
        this.instancia = instancia;
    }

    @Override
    public double f(Point p) {
        double [] W = instancia.getWeights();
        Point [] ps = instancia.getComunities();
        HashSet<Double> values = new HashSet<>();
        double bound = -1;
        for(int i = 0;i< ps.length;i++){
            Point site = ps[i];
            if(values.size() < k){
                values.add(p.dist(site)*W[i]);
                bound = Math.max(bound,p.dist(site));
            }
            else{
                double dist = p.dist(site)*W[i];
                if(dist < bound){
                    values.remove(bound);
                    values.add(dist);
                    bound = -1;
                    for(double d : values){
                        bound = Math.max(bound,d);
                    }
                }
            }
        }
        double sol = 0;
        for(double d : values) sol+=d;
        return sol;
    }
}
