package citygraph.service;


import java.util.List;

public class CaminoDFS {

    private final List<String> camino;
    private final double peso;

    public CaminoDFS(List<String> camino, double peso) {
        this.camino = camino;
        this.peso = peso;
    }

    public List<String> getCamino() {
        return camino;
    }

    public double getPeso() {
        return peso;
    }
}
