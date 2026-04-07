package citygraph.model;

/**
 * Define los diferentes factores de ponderación que el sistema puede optimizar
 * al calcular una ruta entre dos paradas.
 * * Este enumerado permite al motor de búsqueda de rutas seleccionar dinámicamente
 * los pesos de las aristas en el grafo (tiempo, distancia o costo) o aplicar
 * algoritmos especializados para minimizar los cambios de transporte.
 */
public enum CriterioOptimizacion {
    TIEMPO,
    DISTANCIA,
    COSTO,
    TRANSBORDOS

}