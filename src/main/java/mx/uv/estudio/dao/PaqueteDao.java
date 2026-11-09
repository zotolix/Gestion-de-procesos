package mx.uv.estudio.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import mx.uv.estudio.modelo.Paquete;
import mx.uv.estudio.util.Db;

public class PaqueteDao {

    /** Solo los paquetes activos del catálogo. */
    public List<Paquete> todos() throws SQLException {
        List<Paquete> r = new ArrayList<>();
        try (Connection c = Db.con(); PreparedStatement ps = c.prepareStatement(
                "SELECT id, nombre, incluye, precio, duracion_min FROM paquete WHERE activo = 1 ORDER BY precio");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int d = rs.getInt("duracion_min");
                Integer duracion = rs.wasNull() ? null : d;   // wasNull() va justo después de leer la columna
                r.add(new Paquete(rs.getInt("id"), rs.getString("nombre"), rs.getString("incluye"),
                        rs.getBigDecimal("precio"), duracion));
            }
        }
        return r;
    }

    public void crear(String nombre, String incluye, BigDecimal precio, Integer duracionMin) throws SQLException {
        Sql.ejecutar("INSERT INTO paquete (nombre, incluye, precio, duracion_min) VALUES (?,?,?,?)", nombre, incluye, precio, duracionMin);
    }

    public void actualizar(int id, String nombre, String incluye, BigDecimal precio, Integer duracionMin) throws SQLException {
        Sql.ejecutar("UPDATE paquete SET nombre = ?, incluye = ?, precio = ?, duracion_min = ? WHERE id = ?",
                nombre, incluye, precio, duracionMin, id);
    }

    public boolean tieneSesiones(int id) throws SQLException {
        try (Connection c = Db.con(); PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM sesion WHERE paquete_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void borrar(int id) throws SQLException {
        Sql.ejecutar("DELETE FROM paquete WHERE id = ?", id);
    }

    public void retirar(int id) throws SQLException {
        Sql.ejecutar("UPDATE paquete SET activo = 0 WHERE id = ?", id);
    }
}
