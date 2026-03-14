package citygraph.ui;

import citygraph.core.AppState;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigator {
    private final Stage stage;
    private final AppState state;

    public Navigator(Stage stage, AppState state) {
        this.stage = stage;
        this.state = state;
    }

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