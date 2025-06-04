package BusquedasLocales;

import Estructuras.Instancia;
import Estructuras.Solucion;
import GeometryBasics.Point;
import com.gurobi.gurobi.GRBException;


public class HookeJeeves8PLocalSearch implements LocalSearch {

    double limiteMovimiento;
    double factorReduccion;

    public HookeJeeves8PLocalSearch(double limiteMovimiento, double factorReduccion) {
        this.limiteMovimiento = limiteMovimiento;
        this.factorReduccion = factorReduccion;
    }

    static double INF = 1e9;
    static double EPS = 1e-6;
    @Override
    public Solucion improveSolution(Solucion s0) throws GRBException {
        s0.adjustWeightToLP();
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
        return null;
    }

    @Override
    public Solucion improveSolution(Solucion s0, int size, int iter) throws GRBException {
        return null;
    }

    private Solucion improvePoint(Solucion s, int index){
        double distMovimiento = 1;
        Point fac = s.getFacilities()[index];
        limiteMovimiento = 1e-6;
        factorReduccion = 0.75;

        boolean cambia = false;

        double diag = Math.sqrt(2)/2;

        double[] cambiosEjeX = {0,diag,1,diag,0,-diag,-1,-diag};
        double[] cambiosEjeY = {1,diag,0,-diag,-1,-diag,0,diag};


        
        double value = evaluatePoint(s, index, fac);
        
        while(distMovimiento > limiteMovimiento){

            Point[] puntosCandidatos = new Point[8];
            double[] evalCandidatos = new double[8];

            for (int i = 0; i < 8; i++) {
                puntosCandidatos[i] = new Point(fac.getX()+distMovimiento*cambiosEjeX[i],fac.getY()+distMovimiento*cambiosEjeY[i]);
                evalCandidatos[i] = evaluatePoint(s,index,puntosCandidatos[i]);
            }
            int minIndex = -1;

            for (int i = 0; i < 8; i++) {
                if(evalCandidatos[i]<value-EPS){
                    value = evalCandidatos[i];
                    minIndex=i;
                }
            }


            if(minIndex==-1){
                distMovimiento = distMovimiento*factorReduccion;
            }
            else{
                cambia=true;
                fac=puntosCandidatos[minIndex];
            }
            
        }

        if(cambia) {
            Point[] newfacilities = s.getFacilities().clone();
            newfacilities[index] = fac;
            Solucion newS = new Solucion(newfacilities, s.getWeights(), s.getInstancia());
            return newS;
        }
        else{
            return s;
        }

    }

    private double evaluatePoint(Solucion s, int index, Point fac){
        //Point fac = s.getFacilities()[index];

        double D = s.getInstancia().getD();
        double W = s.getInstancia().getW();

        int n = s.getInstancia().getN();
        int p = s.getInstancia().getP();

        Point[] communities = s.getInstancia().getComunities();
        double[][] asign = s.getWeights();

        double aportacion = 0;

        double sumWkj = 0;

        int indexClosest = 0;

        double dist2Closest = fac.dist2(communities[0]);

        for (int com = 0; com < n; com++) {
            double distancia2 = fac.dist2(communities[com]);
            double distancia = fac.dist(communities[com]);
            sumWkj += asign[com][index];

            aportacion += asign[com][index] * distancia;

            if(distancia2<dist2Closest){
                indexClosest = com;
                dist2Closest = distancia2;
            }

        }

        double valCondicion = (D*D*p*sumWkj)/W;

        if(dist2Closest >= valCondicion){
            //todobien
            return aportacion;
        }
        else{
            return INF;
        }

    }



}
