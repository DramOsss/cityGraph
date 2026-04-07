package citygraph;

import citygraph.core.AppState;
import citygraph.ui.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada principal de la aplicación JavaFX.
 * * Esta clase es la encargada de iniciar el ciclo de vida de la aplicación,
 * configurar el escenario inicial y establecer el contexto operativo global.
 * Hereda de {@link Application} para gestionar el arranque del motor gráfico
 * y la inicialización de la ventana principal (Stage).
 */
public class App extends Application {
    /**
     * Inicializa los componentes base del sistema y despliega la interfaz de usuario.
     * * Crea una instancia única de {@link AppState} para la gestión de servicios
     * y el motor de {@link Navigator} para controlar el flujo de navegación.
     * Finalmente, redirige al usuario a la vista de inicio del sistema.
     * * @param stage El escenario (Stage) primario proporcionado por JavaFX donde
     * se renderizará la aplicación.
     */
    @Override
    public void start(Stage stage) {
        AppState state = new AppState();
        Navigator nav = new Navigator(stage, state);
        nav.goTo("/citygraph/home-view.fxml", "CityGraph - Inicio");
    }

    /**
     * Método de arranque estándar de Java.
     * * Invoca al método {@code launch()} de la superclase para iniciar la ejecución
     * de la plataforma JavaFX y sus subprocesos internos.
     * * @param args Argumentos de la línea de comandos (opcionales).
     */
    public static void main(String[] args) {
        launch();
    }
}