package mx.uv.estudio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import mx.uv.estudio.util.Db;

/** Ayudas para no repetir código JDBC en cada DAO. */
final class Sql {

    private Sql() { }

    static void poner(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    /** INSERT, UPDATE o DELETE con su propia conexión. */
    static void ejecutar(String sql, Object... params) throws SQLException {
        try (Connection c = Db.con(); PreparedStatement ps = c.prepareStatement(sql)) {
            poner(ps, params);
            ps.executeUpdate();
        }
    }

    /** INSERT que devuelve el id generado, usando la conexión recibida (para transacciones). */
    static int insertar(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            poner(ps, params);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                return k.getInt(1);
            }
        }
    }
}
