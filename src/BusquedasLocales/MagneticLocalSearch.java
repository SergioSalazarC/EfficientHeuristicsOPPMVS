package BusquedasLocales;

import GeometryBasics.*;
import Estructuras.*;
import com.gurobi.gurobi.*;

import java.util.ArrayList;
import java.util.Collections;

public class MagneticLocalSearch  implements LocalSearch{

    private static int usos = 0;

    public static int cuantosUsos(){
        int aux = usos;
        usos=0;
        return aux;

    }


    private Solucion improvePoint(Solucion s0, int index){
        usos++;
        Instancia instancia = s0.getInstancia();
        double[][] pesos = s0.getWeights();
        Point[] facilities = s0.getFacilities();
        Point[] comunities = instancia.getComunities();

        double D = instancia.getD();
        double p = instancia.getP();
        double W = instancia.getW();
        int n = instancia.getN();
        double W_index = 0;

        for (int i = 0; i < n; i++) {
            W_index+=pesos[i][index];
        }

        Point[] newfacilities = facilities.clone();

        Point fac = facilities[index];

        Vector acum = new Vector(0,0);

        for (int j = 0; j < instancia.getN(); j++) {
            if(pesos[j][index] == 0) continue;

            Point com = comunities[j];

            Vector vi = new Vector(fac,com);
            vi = vi.unitary();
            vi.mul(0.01*pesos[j][index]);
            acum = acum.add(vi);
        }
        Point newfac = fac.add(acum);

        Point may_inf = comunities[0];
        double distnear = newfac.dist2(comunities[0]);

        double R2 = (D*D*p*W_index)/W;

        for(Point com : comunities){
            double aux = newfac.dist2(com);
            if(aux <distnear){
                may_inf = com;
                distnear = aux;
            }
        }

        if(distnear < R2){
            Line r = new Line(facilities[index] , facilities[index].add(acum));
            Circle c = new Circle( Math.sqrt(R2), may_inf);

            Point[] cuts = r.CircleCut(c);

            if(cuts[0].dist(facilities[index]) < cuts[1].dist(facilities[index])){
                newfac = cuts[0];
            }
            else{
                newfac = cuts[1];
            }

            double theta = (2*Math.PI)/500;

            while(true){
                Vector toCero = new Vector(may_inf, new Point(0,0));
                Vector negtoCero = new Vector(new Point(0,0),may_inf);
                newfac.move(toCero);
                Point rotneg = newfac.rot(-theta);
                Point rotpos = newfac.rot(theta);
                newfac.move(negtoCero);
                rotneg.move(negtoCero);
                rotpos.move(negtoCero);

                double valneg = 0;
                double valcero = 0;
                double valpos = 0;
                for (int i = 0; i < n; i++) {
                    valneg += pesos[i][index]*rotneg.dist(comunities[i]);
                    valcero += pesos[i][index]*newfac.dist(comunities[i]);
                    valpos += pesos[i][index]*rotpos.dist(comunities[i]);
                }

                if(valcero>= valneg && valcero>= valpos){
                    break;
                }
                else if(valneg>valpos){
                    if(isFactCircle(rotneg,comunities,R2)){
                        newfac = rotneg;
                    }
                    else{
                        break;
                    }
                }
                else{
                    if(isFactCircle(rotpos,comunities,R2)){
                        newfac = rotpos;
                    }
                    else{
                        break;
                    }
                }

            }

        }
        newfacilities[index] = newfac;

        Solucion s = new Solucion(newfacilities, pesos , instancia);

        if(s.eval()<s0.eval()){
            return s;
        }
        else{
            return s0;
        }




    }

    @Override
    public Solucion improveSolution(Solucion s0) throws GRBException {
        double eval = s0.eval();

        Instancia instancia = s0.getInstancia();

        for (int i = 0; i < instancia.getP(); i++) {
            Solucion s = improvePoint(s0,i);
            double s_eval = s.eval();
            if(s_eval<eval){
                i--;
                eval = s_eval;
                s0 = s;
            }
        }
        return s0;
    }

    @Override
    public Solucion improveSolution(Solucion s0, int size) throws GRBException {
        double eval = s0.eval();

        Instancia instancia = s0.getInstancia();

        ArrayList<Integer> permutacion = new ArrayList<>(3*instancia.getP());
        for (int i = 0; i < instancia.getP(); i++) {
            permutacion.add(i);
        }
        Collections.shuffle(permutacion);

        for (int i = 0; i < size; i++) {
            int k = permutacion.get(i);
            Solucion s = improvePoint(s0,k);
            double s_eval = s.eval();
            if(s_eval<eval){
                i--;
                eval = s_eval;
                s0 = s;
            }
        }
        return s0;
    }

    @Override
    public Solucion improveSolution(Solucion s0, int size, int maxIter) throws GRBException {
        double eval = s0.eval();

        Instancia instancia = s0.getInstancia();


        ArrayList<Integer> indices = new ArrayList<>(30);
        for (int i = 0; i < instancia.getP(); i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);

        for (int i = 0; i < size; i++) {
            int index = indices.get(i);
            int iteraciones = 0;
            while(true){
                Solucion s = improvePoint(s0,index);
                double s_eval = s.eval();
                if(s_eval<eval){
                    eval = s_eval;
                    s0 = s;
                }
                else{
                    break;
                }
                if(iteraciones>=maxIter){
                    break;
                }
                iteraciones++;
            }

        }
        return s0;
    }




    private boolean isFactCircle(Point p, Point[] comunities, double D){
        for(Point q : comunities){
            if(p.dist2(q)<D) return false;
        }
        return true;
    }
}
