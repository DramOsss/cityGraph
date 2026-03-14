package citygraph.graph.exceptions;

public class ParadaNoExisteException extends RuntimeException {
    public ParadaNoExisteException(String id) {
        super("La parada no existe: " + id);
    }
}