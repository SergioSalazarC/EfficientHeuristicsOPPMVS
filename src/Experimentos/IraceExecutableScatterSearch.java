package Experimentos;

import Constructivos.*;
import FuncionesPuntos.*;
import com.gurobi.gurobi.*;
import Estructuras.*;
import AlgoritmosMejora.*;
import Discretizaciones.*;
import BusquedasLocales.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class IraceExecutableScatterSearch {
    public static void main(String[] args) throws FileNotFoundException, GRBException {
        /*
        En este ejecutable se van a usar las instancias del previo
        Por lo tanto una instancia viene definido unicamente por n y p en este formato:
        n p

        PARAMETROS PARA SCATTER SEARCH:
        sizeRefSet Z[1,10]
        sizeDivSet Z[1,10]
        graspType Z[1,2]
        alpha   R[0,1]
        initialSize Z[10,500]
        gens Z[10,500]
        k Z[1,100]
         */

        String file_name = args[0];
        int sizeRefSet = Integer.parseInt(args[1]);
        int sizeDivSet = Integer.parseInt(args[2]);
        int graspType = Integer.parseInt(args[3]);
        double alpha = Double.parseDouble(args[4]);
        int initalSize = Integer.parseInt(args[5]);
        int gens = Integer.parseInt(args[6]);
        int k = Integer.parseInt(args[7]);


        File file = new File(file_name);
        Scanner read = new Scanner(file);

        int n = read.nextInt();
        int p = read.nextInt();

        Instancia instancia = new Instancia(n,p);

        ScatterSearch ss = new ScatterSearch(sizeRefSet,sizeDivSet,new MagneticLocalSearch(),instancia);

        Discretization disc = new VoronoiPointsDiscretization(instancia);

        PointEvaluationFunction f = new kNearestFunction(k,instancia);

        Constructor constructor = null;
        if(graspType==1) constructor = new GRASP_ConstructorLazy(alpha,disc,instancia,f);
        else constructor = new GRASP_ConstructorNotCheck(alpha,disc,instancia,f);

        ss.generationStart(constructor,initalSize);

        for (int i = 0; i < gens; i++) {
            ss.newGeneration();
        }
        System.out.println(ss.getBestSolucion().eval());







    }
}
