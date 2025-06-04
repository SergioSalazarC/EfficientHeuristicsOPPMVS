package Constructivos;

import Estructuras.*;
import Discretizaciones.*;
import FuncionesPuntos.*;
import GeometryBasics.Point;
import com.gurobi.gurobi.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

public class GRASP_ConstructorLazy implements Constructor {


    private double alpha;
    private ArrayList<Point> puntos;

    private ArrayList<PointValue> valores;

    private Instancia instancia;


    public GRASP_ConstructorLazy(double alpha, Discretization disc, Instancia inst, PointEvaluationFunction func) {
        this.alpha = alpha;
        this.puntos = disc.getPuntos();
        this.instancia = inst;
        this.valores = new ArrayList<>();

        for (Point p : puntos) {
            double val = func.f(p);
            if(val < 10000){
                valores.add(new PointValue(p, val));
            }
        }

        Collections.sort(valores);

    }

    @Override
    public Solucion buildSolution(boolean heuristicWeights) throws GRBException {


        Point[] sol = new Point[instancia.getP()];
        int size = 0;
        ArrayList<PointValue> copyPoints = (ArrayList<PointValue>) valores.clone();
        Iterator<PointValue> it = copyPoints.iterator();


        while (it.hasNext()) {
            PointValue next = it.next();
            if (!checkPoint(next.p, sol, size)) it.remove();
        }



        while (size < instancia.getP() && !copyPoints.isEmpty()) {
            double cmin = copyPoints.get(0).value;
            double cmax = copyPoints.get(copyPoints.size() - 1).value;
            double umbral = cmin + alpha * (cmax - cmin);
            //System.out.println(umbral);
            int index = searchIndex(copyPoints, umbral);
            //System.out.println(index);
            //printRank(alpha,copyPoints);

            int randomP = randomLT(index);
            PointValue seleccionado = copyPoints.remove(Math.min(randomP,copyPoints.size()-1));
            sol[size] = seleccionado.p;
            size++;
            it = copyPoints.iterator();
            while (it.hasNext()) {
                PointValue next = it.next();
                if (!checkPoint(next.p, sol, size)) it.remove();
            }
        }
        if (size == instancia.getP()){
            Solucion s = new Solucion(sol, instancia);
            if(heuristicWeights) s.adjustHeuristic();
            else s.adjustWeightToLP();
            return s;
        }
        else return null;
    }

    private boolean checkPoint(Point p, Point[] sol, int size) {
        /*
        FuncionesPuntos.kNearestFunction nearestFunction = new FuncionesPuntos.kNearestFunction(1,instancia);

        double D = instancia.getD();
        if(nearestFunction.f(p) < D) return false;

         */

        for (int i = 0; i < size; i++) {
            if (p.dist(sol[i]) < instancia.getDhat()) return false;

        }
        return true;
    }

    private int randomLT(int index) {
        Random random = new Random();
        int a = random.nextInt(index + 1);
        return a;
    }

    private int searchIndex(ArrayList<PointValue> lista, double umbral) {
        //mejorar con busqueda binaria
        for (int i = 0; i < lista.size(); i++) {
            if(i==lista.size()){
                return lista.size();
            }
            if(lista.get(i).value<= umbral && lista.get(i+1).value > umbral){
                return i;
            }

        }
        return lista.size();


    }

    private class PointValue implements Comparable {

        Point p;
        double value;

        public PointValue(Point p, double val) {
            this.p = p;
            this.value = val;
        }

        @Override
        public int compareTo(Object o) {
            PointValue i = (PointValue) o;
            return Double.compare(this.value, i.value);
        }


    }


    public void printRank(double alpha){
        double cmin = valores.get(0).value;
        double cmax = valores.get(valores.size() - 1).value;
        double umbral = cmin + alpha * (cmax - cmin);
        System.out.println("UMBRAL = " + umbral);
        for (PointValue p : valores){
            System.out.println("("+p.p.getX()+","+p.p.getY()+"): "+p.value);
        }
    }

    public void printRank(double alpha , ArrayList<PointValue> valores){
        double cmin = valores.get(0).value;
        double cmax = valores.get(valores.size() - 1).value;
        double umbral = cmin + alpha * (cmax - cmin);
        System.out.println("UMBRAL = " + umbral);
        for (PointValue p : valores){
            System.out.println("("+p.p.getX()+","+p.p.getY()+"): "+p.value);
        }
    }


}
