package citygraph.service;

import citygraph.algorithms.BellmanFord;
import citygraph.graph.exceptions.CicloNegativoException;
import citygraph.algorithms.Dijkstra;
import citygraph.db.DBConnection;
import citygraph.graph.GrafoTransporte;
import citygraph.model.CriterioOptimizacion;
import citygraph.model.Parada;
import citygraph.model.ResultadoRuta;
import citygraph.model.Ruta;
import citygraph.repository.ParadaRepository;
import citygraph.repository.RutaRepository;

import java.sql.Connection;
import java.sql.SQLException;
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

        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);

            try {
                grafo.agregarRuta(ruta);

                if (tieneCostosNegativos()) {
                    validarSinCiclosNegativos();
                }

                rutaRepository.save(conn, ruta);
                conn.commit();

            } catch (CicloNegativoException e) {
                rollbackSilencioso(conn);
                rollbackAgregarRutaMemoria(ruta);

                throw new IllegalArgumentException(
                        "No se puede agregar la ruta: generaría un ciclo negativo"
                );
            } catch (RuntimeException e) {
                rollbackSilencioso(conn);
                rollbackAgregarRutaMemoria(ruta);
                throw e;
            } catch (Exception e) {
                rollbackSilencioso(conn);
                rollbackAgregarRutaMemoria(ruta);

                throw new RuntimeException("Error al agregar la ruta: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión: " + e.getMessage(), e);
        }
    }

    public void modificarRuta(Ruta ruta) {
        Objects.requireNonNull(ruta, "Ruta no puede ser null");

        Ruta actual = buscarRuta(ruta.getOrigenId(), ruta.getDestinoId());

        Ruta respaldo = new Ruta(
                actual.getOrigenId(),
                actual.getDestinoId(),
                actual.getTiempoMin(),
                actual.getDistanciaKm(),
                actual.getCosto(),
                actual.getTipoTransporte()
        );

        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);

            try {
                grafo.modificarRuta(
                        ruta.getOrigenId(),
                        ruta.getDestinoId(),
                        ruta.getTiempoMin(),
                        ruta.getDistanciaKm(),
                        ruta.getCosto(),
                        ruta.getTipoTransporte()
                );

                if (tieneCostosNegativos()) {
                    validarSinCiclosNegativos();
                }

                rutaRepository.update(conn, ruta);
                conn.commit();

            } catch (CicloNegativoException e) {
                rollbackSilencioso(conn);
                restaurarRutaEnMemoria(respaldo);

                throw new IllegalArgumentException(
                        "No se puede modificar la ruta: generaría un ciclo negativo"
                );
            } catch (RuntimeException e) {
                rollbackSilencioso(conn);
                restaurarRutaEnMemoria(respaldo);
                throw e;
            } catch (Exception e) {
                rollbackSilencioso(conn);
                restaurarRutaEnMemoria(respaldo);

                throw new RuntimeException("Error al modificar la ruta: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión: " + e.getMessage(), e);
        }
    }

    public void eliminarRuta(String origenId, String destinoId) {
        Ruta actual = buscarRuta(origenId, destinoId);

        Ruta respaldo = new Ruta(
                actual.getOrigenId(),
                actual.getDestinoId(),
                actual.getTiempoMin(),
                actual.getDistanciaKm(),
                actual.getCosto(),
                actual.getTipoTransporte()
        );

        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);

            try {
                grafo.eliminarRuta(origenId, destinoId);
                rutaRepository.deleteById(conn, origenId, destinoId);
                conn.commit();

            } catch (RuntimeException e) {
                rollbackSilencioso(conn);
                restaurarRutaEliminada(respaldo);
                throw e;
            } catch (Exception e) {
                rollbackSilencioso(conn);
                restaurarRutaEliminada(respaldo);

                throw new RuntimeException("Error al eliminar la ruta: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión: " + e.getMessage(), e);
        }
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

        if (criterio == CriterioOptimizacion.TRANSBORDOS) {
            return Dijkstra.calcular(grafo, origenId, destinoId, criterio);
        }

        if (criterio == CriterioOptimizacion.COSTO && hayPesosNegativos(criterio)) {
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

    public String nombreAlgoritmoPara(CriterioOptimizacion criterio) {
        if (criterio == CriterioOptimizacion.COSTO && hayPesosNegativos(criterio)) {
            return "Bellman-Ford";
        }

        return "Dijkstra";
    }

    // =========================
    // VALIDACIONES INTERNAS
    // =========================

    private Ruta buscarRuta(String origenId, String destinoId) {
        return grafo.obtenerRuta(origenId, destinoId);
    }

    private boolean tieneCostosNegativos() {
        for (Parada p : grafo.listarParadas()) {
            for (Ruta r : grafo.vecinosDe(p.getId())) {
                if (r.getCosto() < 0) return true;
            }
        }
        return false;
    }

    private void validarSinCiclosNegativos() {
        BellmanFord.validarSinCicloNegativo(grafo);
    }

    private void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void rollbackAgregarRutaMemoria(Ruta ruta) {
        try {
            grafo.eliminarRuta(ruta.getOrigenId(), ruta.getDestinoId());
        } catch (RuntimeException ignored) {
        }
    }

    private void restaurarRutaEnMemoria(Ruta respaldo) {
        try {
            grafo.modificarRuta(
                    respaldo.getOrigenId(),
                    respaldo.getDestinoId(),
                    respaldo.getTiempoMin(),
                    respaldo.getDistanciaKm(),
                    respaldo.getCosto(),
                    respaldo.getTipoTransporte()
            );
        } catch (RuntimeException ignored) {
        }
    }

    private void restaurarRutaEliminada(Ruta respaldo) {
        try {
            grafo.agregarRuta(respaldo);
        } catch (RuntimeException ignored) {
        }
    }
}