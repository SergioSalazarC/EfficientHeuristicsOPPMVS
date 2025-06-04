package Estructuras;

import Experimentos.PruebaHeuristicoPL;
import GeometryBasics.Point;
import GeometryBasics.Vector;
import com.gurobi.gurobi.*;

import java.util.*;

public class Solucion implements Iterable<Point>{

    private int p;

    private Point[] facilities;

    private double[][] weights;

    private Instancia instancia;

    private boolean feasible = false;

    private int repairCounter = 0;

    private double EPSILON=1E-4;

    private double INF = 1000000000;

    private double value = INF;




    public Solucion(Point[] facilities, double[][] weights, Instancia instancia) {
        this.p = facilities.length;
        this.facilities = facilities;
        this.weights = weights;
        this.instancia = instancia;
        this.feasible =  true;
    }

    public Solucion(Point[] facilities, Instancia instancia) {
        this.p = facilities.length;
        this.facilities = facilities;
        this.weights = new double[instancia.getN()][instancia.getP()];
        this.instancia = instancia;
    }
    public double[][] getWeights() {
        return weights;
    }

    private boolean checkDistanceFacilities() {
        for (int f1 = 0; f1 < p; f1++) {
            for (int f2 = f1 + 1; f2 < p; f2++) {
                if (facilities[f1].dist(facilities[f2]) < instancia.getDhat()){
                    feasible = false;
                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkLPfeasibility(){
        int n = instancia.getN();
        double D = instancia.getD();
        Point[] coms = instancia.getComunities();
        double sum = 0;

        for (int j = 0; j < p; j++) {
            double minim = 1e8;
            Point fj = facilities[j];

            for (int i = 0; i < n; i++) {
                Point comi = coms[i];
                double dist2 = fj.dist2(comi);
                minim = Math.min(minim,dist2);
            }

            sum += minim;
        }

        if(sum >= D*D*p){
            return true;
        }
        else{
            feasible = false;
            return false;
        }

    }

    public double eval() {
        if(!feasible || !checkDistanceFacilities()){
            value=INF;
            return INF;
        }

        if(value>=INF){
            if(this == null || this.facilities[0] == null) return INF;
            double value = 0;
            double now = 0;
            Point[] com = instancia.getComunities();
            if (instancia.getP() != this.p) return INF;
            for (int c = 0; c < instancia.getN(); c++) {
                for (int f = 0; f < p; f++) {
                    now += weights[c][f];
                    value += (weights[c][f] * com[c].dist(facilities[f]));
                }
                if (Math.abs(instancia.getWeights()[c]-now)>EPSILON) {
                    feasible = false;
                    return 1000000000;
                }

                now = 0;
            }

            HashSet<Point> conj= new HashSet<>();
            for(Point point : facilities){
                conj.add(point);
            }
            if(conj.size()!=p) return 1000000000;

            feasible=true;

            value = Math.round(value * 1_000_000) / 1_000_000.0;

            this.value = value;
        }

        return value;
    }

    public boolean adjustHeuristic(){
        int n = instancia.getN();
        double D = instancia.getD();
        Point[] coms = instancia.getComunities();
        double sum = 0;

        double[] closestDist = new double[p];

        for (int j = 0; j < p; j++) {
            double minim = 1e8;
            Point fj = facilities[j];

            for (int i = 0; i < n; i++) {
                Point comi = coms[i];
                double dist2 = fj.dist2(comi);
                minim = Math.min(minim,dist2);
            }
            closestDist[j] =minim;

            sum += minim;
        }

        if(sum < D*D*p){
            feasible = false;
            return false;
        }


        LinkedList<Trio> lista = new LinkedList<>();
        Point[] com = instancia.getComunities();

        for (int j = 0; j < p; j++) {
            for (int i = 0; i < n; i++) {
                double d2 = facilities[j].dist2(com[i]);
                lista.add(new Trio(j, i, d2));
            }
        }

        double[] trabajoRequerido = instancia.getWeights().clone();

        double W = instancia.getW();

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
                    this.weights[comm][fac] = capacidad[fac];
                    capacidad[fac]=0;
                    solvalue+= this.weights[comm][fac]*Math.sqrt(d);
                }
                else{
                    capacidad[fac] -= trabajoRequerido[comm];
                    this.weights[comm][fac] = trabajoRequerido[comm];
                    trabajoRequerido[comm]=0;
                    solvalue+= this.weights[comm][fac]*Math.sqrt(d);
                }
            }


        }

        for (int i = 0; i < n; i++) {
            if(trabajoRequerido[i]!=0){
                this.feasible = false;
                return false;
            }
        }

        this.feasible = true;
        this.value = solvalue;
        return true;


    }

    private class Trio implements Comparable {
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
            Trio i = (Trio) o;
            return Double.compare(this.d,i.d);
        }
    }

    public void adjustWeightToLemma2() {

        if(!checkLPfeasibility()){
            feasible=false;
            value = INF;
            return ;
        }

        double denom = 0;
        Point[] com = instancia.getComunities();
        double[] wi = instancia.getWeights();
        for (int f = 0; f < p; f++) {
            double closest = 100000000;
            for (int c = 0; c < instancia.getN(); c++) {
                closest = Math.min(closest, facilities[f].dist2(com[c]));
            }
            denom += closest;
        }

        for (int f = 0; f < p; f++) {
            for (int c = 0; c < instancia.getN(); c++) {

                double val = 100000000;
                for (int i = 0; i < instancia.getN(); i++) {
                    val = Math.min(val, facilities[f].dist2(com[i]));
                }

                weights[c][f] = val * wi[c] / denom;

            }
        }
        feasible=true;
        this.value = INF;

    }




    public boolean adjustWeightToLP() throws GRBException {

        int n = instancia.getN();
        double D = instancia.getD();
        Point[] coms = instancia.getComunities();
        double sum = 0;

        double[] closestDist = new double[p];

        for (int j = 0; j < p; j++) {
            double minim = 1e8;
            Point fj = facilities[j];

            for (int i = 0; i < n; i++) {
                Point comi = coms[i];
                double dist2 = fj.dist2(comi);
                minim = Math.min(minim,dist2);
            }
            closestDist[j] =minim;

            sum += minim;
        }

        if(sum < D*D*p){
            feasible = false;
            return false;
        }

        double W = instancia.getW();
        Point[] com = instancia.getComunities();
        double[] wi = instancia.getWeights();


        //Create enviroment
        GRBEnv env = new GRBEnv(true);
        env.set(GRB.DoubleParam.MemLimit, 16);
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
                variables[c][f].set(GRB.DoubleAttr.Start,this.weights[c][f]);
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
            double constnt1 =  (W*closestDist[j])/(D*D*p);
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
            feasible=false;
            model.dispose();
            env.dispose();
            return false;
        }

        for (int c = 0; c < n; c++) {
            for (int f = 0; f < p; f++) {
                weights[c][f] = variables[c][f].get(GRB.DoubleAttr.X);
            }
        }

        this.value = model.get(GRB.DoubleAttr.ObjVal);

        //System.out.println("Value: " + model.get(GRB.DoubleAttr.ObjVal));


        model.dispose();
        env.dispose();

        this.feasible = true;
        return true;

    }

    public void showWeights(){
        int n = instancia.getN();
        for (int i = 0; i < p; i++) {
            System.out.print("Facility "+i+" -> ");
            for (int j = 0; j < n; j++) {
                System.out.print(weights[j][i]+" ");
            }
            System.out.println();
        }
    }

    public Point[] getFacilities() {
        return facilities;
    }

    public Instancia getInstancia() {
        return instancia;
    }

    @Override
    public Solucion clone(){
        return new Solucion(this.facilities.clone(),this.weights.clone(),this.instancia);
    }

    @Override
    public Iterator iterator() {
        return Arrays.stream(facilities).iterator();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Solucion points = (Solucion) o;
        return Arrays.equals(facilities, points.facilities);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(facilities);
    }

    public void printPoints(){
        for(Point p:this.facilities){
            System.out.println("("+p.getX()+","+p.getY()+"),");
        }
    }

    public void printKalczinskyFormat(){
        System.out.print("<|");
        System.out.print("\"n\" -> "+instancia.getN()+", ");
        System.out.print("\"obj\" -> "+this.eval()+", ");
        System.out.print("\"D\" -> "+this.instancia.getD()+", ");
        System.out.print("\"Dhat\" -> "+this.instancia.getDhat()+", ");
        System.out.print("\"locations\" -> {");
        for (int i = 0; i < p; i++) {
            Point pi = this.facilities[i];
            if(i!=p-1){
                System.out.print("{"+pi.getX()+", "+pi.getY()+"}, ");
            }
            else{
                System.out.print("{"+pi.getX()+", "+pi.getY()+"} ");
            }
        }
        System.out.print("}, ");
        System.out.print("\"weights\" -> {");
        for (int i = 0; i < instancia.getN(); i++) {
            System.out.print("{");
            for (int j = 0; j < instancia.getP(); j++) {
                System.out.print(weights[i][j]);
                if(j!=p-1){
                    System.out.print(", ");
                }
            }
            System.out.print("}");
            if(i!=instancia.getN()-1){
                System.out.print(", ");
            }
        }
        System.out.println("}|> ");
    }


    public boolean isFeasible(){
        return feasible;
    }


    @Override
    public String toString() {
        String ret = "{";


        for(Point p: this.facilities){
            ret+=(("("+p.getX()+","+p.getY()+"), "));
        }
        ret+="}";

        return ret;

    }

    public void repair(int[] indicesReparar) throws GRBException {
        for(Point punto : this.facilities){
            if(Math.abs(punto.getX())>100 || Math.abs(punto.getY())>100){
                System.err.println("Roto");
            }
        }

        Random r = new Random();
        double SIGMA = 3.0;
        if(repairCounter>200){
            //System.err.println("No se repara en más de 200 iteraciones");
            repairCounter=0;
            for (int i = 0; i < indicesReparar.length; i++) {
                int indexToRepair =  indicesReparar[i];
                double x = facilities[indexToRepair].getX() + r.nextGaussian()*SIGMA;
                double y = facilities[indexToRepair].getY() + r.nextGaussian()*SIGMA;

                facilities[indexToRepair] = new Point(x,y);

            }

            for(Point point : this.getFacilities()){
                if(Double.isNaN(point.getX()) || Double.isNaN(point.getY())){
                    //System.err.println("Hay un Nan1");
                }
            }
        }
        else{
            HashSet<Integer> indicesRepararSet = new HashSet<>();
            for (Integer i : indicesReparar) {
                indicesRepararSet.add(i);
            }


            double bound = instancia.getD()* instancia.getD()* p;
            double sumDi = 0;
            double sumDhatj2 = 0;
            double sumDhatj = 0;



            Point[] closer = new Point[p];
            int index = 0;


            for(Point facility : facilities){
                Point better = instancia.getComunities()[0];
                double distance2 = facility.dist2(better);
                for(Point comunity: instancia.getComunities()){
                    if(facility.dist2(comunity)<distance2){
                        better = comunity;
                        distance2 = facility.dist2(comunity);
                    }
                }

                if(indicesRepararSet.contains(index)){
                    sumDhatj2+=distance2;
                    sumDhatj+=Math.sqrt(distance2);
                }
                else{
                    sumDi+=distance2;
                }
                closer[index] = better;
                index++;
            }

            if(sumDi+sumDhatj2>=bound){
                //System.err.println("Hay un Nan1");
            }

            double alpha = bound - sumDi;
            double epsilon = sumDhatj2;
            double kk = indicesReparar.length;

            //USAMOS LA FORMULA DE BASHKARA PARA SABER EL VALOR TAU QUE HAY QUE MOVERSE
            double a = kk;
            double b = 2*sumDhatj;
            double c = epsilon-alpha;

            double theta = -b + Math.sqrt(b*b - 4*a*c);
            theta = theta/(2*a);




            double mov_pendiente =theta;
            //System.out.println(mov_pendiente);

            if(mov_pendiente<0.01){
                mov_pendiente=0.01;
            }

            Point[] newfacilities = this.facilities.clone();




            for (int k = 0; k < indicesReparar.length; k++) {
                int i = indicesReparar[k];
                Vector v = new Vector(closer[i],facilities[i]);
                if(Math.abs(v.getX())<0.001 && Math.abs(v.getY())<0.001){
                    double x = r.nextGaussian()*SIGMA;
                    double y = r.nextGaussian()*SIGMA;
                    Point newfacility = facilities[i].add(new Vector(x,y));
                    newfacilities[i] = newfacility;
                    //System.err.println("Estas extrañamente cerca de una comunidad");
                }
                else{
                    v = v.unitary();
                    v.mul(mov_pendiente);
                    Point newfacility = facilities[i].add(v);
                    newfacilities[i] = newfacility;
                    if(Double.isNaN(newfacility.getX()) || Double.isNaN(newfacility.getY())){
                        //System.out.println("Hay un Nan2");
                    }
                }
            }

            this.facilities = newfacilities;
            for(Point point : this.getFacilities()){
                if(Double.isNaN(point.getX()) || Double.isNaN(point.getY())){
                    //System.out.println("Hay un Nan3");
                }
            }
        }

        //System.out.println(this.facilities[1]);

        this.adjustWeightToLP();
        repairCounter++;


    }
}
