package citygraph.graph;

import citygraph.graph.exceptions.ParadaNoExisteException;
import citygraph.graph.exceptions.RutaNoExisteException;
import citygraph.model.Parada;
import citygraph.model.Ruta;
import citygraph.model.TipoTransporte;

import java.util.*;

/**
 * Clase central que implementa la estructura de datos del grafo de transporte.
 * * Representa la red urbana utilizando un grafo dirigido donde los nodos son
 * las paradas y las aristas son las rutas entre ellas. La implementación
 * utiliza listas de adyacencia mediante un {@code Map} para garantizar
 * eficiencia en la búsqueda de vecinos y en la representación de redes
 * de transporte densas.
 */
public class GrafoTransporte {

    /**
     * Almacenamiento de los nodos del grafo, indexados por su identificador único.
     */
    private final Map<String, Parada> paradas = new HashMap<>();

    /**
     * Representación de las conexiones dirigidas mediante listas de adyacencia.
     * Asocia cada ID de parada con la lista de rutas que parten de ella.
     */
    private final Map<String, List<Ruta>> adyacencia = new HashMap<>();

    // Paradas (CRUD)

    /**
     * Registra una nueva parada en el sistema y prepara su lista de adyacencia.
     * * @param p La instancia de la parada a agregar.
     * @throws IllegalArgumentException Si el ID de la parada ya se encuentra registrado.
     * @throws NullPointerException Si el objeto parada es nulo.
     */
    public void agregarParada(Parada p) {
        Objects.requireNonNull(p, "Parada no puede ser null");
        if (paradas.containsKey(p.getId())) {
            throw new IllegalArgumentException("Ya existe una parada con id: " + p.getId());
        }
        paradas.put(p.getId(), p);
        adyacencia.put(p.getId(), new ArrayList<>());
    }

    /**
     * Actualiza la información de una parada existente.
     * * @param id Identificador único de la parada.
     * @param nuevoNombre Nuevo nombre para la parada.
     * @throws ParadaNoExisteException Si el ID proporcionado no coincide con ninguna parada.
     */
    public void modificarParada(String id, String nuevoNombre) {
        Parada p = paradas.get(id);
        if (p == null) throw new ParadaNoExisteException(id);
        if (nuevoNombre != null && !nuevoNombre.isBlank()) p.setNombre(nuevoNombre);
    }

    /**
     * Elimina una parada del grafo, removiendo tanto el nodo como todas las rutas
     * entrantes y salientes asociadas para mantener la integridad de la red.
     * * @param id Identificador de la parada a eliminar.
     * @throws ParadaNoExisteException Si la parada no se encuentra en el grafo.
     */
    public void eliminarParada(String id) {
        if (!paradas.containsKey(id)) throw new ParadaNoExisteException(id);

        // Quita nodo
        paradas.remove(id);

        // Quita todas las rutas salientes
        adyacencia.remove(id);

        // Quita rutas entrantes (de otros nodos hacia id)
        for (List<Ruta> lista : adyacencia.values()) {
            lista.removeIf(r -> r.getDestinoId().equals(id));
        }
    }

    /**
     * Recupera una instancia de parada basándose en su identificador.
     * * @param id Identificador de la parada.
     * @return El objeto {@code Parada} correspondiente.
     * @throws ParadaNoExisteException Si la parada no existe en el sistema.
     */
    public Parada obtenerParada(String id) {
        Parada p = paradas.get(id);
        if (p == null) throw new ParadaNoExisteException(id);
        return p;
    }

    /**
     * Devuelve una vista inmodificable de todas las paradas registradas en el grafo.
     * * @return Colección de objetos {@code Parada}.
     */
    public Collection<Parada> listarParadas() {
        return Collections.unmodifiableCollection(paradas.values());
    }

    // Rutas (CRUD)

    /**
     * Crea una conexión dirigida (arista) entre dos paradas existentes.
     * * @param r El objeto ruta que define el origen, destino y pesos.
     * @throws ParadaNoExisteException Si el origen o el destino no están registrados.
     * @throws IllegalArgumentException Si ya existe una ruta directa entre esas paradas.
     */
    public void agregarRuta(Ruta r) {
        Objects.requireNonNull(r, "Ruta no puede ser null");
        validarParadaExiste(r.getOrigenId());
        validarParadaExiste(r.getDestinoId());


        List<Ruta> lista = adyacencia.get(r.getOrigenId());
        boolean existe = lista.stream().anyMatch(x -> x.getDestinoId().equals(r.getDestinoId()));
        if (existe) {
            throw new IllegalArgumentException("Ya existe la ruta: " + r.getOrigenId() + " -> " + r.getDestinoId());
        }
        lista.add(r);
    }

    /**
     * Modifica los atributos de peso (tiempo, distancia, costo) de una conexión específica.
     * * @param origenId ID de inicio del tramo.
     * @param destinoId ID de fin del tramo.
     * @param tiempoMin Nuevo tiempo estimado de viaje.
     * @param distanciaKm Nueva distancia física.
     * @param costo Nueva tarifa de pasaje.
     * @param tipoTransporte Nuevo medio de transporte (BUS, METRO, etc.).
     */
    public void modificarRuta(String origenId, String destinoId,
                              Double tiempoMin, Double distanciaKm, Double costo,
                              TipoTransporte tipoTransporte) {
        Ruta ruta = buscarRuta(origenId, destinoId);

        if (tiempoMin != null) ruta.setTiempoMin(tiempoMin);
        if (distanciaKm != null) ruta.setDistanciaKm(distanciaKm);
        if (costo != null) ruta.setCosto(costo);
        if (tipoTransporte != null) ruta.setTipoTransporte(tipoTransporte);
    }

    /**
     * Elimina la conexión directa entre dos paradas.
     * * @param origenId ID de la parada de origen.
     * @param destinoId ID de la parada de destino.
     * @throws RutaNoExisteException Si no existe una arista entre los nodos indicados.
     */
    public void eliminarRuta(String origenId, String destinoId) {
        validarParadaExiste(origenId);
        List<Ruta> lista = adyacencia.get(origenId);

        boolean removed = lista.removeIf(r -> r.getDestinoId().equals(destinoId));
        if (!removed) throw new RutaNoExisteException(origenId, destinoId);
    }

    // Consultas

    /**
     * Obtiene todas las rutas salientes desde una parada específica.
     * * @param origenId ID de la parada a consultar.
     * @return Lista inmodificable de rutas adyacentes.
     * @throws ParadaNoExisteException Si el ID de origen no es válido.
     */
    public List<Ruta> vecinosDe(String origenId) {
        validarParadaExiste(origenId);
        return Collections.unmodifiableList(adyacencia.get(origenId));
    }

    /**
     * Verifica la existencia de una arista entre dos nodos.
     * * @param origenId ID de origen.
     * @param destinoId ID de destino.
     * @return true si existe una ruta directa, false en caso contrario.
     */
    public boolean existeArista(String origenId, String destinoId) {
        if (!paradas.containsKey(origenId) || !paradas.containsKey(destinoId)) return false;
        return adyacencia.get(origenId).stream().anyMatch(r -> r.getDestinoId().equals(destinoId));
    }

    /**
     * @return El número total de nodos (paradas) en el grafo.
     */
    public int numeroParadas() {
        return paradas.size();
    }

    /**
     * @return El número total de aristas (rutas) en toda la red de transporte.
     */
    public int numeroRutas() {
        int total = 0;
        for (List<Ruta> lista : adyacencia.values()) total += lista.size();
        return total;
    }

    // Helpers

    /**
     * Verifica la presencia de una parada en el sistema antes de realizar operaciones.
     * * @param id El identificador único de la parada a validar.
     * @throws ParadaNoExisteException Si el identificador no corresponde a ninguna
     * parada registrada en el grafo.
     */
    private void validarParadaExiste(String id) {
        if (!paradas.containsKey(id)) throw new ParadaNoExisteException(id);
    }

    /**
     * Localiza una conexión específica (arista) entre dos paradas dentro del grafo.
     * * Este método realiza una validación previa de los nodos y recorre la lista
     * de adyacencia del origen para encontrar el destino solicitado.
     * * @param origenId Identificador de la parada donde inicia la ruta.
     * @param destinoId Identificador de la parada donde finaliza la ruta.
     * @return El objeto {@code Ruta} que conecta ambas paradas.
     * @throws ParadaNoExisteException Si alguno de los nodos no está en el grafo.
     * @throws RutaNoExisteException Si no existe una arista directa entre los nodos.
     */
    private Ruta buscarRuta(String origenId, String destinoId) {
        validarParadaExiste(origenId);
        validarParadaExiste(destinoId);

        return adyacencia.get(origenId).stream()
                .filter(r -> r.getDestinoId().equals(destinoId))
                .findFirst()
                .orElseThrow(() -> new RutaNoExisteException(origenId, destinoId));
    }

    /**
     * Reinicia el grafo eliminando todas las paradas y rutas,
     * dejando la estructura de datos vacía.
     */
    public void limpiar() {
        this.paradas.clear();
        this.adyacencia.clear();
    }
}