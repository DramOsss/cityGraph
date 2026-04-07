package citygraph.graph.exceptions;

/**
 * Excepción lanzada cuando los algoritmos de búsqueda detectan un ciclo de peso negativo en el grafo.
 * * Un ciclo negativo ocurre cuando la suma de los pesos de las aristas en un bucle es menor a cero,
 * lo que permitiría reducir el costo total infinitamente al recorrer dicho bucle repetidamente.
 * * Esta condición invalida la existencia de una "ruta óptima" finita. Es detectada principalmente
 * por el algoritmo de {@link citygraph.algorithms.BellmanFord} durante la fase de validación
 * de la red de transporte.
 */
public class CicloNegativoException extends RuntimeException {
    /**
     * Crea una nueva instancia de la excepción con un mensaje descriptivo del error.
     * @param message Detalle sobre la ubicación o naturaleza del ciclo detectado
     * (ej. "Ciclo negativo alcanzable desde el origen").
     */
    public CicloNegativoException(String message) {
        super(message);
    }
}