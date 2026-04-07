package citygraph.graph.exceptions;

/**
 * Excepción personalizada que se lanza cuando se intenta acceder, modificar o eliminar
 * una conexión (arista) que no existe entre dos paradas específicas en el grafo.
 * * Esta excepción es fundamental para validar la integridad de la red de transporte,
 * asegurando que las operaciones de consulta de rutas solo se realicen sobre
 * trayectos previamente registrados en el sistema.
 */
public class RutaNoExisteException extends RuntimeException {
    /**
     * Construye una nueva excepción indicando el trayecto inexistente.
     * * @param origenId Identificador de la parada de inicio del tramo.
     * @param destinoId Identificador de la parada de fin del tramo.
     */
    public RutaNoExisteException(String origenId, String destinoId) {
        super("La ruta no existe: " + origenId + " -> " + destinoId);
    }
}