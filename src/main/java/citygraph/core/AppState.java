package citygraph.core;

import citygraph.service.CityGraphService;
import citygraph.util.AppFactory;

/**
 * Clase responsable de gestionar el estado global y el ciclo de vida de la aplicación.
 * * Actúa como un contenedor central para los servicios principales, asegurando que
 * los datos del sistema de transporte se carguen correctamente al iniciar el programa
 * y estén disponibles para los componentes de la interfaz de usuario.
 */
public class AppState {

    private final CityGraphService service;

    /**
     * Constructor de la clase AppState.
     * * Se encarga de inicializar el servicio de la ciudad utilizando una factoría
     * y de ejecutar la carga inicial de datos desde la base de datos persistente.
     * Al finalizar la carga, imprime en consola un resumen con el conteo de
     * paradas y rutas cargadas exitosamente.
     */
    public AppState() {
        this.service = AppFactory.createCityGraphService();
        this.service.cargarDesdeBD();
        System.out.println("BD OK: paradas=" + service.numeroParadas()
                + " rutas=" + service.numeroRutas());
    }

    /**
     * Proporciona acceso al servicio de lógica de negocio y gestión del grafo.
     * * @return La instancia de {@code CityGraphService} configurada para la sesión actual.
     */
    public CityGraphService getService() {
        return service;
    }
}