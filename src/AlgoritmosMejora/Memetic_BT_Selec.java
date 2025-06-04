package AlgoritmosMejora;

import BusquedasLocales.LocalSearch;
import Constructivos.Constructor;
import Cruces.Cruce;
import Cruces.VotacionClusterizadaCruce;
import Discretizaciones.Discretization;
import Estructuras.Instancia;
import Estructuras.Solucion;
import Mutaciones.Mutacion;
import Mutaciones.NormalRandomMutation;
import com.gurobi.gurobi.GRBException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

public class Memetic_BT_Selec {

    private Solucion[] population;

    private int sizePopulation;

    private Instancia instancia;

    private Discretization disc;

    private LocalSearch improver;

    private Constructor[] cons;

    private ArrayList<Double> hist;

    private Cruce cruce;
    private Mutacion mutacion;



    public Memetic_BT_Selec(int sizePopulation, Constructor constr, Instancia instancia, Discretization disc, LocalSearch imp) throws GRBException {
        cons = new Constructor[1];
        cons[0] = constr;
        this.sizePopulation = sizePopulation;
        population = new Solucion[sizePopulation];

        for (int i = 0; i < sizePopulation; i++) {
            population[i] = constr.buildSolution(false);
        }
        this.instancia = instancia;
        this.disc = disc;
        this.improver = imp;
        hist = new ArrayList<>();
        //cruce = new VotacionGreedyCruce();
        cruce = new VotacionClusterizadaCruce();
        mutacion = new NormalRandomMutation();
    }


    public Memetic_BT_Selec(int sizePopulation, Constructor constr1, int size1, Constructor constr2, Instancia instancia, Discretization disc, LocalSearch imp) throws GRBException {
        cons = new Constructor[2];
        cons[0] = constr1;
        cons[1] = constr2;
        this.sizePopulation = sizePopulation;
        population = new Solucion[sizePopulation];

        for (int i = 0; i < sizePopulation; i++) {
            if(i<size1) population[i] = constr1.buildSolution(false);
            else population[i] =constr2.buildSolution(false);
        }

        this.instancia = instancia;
        this.disc = disc;
        this.improver = imp;
        hist = new ArrayList<>();
        cruce = new VotacionClusterizadaCruce();
        mutacion = new NormalRandomMutation();
    }




    public void newGen(double ratioCross, double ratioMut, double ratioLS) throws GRBException {
        int p = instancia.getP();
        LinkedList<Solucion> candidatos = new LinkedList<>();

        //Cruzamos el 60% de la poblacion
        //Mutamos el 10%, con soluciones del conjunto restante
        int indexCross = (int) (sizePopulation*ratioCross);
        indexCross = 20; //Hardcodeado
        int indexMut = indexCross + (int) (sizePopulation* ratioMut);
        indexMut = 5;

        int sizeMut = Math.max(p/4,1);

        Random r = new Random();
        for (int i = 0; i < indexCross; i++) {
            Solucion s1 = population[r.nextInt(0,50)];
            Solucion s2 = population[r.nextInt(0,50)];
            Solucion s3 = population[r.nextInt(0,50)];
            Solucion s4 = population[r.nextInt(0,50)];

            Solucion padre = (s1.eval() < s2.eval() ? s1 : s2);
            Solucion madre = (s3.eval() < s4.eval() ? s3 : s4);
            candidatos.add(cruce.cross(padre,madre,false));
        }
        for (int i = indexCross; i < indexMut; i++) {
            candidatos.add(mutacion.mutate(population[r.nextInt(0,50)],disc,sizeMut,false));
        }

        //Collections.shuffle(candidatos);

        ArrayList<Integer>  permut = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            permut.add(i);
        }

        double bestval = 1000000000;
        int index = -1;
        for (int i = 0; i < 50; i++) {
            if(population[i].eval() < bestval){
                bestval = population[i].eval();
                index = i;
            }
        }

        candidatos.add(population[index]);

        Collections.shuffle(permut);
        int k=0;

        while(candidatos.size()<50) {
            if(permut.get(k)!=index) candidatos.add(population[permut.get(k)]);
            k++;
        }

        int indexLS = (int) (sizePopulation*ratioLS);
        for (int i = 0; i < indexLS; i++) {
            Solucion s = candidatos.removeFirst();
            s = improver.improveSolution(s);
            candidatos.addLast(s);
        }

        int elem = 0;
        for (Solucion s : candidatos) {
            this.population[elem]=s;
            elem++;
        }



        hist.add(this.avgSol());


        int sizeHistorial = 30;

        if(hist.size()>sizeHistorial){
            int n = hist.size();
            double acum = 0;
            double lastValue = hist.get(n-1);
            for (int i = 0; i < sizeHistorial; i++) {
                acum+=hist.get(n-1-i);
            }
            acum /= sizeHistorial;

            if(Math.abs(lastValue/acum - 1) < 0.05){
                this.rebuildPop();
            }

        }

    }

    public void rebuildPop() throws GRBException {


        Solucion[] newPop = new Solucion[sizePopulation];
        //System.out.println("Regenera Poblacion");
        newPop[0] = this.getBestSolucion();
        for (int i = 1; i < sizePopulation; i++) {
            newPop[i] = cons[i%cons.length].buildSolution(false);
        }
        hist = new ArrayList<>();
        population = newPop;



    }




    public Solucion getBestSolucion(){
        Solucion best = null;
        double value = 1000000000;

        for(Solucion s : population){
            double val_Act = s.eval();
            if(val_Act < value){
                best = s;
                value = val_Act;
            }
        }

        return best;

    }







    public void showSolutions(){
        for(Solucion s :population){
            System.out.println(s);
        }
        System.out.println("---------------");
    }

    public double bestSol(){
        return getBestSolucion().eval();
    }

    public double avgSol(){
        double total = 0;
        double sum = 0;
        for(Solucion s: this.population){
            if(s.isFeasible()){
                sum+=s.eval();
                total+=1;
            }
        }
        return (sum/total);
    }
}
