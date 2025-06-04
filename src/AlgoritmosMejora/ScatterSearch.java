package AlgoritmosMejora;

import BusquedasLocales.*;
import Constructivos.*;
import Estructuras.*;
import GeometryBasics.Point;
import com.gurobi.gurobi.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

public class ScatterSearch {

    private Solucion[] refSet;
    private int sizeRefSet;

    private Solucion[] divSet;
    private int sizeDivSet;

    private LocalSearch improver;

    private Instancia instancia;



    public ScatterSearch(int sizeRefSet, int sizeDivSet, LocalSearch improver, Instancia instancia) {
        refSet = new Solucion[sizeRefSet];
        this.sizeRefSet = sizeRefSet;
        divSet = new Solucion[sizeDivSet];
        this.sizeDivSet = sizeDivSet;
        this.improver = improver;
        this.instancia = instancia;
    }

    public void generationStart(Constructor c, int initialSize) throws GRBException {
        Solucion[] k = new Solucion[initialSize];

        for (int i = 0; i < initialSize; i++) {
            k[i] = c.buildSolution(false);
            k[i] = improver.improveSolution(k[i]);
            //System.out.println(i);
        }

        refreshSets(k);

    }


    public Solucion cross(Solucion s1, Solucion s2, int cant) throws GRBException {

        double DIST = 1;

        int p = instancia.getP();
        int n = instancia.getN();

        ArrayList<Par> votos = new ArrayList<>(2*p);
        for (int i = 0; i < 2 * p; i++) {
            votos.add(new Par(i,0));
        }


        double[][] pesos1 = s1.getWeights();
        double[][] pesos2 = s2.getWeights();
        for (int i = 0; i < n; i++) {
            int index1 = 0;
            int index2 = 0;
            for (int j = 0; j < p; j++) {
                if(pesos1[i][j] > pesos1[i][index1]) index1 = j;
                if(pesos2[i][j] > pesos2[i][index1]) index2 = j;
            }
            if(s1.getFacilities()[index1].dist(instancia.getComunities()[i]) < s2.getFacilities()[index2].dist(instancia.getComunities()[i])){
                //gana1
                votos.get(index1).addVal(1);
            }
            else{
                //gana2
                votos.get(index2+p).addVal(1);
            }
        }

        Collections.sort(votos);

        ArrayList<Point> facilitiesList = new ArrayList<>();

        for (int i = 0; i < 2*p && facilitiesList.size()<p ; i++) {
            Par x = votos.get(i);
            Point posible = null;

            if(x.indice<p){
                posible = s1.getFacilities()[x.indice];
            }
            else{
                posible = s2.getFacilities()[x.indice-p];
            }

            boolean flag_nocerca = false;

            for(Point resto : facilitiesList){
                if(posible.dist(resto) < DIST){
                    flag_nocerca = true;
                    break;
                }
            }

            if(!flag_nocerca) facilitiesList.add(posible);
            //else System.out.println("*");
        }

        Random r = new Random();

        Point[] facilities = new Point[p];
        for (int i = 0; i < p; i++) {
            if(i<facilitiesList.size()){
                facilities[i] = facilitiesList.get(i);
            }
            else{
                facilities[i] = new Point(r.nextDouble()*10,r.nextDouble()*10);
            }
        }


        Solucion cross = new Solucion(facilities,instancia);
        cross.adjustWeightToLP();
        if(cross.eval() >= 10000000){
            //System.out.println("infactible");
            return s1;
        }
        cross = improver.improveSolution(cross);



        return cross;
    }




    /*
    public Solucion cross(Solucion sgood, Solucion sdiv, int cant) throws GRBException {
        Point[] aux = sdiv.getFacilities().clone();
        Point[] good = sgood.getFacilities().clone();
        Random r = new Random();
        HashSet<Integer> indices_remove = new HashSet<>();
        HashSet<Integer> indices_add = new HashSet<>();
        while(indices_remove.size() < cant){
            indices_remove.add(r.nextInt(0,aux.length));
        }
        while(indices_add.size() < cant){
            indices_add.add(r.nextInt(0, aux.length));
        }

        ArrayList<Integer> auxrem = new ArrayList<>(indices_remove);
        ArrayList<Integer> auxadd = new ArrayList<>(indices_add);

        for (int i = 0; i < auxrem.size(); i++) {
            good[auxrem.get(i)] = aux[auxadd.get(i)];
        }

        Solucion cross = new Solucion(good,instancia);
        cross.adjustWeightToLP();
        improver.improveSolution(cross);

        return cross;
    }

     */



    public void newGeneration() throws GRBException {
        HashSet<Solucion> conjunto = new HashSet<>();

        int index = 0;

        for (int i = 0; i < sizeRefSet; i++) {
            for (int j = 0; j < sizeDivSet; j++) {
                conjunto.add(cross(refSet[i],divSet[j],instancia.getP()/2));
            }
        }

        for (int i = 0; i < sizeRefSet; i++) {
            for (int j = i+1; j < sizeRefSet; j++) {
                conjunto.add(cross(refSet[i],refSet[j],instancia.getP()/2));
            }
        }

        for (int i = 0; i < sizeRefSet; i++) {
            conjunto.add( refSet[i]);
        }

        Solucion[] K = new Solucion[conjunto.size()];
        for(Solucion s : conjunto){
            K[index] = s;
            index++;
        }

        refreshSets(K);
    }

    private void refreshSets(Solucion[] k){

        double[] distvalk = new double[k.length];

        HashSet<Integer> mejores = new HashSet<>();
        double maxofmin = 0;
        int index = -1;

        for (int i = 0; i < k.length; i++) {
            double act_value = k[i].eval();
            if(i<sizeRefSet){
                mejores.add(i);
                if(act_value>maxofmin){
                    maxofmin = act_value;
                    index = i;
                }
            }
            else{
                if(act_value<maxofmin){
                    mejores.remove(index);
                    mejores.add(i);
                    double dummyval = 0;
                    int dummyindex = -1;
                    for(Integer j : mejores){
                        double act_val_dummy = k[j].eval();
                        if(act_val_dummy>dummyval){
                            dummyval=act_val_dummy;
                            dummyindex = j;
                        }
                    }
                    index = dummyindex;
                    maxofmin = dummyval;
                }
            }
        }

        index = 0;
        for (Integer i : mejores) {
            refSet[index] = k[i];
            index++;
        }

        mejores = new HashSet<>();
        double minofmax = 10000000;
        index = -1;

        for (int i = 0; i < k.length; i++) {
            double act_value = distance(k[i],refSet);
            distvalk[i] = act_value;
            if(i<sizeDivSet){
                mejores.add(i);
                if(act_value<minofmax){
                    minofmax = act_value;
                    index = i;
                }
            }
            else{
                if(act_value>minofmax){
                    mejores.remove(index);
                    mejores.add(i);
                    double dummyval = 100000000;
                    int dummyindex = -1;
                    for(Integer j : mejores){
                        double act_val_dummy = distvalk[j];
                        if(act_val_dummy<dummyval){
                            dummyval=act_val_dummy;
                            dummyindex = j;
                        }
                    }
                    index = dummyindex;
                    minofmax = dummyval;
                }
            }
        }

        index = 0;
        for(Integer p : mejores){
            divSet[index] = k[p];
            index++;
        }

    }

    public Solucion getBestSolucion(){
        Solucion best = null;
        double value = 1000000000;

        for(Solucion s : refSet){
            double val_Act = s.eval();
            if(val_Act < value){
                best = s;
                value = val_Act;
            }
        }

        return best;

    }







    private double distance(Solucion s0, Solucion[] K){
        double min = 1000000000;
        for(int i = 0 ; i<K.length;i++){
            min = Math.min(min, distance(s0,K[i]));
        }
        return min;
    }
    
    private double distance(Solucion s, Solucion p){
        double val = 0;
        for(Point s_i : s){
            for (Point p_i : p){
                //val+=s_i.dist(p_i);
                val = Math.min(val,s_i.dist(p_i));
            }
        }
        return val;
    }

    private class Par implements Comparable{
        int indice;
        double valor;

        public Par(int indice, double valor) {
            this.indice = indice;
            this.valor = valor;
        }

        public void addVal(double valor){
            this.valor+=valor;
        }

        @Override
        public int compareTo(Object o) {
            Par i = (Par) o;
            return Double.compare(i.valor,this.valor);
        }
    }

}
