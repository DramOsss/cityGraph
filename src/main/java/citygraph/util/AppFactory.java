package citygraph.util;

import citygraph.db.JdbcExecutor;
import citygraph.repository.*;
import citygraph.service.CityGraphService;

public final class AppFactory {

    private AppFactory() {}

    public static JdbcExecutor createJdbcExecutor() {
        return new JdbcExecutor();
    }

    public static ParadaRepository createParadaRepository() {
        return new JdbcParadaRepository(createJdbcExecutor());
    }

    public static RutaRepository createRutaRepository() {
        return new JdbcRutaRepository(createJdbcExecutor());
    }

    public static CityGraphService createCityGraphService() {
        return new CityGraphService(
                createParadaRepository(),
                createRutaRepository()
        );
    }
}