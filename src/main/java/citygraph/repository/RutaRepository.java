package citygraph.repository;

import citygraph.model.Ruta;

import java.util.List;
import java.util.Optional;

public interface RutaRepository {
    List<Ruta> findAll();
    List<Ruta> findByOrigen(String origenId);
    Optional<Ruta> findById(String origenId, String destinoId);
    void save(Ruta ruta);
    void update(Ruta ruta);
    void deleteById(String origenId, String destinoId);
}