package citygraph.service;

import citygraph.algorithms.BellmanFord;
import citygraph.algorithms.Dijkstra;
import citygraph.graph.GrafoTransporte;
import citygraph.model.CriterioOptimizacion;
import citygraph.model.Parada;
import citygraph.model.ResultadoRuta;
import citygraph.model.Ruta;
import citygraph.model.TipoTransporte;
import citygraph.repository.ParadaRepository;
import citygraph.repository.RutaRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CityGraphService {

    private final GrafoTransporte grafo = new GrafoTransporte();
    private final ParadaRepository paradaRepository;
    private final RutaRepository rutaRepository;

    public CityGraphService(ParadaRepository paradaRepository,
                            RutaRepository rutaRepository) {
        this.paradaRepository = Objects.requireNonNull(paradaRepository, "paradaRepository no puede ser null");
        this.rutaRepository = Objects.requireNonNull(rutaRepository, "rutaRepository no puede ser null");
    }

    public void cargarDesdeBD() {
        grafo.limpiar();

        List<Parada> paradas = paradaRepository.findAll();
        for (Parada p : paradas) {
            grafo.agregarParada(p);
        }

        List<Ruta> rutas = rutaRepository.findAll();
        for (Ruta r : rutas) {
            grafo.agregarRuta(r);
        }
    }

    // =========================
    // PARADAS
    // =========================

    public void agregarParada(Parada p) {
        Objects.requireNonNull(p, "Parada no puede ser null");
        paradaRepository.save(p);
        grafo.agregarParada(p);
    }

    public void modificarParada(String id, String nombre) {
        grafo.modificarParada(id, nombre);
        Parada actual = grafo.obtenerParada(id);
        paradaRepository.update(actual);
    }

    public void eliminarParada(String id) {
        grafo.eliminarParada(id);
        paradaRepository.deleteById(id);
    }

    public List<Parada> listarParadas() {
        return new ArrayList<>(grafo.listarParadas());
    }

    public List<Parada> listarParadasOrdenadas() {
        return grafo.listarParadas().stream()
                .sorted(Comparator.comparing(Parada::getId))
                .toList();
    }

    public int numeroParadas() {
        return grafo.numeroParadas();
    }

    // =========================
    // RUTAS
    // =========================

    public void agregarRuta(Ruta ruta) {
        Objects.requireNonNull(ruta, "Ruta no puede ser null");
        rutaRepository.save(ruta);
        grafo.agregarRuta(ruta);
    }

    public void modificarRuta(Ruta ruta) {
        Objects.requireNonNull(ruta, "Ruta no puede ser null");

        grafo.modificarRuta(
                ruta.getOrigenId(),
                ruta.getDestinoId(),
                ruta.getTiempoMin(),
                ruta.getDistanciaKm(),
                ruta.getCosto(),
                ruta.getTipoTransporte()
        );

        rutaRepository.update(ruta);
    }

    public void modificarRuta(String origenId,
                              String destinoId,
                              Double tiempoMin,
                              Double distanciaKm,
                              Double costo,
                              TipoTransporte tipoTransporte) {
        Ruta ruta = new Ruta(origenId, destinoId, tiempoMin, distanciaKm, costo, tipoTransporte);
        modificarRuta(ruta);
    }

    public void eliminarRuta(String origenId, String destinoId) {
        grafo.eliminarRuta(origenId, destinoId);
        rutaRepository.deleteById(origenId, destinoId);
    }

    public List<Ruta> listarRutas() {
        List<Ruta> todas = new ArrayList<>();
        for (Parada p : grafo.listarParadas()) {
            todas.addAll(grafo.vecinosDe(p.getId()));
        }
        return todas;
    }

    public List<Ruta> listarRutasOrdenadas() {
        return listarRutas().stream()
                .sorted(Comparator.comparing(Ruta::getOrigenId)
                        .thenComparing(Ruta::getDestinoId))
                .toList();
    }

    public int numeroRutas() {
        return grafo.numeroRutas();
    }

    // =========================
    // CÁLCULO
    // =========================

    public ResultadoRuta calcularRuta(String origenId,
                                      String destinoId,
                                      CriterioOptimizacion criterio) {
        Objects.requireNonNull(origenId, "origenId no puede ser null");
        Objects.requireNonNull(destinoId, "destinoId no puede ser null");
        Objects.requireNonNull(criterio, "criterio no puede ser null");

        if (hayPesosNegativos(criterio)) {
            return BellmanFord.calcular(grafo, origenId, destinoId, criterio);
        }

        return Dijkstra.calcular(grafo, origenId, destinoId, criterio);
    }

    private boolean hayPesosNegativos(CriterioOptimizacion criterio) {
        for (Ruta ruta : listarRutas()) {
            double peso = switch (criterio) {
                case TIEMPO -> ruta.getTiempoMin();
                case DISTANCIA -> ruta.getDistanciaKm();
                case COSTO -> ruta.getCosto();
                case TRANSBORDOS -> 0.0;
            };

            if (peso < 0) {
                return true;
            }
        }
        return false;
    }
}