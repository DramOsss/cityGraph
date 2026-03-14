package citygraph.core;

import citygraph.service.CityGraphService;

public class AppState {
    private final CityGraphService service = new CityGraphService();
    public CityGraphService getService() { return service; }
}

