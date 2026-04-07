package citygraph.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interfaz funcional diseñada para el mapeo de filas de un conjunto de resultados SQL.
 * * Define la estrategia para transformar una fila actual de un {@code ResultSet} en un
 * objeto de dominio específico. Es utilizada por los métodos de consulta de la capa
 * de persistencia para desacoplar la extracción de datos de la lógica de ejecución
 * de sentencias SQL.
 * * @param <T> El tipo de objeto resultante del mapeo.
 */
@FunctionalInterface
public interface RowMapper<T> {
    /**
     * Transforma los datos de la fila actual del ResultSet en una instancia de tipo T.
     * * @param rs El {@code ResultSet} posicionado en la fila que se desea mapear.
     * @return Una instancia del objeto de tipo {@code T} con los datos cargados.
     * @throws SQLException Si ocurre un error al acceder a las columnas del ResultSet
     * o si hay un problema con el motor de base de datos.
     */
    T map(ResultSet rs) throws SQLException;
}