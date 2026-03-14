package citygraph;

import citygraph.core.AppState;
import citygraph.ui.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        AppState state = new AppState();

        try {
            state.getService().cargarDesdeBD();
            System.out.println("BD OK: paradas=" + state.getService().getGrafo().numeroParadas()
                    + " rutas=" + state.getService().getGrafo().numeroRutas());
        } catch (Throwable t) {
            System.err.println("BD FALLÓ, abriendo app sin datos. Causa real:");
            t.printStackTrace();
        }

        Navigator nav = new Navigator(stage, state);
        nav.goTo("/citygraph/home-view.fxml", "CityGraph - Inicio");
    }

    public static void main(String[] args) {
        launch();
    }
}