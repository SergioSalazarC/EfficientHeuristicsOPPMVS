package Experimentos;

import AlgoritmosMejora.Memetic;
import BusquedasLocales.LocalSearch;
import BusquedasLocales.MagneticLocalSearch;
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
import Mutaciones.DiscretizationMutacion;
import Mutaciones.Mutacion;
import com.gurobi.gurobi.GRBException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class BoxChartsData {

    public static void main(String[] args) throws GRBException, IOException {

        FileWriter fw = new FileWriter("BoxDATA.csv");
        BufferedWriter bw = new BufferedWriter(fw);

        //bw.write("TimeSinArgs,Evaluations,Generations,Value,AlgID,RunID\n");

        int[] nn = {100, 200, 500};
        int[] pp = {2};
        String[] mut = {"DiscMut", "NormalMut"};

        bw.write("100_2,100_3,100_4,100_5,100_10,100_15,100_20,200_2,200_3,200_4,200_5,200_10,200_15,200_20,500_2,500_3,500_4,500_5,500_10,500_15,500_20\n");

        for (int reps = 0; reps < 3; reps++) {
            StringBuilder nLine = new StringBuilder();
            for (int n : nn) {
                for (int p : pp) {

                    Instancia instancia = new Instancia(n, p);

                    double a = System.currentTimeMillis();

                    Discretization disc = new VoronoiPointsDiscretization(instancia);

                    Constructor random = new RandomFeasibleConstructor(disc, instancia);

                    PointEvaluationFunction f = new kNearestFunctionWithThreshold(n / 20, instancia, instancia.getD() * 0.8);
                    Constructor grasp = new GRASP_ConstructorLazy(0.5, disc, instancia, f);

                    LocalSearch ls = new MagneticLocalSearch();

                    int[] sizePoblacion = {25, 25};
                    Constructor[] constructivos = {random, grasp};

                    Cruce cruce = new VotacionClusterizadaCruce();

                    Mutacion mutacion = new DiscretizationMutacion();

                    int sizeMut = Math.max(p / 4, 1);

                    Memetic memetico = new Memetic(sizePoblacion, constructivos, instancia, disc, ls, cruce, mutacion, sizeMut, false);


                    for (int i = 0; i < 200; i++) {
                        memetico.newGen(0.6, 0.1, 1.0 / 16.0, false);
                    }

                    nLine.append(memetico.getBestSolucion().eval());
                    if (!(n == 500 && p == 20)) {
                        nLine.append(",");
                    }

                }
                bw.write(nLine + "\n");
                bw.flush();
                nLine = new StringBuilder();


            }
        }


    }
}
