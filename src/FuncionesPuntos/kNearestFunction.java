package FuncionesPuntos;

import GeometryBasics.Point;
import Estructuras.*;


import java.util.HashMap;
import java.util.HashSet;

public class kNearestFunction implements PointEvaluationFunction{

    private int k;
    private Instancia instancia;

    private HashMap<Point, Double> memo;

    public kNearestFunction(int k, Instancia instancia) {
        this.k = k;
        this.instancia = instancia;
        this.memo = new HashMap<>();
    }

    @Override
    public double f(Point p) {
        HashSet<Double> values = new HashSet<>();
        double bound = -1;
        if(memo.containsKey(p)){
            return memo.get(p);
        }
        for(Point site : instancia.getComunities()){
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
