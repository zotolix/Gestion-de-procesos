package mx.uv.estudio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import mx.uv.estudio.util.Db;

/** Archivos de foto de un proyecto: Original (HU03), Alta y Baja (HU09). */
public class FotoDao {

    public void insertar(Connection c, int proyectoId, String nombre, String calidad, String ruta) throws SQLException {
        Sql.insertar(c, "INSERT INTO foto (proyecto_id, nombre, calidad, ruta) VALUES (?,?,?,?)",
                proyectoId, nombre, calidad, ruta);
    }

    public int contar(int proyectoId, String calidad) throws SQLException {
        try (Connection c = Db.con(); PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM foto WHERE proyecto_id = ? AND calidad = ?")) {
            Sql.poner(ps, proyectoId, calidad);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
