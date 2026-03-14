package citygraph.graph.exceptions;

public class RutaNoExisteException extends RuntimeException {
    public RutaNoExisteException(String origenId, String destinoId) {
        super("La ruta no existe: " + origenId + " -> " + destinoId);
    }
}