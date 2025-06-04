package Experimentos;

import GeometryBasics.*;
import com.gurobi.gurobi.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import Estructuras.*;

public class Drawer {
    public static void main(String[] args) throws GRBException, IOException {

        int n = 5;
        Point[] poblaciones = new Point[n];
        poblaciones[0]=new Point(0,0);
        poblaciones[1] = new Point(4,4);
        poblaciones[2] = new Point(2,4);
        poblaciones[3] = new Point(1,2);
        poblaciones[4] = new Point(0,2);

        int p=2;
        double D = 1;
        double Dh = 0;

        Instancia i = new Instancia(p,poblaciones,D,Dh);

        Point[] facilities = new Point[p];


        double[][] v0 = new double[1000][1000];
        double[][] v1 = new double[1000][1000];
        double[][] v2 = new double[1000][1000];
        double[][] v3 = new double[1000][1000];
        double[][] v4 = new double[1000][1000];
        double[][] f = new double[1000][1000];


        for (int j = 0; j < 1000; j++) {
            for (int k = 0; k < 1000; k++) {
                facilities[0]= new Point(0.005*j,0.005*k);
                facilities[1] = new Point(3,2);

                if(0.005*j==3 && 0.005*k==2) continue;

                double[][] wij = new double[n][p];

                wij[0][0]=1;
                wij[1][1]=1;
                wij[2][1]=1;
                wij[3][0]=1;
                wij[4][0]=1;

                Solucion s = new Solucion(facilities,wij,i);

                s.adjustWeightToLP();

                double[][] aux = s.getWeights();

                v0[j][k] = aux[0][0];
                v1[j][k] = aux[1][0];
                v2[j][k] = aux[2][0];
                v3[j][k] = aux[3][0];
                v4[j][k] = aux[4][0];

                f[j][k] = s.eval();
            }

        }

        File archivo = new File("v0");
        FileWriter fw = new FileWriter(archivo);
        BufferedWriter bw = new BufferedWriter(fw);

        System.out.println("-------------------");

        for (int j = 0; j < 1000; j++) {
            for (int k = 0; k < 1000; k++) {
                bw.write(v0[j][k]+"\n");
            }
        }
        bw.flush();

        archivo = new File("v1");
        fw = new FileWriter(archivo);
        bw = new BufferedWriter(fw);


        for (int j = 0; j < 1000; j++) {
            for (int k = 0; k < 1000; k++) {
                bw.write(v1[j][k]+"\n");
            }
        }
        bw.flush();

        archivo = new File("v2");
        fw = new FileWriter(archivo);
        bw = new BufferedWriter(fw);

        for (int j = 0; j < 1000; j++) {
            for (int k = 0; k < 1000; k++) {
                bw.write(v2[j][k]+"\n");
            }
        }
        bw.flush();

        archivo = new File("v3");
        fw = new FileWriter(archivo);
        bw = new BufferedWriter(fw);

        for (int j = 0; j < 1000; j++) {
            for (int k = 0; k < 1000; k++) {
                bw.write(v3[j][k]+"\n");
            }
        }
        bw.flush();

        archivo = new File("v4");
        fw = new FileWriter(archivo);
        bw = new BufferedWriter(fw);

        for (int j = 0; j < 1000; j++) {
            for (int k = 0; k < 1000; k++) {
                bw.write(v4[j][k]+"\n");
            }
        }
        bw.flush();

        archivo = new File("f");
        fw = new FileWriter(archivo);
        bw = new BufferedWriter(fw);

        for (int j = 0; j < 1000; j++) {
            for (int k = 0; k < 1000; k++) {
                bw.write(f[j][k]+"\n");
            }
        }
        bw.flush();

        System.out.println();
        System.out.println("-------------------");








    }
}
