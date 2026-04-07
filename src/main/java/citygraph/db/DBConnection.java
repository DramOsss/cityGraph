package citygraph.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase responsable de la configuración y gestión de la conectividad con la base de datos.
 * * Centraliza los parámetros de conexión para el motor PostgreSQL, asegurando que
 * los controladores necesarios estén cargados y disponibles para realizar
 * operaciones de persistencia sobre las paradas y rutas del sistema.
 */
public class DBConnection {

    private static final String URL =
            System.getenv().getOrDefault("CITYGRAPH_DB_URL", "jdbc:postgresql://localhost:5432/cityGraphDb");

    private static final String USER =
            System.getenv().getOrDefault("CITYGRAPH_DB_USER", "postgres");

    private static final String PASSWORD =
            System.getenv().getOrDefault("CITYGRAPH_DB_PASSWORD", "12345");

    /**
     * Bloque estático de inicialización.
     * * Carga el controlador (Driver) de PostgreSQL en memoria al momento de cargar
     * la clase. Es fundamental para habilitar la comunicación entre la aplicación
     * Java y el servidor de base de datos.
     * * @throws RuntimeException Si el controlador de PostgreSQL no se encuentra en el classpath.
     */
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL no encontrado. Revisa dependencia Maven.", e);
        }
    }

    /**
     * Establece y devuelve una conexión activa con la base de datos configurada.
     * * Este método es utilizado por las capas de acceso a datos para ejecutar
     * consultas SQL y procedimientos de carga de la red de transporte.
     * * @return Un objeto {@code Connection} listo para ejecutar sentencias.
     * @throws SQLException Si ocurre un error al intentar establecer el enlace con el servidor.
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}