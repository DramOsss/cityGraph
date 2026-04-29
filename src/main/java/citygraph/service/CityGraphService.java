package citygraph.service;

import citygraph.algorithms.BellmanFord;
import citygraph.algorithms.Dfs;
import citygraph.graph.exceptions.CicloNegativoException;
import citygraph.algorithms.Dijkstra;
import citygraph.db.DBConnection;
import citygraph.graph.GrafoTransporte;
import citygraph.model.*;
import citygraph.repository.ParadaRepository;
import citygraph.repository.RutaRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Clase de servicio principal que actúa como fachada (Facade) para la gestión del sistema.
 * * Coordina la interacción entre la estructura de datos en memoria ({@link GrafoTransporte}),
 * la persistencia en base de datos (repositorios) y la ejecución de algoritmos de optimización.
 * Asegura la integridad del grafo, validando que las modificaciones no introduzcan
 * estados inconsistentes como ciclos negativos.
 */
public class CityGraphService {

    private final GrafoTransporte grafo = new GrafoTransporte();
    private final ParadaRepository paradaRepository;
    private final RutaRepository rutaRepository;

    /**
     * Constructor que inicializa el servicio con los repositorios necesarios.
     * * @param paradaRepository Implementación de persistencia para paradas.
     * @param rutaRepository Implementación de persistencia para rutas.
     * @throws NullPointerException Si alguno de los repositorios es nulo.
     */
    public CityGraphService(ParadaRepository paradaRepository,
                            RutaRepository rutaRepository) {
        this.paradaRepository = Objects.requireNonNull(paradaRepository, "paradaRepository no puede ser null");
        this.rutaRepository = Objects.requireNonNull(rutaRepository, "rutaRepository no puede ser null");
    }

    /**
     * Sincroniza el grafo en memoria con los datos actuales de la base de datos.
     * * Limpia la estructura actual y recarga todas las paradas y rutas,
     * reconstruyendo la red de transporte completa.
     */
    public void cargarDesdeBD() {
        grafo.limpiar();

        try {
            List<Parada> paradas = paradaRepository.findAll();
            for (Parada p : paradas) {
                grafo.agregarParada(p);
            }

            List<Ruta> rutas = rutaRepository.findAll();
            for (Ruta r : rutas) {
                grafo.agregarRuta(r);
            }

        } catch (RuntimeException e) {
            grafo.limpiar();
            throw new RuntimeException("Error al cargar los datos desde la base de datos", e);
        }
    }

    // =========================
    // PARADAS
    // =========================

    /**
     * Registra una nueva parada tanto en el almacenamiento persistente como en el grafo.
     * * @param p La parada a agregar.
     */
    public void agregarParada(Parada p) {
        Objects.requireNonNull(p, "Parada no puede ser null");
        paradaRepository.save(p);
        grafo.agregarParada(p);
    }
    /**
     * Actualiza la información de una parada y sincroniza los cambios en la base de datos.
     * * @param id Identificador de la parada.
     * @param nombre Nuevo nombre descriptivo.
     */
    public void modificarParada(String id, String nombre) {
        grafo.modificarParada(id, nombre);
        Parada actual = grafo.obtenerParada(id);
        paradaRepository.update(actual);
    }
    /**
     * Elimina una parada de forma lógica y física en todo el sistema.
     * * @param id Identificador de la parada a remover.
     */
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

    /**
     * Agrega una nueva ruta validando que no comprometa la integridad del grafo.
     * * Realiza una validación de ciclos negativos si la ruta posee costos menores a cero,
     * revirtiendo la operación en memoria si la validación falla.
     * * @param ruta La ruta a insertar.
     * @throws IllegalArgumentException Si la ruta genera un ciclo de costo negativo.
     */
    public void agregarRuta(Ruta ruta) {
        Objects.requireNonNull(ruta, "Ruta no puede ser null");

        boolean agregadaEnMemoria = false;

        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);

            try {
                grafo.agregarRuta(ruta);
                agregadaEnMemoria = true;

                if (tieneCostosNegativos()) {
                    validarSinCiclosNegativos();
                }

                rutaRepository.save(conn, ruta);
                conn.commit();

            } catch (CicloNegativoException e) {
                rollbackConn(conn);

                if (agregadaEnMemoria) {
                    rollbackAgregarRutaMemoria(ruta);
                }

                throw new IllegalArgumentException(
                        "No se puede agregar la ruta: generaría un ciclo negativo", e
                );

            } catch (RuntimeException e) {
                rollbackConn(conn);

                if (agregadaEnMemoria) {
                    rollbackAgregarRutaMemoria(ruta);
                }

                throw e;

            } catch (Exception e) {
                rollbackConn(conn);

                if (agregadaEnMemoria) {
                    rollbackAgregarRutaMemoria(ruta);
                }

                throw new RuntimeException("Error al agregar la ruta", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión al agregar la ruta", e);
        }
    }

    /**
     * Modifica los atributos de una ruta existente y valida la estabilidad del grafo.
     * * Si el cambio de pesos genera inconsistencias algorítmicas (ciclos negativos),
     * se restaura el estado anterior de la ruta.
     * * @param ruta Objeto ruta con los nuevos valores.
     */
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
                rollbackConn(conn);
                restaurarRutaEnMemoria(respaldo);

                throw new IllegalArgumentException(
                        "No se puede modificar la ruta: generaría un ciclo negativo"
                );
            } catch (RuntimeException e) {
                rollbackConn(conn);
                restaurarRutaEnMemoria(respaldo);
                throw e;
            } catch (Exception e) {
                rollbackConn(conn);
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
                rollbackConn(conn);
                restaurarRutaEliminada(respaldo);
                throw e;
            } catch (Exception e) {
                rollbackConn(conn);
                restaurarRutaEliminada(respaldo);

                throw new RuntimeException("Error al eliminar la ruta: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión: " + e.getMessage(), e);
        }
    }
    /**
     * Devuelve una lista con todas las conexiones registradas en el sistema.
     * * Recorre cada parada del grafo y recopila sus rutas adyacentes para
     * ofrecer una vista global de la red de transporte.
     * * @return Una lista que contiene todos los objetos {@link Ruta} del grafo.
     */
    public List<Ruta> listarRutas() {
        List<Ruta> todas = new ArrayList<>();
        for (Parada p : grafo.listarParadas()) {
            todas.addAll(grafo.vecinosDe(p.getId()));
        }
        return todas;
    }
    /**
     * Devuelve una lista con todas las conexiones registradas en el sistema.
     * * Recorre cada parada del grafo y recopila sus rutas adyacentes para
     * ofrecer una vista global de la red de transporte.
     * * @return Una lista que contiene todos los objetos {@link Ruta} del grafo.
     */
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

    /**
     * Calcula el camino óptimo entre dos paradas utilizando el algoritmo más adecuado.
     * * Selecciona automáticamente entre Dijkstra y Bellman-Ford dependiendo de la
     * presencia de pesos negativos en el criterio de optimización seleccionado.
     * * @param origenId ID de inicio.
     * @param destinoId ID de fin.
     * @param criterio El factor a minimizar (Tiempo, Distancia, Costo o Transbordos).
     * @return Un objeto {@link ResultadoRuta} con el detalle del camino y métricas.
     */
    public ResultadoRuta calcularRuta(String origenId,
                                      String destinoId,
                                      CriterioOptimizacion criterio) {

        Objects.requireNonNull(origenId, "origenId no puede ser null");
        Objects.requireNonNull(destinoId, "destinoId no puede ser null");
        Objects.requireNonNull(criterio, "criterio no puede ser null");


        if (criterio == CriterioOptimizacion.COSTO && hayPesosNegativos(criterio)) {
            return BellmanFord.calcular(grafo, origenId, destinoId, criterio);
        }

        return Dijkstra.calcular(grafo, origenId, destinoId, criterio);
    }

    /**
     * Evalúa si existen aristas con valores negativos para un criterio específico.
     * * Esta validación es crucial para determinar la compatibilidad de los algoritmos;
     * si se detectan pesos negativos en el criterio de optimización actual, el sistema
     * debe alternar de Dijkstra a un algoritmo capaz de manejar tales valores,
     * como Bellman-Ford.
     * * @param criterio El factor de optimización que se desea inspeccionar.
     * @return {@code true} si al menos una ruta en el grafo posee un peso menor a cero
     * bajo el criterio dado; {@code false} en caso contrario.
     */
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

    /**
     * Identifica el nombre del algoritmo que se utilizará para un cálculo específico.
     * * @param criterio Criterio de búsqueda.
     * @return "Bellman-Ford" si hay pesos negativos, de lo contrario "Dijkstra".
     */
    public String nombreAlgoritmoPara(CriterioOptimizacion criterio) {
        if (criterio == CriterioOptimizacion.COSTO && hayPesosNegativos(criterio)) {
            return "Bellman-Ford";
        }

        return "Dijkstra";
    }



    public List<List<String>> calcularDFS(String origenId,
                                          String destinoId,
                                          CriterioOptimizacion criterio,
                                          int maxAlternativos) {


        Objects.requireNonNull(origenId, "origenId no puede ser null");
        Objects.requireNonNull(destinoId, "destinoId no puede ser null");
        Objects.requireNonNull(criterio, "criterio no puede ser null");

        return Dfs.calcularCaminosDFS(grafo,origenId,destinoId,criterio,maxAlternativos);

    }


    // =========================
    // VALIDACIONES INTERNAS
    // =========================

    /**
     * Localiza una instancia de ruta específica dentro del conjunto de conexiones del grafo.
     * * Realiza una búsqueda filtrada basándose en la combinación única de los
     * identificadores de parada de origen y destino.
     * * @param origenId Identificador de la parada donde comienza la ruta.
     * @param destinoId Identificador de la parada donde termina la ruta.
     * @return El objeto {@link citygraph.model.Ruta} correspondiente si se encuentra;
     * {@code null} si no existe una conexión directa entre ambos puntos.
     */
    private Ruta buscarRuta(String origenId, String destinoId) {
        return grafo.obtenerRuta(origenId, destinoId);
    }

    /**
     * Verifica si el sistema contiene rutas con valores de costo negativos.
     * * @return {@code true} si existe al menos una arista con costo < 0.
     */
    private boolean tieneCostosNegativos() {
        for (Parada p : grafo.listarParadas()) {
            for (Ruta r : grafo.vecinosDe(p.getId())) {
                if (r.getCosto() < 0) return true;
            }
        }
        return false;
    }

    /**
     * Ejecuta una validación exhaustiva para asegurar que no existan ciclos cuyo
     * peso total sea negativo, lo cual invalidaría los cálculos de optimización.
     */
    private void validarSinCiclosNegativos() {
        BellmanFord.validarSinCicloNegativo(grafo);
    }
    /**
     * Ejecuta una reversión de la transacción en la base de datos sin lanzar excepciones.
     * * Se utiliza durante el manejo de errores para asegurar que, si una operación
     * falla, la base de datos no quede en un estado parcial o inconsistente.
     * * @param conn La conexión JDBC activa sobre la cual realizar el rollback.
     */
    private void rollbackConn(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException e) {
            System.err.println("Error al hacer rollback en la base de datos:");
            e.printStackTrace();
        }
    }
    /**
     * Revierte la inserción de una ruta en la memoria RAM tras un fallo en la persistencia.
     * * Asegura que si la escritura en el disco falla, el {@link GrafoTransporte}
     * no conserve una ruta que no existe realmente en la base de datos.
     * * @param ruta La instancia de la ruta que debe ser removida del grafo.
     */
    private void rollbackAgregarRutaMemoria(Ruta ruta) {
        try {
            grafo.eliminarRuta(ruta.getOrigenId(), ruta.getDestinoId());
        } catch (RuntimeException e) {
            System.err.println("Error al revertir la ruta en memoria: "
                    + ruta.getOrigenId() + " -> " + ruta.getDestinoId());
            e.printStackTrace();
        }
    }
    /**
     * Restaura el estado previo de una ruta en el grafo tras una modificación fallida.
     * * Si una actualización de datos es rechazada (por ejemplo, por generar un
     * ciclo negativo), este método devuelve los valores originales a la
     * arista en memoria.
     * * @param respaldo Objeto {@link Ruta} con los valores previos a la edición.
     */
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
    /**
     * Reinserta en el grafo una ruta que fue eliminada durante una transacción fallida.
     * * Si el proceso de borrado en la base de datos no puede completarse, este
     * método garantiza que la ruta vuelva a estar disponible en la estructura
     * de datos activa.
     * * @param respaldo La ruta que debe ser reintegrada al sistema.
     */
    private void restaurarRutaEliminada(Ruta respaldo) {
        try {
            grafo.agregarRuta(respaldo);
        } catch (RuntimeException ignored) {
        }
    }
}