package citygraph.model;

import java.util.Collections;
import java.util.List;

public class ResultadoRuta {
    private final List<String> camino;
    private final boolean existe;

    private final double pesoOptimo;
    private final double tiempoTotal;
    private final double distanciaTotal;
    private final double costoTotal;
    private final int transbordosTotal;

    public ResultadoRuta(List<String> camino,
                         boolean existe,
                         double pesoOptimo,
                         double tiempoTotal,
                         double distanciaTotal,
                         double costoTotal,
                         int transbordosTotal) {

        this.camino = camino == null ? List.of() : List.copyOf(camino);
        this.existe = existe;
        this.pesoOptimo = pesoOptimo;
        this.tiempoTotal = tiempoTotal;
        this.distanciaTotal = distanciaTotal;
        this.costoTotal = costoTotal;
        this.transbordosTotal = transbordosTotal;
    }

    public List<String> getCamino() { return Collections.unmodifiableList(camino); }
    public boolean existeRuta() { return existe; }

    public double getPesoOptimo() { return pesoOptimo; }
    public double getTiempoTotal() { return tiempoTotal; }
    public double getDistanciaTotal() { return distanciaTotal; }
    public double getCostoTotal() { return costoTotal; }
    public int getTransbordosTotal() { return transbordosTotal; }

    @Override
    public String toString() {
        if (!existe) return "No existe ruta.";
        return "Camino: " + camino +
                " | Peso óptimo: " + pesoOptimo +
                " | t=" + tiempoTotal + "min" +
                " | d=" + distanciaTotal + "km" +
                " | $=" + costoTotal +
                " | tr=" + transbordosTotal;
    }
}