package Experimentos;

import Discretizaciones.*;
import Estructuras.*;
import java.io.IOException;

public class instancesgen {
    public static void main(String[] args) throws IOException {
        int n = 500;
        VoronoiPointsDiscretization vp = new VoronoiPointsDiscretization(new Instancia(n,2));
        vp.printPuntos();
    }
}
