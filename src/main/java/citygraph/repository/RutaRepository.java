package citygraph.repository;

import citygraph.model.Ruta;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define el contrato para las operaciones de persistencia de rutas.
 * * Establece los métodos necesarios para la gestión del ciclo de vida de las aristas
 * del grafo (CRUD). Permite desacoplar la lógica de red de transporte de la
 * implementación técnica del almacenamiento, facilitando la gestión de conexiones
 * dirigidas y sus pesos asociados.
 */
public interface RutaRepository {
    /**
     * Recupera la totalidad de las rutas registradas en el sistema de transporte.
     * * @return Una lista que contiene todas las instancias de {@link citygraph.model.Ruta}.
     */
    List<Ruta> findAll();
    /**
     * Recupera todas las rutas que tienen como punto de partida una parada específica.
     * * Este método es fundamental para la construcción eficiente de las listas de
     * adyacencia en la estructura del grafo.
     * * @param origenId El identificador único de la parada de origen.
     * @return Una lista de rutas salientes desde el nodo indicado.
     */
    List<Ruta> findByOrigen(String origenId);
    /**
     * Localiza una conexión específica entre dos paradas mediante su clave compuesta.
     * * @param origenId Identificador de la parada de inicio.
     * @param destinoId Identificador de la parada de fin.
     * @return Un {@code Optional} con la ruta encontrada, o un {@code Optional.empty()}
     * si no existe una conexión directa entre ambos nodos.
     */
    Optional<Ruta> findById(String origenId, String destinoId);
    /**
     * Registra una nueva conexión dirigida en el medio de almacenamiento persistente.
     * * @param ruta El objeto {@code Ruta} con los datos de origen, destino y métricas.
     */
    void save(Ruta ruta);
    /**
     * Actualiza los atributos de una ruta existente (pesos y tipo de transporte).
     * * @param ruta La instancia con los datos modificados para el tramo correspondiente.
     */
    void update(Ruta ruta);
    /**
     * Elimina de forma permanente la conexión entre dos paradas del repositorio.
     * * @param origenId Identificador de la parada de origen.
     * @param destinoId Identificador de la parada de destino.
     */
    void deleteById(String origenId, String destinoId);
}