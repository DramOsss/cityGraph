package citygraph.util;

import citygraph.db.JdbcExecutor;
import citygraph.repository.*;
import citygraph.service.CityGraphService;

/**
 * Clase de fábrica (Factory) encargada de la instanciación centralizada de los componentes del sistema.
 * * Sigue el principio de Inversión de Control (IoC) al gestionar la creación y
 * ensamblaje de las dependencias de la aplicación. Proporciona métodos estáticos
 * para obtener instancias configuradas del ejecutor JDBC, los repositorios de
 * persistencia y el servicio de lógica de negocio del grafo.
 */
public final class AppFactory {

    private AppFactory() {}

    /**
     * Crea una nueva instancia del motor de ejecución de consultas SQL.
     * * @return Un objeto {@link citygraph.db.JdbcExecutor} para operaciones de base de datos.
     */
    public static JdbcExecutor createJdbcExecutor() {
        return new JdbcExecutor();
    }

    /**
     * Crea e inicializa el repositorio encargado de la persistencia de paradas.
     * * Inyecta una nueva instancia de {@code JdbcExecutor} en la implementación
     * concreta del repositorio de paradas.
     * * @return Una implementación de {@link ParadaRepository} conectada a la base de datos.
     */
    public static ParadaRepository createParadaRepository() {
        return new JdbcParadaRepository(createJdbcExecutor());
    }

    /**
     * Crea e inicializa el repositorio encargado de la persistencia de rutas.
     * * Inyecta una nueva instancia de {@code JdbcExecutor} en la implementación
     * concreta del repositorio de rutas.
     * * @return Una implementación de {@link RutaRepository} conectada a la base de datos.
     */
    public static RutaRepository createRutaRepository() {
        return new JdbcRutaRepository(createJdbcExecutor());
    }

    /**
     * Orquestador principal que construye el servicio de lógica de negocio del sistema.
     * * Realiza la inyección de dependencias necesaria, vinculando los repositorios
     * de paradas y rutas al {@link citygraph.service.CityGraphService} para que
     * este pueda gestionar el grafo tanto en memoria como en almacenamiento persistente.
     * * @return Una instancia configurada de {@link CityGraphService}.
     */
    public static CityGraphService createCityGraphService() {
        return new CityGraphService(
                createParadaRepository(),
                createRutaRepository()
        );
    }
}