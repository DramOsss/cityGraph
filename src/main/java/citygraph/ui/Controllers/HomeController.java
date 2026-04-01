package citygraph.ui.Controllers;

import citygraph.core.AppState;
import citygraph.ui.Navigator;
import citygraph.ui.StateAware;

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