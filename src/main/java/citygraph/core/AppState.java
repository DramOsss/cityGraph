package citygraph.core;

import citygraph.service.CityGraphService;
import citygraph.util.AppFactory;

public class AppState {

    private final CityGraphService service;

    public AppState() {
        this.service = AppFactory.createCityGraphService();
        this.service.cargarDesdeBD();
        System.out.println("BD OK: paradas=" + service.numeroParadas()
                + " rutas=" + service.numeroRutas());
    }

    public CityGraphService getService() {
        return service;
    }
}