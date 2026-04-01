package citygraph.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcExecutor {

    public int update(String sql, PreparedStatementSetter setter) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (setter != null) {
                setter.setValues(ps);
            }

            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando update", e);
        }
    }

    public <T> List<T> queryList(String sql,
                                 PreparedStatementSetter setter,
                                 RowMapper<T> mapper) {

        List<T> result = new ArrayList<>();

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (setter != null) {
                setter.setValues(ps);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando queryList", e);
        }
    }

    public <T> Optional<T> queryOne(String sql,
                                    PreparedStatementSetter setter,
                                    RowMapper<T> mapper) {

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (setter != null) {
                setter.setValues(ps);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper.map(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando queryOne", e);
        }
    }
}