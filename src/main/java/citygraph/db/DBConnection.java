package citygraph.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            System.getenv().getOrDefault("CITYGRAPH_DB_URL", "jdbc:postgresql://localhost:5432/cityGraphDb");

    private static final String USER =
            System.getenv().getOrDefault("CITYGRAPH_DB_USER", "postgres");

    private static final String PASSWORD =
            System.getenv().getOrDefault("CITYGRAPH_DB_PASSWORD", "12345");

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL no encontrado. Revisa dependencia Maven.", e);
        }
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}