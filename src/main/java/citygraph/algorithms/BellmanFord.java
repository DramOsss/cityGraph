package citygraph.algorithms;

import citygraph.graph.GrafoTransporte;
import citygraph.model.CriterioOptimizacion;
import citygraph.model.ResultadoRuta;
import citygraph.model.Ruta;

import java.util.*;

/**
 * Implementación del algoritmo de Bellman-Ford para el Sistema de Gestión de Rutas.
 * * Este algoritmo permite encontrar la ruta más corta en un grafo dirigido, con la
 * capacidad crítica de manejar aristas con pesos negativos (útil para representar
 * descuentos o tarifas especiales) y detectar ciclos negativos.
 */
public class BellmanFord {

    /**
     * Calcula la ruta óptima entre dos paradas basándose en un criterio de optimización.
     * * El algoritmo ejecuta un proceso de relajación de aristas durante (n-1) iteraciones.
     * Incluye una optimización de finalización anticipada si las distancias convergen
     * antes de completar todas las iteraciones.
     * * @param grafo El grafo dirigido que representa la red de transporte urbano.
     * @param origenId Identificador único de la parada de salida.
     * @param destinoId Identificador único de la parada de llegada.
     * @param criterio El factor a minimizar: TIEMPO, DISTANCIA o COSTO.
     * @return Un objeto ResultadoRuta con el desglose del camino y métricas acumuladas.
     * @throws IllegalStateException Si se detecta un ciclo de peso negativo en la red.
     */
    public static ResultadoRuta calcular(GrafoTransporte grafo,
                                         String origenId,
                                         String destinoId,
                                         CriterioOptimizacion criterio) {

        Objects.requireNonNull(grafo, "grafo null");
        Objects.requireNonNull(origenId, "origenId null");
        Objects.requireNonNull(destinoId, "destinoId null");
        Objects.requireNonNull(criterio, "criterio null");



        grafo.obtenerParada(origenId);
        grafo.obtenerParada(destinoId);

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();

        for (var parada : grafo.listarParadas()) {
            dist.put(parada.getId(), Double.POSITIVE_INFINITY);
        }
        dist.put(origenId, 0.0);

        List<Ruta> todasLasRutas = new ArrayList<>();
        grafo.listarParadas().forEach(p -> todasLasRutas.addAll(grafo.vecinosDe(p.getId())));

        int n = grafo.numeroParadas();

        for (int i = 1; i < n; i++) {
            boolean huboCambio = false;

            for (Ruta r : todasLasRutas) {
                String u = r.getOrigenId();
                String v = r.getDestinoId();

                double du = dist.getOrDefault(u, Double.POSITIVE_INFINITY);
                if (Double.isInfinite(du)) continue;

                double peso = pesoSegunCriterio(r, criterio);
                double candidato = du + peso;

                if (candidato < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, candidato);
                    prev.put(v, u);
                    huboCambio = true;
                }
            }

            if (!huboCambio) break;
        }

        for (Ruta r : todasLasRutas) {
            String u = r.getOrigenId();
            String v = r.getDestinoId();

            double du = dist.getOrDefault(u, Double.POSITIVE_INFINITY);
            if (Double.isInfinite(du)) continue;

            double peso = pesoSegunCriterio(r, criterio);

            if (du + peso < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                throw new IllegalStateException(
                        "Se detectó un ciclo negativo. No existe una ruta óptima válida."
                );
            }
        }

        double mejor = dist.getOrDefault(destinoId, Double.POSITIVE_INFINITY);
        if (Double.isInfinite(mejor)) {
            return resultadoVacio();
        }

        List<String> camino = reconstruirCamino(prev, origenId, destinoId);
        List<Ruta> tramos = reconstruirTramos(grafo, camino);
        Totales totales = calcularTotales(tramos);

        return new ResultadoRuta(
                camino,
                true,
                mejor,
                totales.tiempo,
                totales.distancia,
                totales.costo,
                totales.transbordos
        );
    }

    /**
     * Extrae el peso de la ruta según el criterio seleccionado por el usuario.
     * * @param r La instancia de la ruta a evaluar.
     * @param criterio El criterio de optimización activo.
     * @return El valor numérico del peso (minutos, kilómetros o unidades monetarias).
     */
    private static double pesoSegunCriterio(Ruta r, CriterioOptimizacion criterio) {
        return switch (criterio) {
            case TIEMPO -> r.getTiempoMin();
            case DISTANCIA -> r.getDistanciaKm();
            case COSTO -> r.getCosto();
            case TRANSBORDOS -> throw new UnsupportedOperationException(
                    "TRANSBORDOS no está soportado en este algoritmo."
            );
        };
    }

    /**
     * Reconstruye la secuencia de IDs de paradas desde el destino hasta el origen.
     * * @param prev Mapa de precedencia generado por el algoritmo.
     * @param origenId ID de la parada inicial.
     * @param destinoId ID de la parada final.
     * @return Lista ordenada de paradas que forman el camino más corto.
     */
    private static List<String> reconstruirCamino(Map<String, String> prev,
                                                  String origenId,
                                                  String destinoId) {
        LinkedList<String> camino = new LinkedList<>();
        String actual = destinoId;

        while (actual != null) {
            camino.addFirst(actual);
            if (actual.equals(origenId)) break;
            actual = prev.get(actual);
        }

        if (camino.isEmpty() || !camino.getFirst().equals(origenId)) {
            return Collections.emptyList();
        }

        return camino;
    }

    /**
     * Reconstruye la lista de objetos Ruta que conectan las paradas del camino calculado.
     * * @param grafo El grafo que contiene la información de las conexiones.
     * @param camino Lista ordenada de IDs de paradas que forman el trayecto.
     * @return Una lista de objetos {@code Ruta} que representan cada tramo del viaje.
     * @throws IllegalStateException Si no se encuentra una arista física entre dos paradas consecutivas del camino.
     */
    private static List<Ruta> reconstruirTramos(GrafoTransporte grafo, List<String> camino) {
        List<Ruta> tramos = new ArrayList<>();

        for (int i = 0; i < camino.size() - 1; i++) {
            String origen = camino.get(i);
            String destino = camino.get(i + 1);

            Ruta ruta = grafo.vecinosDe(origen).stream()
                    .filter(r -> r.getDestinoId().equals(destino))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No se encontró la arista del camino: " + origen + " -> " + destino
                    ));

            tramos.add(ruta);
        }

        return tramos;
    }

    /**
     * Agrega y calcula los valores totales de tiempo, distancia, costo y transbordos de la ruta.
     * * @param tramos Lista de rutas que componen el trayecto completo.
     * @return Un record {@code Totales} con las sumatorias de todos los parámetros de la ruta.
     */
    private static Totales calcularTotales(List<Ruta> tramos) {
        double tiempo = 0.0;
        double distancia = 0.0;
        double costo = 0.0;

        for (Ruta ruta : tramos) {
            tiempo += ruta.getTiempoMin();
            distancia += ruta.getDistanciaKm();
            costo += ruta.getCosto();
        }

        int transbordos = calcularTransbordos(tramos);

        return new Totales(tiempo, distancia, costo, transbordos);
    }

    /**
     * Contabiliza los transbordos realizados en el trayecto basándose en el tipo de transporte.
     * * Se incrementa el contador cada vez que el tipo de transporte de un tramo
     * difiere del tramo inmediatamente anterior.
     * * @param tramos Lista de objetos Ruta que componen el camino.
     * @return El número total de transbordos detectados.
     */
    private static int calcularTransbordos(List<Ruta> tramos) {
        if (tramos.isEmpty()) return 0;

        int total = 0;
        for (int i = 1; i < tramos.size(); i++) {
            Ruta anterior = tramos.get(i - 1);
            Ruta actual = tramos.get(i);

            if (anterior.getTipoTransporte() != actual.getTipoTransporte()) {
                total++;
            }
        }

        return total;
    }

    /**
     * Genera un objeto de resultado que representa la ausencia de una ruta válida.
     * * Se utiliza cuando el destino es inalcanzable desde el origen, estableciendo
     * la distancia en infinito y los contadores en cero.
     * * @return Un {@code ResultadoRuta} con valores por defecto para rutas inexistentes.
     */
    private static ResultadoRuta resultadoVacio() {
        return new ResultadoRuta(
                Collections.emptyList(),
                false,
                Double.POSITIVE_INFINITY,
                0.0,
                0.0,
                0.0,
                0
        );
    }

    /**
     * Registro (Record) que encapsula los metadatos finales de una ruta calculada.
     * * Almacena de forma inmutable los totales de tiempo, distancia, costo y
     * la cantidad de cambios de transporte realizados.
     */
    private record Totales(double tiempo, double distancia, double costo, int transbordos) {}
}