package FuncionesPuntos;

import FuncionesPuntos.PointEvaluationFunction;
import GeometryBasics.Point;

public class DummyFunction implements PointEvaluationFunction {

    @Override
    public double f(Point p) {
        return 0;
    }
}
