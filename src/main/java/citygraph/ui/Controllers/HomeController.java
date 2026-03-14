package citygraph.ui.Controllers;

import citygraph.ui.Navigator;
import citygraph.ui.StateAware;
import citygraph.core.AppState;

public class HomeController implements StateAware {

    private Navigator nav;

    @Override
    public void setState(AppState state, Navigator nav) {
        this.nav = nav;
    }

    public void onParadas() {
        nav.goTo("/citygraph/paradas-view.fxml", "Paradas");
    }

    public void onRutas() {
        nav.goTo("/citygraph/rutas-view.fxml", "Rutas");
    }

    public void onCalcular() {
        nav.goTo("/citygraph/calcular-view.fxml", "Calcular Ruta");
    }
}