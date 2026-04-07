package citygraph.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Clase de utilidad para la ejecución simplificada de operaciones JDBC.
 * * Proporciona una capa de abstracción sobre la API de JDBC estándar para manejar
 * de forma segura la apertura y cierre de conexiones, la preparación de sentencias
 * y el mapeo de resultados. Reduce el código repetitivo y centraliza el manejo
 * de excepciones de SQL.
 */
public class JdbcExecutor {

    /**
     * Ejecuta una sentencia SQL de actualización (INSERT, UPDATE, DELETE).
     * * @param sql La sentencia SQL a ejecutar.
     * @param setter Implementación funcional para asignar los parámetros al PreparedStatement.
     * @return El número de filas afectadas por la operación.
     * @throws RuntimeException Si ocurre un error de acceso a datos o una violación de restricciones SQL.
     */
    public int update(String sql, PreparedStatementSetter setter) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (setter != null) {
                setter.setValues(ps);
            }

            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error ejecutando update.\nSQL: " + sql +
                            "\nMensaje SQL: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Ejecuta una consulta SQL y devuelve una lista de objetos mapeados.
     * * @param <T> El tipo de objeto de dominio que representa cada fila.
     * @param sql La sentencia SELECT a ejecutar.
     * @param setter Implementación funcional para asignar los parámetros de búsqueda.
     * @param mapper Implementación funcional para transformar cada fila del ResultSet en un objeto.
     * @return Una lista con todos los registros encontrados, o una lista vacía si no hay resultados.
     * @throws RuntimeException Si ocurre un error durante la ejecución de la consulta o el mapeo.
     */
    public <T> List<T> queryList(String sql,
                                 PreparedStatementSetter setter,
                                 RowMapper<T> mapper) {

        List<T> result = new ArrayList<>();

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (setter != null) {
                setter.setValues(ps);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando queryList", e);
        }
    }

    /**
     * Ejecuta una consulta SQL diseñada para retornar un único registro.
     * * @param <T> El tipo de objeto esperado.
     * @param sql La sentencia SELECT a ejecutar.
     * @param setter Implementación funcional para asignar los parámetros de búsqueda.
     * @param mapper Implementación funcional para transformar la fila encontrada.
     * @return Un {@code Optional} que contiene el objeto mapeado, o un {@code Optional.empty()} si no hubo resultados.
     * @throws RuntimeException Si ocurre un error de base de datos durante la operación.
     */
    public <T> Optional<T> queryOne(String sql,
                                    PreparedStatementSetter setter,
                                    RowMapper<T> mapper) {

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (setter != null) {
                setter.setValues(ps);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper.map(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando queryOne", e);
        }
    }
}