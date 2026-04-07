package citygraph.repository;

import citygraph.model.Ruta;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface RutaRepository {
    List<Ruta> findAll();
    List<Ruta> findByOrigen(String origenId);
    Optional<Ruta> findById(String origenId, String destinoId);
    void save(Connection conn, Ruta ruta);
    void update(Connection conn, Ruta ruta);
    void deleteById(Connection conn, String origenId, String destinoId);
}