package citygraph.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interfaz funcional diseñada para la configuración de parámetros en sentencias SQL preparadas.
 * * Permite abstraer la lógica de asignación de valores a un {@code PreparedStatement},
 * facilitando la separación entre la definición de la consulta y la inyección de
 * datos específicos. Es utilizada principalmente por las clases ejecutoras de la
 * capa de persistencia para manejar consultas parametrizadas de forma limpia.
 */
@FunctionalInterface
public interface PreparedStatementSetter {

    /**
     * Asigna los valores correspondientes a los marcadores de parámetros (?)
     * en el objeto PreparedStatement proporcionado.
     * * @param ps El objeto {@code PreparedStatement} sobre el cual se asocian los valores.
     * @throws SQLException Si ocurre un error de acceso a la base de datos o durante
     * la asignación de tipos de datos.
     */
    void setValues(PreparedStatement ps) throws SQLException;
}