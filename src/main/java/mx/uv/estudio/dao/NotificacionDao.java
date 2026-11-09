package mx.uv.estudio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import mx.uv.estudio.modelo.Notificacion;
import mx.uv.estudio.util.Db;

public class NotificacionDao {

    public List<Notificacion> deRol(String rol) throws SQLException {
        List<Notificacion> r = new ArrayList<>();
        try (Connection c = Db.con(); PreparedStatement ps = c.prepareStatement(
                "SELECT id, rol, texto, fecha, leida FROM notificacion WHERE rol = ? ORDER BY id DESC")) {
            ps.setString(1, rol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    r.add(new Notificacion(rs.getInt("id"), rs.getString("rol"), rs.getString("texto"),
                            rs.getObject("fecha", LocalDate.class), rs.getBoolean("leida")));
                }
            }
        }
        return r;
    }

    public void insertar(String rol, String texto) throws SQLException {
        Sql.ejecutar("INSERT INTO notificacion (rol, texto, fecha, leida) VALUES (?, ?, ?, 0)", rol, texto, LocalDate.now());
    }

    public void marcarLeida(int id) throws SQLException {
        Sql.ejecutar("UPDATE notificacion SET leida = 1 WHERE id = ?", id);
    }
}
