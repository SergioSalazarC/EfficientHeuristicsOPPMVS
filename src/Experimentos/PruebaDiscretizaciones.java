package Experimentos;

import GeometryBasics.Point;
import com.gurobi.gurobi.*;

import java.util.HashSet;

import Estructuras.*;
import Discretizaciones.*;
import Constructivos.*;
import FuncionesPuntos.*;

public class PruebaDiscretizaciones {



    public static void main(String[] args) throws GRBException {

        int repeticiones = 10;

        int n = 500;
        int p = 20;

        Instancia instance = new Instancia(n,p);


        Point oo = new Point(0, 0);
        Point ii = new Point(10, 10);


        Discretization[] discs = new Discretization[3];
        discs[0] = new MixDiscretization(instance,oo,ii,1,40,60,1);
        discs[1] = new VoronoiPointsDiscretization(instance);
        discs[2] = new VoronoiEdgeDiscretization(instance,0.1);




        //grad
        HashSet<Point> map = new HashSet<>();
        double[][] min = new double[3][5];
        for (int ik = 0; ik < 1; ik++) {
            for (int id = 0; id < 1; id++) {
                min[id][ik] = 1000000000;

                for (int i = 0; i < repeticiones; i++) {
                    System.out.println(i);
                    Constructor grasp = new GRASP_ConstructorLazy(0.5,discs[id],instance,new kNearestFunction(ik*5+1,instance));
                    Solucion s = grasp.buildSolution(false);

                    Point[] f = s.getFacilities();

                    for (int j = 0; j < f.length; j++) {
                        map.add(f[j]);
                    }

                    if(s==null) continue;
                    double a = System.currentTimeMillis();
                    s.adjustWeightToLP();
                    double b = System.currentTimeMillis()-a;
                    System.out.println(b);
                    min[id][ik] = Math.min(min[id][ik], s.eval());
                }
            }
        }



        for (int ik = 0; ik < 5; ik++) {
            for (int id = 0; id < 3; id++) {
                int kval = ik*5+1;
                System.out.println("disc="+id+" k="+kval+": "+min[id][ik]);
            }
        }











    }
}
