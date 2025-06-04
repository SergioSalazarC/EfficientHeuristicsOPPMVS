package Experimentos;

import BusquedasLocales.*;
import GeometryBasics.Point;
import com.gurobi.gurobi.GRBException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import Estructuras.*;
import Discretizaciones.*;
import Constructivos.*;

public class ExperimentoBombing {
    public static void main(String[] args) throws GRBException, IOException {


        /*
        int p = Integer.parseInt(args[0]);
        int n = Integer.parseInt(args[1]);
        int iteraciones = Integer.parseInt(args[2]);

         */

        int p = 3;
        int n =100;
        int iteraciones = 1000000;



        String f = "LogBombing_"+p+"_"+n+".txt";
        File fileLog = new File(f);
        fileLog.createNewFile();
        FileWriter fwLog = new FileWriter(f);

        String f2 = "LogImproveBombing_"+p+"_"+n+".txt";
        File fileImprove = new File(f2);
        fileImprove.createNewFile();
        FileWriter fwImprove = new FileWriter(f2);

        Instancia instancia = new Instancia(n,p);

        Discretization disc = new VoronoiPointsDiscretization(instancia);



        int total = disc.getPuntos().size();

        Constructor c = new RandomConstructor(disc,instancia);

        LocalSearch m = new MagneticLocalSearch();

        double best_value = Double.MAX_VALUE;

        for (int i = 0; i < iteraciones ; i++) {
            Solucion s = c.buildSolution(false);
            double val0 = s.eval();
            fwLog.write(Double.toString(s.eval())+"\n");
            m.improveSolution(s);

            fwLog.write(Double.toString(s.eval())+"\n");
            fwLog.write("\n");
            fwLog.flush();

            double val = s.eval();
            if(val<best_value){
                best_value = val;
                fwImprove.write(Double.toString(val0)+"\n");
                String aux = "";
                fwImprove.write(Double.toString(val)+"\n");
                for(Point fac : s.getFacilities()){
                    aux = aux + "(" + fac.getX()+" , "+fac.getY()+"), ";
                }
                fwImprove.write(aux+"\n");
                fwImprove.write("------------------\n");
                fwImprove.flush();

            }
        }
        fwImprove.close();
        fwLog.close();



    }
}
