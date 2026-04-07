package citygraph.repository;

import citygraph.model.Parada;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define el contrato para las operaciones de persistencia de paradas.
 * * Establece los métodos necesarios para el ciclo de vida de los nodos del grafo
 * (CRUD), permitiendo desacoplar la lógica de negocio de la implementación
 * específica del almacenamiento, ya sea en memoria, archivos o bases de datos
 * relacionales.
 */
public interface ParadaRepository {
    /**
     * Recupera la colección completa de paradas almacenadas en el sistema.
     * * @return Una lista que contiene todas las instancias de {@link citygraph.model.Parada}.
     */
    List<Parada> findAll();
    /**
     * Localiza una parada específica mediante su identificador único.
     * * @param id El identificador alfanumérico de la parada.
     * @return Un {@code Optional} con la parada encontrada, o un {@code Optional.empty()}
     * si no existe ningún registro con ese ID.
     */
    Optional<Parada> findById(String id);
    /**
     * Registra una nueva parada en el medio de almacenamiento persistente.
     * * @param parada El objeto {@code Parada} que se desea guardar.
     */
    void save(Parada parada);
    /**
     * Actualiza la información de una parada ya existente en el repositorio.
     * * @param parada La instancia con los datos modificados que se sincronizarán
     * con el almacenamiento.
     */
    void update(Parada parada);
    /**
     * Elimina de forma definitiva una parada del repositorio basándose en su ID.
     * * @param id El identificador único de la parada a remover.
     */
    void deleteById(String id);
}