package citygraph.repository;

import citygraph.db.JdbcExecutor;
import citygraph.model.Ruta;

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
                SELECT origen_id, destino_id, tiempo_min, distancia_km, costo, transbordos
                FROM rutas
                ORDER BY origen_id, destino_id
                """;

        return jdbc.queryList(sql, null, rs -> new Ruta(
                rs.getString("origen_id"),
                rs.getString("destino_id"),
                rs.getDouble("tiempo_min"),
                rs.getDouble("distancia_km"),
                rs.getDouble("costo"),
                rs.getInt("transbordos")
        ));
    }

    @Override
    public List<Ruta> findByOrigen(String origenId) {
        String sql = """
                SELECT origen_id, destino_id, tiempo_min, distancia_km, costo, transbordos
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
                        rs.getInt("transbordos")
                )
        );
    }

    @Override
    public Optional<Ruta> findById(String origenId, String destinoId) {
        String sql = """
                SELECT origen_id, destino_id, tiempo_min, distancia_km, costo, transbordos
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
                        rs.getInt("transbordos")
                )
        );
    }

    @Override
    public void save(Ruta ruta) {
        String sql = """
                INSERT INTO rutas(origen_id, destino_id, tiempo_min, distancia_km, costo, transbordos)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        jdbc.update(sql, ps -> {
            ps.setString(1, ruta.getOrigenId());
            ps.setString(2, ruta.getDestinoId());
            ps.setDouble(3, ruta.getTiempoMin());
            ps.setDouble(4, ruta.getDistanciaKm());
            ps.setDouble(5, ruta.getCosto());
            ps.setInt(6, ruta.getTransbordos());
        });
    }

    @Override
    public void update(Ruta ruta) {
        String sql = """
                UPDATE rutas
                SET tiempo_min = ?, distancia_km = ?, costo = ?, transbordos = ?
                WHERE origen_id = ? AND destino_id = ?
                """;

        jdbc.update(sql, ps -> {
            ps.setDouble(1, ruta.getTiempoMin());
            ps.setDouble(2, ruta.getDistanciaKm());
            ps.setDouble(3, ruta.getCosto());
            ps.setInt(4, ruta.getTransbordos());
            ps.setString(5, ruta.getOrigenId());
            ps.setString(6, ruta.getDestinoId());
        });
    }

    @Override
    public void deleteById(String origenId, String destinoId) {
        String sql = "DELETE FROM rutas WHERE origen_id = ? AND destino_id = ?";
        jdbc.update(sql, ps -> {
            ps.setString(1, origenId);
            ps.setString(2, destinoId);
        });
    }
}