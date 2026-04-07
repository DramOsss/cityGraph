package citygraph.repository;

import citygraph.db.JdbcExecutor;
import citygraph.model.Ruta;
import citygraph.model.TipoTransporte;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcRutaRepository implements RutaRepository {

    private final JdbcExecutor jdbc;

    public JdbcRutaRepository(JdbcExecutor jdbc) {
        this.jdbc = jdbc;
    }

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