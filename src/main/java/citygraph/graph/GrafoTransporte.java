package citygraph.graph;

import citygraph.graph.exceptions.ParadaNoExisteException;
import citygraph.graph.exceptions.RutaNoExisteException;
import citygraph.model.Parada;
import citygraph.model.Ruta;
import citygraph.model.TipoTransporte;

import java.util.*;

public class GrafoTransporte {


    private final Map<String, Parada> paradas = new HashMap<>();


    private final Map<String, List<Ruta>> adyacencia = new HashMap<>();

    // Paradas (CRUD)

    public void agregarParada(Parada p) {
        Objects.requireNonNull(p, "Parada no puede ser null");
        if (paradas.containsKey(p.getId())) {
            throw new IllegalArgumentException("Ya existe una parada con id: " + p.getId());
        }
        paradas.put(p.getId(), p);
        adyacencia.put(p.getId(), new ArrayList<>());
    }

    public void modificarParada(String id, String nuevoNombre) {
        Parada p = paradas.get(id);
        if (p == null) throw new ParadaNoExisteException(id);
        if (nuevoNombre != null && !nuevoNombre.isBlank()) p.setNombre(nuevoNombre);
    }

    public void eliminarParada(String id) {
        if (!paradas.containsKey(id)) throw new ParadaNoExisteException(id);

        // Quita nodo
        paradas.remove(id);

        // Quita todas las rutas salientes
        adyacencia.remove(id);

        // Quita rutas entrantes (de otros nodos hacia id)
        for (List<Ruta> lista : adyacencia.values()) {
            lista.removeIf(r -> r.getDestinoId().equals(id));
        }
    }

    public Parada obtenerParada(String id) {
        Parada p = paradas.get(id);
        if (p == null) throw new ParadaNoExisteException(id);
        return p;
    }

    public Collection<Parada> listarParadas() {
        return Collections.unmodifiableCollection(paradas.values());
    }

    // Rutas (CRUD)

    public void agregarRuta(Ruta r) {
        Objects.requireNonNull(r, "Ruta no puede ser null");
        validarParadaExiste(r.getOrigenId());
        validarParadaExiste(r.getDestinoId());


        List<Ruta> lista = adyacencia.get(r.getOrigenId());
        boolean existe = lista.stream().anyMatch(x -> x.getDestinoId().equals(r.getDestinoId()));
        if (existe) {
            throw new IllegalArgumentException("Ya existe la ruta: " + r.getOrigenId() + " -> " + r.getDestinoId());
        }
        lista.add(r);
    }

    public void modificarRuta(String origenId, String destinoId,
                              Double tiempoMin, Double distanciaKm, Double costo,
                              TipoTransporte tipoTransporte) {
        Ruta ruta = buscarRuta(origenId, destinoId);

        if (tiempoMin != null) ruta.setTiempoMin(tiempoMin);
        if (distanciaKm != null) ruta.setDistanciaKm(distanciaKm);
        if (costo != null) ruta.setCosto(costo);
        if (tipoTransporte != null) ruta.setTipoTransporte(tipoTransporte);
    }

    public void eliminarRuta(String origenId, String destinoId) {
        validarParadaExiste(origenId);
        List<Ruta> lista = adyacencia.get(origenId);

        boolean removed = lista.removeIf(r -> r.getDestinoId().equals(destinoId));
        if (!removed) throw new RutaNoExisteException(origenId, destinoId);
    }

    // Consultas

    public List<Ruta> vecinosDe(String origenId) {
        validarParadaExiste(origenId);
        return Collections.unmodifiableList(adyacencia.get(origenId));
    }

    public boolean existeArista(String origenId, String destinoId) {
        if (!paradas.containsKey(origenId) || !paradas.containsKey(destinoId)) return false;
        return adyacencia.get(origenId).stream().anyMatch(r -> r.getDestinoId().equals(destinoId));
    }

    public int numeroParadas() {
        return paradas.size();
    }

    public int numeroRutas() {
        int total = 0;
        for (List<Ruta> lista : adyacencia.values()) total += lista.size();
        return total;
    }

    // Helpers

    private void validarParadaExiste(String id) {
        if (!paradas.containsKey(id)) throw new ParadaNoExisteException(id);
    }

    private Ruta buscarRuta(String origenId, String destinoId) {
        validarParadaExiste(origenId);
        validarParadaExiste(destinoId);

        return adyacencia.get(origenId).stream()
                .filter(r -> r.getDestinoId().equals(destinoId))
                .findFirst()
                .orElseThrow(() -> new RutaNoExisteException(origenId, destinoId));
    }

    public void limpiar() {
        this.paradas.clear();
        this.adyacencia.clear();
    }
}