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

        grafo.agregarRuta(ruta); // agregar temporalmente para validar el estado final

        try {
            if (tieneCostosNegativos()) {
                validarSinCiclosNegativos();
            }

            rutaRepository.save(ruta);

        } catch (Exception e) {
            grafo.eliminarRuta(ruta.getOrigenId(), ruta.getDestinoId());

            throw new IllegalArgumentException(
                    "No se puede agregar la ruta: generaría un ciclo negativo"
            );
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

        Ruta rutaAnterior = buscarRuta(ruta.getOrigenId(), ruta.getDestinoId());
        if (rutaAnterior == null) {
            throw new IllegalArgumentException("La ruta no existe");
        }

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

            rutaRepository.update(ruta);

        } catch (Exception e) {
            grafo.modificarRuta(
                    rutaAnterior.getOrigenId(),
                    rutaAnterior.getDestinoId(),
                    rutaAnterior.getTiempoMin(),
                    rutaAnterior.getDistanciaKm(),
                    rutaAnterior.getCosto(),
                    rutaAnterior.getTipoTransporte()
            );

            throw new IllegalArgumentException(
                    "No se puede modificar la ruta: generaría un ciclo negativo"
            );
        }
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

    /**
     * Remueve una conexión entre dos paradas del sistema.
     * * @param origenId ID de origen.
     * @param destinoId ID de destino.
     */
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

        if (criterio == CriterioOptimizacion.TRANSBORDOS) {
            return Dijkstra.calcular(grafo, origenId, destinoId, criterio);
        }

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
        return listarRutas().stream()
                .filter(r -> r.getOrigenId().equals(origenId)
                        && r.getDestinoId().equals(destinoId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Verifica si el sistema contiene rutas con valores de costo negativos.
     * * @return {@code true} si existe al menos una arista con costo < 0.
     */
    private boolean tieneCostosNegativos() {
        return listarRutas().stream()
                .anyMatch(r -> r.getCosto() < 0);
    }

    /**
     * Ejecuta una validación exhaustiva para asegurar que no existan ciclos cuyo
     * peso total sea negativo, lo cual invalidaría los cálculos de optimización.
     */
    private void validarSinCiclosNegativos() {
        for (Parada p : listarParadas()) {
            BellmanFord.calcular(
                    grafo,
                    p.getId(),
                    p.getId(),
                    CriterioOptimizacion.COSTO
            );
        }
    }
}