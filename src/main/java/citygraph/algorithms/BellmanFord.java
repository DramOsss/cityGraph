package citygraph.algorithms;

import citygraph.graph.GrafoTransporte;
import citygraph.graph.exceptions.CicloNegativoException;
import citygraph.model.CriterioOptimizacion;
import citygraph.model.ResultadoRuta;
import citygraph.model.Ruta;

import java.util.*;

public class BellmanFord {

    private BellmanFord() {
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

    private static List<Ruta> recopilarTodasLasRutas(GrafoTransporte grafo) {
        List<Ruta> rutas = new ArrayList<>();
        for (var parada : grafo.listarParadas()) {
            rutas.addAll(grafo.vecinosDe(parada.getId()));
        }
        return rutas;
    }

    private static double pesoSegunCriterio(Ruta r, CriterioOptimizacion criterio) {
        return switch (criterio) {
            case TIEMPO -> r.getTiempoMin();
            case DISTANCIA -> r.getDistanciaKm();
            case COSTO -> r.getCosto();
            case TRANSBORDOS -> 1.0;
        };
    }

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

    private record Totales(double tiempo, double distancia, double costo, int transbordos) {
    }
}