package citygraph.model;

import java.util.Collections;
import java.util.List;

/**
 * Clase que encapsula la solución final de un cálculo de ruta entre dos puntos del grafo.
 * * Actúa como un objeto de transferencia de datos (DTO) que contiene tanto la secuencia
 * de paradas (el camino) como las métricas acumuladas (tiempo, distancia, costo y transbordos).
 * Es el resultado estándar devuelto por los algoritmos de optimización del sistema.
 */
public class ResultadoRuta {
    private final List<String> camino;
    private final boolean existe;

    private final double pesoOptimo;
    private final double tiempoTotal;
    private final double distanciaTotal;
    private final double costoTotal;
    private final int transbordosTotal;

    /**
     * Constructor principal para inicializar el reporte de ruta calculada.
     * * @param camino Lista de IDs de paradas. Se genera una copia defensiva para asegurar inmutabilidad.
     * @param existe Estado de disponibilidad de la ruta.
     * @param pesoOptimo Valor del peso minimizado por el algoritmo.
     * @param tiempoTotal Tiempo total acumulado en minutos.
     * @param distanciaTotal Distancia total acumulada en kilómetros.
     * @param costoTotal Costo monetario total del viaje.
     * @param transbordosTotal Número de cambios de transporte detectados.
     */
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

    /**
     * @return Una vista inmodificable de la secuencia de paradas que forman el camino.
     */
    public List<String> getCamino() { return Collections.unmodifiableList(camino); }
    /**
     * @return {@code true} si existe una conexión entre origen y destino; {@code false} en caso de aislamiento.
     */
    public boolean existeRuta() { return existe; }
    /** @return El peso optimizado (tiempo, distancia o costo) según el criterio de búsqueda. */
    public double getPesoOptimo() { return pesoOptimo; }
    /** @return El tiempo total de viaje en minutos. */
    public double getTiempoTotal() { return tiempoTotal; }
    /** @return La distancia total del recorrido en kilómetros. */
    public double getDistanciaTotal() { return distanciaTotal; }
    /** @return El costo total del pasaje. */
    public double getCostoTotal() { return costoTotal; }
    /** @return La cantidad de transbordos realizados. */
    public int getTransbordosTotal() { return transbordosTotal; }

    /**
     * Genera una representación textual detallada del resultado para depuración o logs.
     * * @return Una cadena con el desglose de métricas o un mensaje de error si la ruta no existe.
     */
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