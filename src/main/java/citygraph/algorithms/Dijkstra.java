package citygraph.algorithms;

import citygraph.graph.GrafoTransporte;
import citygraph.model.CriterioOptimizacion;
import citygraph.model.ResultadoRuta;
import citygraph.model.Ruta;
import citygraph.model.TipoTransporte;

import java.util.*;

/**
 * Implementación del algoritmo de Dijkstra para la búsqueda de rutas óptimas en redes de transporte.
 * * Esta clase proporciona dos variantes de optimización:
 * 1. **Cálculo Normal:** Minimiza pesos acumulativos simples como tiempo, distancia o costo.
 * 2. **Cálculo por Transbordos:** Utiliza un enfoque de "Grafo de Estados" donde el peso se
 * incrementa solo cuando se cambia el {@link TipoTransporte} entre tramos consecutivos.
 * * Utiliza una {@link PriorityQueue} para garantizar una complejidad de $O((V+E) \log V)$,
 * lo que la hace ideal para las funciones de búsqueda en tiempo real de la aplicación.
 */
public class Dijkstra {

    /**
     * Representa un estado del algoritmo en la cola de prioridad.
     * Almacena la parada actual, la distancia acumulada hasta el momento y el tipo
     * de transporte del último tramo utilizado para evaluar posibles transbordos.
     */
    private static class Estado implements Comparable<Estado> {
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
     * Llave compuesta utilizada para identificar estados únicos en el mapa de distancias.
     * Permite que el algoritmo trate una misma parada como diferentes "nodos" dependiendo
     * del medio de transporte con el que se llegó a ella, permitiendo el cálculo exacto
     * de transbordos mínimos.
     */
    private static class Key {
        String id;
        TipoTransporte tipo;

        Key(String id, TipoTransporte tipo) {
            this.id = id;
            this.tipo = tipo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key key)) return false;
            return Objects.equals(id, key.id) && tipo == key.tipo;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, tipo);
        }
    }

    /**
     * Punto de entrada principal para el cálculo de rutas mediante Dijkstra.
     * * Dirige la ejecución hacia el método especializado según si el usuario
     * desea optimizar métricas físicas/económicas o la comodidad del viaje (transbordos).
     * * @param grafo El grafo de la ciudad.
     * @param origenId Identificador de partida.
     * @param destinoId Identificador de llegada.
     * @param criterio Factor de optimización seleccionado.
     * @return {@link ResultadoRuta} con el camino y métricas calculadas.
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
     * Contabiliza el número de cambios de tipo de transporte a lo largo de un trayecto.
     * * Compara el {@link TipoTransporte} de cada tramo con el anterior; cada discrepancia
     * se registra como un transbordo adicional.
     * * @param tramos Lista de rutas que componen el camino.
     * @return El número total de transbordos realizados.
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
     * Reconstruye la secuencia de identificadores de paradas a partir del mapa de predecesores.
     * * Traza el camino inverso desde el destino hasta el origen para generar la
     * lista ordenada de la ruta.
     * * @param prev Mapa que asocia cada parada con el nodo que permitió llegar a ella óptimamente.
     * @param origenId Nodo de inicio.
     * @param destinoId Nodo final.
     * @return Lista ordenada de paradas del trayecto.
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
     * Recupera los objetos {@link Ruta} correspondientes a los nodos del camino calculado.
     * * Busca en la lista de adyacencia del grafo las aristas específicas que conectan
     * cada par de nodos consecutivos en la ruta óptima.
     * * @param grafo Estructura de datos del grafo.
     * @param camino Lista de IDs de paradas.
     * @return Lista de objetos Ruta que representan los tramos físicos.
     * @throws IllegalStateException Si una conexión del camino no existe en el grafo.
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