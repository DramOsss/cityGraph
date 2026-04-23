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

    private static final String URL = "jdbc:postgresql://localhost:5432/cityGraphDb";

    private static final String USER = "postgres";

    private static final String PASSWORD = "12345";


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