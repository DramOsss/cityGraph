package citygraph.model;

/**
 * Define las diferentes categorías de medios de transporte disponibles en la red urbana.
 * * Este enumerado es esencial para clasificar las rutas (aristas) del grafo,
 * permitiendo al sistema calcular transbordos, filtrar trayectos por preferencia
 * del usuario y aplicar lógicas de peso específicas según la naturaleza del
 * desplazamiento (mecánico o peatonal).
 */
public enum TipoTransporte {
    BUS,
    METRO,
    TELEFERICO,
    TRANVIA,
    TREN,
    A_PIE
}