package citygraph.algorithms;

import citygraph.graph.GrafoTransporte;
import citygraph.graph.exceptions.CicloNegativoException;
import citygraph.model.CriterioOptimizacion;
import citygraph.model.ResultadoRuta;
import citygraph.model.Ruta;

import java.util.*;

/**
 * Implementación del algoritmo de Bellman-Ford para la búsqueda de caminos mínimos en el grafo.
 * * A diferencia de Dijkstra, esta implementación es capaz de gestionar aristas con pesos negativos,
 * lo cual es fundamental cuando el criterio de optimización es el COSTO y existen bonificaciones
 * o descuentos en la red de transporte.
 * * El algoritmo opera realizando $V-1$ iteraciones (donde $V$ es el número de paradas) para
 * asegurar la convergencia de las distancias mínimas, incluyendo una fase final de
 * verificación para detectar ciclos de peso negativo.
 */
public class BellmanFord {

    /**
     * Constructor privado para asegurar que la clase se utilice de forma estática
     * como una utilidad de cálculo.
     */
    private BellmanFord() {
    }

    /**
     * Ejecuta el cálculo de la ruta óptima entre dos nodos basándose en un criterio específico.
     * * El proceso incluye:
     * 1. Inicialización de distancias a infinito y origen a cero.
     * 2. Relajación de todas las aristas del grafo durante $N-1$ iteraciones.
     * 3. Verificación de ciclos negativos (si una distancia puede seguir bajando tras $N-1$ pasos).
     * 4. Reconstrucción del camino y cálculo de métricas totales.
     * * @param grafo La instancia de {@link GrafoTransporte} que contiene la red.
     * @param origenId Identificador de la parada de partida.
     * @param destinoId Identificador de la parada de llegada.
     * @param criterio El factor a minimizar (TIEMPO, COSTO, DISTANCIA, etc.).
     * @return Un objeto {@link ResultadoRuta} con el camino detallado y sus costos acumulados.
     * @throws CicloNegativoException Si se detecta un bucle de costo negativo que hace
     * que la ruta óptima sea indefinida (tendiendo al infinito negativo).
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

        List<Ruta> todasLasRutas = recopilarTodasLasRutas(grafo);

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();

        for (var parada : grafo.listarParadas()) {
            dist.put(parada.getId(), Double.POSITIVE_INFINITY);
        }
        dist.put(origenId, 0.0);

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
                throw new CicloNegativoException(
                        "Se detectó un ciclo negativo alcanzable desde el origen. No existe una ruta óptima válida."
                );
            }
        }

        double mejor = dist.getOrDefault(destinoId, Double.POSITIVE_INFINITY);
        if (Double.isInfinite(mejor)) {
            return resultadoVacio();
        }

        List<String> camino = reconstruirCamino(prev, origenId, destinoId);
        if (camino.isEmpty()) {
            return resultadoVacio();
        }

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
     * Valida si el grafo completo contiene ciclos negativos.
     *
     * Esta versión no calcula caminos ni totales. Solo valida el grafo.
     * Inicializa todas las distancias en 0 para simular una super-fuente
     * y así detectar ciclos negativos en cualquier componente del grafo.
     */
    public static void validarSinCicloNegativo(GrafoTransporte grafo) {
        Objects.requireNonNull(grafo, "grafo null");

        List<Ruta> todasLasRutas = recopilarTodasLasRutas(grafo);
        Map<String, Double> dist = new HashMap<>();

        for (var parada : grafo.listarParadas()) {
            dist.put(parada.getId(), 0.0);
        }

        int n = grafo.numeroParadas();

        for (int i = 1; i < n; i++) {
            boolean huboCambio = false;

            for (Ruta r : todasLasRutas) {
                String u = r.getOrigenId();
                String v = r.getDestinoId();

                double candidato = dist.get(u) + r.getCosto();
                if (candidato < dist.get(v)) {
                    dist.put(v, candidato);
                    huboCambio = true;
                }
            }

            if (!huboCambio) break;
        }

        for (Ruta r : todasLasRutas) {
            String u = r.getOrigenId();
            String v = r.getDestinoId();

            if (dist.get(u) + r.getCosto() < dist.get(v)) {
                throw new CicloNegativoException(
                        "Se detectó un ciclo negativo en el grafo. No existe una ruta óptima válida."
                );
            }
        }
    }
    /**
     * Transforma la lista de adyacencia del grafo en una colección plana de todas las aristas.
     * * Dado que Bellman-Ford requiere iterar sobre todas las rutas del sistema en cada
     * paso de relajación, este método centraliza todas las conexiones de todas las
     * paradas en una sola lista para optimizar el bucle principal del algoritmo.
     * * @param grafo El {@link GrafoTransporte} del cual se extraerán las conexiones.
     * @return Una lista que contiene todas las instancias de {@link Ruta} presentes en la red.
     */
    private static List<Ruta> recopilarTodasLasRutas(GrafoTransporte grafo) {
        List<Ruta> rutas = new ArrayList<>();
        for (var parada : grafo.listarParadas()) {
            rutas.addAll(grafo.vecinosDe(parada.getId()));
        }
        return rutas;
    }
    /**
     * Determina el peso numérico de una arista según el criterio de optimización activo.
     * * @param r La ruta a evaluar.
     * @param criterio El criterio seleccionado por el usuario.
     * @return El valor numérico (double) que el algoritmo intentará minimizar.
     */
    private static double pesoSegunCriterio(Ruta r, CriterioOptimizacion criterio) {
        return switch (criterio) {
            case TIEMPO -> r.getTiempoMin();
            case DISTANCIA -> r.getDistanciaKm();
            case COSTO -> r.getCosto();
            case TRANSBORDOS -> 1.0;
        };
    }
    /**
     * Reconstruye la secuencia de nodos que forman el camino óptimo desde el destino hacia el origen.
     * * Utiliza el mapa de predecesores generado durante la fase de relajación para
     * trazar la ruta de regreso y presentarla en el orden correcto de viaje.
     * * @param prev Mapa que vincula cada nodo con su antecesor en el camino mínimo.
     * @param origenId Nodo inicial del viaje.
     * @param destinoId Nodo final del viaje.
     * @return Una lista ordenada de IDs de paradas que representan el trayecto.
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
     * Recupera las instancias de {@link Ruta} que conectan el camino de nodos calculado.
     * * Esto permite acceder a los metadatos de cada tramo (como el tipo de transporte
     * o la distancia específica) que no están presentes en la lista de IDs.
     * * @param grafo El grafo de referencia.
     * @param camino La lista de IDs de paradas calculada previamente.
     * @return Una lista de objetos Ruta que representan los tramos físicos del viaje.
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
     * Consolida las métricas acumuladas de todos los tramos que componen la ruta óptima.
     * * Recorre la lista de rutas seleccionadas para sumar los tiempos de viaje,
     * las distancias en kilómetros y los costos monetarios, permitiendo generar
     * el reporte detallado que se muestra en la interfaz de usuario.
     * * @param tramos La lista de objetos {@link Ruta} que forman el camino calculado.
     * @return Una instancia de {@link Totales} con la sumatoria de todas las variables del viaje.
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

        int transbordos = 1;

        return new Totales(tiempo, distancia, costo, transbordos);
    }


    /**
     * Genera una estructura de datos estandarizada para representar la ausencia de un camino.
     * * Se utiliza cuando el nodo destino es inalcanzable desde el origen,
     * devolviendo una lista vacía de paradas y estableciendo el peso óptimo
     * en {@link Double#POSITIVE_INFINITY} para indicar la desconexión.
     * * @return Un objeto {@link ResultadoRuta} marcado como inexistente (false).
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
     * Estructura interna de transporte de datos (Record) para agrupar los totales acumulados.
     */
    private record Totales(double tiempo, double distancia, double costo, int transbordos) {
    }
}