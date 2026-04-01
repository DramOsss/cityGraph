package citygraph.algorithms;

import citygraph.graph.GrafoTransporte;
import citygraph.model.CriterioOptimizacion;
import citygraph.model.ResultadoRuta;
import citygraph.model.Ruta;

import java.util.*;

public class Dijkstra {

    private static class NodoDist implements Comparable<NodoDist> {
        String id;
        double dist;

        NodoDist(String id, double dist) {
            this.id = id;
            this.dist = dist;
        }

        @Override
        public int compareTo(NodoDist o) {
            return Double.compare(this.dist, o.dist);
        }
    }

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
        Set<String> visitado = new HashSet<>();

        PriorityQueue<NodoDist> pq = new PriorityQueue<>();

        dist.put(origenId, 0.0);
        pq.add(new NodoDist(origenId, 0.0));

        while (!pq.isEmpty()) {
            NodoDist actual = pq.poll();

            if (visitado.contains(actual.id)) continue;
            visitado.add(actual.id);

            if (actual.id.equals(destinoId)) break;

            for (Ruta r : grafo.vecinosDe(actual.id)) {
                String v = r.getDestinoId();
                if (visitado.contains(v)) continue;

                double w = peso(r, criterio);
                double nueva = dist.get(actual.id) + w;

                if (nueva < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, nueva);
                    prev.put(v, actual.id);
                    pq.add(new NodoDist(v, nueva));
                }
            }
        }

        if (!dist.containsKey(destinoId)) {
            return new ResultadoRuta(List.of(), false,
                    Double.POSITIVE_INFINITY, 0, 0, 0, 0);
        }

        List<String> camino = reconstruirCamino(prev, origenId, destinoId);
        List<Ruta> tramos = reconstruirTramos(grafo, camino);

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
                dist.get(destinoId),
                tiempo,
                distancia,
                costo,
                transbordos
        );
    }

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