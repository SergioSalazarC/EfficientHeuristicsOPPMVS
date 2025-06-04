package Experimentos;

import AlgoritmosMejora.Memetic;
import BusquedasLocales.HookeJeeves8PLocalSearch;
import BusquedasLocales.LocalSearch;
import Constructivos.Constructor;
import Constructivos.GRASP_ConstructorLazy;
import Constructivos.RandomFeasibleConstructor;
import Cruces.Cruce;
import Cruces.VotacionClusterizadaCruce;
import Discretizaciones.Discretization;
import Discretizaciones.VoronoiPointsDiscretization;
import Estructuras.Instancia;
import FuncionesPuntos.PointEvaluationFunction;
import FuncionesPuntos.kNearestFunctionWithThreshold;
import GeometryBasics.Point;
import Mutaciones.DiscretizationMutacion;
import Mutaciones.Mutacion;
import com.gurobi.gurobi.GRBException;

import java.io.*;
import java.util.StringTokenizer;


public class MainMemeticoIrace {

    public static void main(String[] args) throws GRBException, IOException {
        File instanciaDat = new File (args[0]);
        FileReader fr = new FileReader (instanciaDat);
        BufferedReader br = new BufferedReader(fr);
        StringTokenizer dat =new StringTokenizer(br.readLine());

        int n = Integer.parseInt(dat.nextToken());
        int p = Integer.parseInt(dat.nextToken());


        Instancia instancia = new Instancia(n,p);

        Discretization disc = new VoronoiPointsDiscretization(instancia);

        Constructor random = new RandomFeasibleConstructor(disc,instancia);

        PointEvaluationFunction f = new kNearestFunctionWithThreshold(Integer.parseInt(args[9]),instancia,instancia.getD()*Double.parseDouble(args[10]));
        Constructor grasp = new GRASP_ConstructorLazy(Double.parseDouble(args[2]),disc,instancia,f);

        LocalSearch ls = new HookeJeeves8PLocalSearch(Double.parseDouble(args[7]),Double.parseDouble(args[8]));

        int[] sizePoblacion = {Integer.parseInt(args[1]),50-Integer.parseInt(args[1])};
        Constructor[] constructivos = {random, grasp};

        Cruce cruce = new VotacionClusterizadaCruce();

        Mutacion mutacion = new DiscretizationMutacion();

        double porcentajeMutacion = Integer.parseInt(args[3])/100.0;

        int sizeMut = (int) Math.max(p*porcentajeMutacion,1);

        Memetic memetico = new Memetic(sizePoblacion, constructivos, instancia,disc,ls, cruce, mutacion, sizeMut, true );


        for (int i = 0; i < 1000; i++) {
            memetico.newGen(Double.parseDouble(args[4]),Double.parseDouble(args[5]),Double.parseDouble(args[6]), true);
        }

        memetico.hardAdjustToLP();

        double b = System.currentTimeMillis();

        double sol = memetico.getBestSolucion().eval();

        System.out.print(sol);


    }
}
