package citygraph.repository;

import citygraph.model.Parada;

import java.util.List;
import java.util.Optional;

public interface ParadaRepository {
    List<Parada> findAll();
    Optional<Parada> findById(String id);
    void save(Parada parada);
    void update(Parada parada);
    void deleteById(String id);
}