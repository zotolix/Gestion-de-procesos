package mx.uv.estudio.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import mx.uv.estudio.modelo.Pago;
import mx.uv.estudio.util.Db;

public class PagoDao {

    public int insertar(Connection c, int sesionId, String concepto, BigDecimal monto, String metodo, LocalDate fecha) throws SQLException {
        return Sql.insertar(c, "INSERT INTO pago (sesion_id, concepto, monto, metodo, fecha) VALUES (?,?,?,?,?)",
                sesionId, concepto, monto, metodo, fecha);
    }

    public List<Pago> deSesion(int sesionId) throws SQLException {
        return lista("SELECT id, sesion_id, concepto, monto, metodo, fecha FROM pago WHERE sesion_id = ? ORDER BY id", sesionId);
    }

    private List<Pago> lista(String sql, Object param) throws SQLException {
        List<Pago> r = new ArrayList<>();
        try (Connection c = Db.con(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    r.add(new Pago(rs.getInt("id"), rs.getInt("sesion_id"), rs.getString("concepto"), rs.getBigDecimal("monto"),
                            rs.getString("metodo"), rs.getObject("fecha", LocalDate.class)));
                }
            }
        }
        return r;
    }
}
