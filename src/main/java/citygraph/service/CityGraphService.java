package citygraph.service;

import citygraph.algorithms.Dijkstra;
import citygraph.graph.GrafoTransporte;
import citygraph.model.CriterioOptimizacion;
import citygraph.model.Parada;
import citygraph.model.ResultadoRuta;
import citygraph.model.Ruta;
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
        this.paradaRepository = paradaRepository;
        this.rutaRepository = rutaRepository;
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

    public void modificarParada(String id, String nombre, Double lat, Double lon) {
        grafo.modificarParada(id, nombre, lat, lon);
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

    public void agregarRuta(Ruta r) {
        Objects.requireNonNull(r, "Ruta no puede ser null");
        rutaRepository.save(r);
        grafo.agregarRuta(r);
    }

    public void modificarRuta(Ruta r) {
        Objects.requireNonNull(r, "Ruta no puede ser null");

        grafo.modificarRuta(
                r.getOrigenId(),
                r.getDestinoId(),
                r.getTiempoMin(),
                r.getDistanciaKm(),
                r.getCosto(),
                r.getTransbordos()
        );

        rutaRepository.update(r);
    }

    public void modificarRuta(String origenId, String destinoId,
                              Double tiempoMin, Double distanciaKm, Double costo, Integer transbordos) {
        Ruta ruta = new Ruta(origenId, destinoId, tiempoMin, distanciaKm, costo, transbordos);
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
        List<Ruta> todas = listarRutas();
        todas.sort(Comparator.comparing(Ruta::getOrigenId)
                .thenComparing(Ruta::getDestinoId));
        return todas;
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
        return Dijkstra.calcular(grafo, origenId, destinoId, criterio);
    }
}