package FuncionesPuntos;

import GeometryBasics.Point;
import Estructuras.*;

import java.util.HashMap;
import java.util.HashSet;

public class kNearestFunctionWithThreshold implements PointEvaluationFunction{

    private int k;
    private Instancia instancia;

    private HashMap<Point, Double> memo;

    private double threshold;

    public kNearestFunctionWithThreshold(int k, Instancia instancia, double threshold) {
        this.k = k;
        this.instancia = instancia;
        this.memo = new HashMap<>();
        this.threshold = threshold;
    }

    @Override
    public double f(Point p) {
        HashSet<Double> values = new HashSet<>();
        double bound = -1;
        if(memo.containsKey(p)){
            return memo.get(p);
        }
        for(Point site : instancia.getComunities()){
            if(p.dist(site)<threshold){
                memo.put(p,1000000000.0);
                return 1000000000.0;
            }
            if(values.size() < k){
                values.add(p.dist(site));
                bound = Math.max(bound,p.dist(site));
            }
            else{
                double dist = p.dist(site);
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
        memo.put(p,sol);
        return sol;
    }
}
