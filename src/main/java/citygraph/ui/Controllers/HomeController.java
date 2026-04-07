package citygraph.ui.Controllers;

import citygraph.core.AppState;
import citygraph.ui.Navigator;
import citygraph.ui.StateAware;

/**
 * Controlador de la interfaz de usuario para la pantalla principal de la aplicación.
 * * Actúa como el centro de navegación del sistema, permitiendo al usuario acceder
 * a los diferentes módulos de gestión (Paradas y Rutas) y al motor de cálculo
 * de itinerarios. Esta clase implementa {@link StateAware} para mantener la
 * coherencia de la navegación dentro del contexto de la aplicación.
 */
public class HomeController implements StateAware {

    private Navigator nav;

    /**
     * Establece el estado inicial y el navegador para el controlador.
     * * @param state El estado global de la aplicación (proporcionado por el motor de UI).
     * @param nav El objeto {@link Navigator} encargado del intercambio de vistas.
     */
    @Override
    public void setState(AppState state, Navigator nav) {
        this.nav = nav;
    }

    /**
     * Redirige al usuario a la vista de gestión de paradas.
     * * Permite realizar operaciones CRUD sobre los nodos del grafo de transporte.
     */
    public void onParadas() {
        nav.goTo("/citygraph/paradas-view.fxml", "Paradas");
    }

    /**
     * Redirige al usuario a la vista de gestión de rutas.
     * * Permite administrar las conexiones, distancias y costos entre las paradas existentes.
     */
    public void onRutas() {
        nav.goTo("/citygraph/rutas-view.fxml", "Rutas");
    }

    /**
     * Redirige al usuario al panel de cálculo de rutas óptimas.
     * * Permite ejecutar los algoritmos de optimización para encontrar el mejor
     * camino según diversos criterios.
     */
    public void onCalcular() {
        nav.goTo("/citygraph/calcular-view.fxml", "Calcular Ruta");
    }
}