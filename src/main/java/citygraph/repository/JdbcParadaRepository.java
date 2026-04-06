package citygraph.repository;

import citygraph.db.JdbcExecutor;
import citygraph.model.Parada;

import java.util.List;
import java.util.Optional;

public class JdbcParadaRepository implements ParadaRepository {

    private final JdbcExecutor jdbc;

    public JdbcParadaRepository(JdbcExecutor jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Parada> findAll() {
        String sql = "SELECT id, nombre FROM paradas ORDER BY id";

        return jdbc.queryList(sql, null, rs -> new Parada(
                rs.getString("id"),
                rs.getString("nombre")
        ));
    }

    @Override
    public Optional<Parada> findById(String id) {
        String sql = "SELECT id, nombre FROM paradas WHERE id = ?";

        return jdbc.queryOne(sql,
                ps -> ps.setString(1, id),
                rs -> new Parada(
                        rs.getString("id"),
                        rs.getString("nombre")
                )
        );
    }

    @Override
    public void save(Parada parada) {
        String sql = "INSERT INTO paradas(id, nombre) VALUES (?, ?)";

        jdbc.update(sql, ps -> {
            ps.setString(1, parada.getId());
            ps.setString(2, parada.getNombre());
        });
    }

    @Override
    public void update(Parada parada) {
        String sql = "UPDATE paradas SET nombre = ? WHERE id = ?";

        jdbc.update(sql, ps -> {
            ps.setString(1, parada.getNombre());
            ps.setString(4, parada.getId());
        });
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM paradas WHERE id = ?";
        jdbc.update(sql, ps -> ps.setString(1, id));
    }
}