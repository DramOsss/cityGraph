package citygraph.ui;

import citygraph.core.AppState;

/**
 * Interfaz que define el contrato para componentes de la interfaz de usuario que
 * requieren acceso al estado global y al motor de navegación.
 * * Permite que el {@link Navigator} inyecte de forma estandarizada las dependencias
 * necesarias (servicios, datos compartidos y lógica de transición) en los controladores
 * de JavaFX inmediatamente después de su instanciación.
 */
public interface StateAware {
    /**
     * Inicializa el componente con el contexto operativo de la aplicación.
     * * Este método debe ser invocado por el orquestador de navegación para
     * proporcionar al controlador las herramientas necesarias para interactuar
     * con la lógica de negocio y otras vistas.
     * * @param state El objeto {@link AppState} que contiene la instancia única
     * del servicio de grafos y el estado persistente.
     * @param nav La instancia de {@link Navigator} que permite al componente
     * disparar transiciones hacia otras escenas.
     */
    void setState(AppState state, Navigator nav);
}