package citygraph.ui;

import citygraph.core.AppState;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Motor de navegación centralizado para la gestión de escenas en la interfaz de usuario.
 * * Esta clase se encarga de realizar la carga dinámica de archivos FXML, la inyección
 * del estado global de la aplicación y la transición entre las diferentes vistas
 * del sistema. Actúa como un orquestador que desacopla la lógica de los controladores
 * del manejo directo del escenario ({@link Stage}) de JavaFX.
 */
public class Navigator {
    private final Stage stage;
    private final AppState state;

    /**
     * Crea una instancia de Navigator vinculada a una ventana y un estado específico.
     * * @param stage El escenario (Stage) principal de JavaFX.
     * @param state El objeto {@link AppState} que contiene los servicios y datos globales.
     */
    public Navigator(Stage stage, AppState state) {
        this.stage = stage;
        this.state = state;
    }

    /**
     * Realiza la transición hacia una nueva vista FXML.
     * * El método carga el archivo de recursos, identifica al controlador asociado y,
     * si este implementa la interfaz {@link StateAware}, le inyecta automáticamente
     * el estado global y la referencia a este navegador para permitir navegaciones
     * subsiguientes.
     * * @param fxmlPath Ruta relativa al archivo .fxml (ej. "/citygraph/home-view.fxml").
     * @param title El título que se mostrará en la barra de la ventana.
     * @throws RuntimeException Si ocurre un error de E/S o de instanciación al cargar el FXML.
     */
    public void goTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof StateAware sa) {
                sa.setState(state, this);
            }

            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Error cargando vista: " + fxmlPath, e);
        }
    }
}