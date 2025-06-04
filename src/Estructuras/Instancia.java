package Estructuras;

import GeometryBasics.Point;

public class Instancia {

    private int n;
    private int p;
    private Point[] comunities;
    private double[] weights;  // w_i

    private double D;

    private double Dhat;

    private double W;


    public Instancia(int p, Point[] comunities, double[] weights, double d, double dhat) {
        this.p = p;
        this.n = comunities.length;
        this.comunities = comunities;
        this.weights = weights;
        D = d;
        Dhat = dhat;
        for (double weight : weights) {
            W += weight;
        }
    }

    public Instancia(int n, int p){
        double D = 15 / Math.sqrt(n * p);
        double Dhat = 2 * D;

        Point[] poblaciones = new Point[n];

        int r_x = 97;
        int r_y = 367;
        int lambda = 12219;

        for (int i = 0; i < n; i++) {
            poblaciones[i] = (new Point(r_x / 10000.0, r_y / 10000.0));
            r_x = transform(r_x, lambda);
            r_y = transform(r_y, lambda);
        }


        double[] weights = new double[n];

        for (int i = 0; i < n; i++) {
            weights[i] = 1; //random.nextInt(1,11);
        }

        for (double weight : weights) {
            W += weight;
        }

        this.n = n;
        this.p = p;
        this.comunities = poblaciones;
        this.weights = weights;
        this.D=D;
        this.Dhat = Dhat;

    }

    public Instancia(int p, Point[] comunities, double d, double dhat) {
        this.p = p;
        this.n = comunities.length;
        this.comunities = comunities;

        this.weights = new double[this.n];

        for (int i = 0; i < n; i++) {
            weights[i] = 1;
        }

        D = d;
        Dhat = dhat;
        //optimizar
        for (double weight : weights) {
            W += weight;
        }
    }

    public int getN() {
        return n;
    }


    public int getP() {
        return p;
    }

    public double getW() {
        return W;
    }

    public Point[] getComunities() {
        return comunities;
    }


    public double[] getWeights() {
        return weights;
    }


    public double getD() {
        return D;
    }


    public double getDhat() {
        return Dhat;
    }

    private static int transform(int r_k, int lambda) {
        double r_x = 12219 * r_k - Math.floor(lambda * r_k / 100000) * 100000;
        return (int) r_x;
    }


}
