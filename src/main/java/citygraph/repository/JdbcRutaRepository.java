package citygraph.repository;

import citygraph.db.JdbcExecutor;
import citygraph.model.Ruta;
import citygraph.model.TipoTransporte;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de la persistencia de rutas utilizando JDBC y PostgreSQL.
 * * Esta clase gestiona el ciclo de vida de las aristas del grafo en la base de datos.
 * Traduce las entidades de tipo {@link citygraph.model.Ruta} a registros en la tabla 'rutas',
 * manejando la conversión de tipos enumerados y tipos numéricos de punto flotante para
 * representar los pesos del grafo (tiempo, distancia y costo).
 */
public class JdbcRutaRepository implements RutaRepository {

    private final JdbcExecutor jdbc;

    /**
     * Constructor que inyecta la dependencia del ejecutor JDBC.
     * * @param jdbc Instancia de {@code JdbcExecutor} configurada para la aplicación.
     */
    public JdbcRutaRepository(JdbcExecutor jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Recupera todas las rutas registradas en el sistema.
     * * Los resultados se devuelven ordenados jerárquicamente por el ID de origen
     * y luego por el ID de destino.
     * * @return Una lista de objetos {@code Ruta} con la configuración completa de cada tramo.
     */
    @Override
    public List<Ruta> findAll() {
        String sql = """
                SELECT origen_id, destino_id, tiempo_min, distancia_km, costo, tipo_transporte
                FROM rutas
                ORDER BY origen_id, destino_id
                """;

        return jdbc.queryList(sql, null, rs -> new Ruta(
                rs.getString("origen_id"),
                rs.getString("destino_id"),
                rs.getDouble("tiempo_min"),
                rs.getDouble("distancia_km"),
                rs.getDouble("costo"),
                TipoTransporte.valueOf(rs.getString("tipo_transporte"))
        ));
    }

    /**
     * Recupera todas las rutas que tienen como punto de partida una parada específica.
     * * Este método es fundamental para reconstruir las listas de adyacencia del grafo.
     * * @param origenId El identificador de la parada de origen.
     * @return Lista de rutas salientes desde el nodo indicado.
     */
    @Override
    public List<Ruta> findByOrigen(String origenId) {
        String sql = """
                SELECT origen_id, destino_id, tiempo_min, distancia_km, costo, tipo_transporte
                FROM rutas
                WHERE origen_id = ?
                ORDER BY destino_id
                """;

        return jdbc.queryList(sql,
                ps -> ps.setString(1, origenId),
                rs -> new Ruta(
                        rs.getString("origen_id"),
                        rs.getString("destino_id"),
                        rs.getDouble("tiempo_min"),
                        rs.getDouble("distancia_km"),
                        rs.getDouble("costo"),
                        TipoTransporte.valueOf(rs.getString("tipo_transporte"))
                )
        );
    }

    /**
     * Busca una conexión específica entre dos paradas.
     * * @param origenId Identificador de la parada de inicio.
     * @param destinoId Identificador de la parada de fin.
     * @return Un {@code Optional} con la ruta encontrada o vacío si no existe conexión directa.
     */
    @Override
    public Optional<Ruta> findById(String origenId, String destinoId) {
        String sql = """
                SELECT origen_id, destino_id, tiempo_min, distancia_km, costo, tipo_transporte
                FROM rutas
                WHERE origen_id = ? AND destino_id = ?
                """;

        return jdbc.queryOne(sql,
                ps -> {
                    ps.setString(1, origenId);
                    ps.setString(2, destinoId);
                },
                rs -> new Ruta(
                        rs.getString("origen_id"),
                        rs.getString("destino_id"),
                        rs.getDouble("tiempo_min"),
                        rs.getDouble("distancia_km"),
                        rs.getDouble("costo"),
                        TipoTransporte.valueOf(rs.getString("tipo_transporte"))
                )
        );
    }

    /**
     * Persiste una nueva ruta en la base de datos.
     * * @param ruta El objeto {@code Ruta} que contiene los datos del tramo,
     * incluyendo el tipo de transporte y métricas asociadas.
     */
    @Override
    public void save(Connection conn, Ruta ruta) {
        String sql = """
        INSERT INTO rutas(origen_id, destino_id, tiempo_min, distancia_km, costo, tipo_transporte)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ruta.getOrigenId());
            ps.setString(2, ruta.getDestinoId());
            ps.setDouble(3, ruta.getTiempoMin());
            ps.setDouble(4, ruta.getDistanciaKm());
            ps.setDouble(5, ruta.getCosto());
            ps.setString(6, ruta.getTipoTransporte().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando ruta", e);
        }
    }

    /**
     * Actualiza los valores de una ruta existente (tiempo, distancia, costo y tipo).
     * * Utiliza la combinación de origen y destino para localizar el registro único a modificar.
     * * @param ruta Objeto con los datos actualizados para el tramo correspondiente.
     */
    @Override
    public void update(Connection conn, Ruta ruta) {
        String sql = """
        UPDATE rutas
        SET tiempo_min = ?, distancia_km = ?, costo = ?, tipo_transporte = ?
        WHERE origen_id = ? AND destino_id = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, ruta.getTiempoMin());
            ps.setDouble(2, ruta.getDistanciaKm());
            ps.setDouble(3, ruta.getCosto());
            ps.setString(4, ruta.getTipoTransporte().name());
            ps.setString(5, ruta.getOrigenId());
            ps.setString(6, ruta.getDestinoId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando ruta", e);
        }
    }

    /**
     * Elimina de forma permanente la conexión entre dos paradas en la base de datos.
     * * @param origenId Identificador de la parada de origen.
     * @param destinoId Identificador de la parada de destino.
     */
    @Override
    public void deleteById(Connection conn, String origenId, String destinoId) {
        String sql = "DELETE FROM rutas WHERE origen_id = ? AND destino_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, origenId);
            ps.setString(2, destinoId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando ruta", e);
        }
    }
}