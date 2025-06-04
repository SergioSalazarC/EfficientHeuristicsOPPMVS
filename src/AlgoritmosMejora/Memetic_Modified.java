package AlgoritmosMejora;

import BusquedasLocales.LocalSearch;
import Constructivos.Constructor;
import Cruces.Cruce;
import Discretizaciones.Discretization;
import Estructuras.Instancia;
import Estructuras.Solucion;
import Mutaciones.Mutacion;
import com.gurobi.gurobi.GRBException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class Memetic_Modified {

    private Solucion[] poblacion;
    private int volumenTotalPoblacion;

    private int[] volumenPoblacion;
    private Instancia instancia;

    private int n;

    private int p;
    private Discretization discretizacion;
    private LocalSearch busquedaLocal;
    private Constructor[] constructivos;
    private ArrayList<Double> historico;
    private Cruce cruce;
    private Mutacion mutacion;

    private int volumenMutacion;
    private long evaluations = 0;



    public Memetic_Modified(int[] volumenPoblacion, Constructor[] constructivos, Instancia instancia, Discretization discretizacion, LocalSearch busquedaLocal, Cruce cruce, Mutacion mutacion, int tamMutacion, boolean heuristicEvaluation) throws GRBException {
        this.volumenTotalPoblacion =0;
        for (int i : volumenPoblacion) this.volumenTotalPoblacion +=i;

        this.volumenPoblacion = volumenPoblacion;

        poblacion = new Solucion[this.volumenTotalPoblacion];
        int index = 0;
        for (int i = 0; i < volumenPoblacion.length; i++) {
            int cantidad = volumenPoblacion[i];
            for (int j = 0; j < cantidad; j++) {
                poblacion[index] = constructivos[i].buildSolution(heuristicEvaluation);
                index++;
            }
        }

        this.constructivos = constructivos;

        this.instancia = instancia;

        this.p = p;

        this.n = n;

        this.discretizacion = discretizacion;

        this.busquedaLocal = busquedaLocal;

        this.cruce = cruce;

        this.mutacion = mutacion;

        this.volumenMutacion = tamMutacion;

        this.historico = new ArrayList<>();
    }




    public void newGenReducingLS(double ratioCross, double ratioMut, double ratioLS, int sizeAplicated, int iterMax, boolean heuristicEvaluation) throws GRBException {
        int p = instancia.getP();
        LinkedList<Solucion> candidatos = new LinkedList<>();
        for(Solucion s : this.poblacion){
            candidatos.add(s);
        }
        //Cruzamos el 60% de la poblacion
        //Mutamos el 10%, con soluciones del conjunto restante
        int indexCross = (int) (volumenTotalPoblacion *ratioCross);
        int indexMut = indexCross + (int) (volumenTotalPoblacion * ratioMut);

        int sizeMut = Math.max(p/4,1);

        for (int i = 0; i < indexCross; i+=2) {
            evaluations++;
            candidatos.add(cruce.cross(poblacion[i], poblacion[i+1],heuristicEvaluation));
        }
        for (int i = indexCross; i < indexMut; i++) {
            evaluations++;
            candidatos.add(mutacion.mutate(poblacion[i], discretizacion,sizeMut,heuristicEvaluation));
        }

        Collections.shuffle(candidatos);



        int indexLS = (int) (volumenTotalPoblacion *ratioLS);
        for (int i = 0; i < indexLS; i++) {
            Solucion s = candidatos.removeFirst();
            int n = s.getInstancia().getN();
            s = busquedaLocal.improveSolution(s,sizeAplicated,iterMax);
            candidatos.addLast(s);
        }

        Collections.shuffle(candidatos);

        //Aplicamos el torneo binario
        Solucion[] newGen =new Solucion[volumenTotalPoblacion];

        for (int i = 0; i < volumenTotalPoblacion; i++) {
            Solucion s1 = candidatos.removeFirst();
            Solucion s2 = candidatos.removeFirst();
            evaluations+=2;
            if(s1.eval()<s2.eval()){
                newGen[i]=s1;
                candidatos.addLast(s2);
            }
            else{
                newGen[i]=s2;
                candidatos.addLast(s1);
            }
        }

        this.poblacion = newGen;

        historico.add(this.avgSol());


        int sizeHistorial = 30;

        if(historico.size()>sizeHistorial){
            int n = historico.size();
            double acum = 0;
            double lastValue = historico.get(n-1);
            for (int i = 0; i < sizeHistorial; i++) {
                acum+= historico.get(n-1-i);
            }
            acum /= sizeHistorial;

            if(Math.abs(lastValue/acum - 1) < 0.05){
                this.rebuildPop(heuristicEvaluation);
            }

        }

    }

    public void newGenReducingSize(double ratioCross, double ratioMut, double ratioLS, int sizeAplicated, boolean heuristicEvaluation) throws GRBException {
        LinkedList<Solucion> candidatos = new LinkedList<>();
        for(Solucion s : this.poblacion){
            candidatos.add(s);
        }
        //Cruzamos el 60% de la poblacion
        //Mutamos el 10%, con soluciones del conjunto restante
        int indexCross = (int) (volumenTotalPoblacion *ratioCross);
        int indexMut = indexCross + (int) (volumenTotalPoblacion * ratioMut);

        int sizeMut = (int) Math.max(Math.ceil(p/4),1);

        for (int i = 0; i < indexCross; i+=2) {
            evaluations++;
            candidatos.add(cruce.cross(poblacion[i], poblacion[i+1],heuristicEvaluation));
        }
        for (int i = indexCross; i < indexMut; i++) {
            evaluations++;
            candidatos.add(mutacion.mutate(poblacion[i], discretizacion,sizeMut,heuristicEvaluation));
        }

        Collections.shuffle(candidatos);



        int indexLS = (int) (volumenTotalPoblacion *ratioLS);
        for (int i = 0; i < indexLS; i++) {
            Solucion s = candidatos.removeFirst();
            int n = s.getInstancia().getN();
            s = busquedaLocal.improveSolution(s,sizeAplicated);
            candidatos.addLast(s);
        }

        Collections.shuffle(candidatos);

        //Aplicamos el torneo binario
        Solucion[] newGen =new Solucion[volumenTotalPoblacion];

        for (int i = 0; i < volumenTotalPoblacion; i++) {
            Solucion s1 = candidatos.removeFirst();
            Solucion s2 = candidatos.removeFirst();
            evaluations+=2;
            if(s1.eval()<s2.eval()){
                newGen[i]=s1;
                candidatos.addLast(s2);
            }
            else{
                newGen[i]=s2;
                candidatos.addLast(s1);
            }
        }

        this.poblacion = newGen;

        historico.add(this.avgSol());


        int sizeHistorial = 30;

        if(historico.size()>sizeHistorial){
            int n = historico.size();
            double acum = 0;
            double lastValue = historico.get(n-1);
            for (int i = 0; i < sizeHistorial; i++) {
                acum+= historico.get(n-1-i);
            }
            acum /= sizeHistorial;

            if(Math.abs(lastValue/acum - 1) < 0.05){
                this.rebuildPop(heuristicEvaluation);
            }

        }

    }


    public void newGen(double ratioCross, double ratioMut, double ratioLS, boolean heuristicEvaluation) throws GRBException {
        LinkedList<Solucion> candidatos = new LinkedList<>();



        int indexCross = (int) (volumenTotalPoblacion *ratioCross);
        int indexMut = (int) (volumenTotalPoblacion * ratioMut*ratioMut);

        for (int i = 0; i < indexCross; i+=2) {
            evaluations++;
            candidatos.add(cruce.cross(poblacion[i], poblacion[i+1],heuristicEvaluation));
        }

        Collections.shuffle(candidatos);

        for (int i = 0; i < indexMut; i++) {
            evaluations++;
            candidatos.add(mutacion.mutate(candidatos.get(i), discretizacion,volumenMutacion,heuristicEvaluation));
        }

        Collections.shuffle(candidatos);

        int indexLS = (int) (volumenTotalPoblacion *ratioMut * ratioLS);
        if(indexLS==0) indexLS=1;

        for (int i = 0; i < indexLS; i++) {
            Solucion s = candidatos.removeFirst();
            s = busquedaLocal.improveSolution(s);
            candidatos.addLast(s);
        }

        for(Solucion s : this.poblacion){
            candidatos.add(s);
        }

        Collections.shuffle(candidatos);




        //Aplicamos el torneo binario
        Solucion[] newGen =new Solucion[volumenTotalPoblacion];

        for (int i = 0; i < volumenTotalPoblacion; i++) {
            Solucion s1 = candidatos.removeFirst();
            Solucion s2 = candidatos.removeFirst();
            evaluations+=2;
            if(s1.eval()<s2.eval()){
                newGen[i]=s1;
                candidatos.addLast(s2);
            }
            else{
                newGen[i]=s2;
                candidatos.addLast(s1);
            }
        }

        this.poblacion = newGen;

        historico.add(this.avgSol());


        int sizeHistorial = 30;

        if(historico.size()>sizeHistorial){
            int n = historico.size();
            double acum = 0;
            double lastValue = historico.get(n-1);
            for (int i = 0; i < sizeHistorial; i++) {
                acum+= historico.get(n-1-i);
            }
            acum /= sizeHistorial;

            if(Math.abs(lastValue/acum - 1) < 0.05){
                this.rebuildPop(heuristicEvaluation);
            }

        }

    }

    public void rebuildPop(boolean heuristicEvaluation) throws GRBException {


        Solucion[] newPop = new Solucion[volumenTotalPoblacion];
        //System.out.println("Regenera Poblacion");
        newPop[0] = this.getBestSolucion();
        for (int i = 1; i < volumenTotalPoblacion; i++) {
            newPop[i] = constructivos[i% constructivos.length].buildSolution(heuristicEvaluation);
        }
        historico = new ArrayList<>();
        poblacion = newPop;
    }

    public Solucion getBestSolucion(){
        Solucion best = null;
        double value = 1000000000;

        for(Solucion s : poblacion){
            this.evaluations++;
            double val_Act = s.eval();
            if(val_Act < value){
                best = s;
                value = val_Act;
            }
        }

        return best;

    }

    public void showSolutions(){
        for(Solucion s : poblacion){
            System.out.println(s);
        }
        System.out.println("---------------");
    }

    public double bestSol(){
        evaluations++;
        return getBestSolucion().eval();
    }

    public double avgSol(){
        double total = 0;
        double sum = 0;
        for(Solucion s: this.poblacion){
            if(s.isFeasible()){
                sum+=s.eval();
                total+=1;
            }
        }
        return (sum/total);
    }

    public void hardAdjustToLP() throws GRBException {
        for(Solucion s: poblacion){
            s.adjustWeightToLP();
        }
    }


    public long getEvaluations() {
        return evaluations;
    }
}
