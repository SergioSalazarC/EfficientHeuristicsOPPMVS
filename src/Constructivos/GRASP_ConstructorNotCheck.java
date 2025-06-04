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

public class GRASP_ConstructorNotCheck implements Constructor {


    private double alpha;
    private ArrayList<Point> puntos;

    private ArrayList<PointValue> valores;

    private Instancia instancia;

    private PointEvaluationFunction func;

    public GRASP_ConstructorNotCheck(double alpha, Discretization disc, Instancia inst, PointEvaluationFunction func) {
        this.alpha = alpha;
        this.puntos = disc.getPuntos();
        this.instancia = inst;
        this.func = func;
        this.valores = new ArrayList<>();

    }

    @Override
    public Solucion buildSolution(boolean heuristicWeights) throws GRBException {

        this.valores = new ArrayList<>();
        for (Point p : puntos) {
            valores.add(new PointValue(p, func.f(p)));
        }

        Collections.sort(valores);
        Point[] sol = new Point[instancia.getP()];
        int size = 0;
        Iterator<PointValue> it;


        while (size < instancia.getP() && !valores.isEmpty()) {
            double cmin = valores.get(0).value;
            double cmax = valores.get(valores.size() - 1).value;
            double umbral = cmin + alpha * (cmax - cmin);

            int index = binarySearchIndex(valores, umbral);
            int randomP = randomLT(index);
            PointValue seleccionado = valores.remove(Math.min(randomP,valores.size()-1));
            sol[size] = seleccionado.p;
            size++;
            it = valores.iterator();
            while (it.hasNext()) {
                PointValue next = it.next();
                if (!checkPoint(next.p, sol, size)) it.remove();
            }
        }
        if (size == instancia.getP()){
            Solucion s =  new Solucion(sol, instancia);
            if(heuristicWeights) s.adjustHeuristic();
            else s.adjustWeightToLP();
            return s;
        }
        else return null;
    }

    private boolean checkPoint(Point p, Point[] sol, int size) {
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

    private int binarySearchIndex(ArrayList<PointValue> lista, double umbral) {
        int l = 0;
        int r = lista.size() - 1;

        if (umbral < lista.get(l).value) return l;
        if (umbral > lista.get(r).value) return r + 1;

        while (r - l > 1) {
            int m = (l + r) / 2;
            if (umbral == lista.get(m).value) return m;
            if (lista.get(m).value<umbral) l=m;
            else r = m;
        }

        if(lista.get(l).value == umbral) return l;
        if(lista.get(r).value == umbral) return r;
        if(lista.get(l).value > umbral) return l-1;
        if (lista.get(l).value < umbral && umbral < lista.get(r).value) return l;
        if (lista.get(r).value < umbral) return r;

        System.out.println("error");
        return l;

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


}
