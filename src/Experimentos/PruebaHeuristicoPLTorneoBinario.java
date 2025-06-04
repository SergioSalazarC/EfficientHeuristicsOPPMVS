package Experimentos;

import Constructivos.Constructor;
import Constructivos.RandomConstructor;
import Discretizaciones.Discretization;
import Discretizaciones.UniformDiscretization;
import Discretizaciones.VoronoiPointsDiscretization;
import Estructuras.Instancia;
import Estructuras.Solucion;
import GeometryBasics.Point;
import com.gurobi.gurobi.*;

import java.io.*;
import java.nio.Buffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

public class PruebaHeuristicoPLTorneoBinario {

    public static double[][] weights;
    public static double[][] weights2;

    public static double EPS = 1e-3;

    public static Point randomP(){
        Random r = new Random();
        double x = r.nextDouble()*10;
        double y = r.nextDouble()*10;
        return new Point(x,y);

    }

    public static double[] PL(int n, int p, double W, Point[] com, double D, double[] wi, Point[] facilities) throws GRBException {
        double a = System.currentTimeMillis();
        //Create enviroment
        GRBEnv env = new GRBEnv(true);
        env.set(GRB.DoubleParam.MemLimit, 2);
        //env.set(GRB.DoubleParam.NodefileStart, 0.5);
        //env.set(GRB.DoubleParam.NodeLimit, 5.0);
        env.set(GRB.IntParam.LogToConsole, 0);
        //env.set(GRB.IntParam.Aggregate,2);
        //env.set(GRB.IntParam.Presolve,2);
        env.start();


        //Create empty model
        GRBModel model = new GRBModel(env);

        //Crete variables
        GRBVar[][] variables = new GRBVar[n][p];

        for (int c = 0; c < n; c++) {
            for (int f = 0; f < p; f++) {
                variables[c][f] = model.addVar(0.0,5.0,0.0,GRB.CONTINUOUS,"w_"+c+f);
                //variables[c][f].set(GRB.DoubleAttr.Start,weights[c][f]);
            }
        }




        //Set objective
        GRBLinExpr expr = new GRBLinExpr();
        for (int c = 0; c < n; c++) {
            for (int f = 0; f < p; f++) {
                double dixj = com[c].dist(facilities[f]);
                expr.addTerm(dixj,variables[c][f]);
            }
        }
        model.setObjective(expr,GRB.MINIMIZE);

        //Add (1) Constraints
        for (int j = 0; j < p; j++) {
            expr = new GRBLinExpr(); //sumatorio
            for (int k = 0; k < n; k++) {
                expr.addTerm(1.0,variables[k][j]);
            }
            //constante
            double closest = 1000000000;
            for (int c = 0; c < n; c++) {
                closest = Math.min(closest, facilities[j].dist2(com[c]));
            }
            double constnt1 =  (W*closest)/(D*D*p);
            model.addConstr(expr,GRB.LESS_EQUAL,constnt1,"C1_"+j);
        }

        //Add (2) constraints
        for (int i = 0; i < n; i++) {
            expr = new GRBLinExpr(); //sumatorio
            for (int j = 0; j < p; j++) {
                expr.addTerm(1.0,variables[i][j]);
            }
            model.addConstr(expr,GRB.EQUAL,wi[i],"C2_"+i);
        }

        //Optimize model


        model.optimize();

        int status = model.get(GRB.IntAttr.Status);
        if (status==GRB.Status.INFEASIBLE){
            model.dispose();
            env.dispose();
            double b = System.currentTimeMillis();
            return new double[]{-1, b - a};
        }

        for (int c = 0; c < n; c++) {
            for (int f = 0; f < p; f++) {
                weights[c][f] = variables[c][f].get(GRB.DoubleAttr.X);
            }
        }

        //System.out.println("Value: " + model.get(GRB.DoubleAttr.ObjVal));

        double sol = model.get(GRB.DoubleAttr.ObjVal);

        model.dispose();
        env.dispose();

        double b = System.currentTimeMillis();

        return new double[]{sol , b - a};
    }

    public static void adjustWeightToLemma2(int p, int n, Point[] facilities,  Point[] com, double[] wi) {


        double denom = 0;
        for (int f = 0; f < p; f++) {
            double closest = 100000000;
            for (int c = 0; c < n; c++) {
                closest = Math.min(closest, facilities[f].dist2(com[c]));
            }
            denom += closest;
        }

        for (int f = 0; f < p; f++) {
            for (int c = 0; c < n; c++) {

                double val = 100000000;
                for (int i = 0; i < n; i++) {
                    val = Math.min(val, facilities[f].dist2(com[i]));
                }

                weights[c][f] = val * wi[c] / denom;

            }
        }

    }


    public static double[] PLwithStartingSol(int n, int p, double W, Point[] com, double D, double[] wi, Point[] facilities,boolean fac) throws GRBException {
        double a = System.currentTimeMillis();
        //Create enviroment
        if(fac) PLHeuristic(n,p,W,com,D,wi,facilities);
        GRBEnv env = new GRBEnv(true);
        env.set(GRB.DoubleParam.MemLimit, 2);
        //env.set(GRB.DoubleParam.NodefileStart, 0.5);
        //env.set(GRB.DoubleParam.NodeLimit, 5.0);
        env.set(GRB.IntParam.LogToConsole, 0);
        //env.set(GRB.IntParam.Aggregate,2);
        //env.set(GRB.IntParam.Presolve,2);
        env.start();


        //Create empty model
        GRBModel model = new GRBModel(env);

        //Crete variables
        GRBVar[][] variables = new GRBVar[n][p];

        for (int c = 0; c < n; c++) {
            for (int f = 0; f < p; f++) {
                variables[c][f] = model.addVar(0.0,5.0,0.0,GRB.CONTINUOUS,"w_"+c+f);
                if(fac) variables[c][f].set(GRB.DoubleAttr.Start,weights2[c][f]);
            }
        }




        //Set objective
        GRBLinExpr expr = new GRBLinExpr();
        for (int c = 0; c < n; c++) {
            for (int f = 0; f < p; f++) {
                double dixj = com[c].dist(facilities[f]);
                expr.addTerm(dixj,variables[c][f]);
            }
        }
        model.setObjective(expr,GRB.MINIMIZE);

        //Add (1) Constraints
        for (int j = 0; j < p; j++) {
            expr = new GRBLinExpr(); //sumatorio
            for (int k = 0; k < n; k++) {
                expr.addTerm(1.0,variables[k][j]);
            }
            //constante
            double closest = 1000000000;
            for (int c = 0; c < n; c++) {
                closest = Math.min(closest, facilities[j].dist2(com[c]));
            }
            double constnt1 =  (W*closest)/(D*D*p);
            model.addConstr(expr,GRB.LESS_EQUAL,constnt1,"C1_"+j);
        }

        //Add (2) constraints
        for (int i = 0; i < n; i++) {
            expr = new GRBLinExpr(); //sumatorio
            for (int j = 0; j < p; j++) {
                expr.addTerm(1.0,variables[i][j]);
            }
            model.addConstr(expr,GRB.EQUAL,wi[i],"C2_"+i);
        }

        //Optimize model


        model.optimize();

        int status = model.get(GRB.IntAttr.Status);
        if (status==GRB.Status.INFEASIBLE){
            model.dispose();
            env.dispose();
            double b = System.currentTimeMillis();
            return new double[]{-1, b - a};
        }

        for (int c = 0; c < n; c++) {
            for (int f = 0; f < p; f++) {
                weights[c][f] = variables[c][f].get(GRB.DoubleAttr.X);
            }
        }

        //System.out.println("Value: " + model.get(GRB.DoubleAttr.ObjVal));

        double sol = model.get(GRB.DoubleAttr.ObjVal);

        model.dispose();
        env.dispose();

        double b = System.currentTimeMillis();

        return new double[]{sol , b - a};
    }

    public static double[] PLwithParams(int n, int p, double W, Point[] com, double D, double[] wi, Point[] facilities) throws GRBException {
        double a = System.currentTimeMillis();
        //Create enviroment
        GRBEnv env = new GRBEnv(true);
        env.set(GRB.DoubleParam.MemLimit, 2);
        //env.set(GRB.DoubleParam.NodefileStart, 0.5);
        //env.set(GRB.DoubleParam.NodeLimit, 5.0);
        env.set(GRB.IntParam.LogToConsole, 0);
        env.set(GRB.IntParam.Aggregate,2);
        env.set(GRB.IntParam.Presolve,2);
        env.start();


        //Create empty model
        GRBModel model = new GRBModel(env);

        //Crete variables
        GRBVar[][] variables = new GRBVar[n][p];

        for (int c = 0; c < n; c++) {
            for (int f = 0; f < p; f++) {
                variables[c][f] = model.addVar(0.0,5.0,0.0,GRB.CONTINUOUS,"w_"+c+f);
            }
        }




        //Set objective
        GRBLinExpr expr = new GRBLinExpr();
        for (int c = 0; c < n; c++) {
            for (int f = 0; f < p; f++) {
                double dixj = com[c].dist(facilities[f]);
                expr.addTerm(dixj,variables[c][f]);
            }
        }
        model.setObjective(expr,GRB.MINIMIZE);

        //Add (1) Constraints
        for (int j = 0; j < p; j++) {
            expr = new GRBLinExpr(); //sumatorio
            for (int k = 0; k < n; k++) {
                expr.addTerm(1.0,variables[k][j]);
            }
            //constante
            double closest = 1000000000;
            for (int c = 0; c < n; c++) {
                closest = Math.min(closest, facilities[j].dist2(com[c]));
            }
            double constnt1 =  (W*closest)/(D*D*p);
            model.addConstr(expr,GRB.LESS_EQUAL,constnt1,"C1_"+j);
        }

        //Add (2) constraints
        for (int i = 0; i < n; i++) {
            expr = new GRBLinExpr(); //sumatorio
            for (int j = 0; j < p; j++) {
                expr.addTerm(1.0,variables[i][j]);
            }
            model.addConstr(expr,GRB.EQUAL,wi[i],"C2_"+i);
        }

        //Optimize model


        model.optimize();

        int status = model.get(GRB.IntAttr.Status);
        if (status==GRB.Status.INFEASIBLE){
            model.dispose();
            env.dispose();
            double b = System.currentTimeMillis();
            return new double[]{-1, b - a};
        }

        for (int c = 0; c < n; c++) {
            for (int f = 0; f < p; f++) {
                weights[c][f] = variables[c][f].get(GRB.DoubleAttr.X);
            }
        }

        //System.out.println("Value: " + model.get(GRB.DoubleAttr.ObjVal));

        double sol = model.get(GRB.DoubleAttr.ObjVal);

        model.dispose();
        env.dispose();

        double b = System.currentTimeMillis();

        return new double[]{sol , b - a};
    }

    public static class Trio implements Comparable {
        int f;
        int c;
        double d;

        public Trio(int f, int c, double d) {
            this.f = f;
            this.c = c;
            this.d = d;
        }

        @Override
        public int compareTo(Object o) {
            Trio  i = (Trio) o;
            return Double.compare(this.d,i.d);
        }
    }

    public static double[] PLHeuristic(int n, int p, double W, Point[] com, double D, double[] wi, Point[] facilities) {
        double a = System.currentTimeMillis();

        LinkedList<Trio> lista = new LinkedList<>();

        for (int j = 0; j < p; j++) {
            for (int i = 0; i < n; i++) {
                double d2 = facilities[j].dist2(com[i]);
                lista.add(new Trio(j, i, d2));
            }
        }

        double[] trabajoRequerido = wi.clone();

        Collections.sort(lista);

        double[] capacidad = new double[p];

        for (int i = 0; i < p; i++) {
            Point fac = facilities[i];
            double dist = 1e8;
            for (int j = 0; j < n; j++) {
                dist = Math.min(dist, fac.dist2(com[j]));
            }
            capacidad[i] = (W*dist)/(D*D*p);
        }

        double solvalue = 0;

        for(Trio t:  lista){
            int fac = t.f;
            int comm = t.c;
            double d = t.d;

            if(trabajoRequerido[comm]>0 && capacidad[fac]>0){
                if(trabajoRequerido[comm]>capacidad[fac]){
                    trabajoRequerido[comm] -= capacidad[fac];
                    weights2[comm][fac] += capacidad[fac];
                    capacidad[fac]=0;
                    solvalue+= weights2[comm][fac]*Math.sqrt(d);
                }
                else{
                    capacidad[fac] -= trabajoRequerido[comm];
                    weights2[comm][fac] += trabajoRequerido[comm];
                    trabajoRequerido[comm]=0;
                    solvalue+= weights2[comm][fac]*Math.sqrt(d);
                }
            }


        }

        double b = System.currentTimeMillis();


        for (int i = 0; i < n; i++) {
            if(trabajoRequerido[i]!=0) return new double[]{-1,b-a};
        }


        return new double[]{solvalue,b-a};

    }


    public static void main(String[] args) throws GRBException, IOException {
        int[] nn = {100,200,500};
        int[] pp = {2,3,4,5,10,15,20};
        //int[] nn = {5000};
        //int[] pp = {100};

        FileWriter fw = new FileWriter("DatosDistribucionErrorHeuristico.txt");
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter pw = new PrintWriter(bw);

        FileWriter fw2 = new FileWriter("DatosDistribucionCorrectoHeuristico.txt");
        BufferedWriter bw2 = new BufferedWriter(fw2);
        PrintWriter pw2 = new PrintWriter(bw2);


        int totalmal=0;
        int repes = 2000;
        for (int n :  nn) {
            for(int p : pp){
                Instancia instancia = new Instancia(n,p);

                Discretization disc = new VoronoiPointsDiscretization(instancia);
                Discretization disc2 = new UniformDiscretization(new Point(0,0),new Point(10,10), 0.1);

                Constructor constructivo = new RandomConstructor(disc,instancia);




                int mal = 0;


                for (int i = 0; i < repes; i++) {
                    Solucion s = constructivo.buildSolution(false);
                    Solucion s2 = constructivo.buildSolution(false);
                    double D = instancia.getD();
                    double W = instancia.getW();
                    Point[] fac = s.getFacilities();
                    Point[] com = instancia.getComunities();
                    double[] wi = instancia.getWeights();
                    weights = new double[n][p];

                    double[] solPL = PL(n,p,W,com,D,wi,fac);


                    weights2 = new double[n][p];

                    double[] solHeur = PLHeuristic(n,p,W,com,D,wi,fac);

                    double LP1 = solPL[0];
                    double Heur1 = solHeur[0];

                    weights = new double[n][p];

                    solPL = PL(n,p,W,com,D,wi,s2.getFacilities());


                    weights2 = new double[n][p];

                    solHeur = PLHeuristic(n,p,W,com,D,wi,s2.getFacilities());

                    double LP2 = solPL[0];
                    double Heur2 = solHeur[0];


                    if((LP1>LP2 && Heur1<=Heur2) || (LP2>LP1 && Heur2<=Heur1) || (LP1==LP2 && Heur1!=Heur2)){
                        mal++;
                        double v = (LP1-LP2)/Math.min(LP1,LP2);
                        String aux  = Double.toString(v);
                        pw.print(aux+"\n");
                    }
                    else{
                        double v = (LP1-LP2)/Math.min(LP1,LP2);
                        String aux  = Double.toString(v);
                        pw2.print(aux+"\n");
                    }

                }

                double porc = ((double)mal*100.0)/(double)repes;

                totalmal+=mal;

                System.out.println("Conjunto: n="+n+" p="+p+" ");
                System.out.println("TorneoError = " + mal +": "+porc+"%");
                System.out.println("------------------------------");
                pw.flush();
                pw2.flush();
            }


        }

        double totalrepes = repes*21;
        double errortotal = (totalmal*100.0)/totalrepes;
        System.out.println("Porcentaje de error final: "+errortotal);







    }
}
