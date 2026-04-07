package citygraph.graph.exceptions;

/**
 * Excepción personalizada que se lanza cuando se intenta acceder o realizar una operación
 * sobre una parada que no se encuentra registrada en el grafo de transporte.
 * * Esta excepción extiende de {@code RuntimeException}, permitiendo un manejo flexible
 * de errores de validación de nodos sin obligar a la captura explícita en cada nivel
 * de la lógica de negocio.
 */
public class ParadaNoExisteException extends RuntimeException {
    /**
     * Construye una nueva excepción con un mensaje detallado que incluye el
     * identificador de la parada que causó el error.
     * * @param id El identificador único (ID) de la parada que no pudo ser localizada
     * en la estructura de datos del grafo.
     */
    public ParadaNoExisteException(String id) {
        super("La parada no existe: " + id);
    }
}