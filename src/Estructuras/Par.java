package Estructuras;

public class Par<T, Q> {

    private T e1;
    private Q e2;


    public Par(T e1, Q e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public T getE1() {
        return e1;
    }

    public void setE1(T e1) {
        this.e1 = e1;
    }

    public Q getE2() {
        return e2;
    }

    public void setE2(Q e2) {
        this.e2 = e2;
    }


}
