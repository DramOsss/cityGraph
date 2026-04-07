package citygraph.algorithms;

import citygraph.graph.GrafoTransporte;
import citygraph.model.CriterioOptimizacion;
import citygraph.model.ResultadoRuta;
import citygraph.model.Ruta;
import citygraph.model.TipoTransporte;

import java.util.*;

/**
 * Implementación del algoritmo de Dijkstra para el Sistema de Gestión de Rutas. [cite: 5, 6]
 * * Este algoritmo encuentra la ruta más corta entre dos paradas en un grafo dirigido
 * utilizando una cola de prioridad para optimizar el tiempo de búsqueda. [cite: 12, 18]
 * Es ideal para cálculos rápidos de tiempo de viaje o distancia física. [cite: 18, 27]
 */
public class Dijkstra {

    /**
     * Clase interna para representar un nodo en la cola de prioridad.
     * Permite comparar paradas basadas en su distancia acumulada desde el origen.
     */
    private static class NodoDist implements Comparable<NodoDist> {
        String id;
        double dist;
        TipoTransporte ultimoTipo;

        Estado(String id, double dist, TipoTransporte ultimoTipo) {
            this.id = id;
            this.dist = dist;
            this.ultimoTipo = ultimoTipo;
        }

        @Override
        public int compareTo(Estado o) {
            return Double.compare(this.dist, o.dist);
        }
    }

    /**
     * Calcula la ruta óptima entre dos paradas utilizando el algoritmo de Dijkstra. [cite: 18]
     * * @param grafo El grafo de transporte basado en listas de adyacencia. [cite: 16]
     * @param origenId Identificador de la parada de origen. [cite: 13, 27]
     * @param destinoId Identificador de la parada de destino. [cite: 13, 27]
     * @param criterio El factor de optimización: TIEMPO, DISTANCIA o COSTO. [cite: 9, 15, 27]
     * @return Un objeto {@code ResultadoRuta} con el camino óptimo y métricas totales. [cite: 29, 66]
     * @throws Objects.requireNonNull Si alguno de los parámetros obligatorios es nulo.
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

        if (criterio != CriterioOptimizacion.TRANSBORDOS) {
            return calcularNormal(grafo, origenId, destinoId, criterio);
        }

        return calcularPorTransbordos(grafo, origenId, destinoId);
    }

    /**
     * Realiza el cálculo estándar de la ruta más corta basado en pesos acumulativos.
     * * Implementa el algoritmo de Dijkstra para optimizar el trayecto según el tiempo
     * de viaje, la distancia física o el costo del pasaje. Utiliza una cola de
     * prioridad para garantizar la eficiencia en redes de transporte densas.
     * * @param grafo El grafo dirigido que representa la red de transporte.
     * @param origenId Identificador único de la parada de inicio.
     * @param destinoId Identificador único de la parada de llegada.
     * @param criterio El factor de peso a minimizar (TIEMPO, DISTANCIA o COSTO).
     * @return Un objeto ResultadoRuta con el camino detallado y las métricas acumuladas.
     */
    private static ResultadoRuta calcularNormal(GrafoTransporte grafo,
                                                String origenId,
                                                String destinoId,
                                                CriterioOptimizacion criterio) {

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Set<String> visitado = new HashSet<>();

        PriorityQueue<Estado> pq = new PriorityQueue<>();

        dist.put(origenId, 0.0);
        pq.add(new Estado(origenId, 0.0, null));

        while (!pq.isEmpty()) {
            Estado actual = pq.poll();

            if (visitado.contains(actual.id)) continue;
            visitado.add(actual.id);

            if (actual.id.equals(destinoId)) break;

            for (Ruta r : grafo.vecinosDe(actual.id)) {
                String v = r.getDestinoId();
                if (visitado.contains(v)) continue;

                double w = pesoNormal(r, criterio);
                double nueva = dist.get(actual.id) + w;

                if (nueva < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, nueva);
                    prev.put(v, actual.id);
                    pq.add(new Estado(v, nueva, r.getTipoTransporte()));
                }
            }
        }

        if (!dist.containsKey(destinoId)) {
            return new ResultadoRuta(List.of(), false,
                    Double.POSITIVE_INFINITY, 0, 0, 0, 0);
        }

        List<String> camino = reconstruirCamino(prev, origenId, destinoId);
        List<Ruta> tramos = reconstruirTramos(grafo, camino);

        return construirResultado(camino, tramos, dist.get(destinoId));
    }

    /**
     * Calcula la ruta que minimiza la cantidad total de transbordos entre dos paradas.
     * * Utiliza una variante de búsqueda por estados donde el peso (costo 1.0) se aplica
     * únicamente cuando el tipo de transporte de una ruta es diferente al del tramo anterior.
     * Permite al usuario encontrar la opción de viaje con menos interrupciones.
     * * @param grafo El grafo de la ciudad con sus paradas y rutas.
     * @param origenId ID de la parada de origen.
     * @param destinoId ID de la parada de destino.
     * @return ResultadoRuta con el trayecto que ofrece el menor número de cambios de transporte.
     */
    private static ResultadoRuta calcularPorTransbordos(GrafoTransporte grafo,
                                                        String origenId,
                                                        String destinoId) {

        Map<Key, Double> dist = new HashMap<>();
        Map<Key, Key> prevEstado = new HashMap<>();
        Map<Key, Ruta> prevRuta = new HashMap<>();
        PriorityQueue<Estado> pq = new PriorityQueue<>();

        Key inicio = new Key(origenId, null);
        dist.put(inicio, 0.0);
        pq.add(new Estado(origenId, 0.0, null));

        while (!pq.isEmpty()) {
            Estado actual = pq.poll();
            Key keyActual = new Key(actual.id, actual.ultimoTipo);

            double distActual = dist.getOrDefault(keyActual, Double.POSITIVE_INFINITY);
            if (actual.dist > distActual) continue;

            for (Ruta r : grafo.vecinosDe(actual.id)) {
                TipoTransporte nuevoTipo = r.getTipoTransporte();
                String vecino = r.getDestinoId();

                double costoTransbordo;
                if (actual.ultimoTipo == null) {
                    costoTransbordo = 0.0;
                } else if (actual.ultimoTipo == nuevoTipo) {
                    costoTransbordo = 0.0;
                } else {
                    costoTransbordo = 1.0;
                }

                double nuevaDist = actual.dist + costoTransbordo;
                Key keyVecino = new Key(vecino, nuevoTipo);

                if (nuevaDist < dist.getOrDefault(keyVecino, Double.POSITIVE_INFINITY)) {
                    dist.put(keyVecino, nuevaDist);
                    prevEstado.put(keyVecino, keyActual);
                    prevRuta.put(keyVecino, r);
                    pq.add(new Estado(vecino, nuevaDist, nuevoTipo));
                }
            }
        }

        Key mejorFinal = null;
        double mejor = Double.POSITIVE_INFINITY;

        for (Map.Entry<Key, Double> e : dist.entrySet()) {
            if (e.getKey().id.equals(destinoId) && e.getValue() < mejor) {
                mejor = e.getValue();
                mejorFinal = e.getKey();
            }
        }

        if (mejorFinal == null) {
            return new ResultadoRuta(List.of(), false,
                    Double.POSITIVE_INFINITY, 0, 0, 0, 0);
        }

        List<Ruta> tramos = new ArrayList<>();
        Key actual = mejorFinal;

        while (actual != null && !actual.id.equals(origenId)) {
            Ruta ruta = prevRuta.get(actual);
            if (ruta == null) break;
            tramos.add(ruta);
            actual = prevEstado.get(actual);
        }

        Collections.reverse(tramos);

        List<String> camino = new ArrayList<>();
        camino.add(origenId);
        for (Ruta r : tramos) {
            camino.add(r.getDestinoId());
        }

        return construirResultado(camino, tramos, mejor);
    }

    /**
     * Obtiene el valor numérico del peso de una ruta según el criterio seleccionado.
     * * @param r La instancia de la ruta a evaluar.
     * @param criterio El criterio de optimización (Tiempo, Distancia o Costo).
     * @return El peso correspondiente (minutos, kilómetros o valor monetario).
     * @throws IllegalArgumentException Si se intenta procesar el criterio de TRANSBORDOS.
     */
    private static double pesoNormal(Ruta r, CriterioOptimizacion criterio) {
        return switch (criterio) {
            case TIEMPO -> r.getTiempoMin();
            case DISTANCIA -> r.getDistanciaKm();
            case COSTO -> r.getCosto();
            case TRANSBORDOS -> throw new IllegalArgumentException("Use calcularPorTransbordos");
        };
    }

    /**
     * Construye y consolida el objeto de resultado final con todas las métricas del viaje.
     * * Agrupa la información del camino de paradas, los tramos físicos recorridos y
     * realiza la sumatoria final de tiempos, distancias y costos para su
     * presentación en la interfaz de usuario.
     * * @param camino Lista ordenada de los nombres de las paradas.
     * @param tramos Lista de objetos Ruta que forman el trayecto completo.
     * @param pesoOptimo El valor del peso total minimizado por el algoritmo.
     * @return Un objeto ResultadoRuta completo y listo para ser visualizado.
     */
    private static ResultadoRuta construirResultado(List<String> camino,
                                                    List<Ruta> tramos,
                                                    double pesoOptimo) {
        double tiempo = 0;
        double distancia = 0;
        double costo = 0;

        for (Ruta arista : tramos) {
            tiempo += arista.getTiempoMin();
            distancia += arista.getDistanciaKm();
            costo += arista.getCosto();
        }

        int transbordos = calcularTransbordos(tramos);

        return new ResultadoRuta(
                camino,
                true,
                pesoOptimo,
                tiempo,
                distancia,
                costo,
                transbordos
        );
    }

    /**
     * Determina el peso de una ruta según el criterio de optimización seleccionado. [cite: 15, 56]
     * * @param r La ruta a evaluar. [cite: 15]
     * @param criterio El criterio aplicado (Tiempo, Distancia o Costo). [cite: 41, 56]
     * @return El valor numérico del peso para el cálculo del algoritmo. [cite: 15]
     */
    private static double peso(Ruta r, CriterioOptimizacion criterio) {
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
     * Calcula la cantidad de transbordos realizados basándose en el cambio de tipo de transporte. [cite: 9, 15]
     * * @param tramos Lista de rutas que componen el trayecto. [cite: 14]
     * @return El número total de cambios de transporte detectados. [cite: 41]
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
     * Reconstruye la secuencia de nombres de paradas desde el mapa de nodos previos. [cite: 13]
     * * @param prev Mapa que asocia cada nodo con su predecesor en la ruta mínima.
     * @param origenId ID de la parada inicial. [cite: 13]
     * @param destinoId ID de la parada final. [cite: 13]
     * @return Lista ordenada de paradas que forman el camino más corto. [cite: 27]
     */
    private static List<String> reconstruirCamino(Map<String, String> prev, String origenId, String destinoId) {
        LinkedList<String> camino = new LinkedList<>();
        String actual = destinoId;
        camino.addFirst(actual);

        while (!actual.equals(origenId)) {
            actual = prev.get(actual);
            if (actual == null) return List.of();
            camino.addFirst(actual);
        }
        return camino;
    }

    /**
     * Obtiene los objetos Ruta (aristas) correspondientes al camino de paradas encontrado. [cite: 14]
     * * @param grafo La estructura de datos que contiene las conexiones. [cite: 12]
     * @param camino Lista ordenada de identificadores de paradas. [cite: 13]
     * @return Lista de objetos {@code Ruta} con sus respectivos atributos de viaje. [cite: 15]
     * @throws IllegalStateException Si no existe una conexión directa entre dos paradas del camino.
     */
    private static List<Ruta> reconstruirTramos(GrafoTransporte grafo, List<String> camino) {
        List<Ruta> tramos = new ArrayList<>();

        for (int i = 0; i < camino.size() - 1; i++) {
            String u = camino.get(i);
            String v = camino.get(i + 1);

            Ruta arista = grafo.vecinosDe(u).stream()
                    .filter(r -> r.getDestinoId().equals(v))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No se encontró la ruta " + u + " -> " + v));

            tramos.add(arista);
        }

        return tramos;
    }
}