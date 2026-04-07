package citygraph.repository;

import citygraph.db.JdbcExecutor;
import citygraph.model.Parada;

import java.util.List;
import java.util.Optional;

/**
 * Implementación de la persistencia de paradas utilizando JDBC y PostgreSQL.
 * * Esta clase actúa como un repositorio de datos que traduce las operaciones de dominio
 * del grafo en sentencias SQL. Utiliza un {@code JdbcExecutor} para gestionar la
 * ejecución de consultas y el mapeo de los registros de la tabla 'paradas'
 * hacia objetos de tipo {@link citygraph.model.Parada}.
 */
public class JdbcParadaRepository implements ParadaRepository {

    /**
     * Constructor que inyecta la dependencia del ejecutor JDBC.
     * * @param jdbc Instancia de {@code JdbcExecutor} configurada para la aplicación.
     */
    private final JdbcExecutor jdbc;

    /**
     * Constructor que inyecta la dependencia del ejecutor JDBC.
     * * @param jdbc Instancia de {@code JdbcExecutor} configurada para la aplicación.
     */
    public JdbcParadaRepository(JdbcExecutor jdbc) {
        this.jdbc = jdbc;
    }
    /**
     * Recupera la totalidad de las paradas registradas en la base de datos.
     * * Los resultados se devuelven ordenados alfabéticamente por su identificador único.
     * * @return Una lista de objetos {@code Parada} con todos los registros encontrados.
     */
    @Override
    public List<Parada> findAll() {
        String sql = "SELECT id, nombre FROM paradas ORDER BY id";

        return jdbc.queryList(sql, null, rs -> new Parada(
                rs.getString("id"),
                rs.getString("nombre")
        ));
    }

    /**
     * Busca una parada específica en la base de datos basándose en su ID.
     * * @param id El identificador único de la parada.
     * @return Un {@code Optional} conteniendo la parada si fue localizada,
     * o vacío si no existe el registro.
     */
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

    /**
     * Inserta una nueva parada en la tabla de persistencia.
     * * @param parada El objeto {@code Parada} con los datos a persistir.
     * @throws RuntimeException Si ocurre un error de duplicidad de ID o violación de restricciones.
     */
    @Override
    public void save(Parada parada) {
        String sql = "INSERT INTO paradas(id, nombre) VALUES (?, ?)";

        jdbc.update(sql, ps -> {
            ps.setString(1, parada.getId());
            ps.setString(2, parada.getNombre());
        });
    }

    /**
     * Actualiza la información de una parada existente en la base de datos.
     * * @param parada Objeto con los datos actualizados. El ID se utiliza como
     * criterio de búsqueda y el nombre como campo a modificar.
     */
    @Override
    public void update(Parada parada) {
        String sql = "UPDATE paradas SET nombre = ? WHERE id = ?";

        jdbc.update(sql, ps -> {
            ps.setString(1, parada.getNombre());
            ps.setString(2, parada.getId());
        });
    }

    /**
     * Elimina de forma permanente el registro de una parada mediante su identificador.
     * * @param id Identificador de la parada a remover.
     */
    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM paradas WHERE id = ?";
        jdbc.update(sql, ps -> ps.setString(1, id));
    }
}