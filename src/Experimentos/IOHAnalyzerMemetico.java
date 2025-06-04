package Experimentos;

import AlgoritmosMejora.Memetic;
import AlgoritmosMejora.Memetic_BT_Selec;
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
import GeometryBasics.Point;
import Mutaciones.DiscretizationMutacion;
import Mutaciones.Mutacion;
import Mutaciones.NormalRandomMutation;
import com.gurobi.gurobi.GRBException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class IOHAnalyzerMemetico {

    public static void main(String[] args) throws GRBException, IOException {

        FileWriter fw = new FileWriter("IOHInf.csv");
        BufferedWriter bw = new BufferedWriter(fw);

        bw.write("TimeSinArgs,Evaluations,Generations,Value,AlgID,RunID\n");

        //int[] nn = {Integer.parseInt(args[0])};
        //int [] pp = {Integer.parseInt(args[1])};
        int[] nn = {200};
        int[] pp = {10};
        String [] mut = {"DiscMut","NormalMut"};


        for (int n : nn) {
            for (int p : pp){
                for (int iteraciones = 0; iteraciones < 20; iteraciones++) {
                    for (String mutacSelec : mut){
                        Instancia instancia = new Instancia(n,p);

                        double a = System.currentTimeMillis();

                        Discretization disc = new VoronoiPointsDiscretization(instancia);

                        Constructor random = new RandomFeasibleConstructor(disc,instancia);

                        PointEvaluationFunction f = new kNearestFunctionWithThreshold(n/20,instancia,instancia.getD()*0.8);
                        Constructor grasp = new GRASP_ConstructorLazy(0.5,disc,instancia,f);

                        LocalSearch ls = new MagneticLocalSearch();

                        int[] sizePoblacion = {25,25};
                        Constructor[] constructivos = {random, grasp};

                        Cruce cruce = new VotacionClusterizadaCruce();

                        Mutacion mutacion = new DiscretizationMutacion();

                        if(mutacSelec.equals("NormalMut")) mutacion = new NormalRandomMutation();

                        int sizeMut = Math.max(p/4,1);

                        Memetic memetico = new Memetic(sizePoblacion, constructivos, instancia,disc,ls, cruce, mutacion,sizeMut, false);


                        double bestValue = Double.MAX_VALUE;

                        for (int i = 0; i < 5000; i++) {
                            memetico.newGen(0.6,0.1,1.0/16.0, false);
                            double val = memetico.bestSol();
                            if(val<bestValue){
                                bestValue = val;
                                double b = System.currentTimeMillis();
                                long time = (long) (b-a);
                                long evaluations = memetico.getEvaluations();
                                double value = bestValue;
                                String algId = "Mem|"+mutacSelec+"|"+iteraciones;
                                String runId = "n:"+n+" p:"+p;
                                bw.write(time+","+evaluations+","+i+","+value+","+algId+","+runId+"\n");
                                System.out.println(time+","+evaluations+","+i+","+value+","+algId+","+runId);
                                bw.flush();
                            }
                            else{
                                System.out.println(i);
                            }

                        }
                    }
                }


            }

        }


    }
}
